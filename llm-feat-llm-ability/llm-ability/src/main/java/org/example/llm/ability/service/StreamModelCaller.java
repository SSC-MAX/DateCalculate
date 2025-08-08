package org.example.llm.ability.service;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.ability.model.domain.ModelBasicParam;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.common.model.domain.ChatRequestContext;
import org.example.llm.common.util.ResourceUtil;
import org.example.llm.common.util.sse.BaseSSEObserver;
import org.example.llm.common.util.sse.SSEUtil;

import java.util.Map;

/**
 * @author : zybi
 * @date : 2025/5/19 11:19
 */
@Slf4j
public class StreamModelCaller {

    private static final String SCENE_JSON_FILE = "scene.json";
    private static final String MODEL_JSON_FILE = "model.json";

    public static StreamModelCallerUtil.Builder create(String sceneCode) {
        try {
            String sceneJson = ResourceUtil.readJsonFromResource(SCENE_JSON_FILE);
            String modelJson = ResourceUtil.readJsonFromResource(MODEL_JSON_FILE);

            JSONObject sceneConfig = JSONObject.parseObject(sceneJson);
            String modelOfScene = sceneConfig.getString(sceneCode);

            log.info("使用模型： {}", modelOfScene);

            JSONObject models = JSONObject.parseObject(modelJson);
            JSONObject modelConfig = models.getJSONObject(modelOfScene);

            ModelBasicParam modelBasicParam = modelConfig.to(ModelBasicParam.class);
            return StreamModelCallerUtil.create(modelBasicParam);
        }
        catch (Exception e) {
            throw new RuntimeException("读取配置异常: " + e.getMessage());
        }
    }

    private static SourceModelRequest buildRequest(StreamModelCallerUtil.Builder builder) {
        return new SourceModelRequest()
                .setModel(builder.getModelBasicParam().getModel())
                .setTemperature(builder.getModelBasicParam().getTemperature())
                .setMaxTokens(builder.getModelBasicParam().getMaxTokens())
                .setMessages(builder.getMessages())
                .setStream(!Boolean.FALSE.equals(builder.getStream()))
                .setEnableThinking(builder.getEnableThinking())
                .setTools(builder.getTools())
                .setResponseFormat(builder.getResponseFormat());
    }


    static void call(StreamModelCallerUtil.Builder builder) {
        SourceModelRequest sourceModelRequest = buildRequest(builder);

        SSEUtil.<SourceModelResponse>create(builder.getModelBasicParam().getUrl())
                .addAllHeaders(Map.of("Authorization", "Bearer " + builder.getModelBasicParam().getAppKey()))
                //.addAllQueryParams(null)
                .setBodyParam(sourceModelRequest)
                .setPrintRequestParams(Boolean.TRUE)
                .setPrintResponseStr(Boolean.FALSE)
                .setObserver(new BaseSSEObserver<>() {
                    @Override
                    public void onOpen(ChatRequestContext context) {
                        log.info("能力层 ==> 与信源建连成功，url: {}", builder.getModelBasicParam().getUrl());
                        if (builder.getOnOpen() != null) {
                            builder.getOnOpen().accept(context);
                        }
                    }
                    @Override
                    public SourceModelResponse onMessage(ChatRequestContext context, String message) {
                        //log.info("能力层 ==> 接收到字符串消息: {}", message);

                        return builder.getOnMessage().apply(context, message);
                    }
                    @Override
                    public void onError(ChatRequestContext context, Throwable error) {
                        log.error("能力层 ==> 监听到异常", error);

                        if (builder.getOnError() != null) {
                            builder.getOnError().accept(context, error);
                        }
                    }
                    @Override
                    public void onCompleted(ChatRequestContext context) {
                        System.out.println("\n");
                        log.info("能力层 ==> 关闭连接");
                        if (builder.getOnComplete() != null) {
                            builder.getOnComplete().accept(context);
                        }
                    }
                })
                .setIsEndFlag(sourceModelResponse -> {
                    boolean nullResponse = sourceModelResponse == null;
                    if (nullResponse) {
                        return true;
                    }
                    if (Boolean.TRUE.equals(sourceModelResponse.getIsEnd())) {
                        return true;
                    }

                    boolean isEmptyChoice = sourceModelResponse.getChoices() == null || sourceModelResponse.getChoices().isEmpty();
                    if (isEmptyChoice) {
                        return true;
                    }
                    for (SourceModelResponse.Choice choice : sourceModelResponse.getChoices()) {
                        if (choice.getFinishReason() != null && !choice.getFinishReason().trim().isEmpty()) {
                            return true;
                        }
                    }

                    return false;
                })
                .execute();
    }

}
