package org.example.llm.test;

import com.alibaba.fastjson2.JSONObject;
import com.google.gson.*;
import org.example.llm.common.enums.WSContentTypeEnum;
import org.example.llm.common.model.domain.ApiResponse;
import org.example.llm.common.model.domain.ChatMessageRequest;
import org.example.llm.common.model.domain.ChatResponseData;
import org.example.llm.test.domain.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : zybi
 * @date : 2025/5/29 10:37
 */
public class Common {

    static int lineSize = 100;
    static int curSize = 0;

    public static WSContentTypeEnum handleResponse(ApiResponse<ChatResponseData> response, WSContentTypeEnum lastTypeEnum) {
        if (response.getCode() != 0) {
            throw new RuntimeException("异常：" + JSONObject.toJSONString(response));
        }

        ChatResponseData data = response.getData();
        if (data == null) {
            return null;
        }

        String contentType = data.getContentType();
        String content = data.getContent();
        WSContentTypeEnum typeEnum = WSContentTypeEnum.getByType(contentType);

        if (contentType == null) {
            return null;
        }

        if (data.getDialogEnd()) {
            return null;
        }

        if (lastTypeEnum == null || lastTypeEnum != typeEnum) {
            System.out.printf("\n\n==============[%s： %s]===============\n", typeEnum.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date()));
            curSize = 0;
        }

        switch (typeEnum) {
            case STEP, UPDATE, RECOMMEND_QUESTION, TEST, SELECT, FILE_URL, PLAN_COST_TIME, LOADING, VIRTUAL_HUMAN -> System.out.println(formatAndPrint(content));
            default -> System.out.print(content);
        }

        curSize += content.length();
        if (curSize >= lineSize) {
            System.out.println();
            curSize = 0;
        }
        return typeEnum;
    }

    public static void handleResponse(ChatMessageRequest request, ApiResponse<ChatResponseData> response, Context context) {
        if (response.getCode() != 0) {
            throw new RuntimeException("异常：" + JSONObject.toJSONString(response));
        }

        ChatResponseData data = response.getData();
        if (data == null) {
            return;
        }

        String contentType = data.getContentType();
        String content = data.getContent();
        WSContentTypeEnum typeEnum = WSContentTypeEnum.getByType(contentType);

        if (Boolean.TRUE.equals(data.getDialogEnd())) {
            System.out.printf("\n# <=== sessionId: %s, sceneId: %s, taskId: %s \n", data.getSessionId(), data.getSceneId(), data.getTaskId());
            System.out.println("### <==== 此轮dialog已结束");
            request.setChatId(data.getChatId());
            if (!data.getSceneEnd()) {
                request.setSessionId(data.getSessionId());
                request.setSceneId(data.getSceneId());
                request.setTaskId(data.getTaskId());

            } else {
                System.out.println("##### <==== 此轮session已结束");
            }
            return;
        }

        if (context.getLastTypeEnum() == null || context.getLastTypeEnum() != typeEnum) {
            System.out.printf("\n\n==============[%s]===============\n", typeEnum.getName());
            curSize = 0;
        }

        switch (typeEnum) {
            case STEP, UPDATE, RECOMMEND_QUESTION, TEST, SELECT, FILE_URL, PLAN_COST_TIME -> System.out.println(formatAndPrint(content));
            case VIRTUAL_HUMAN, EDITABLE_FILE -> System.out.println(content);
            default -> System.out.print(content);
        }

        curSize += content.length();
        if (curSize >= lineSize) {
            System.out.println();
            curSize = 0;
        }
        context.setLastTypeEnum(typeEnum);
    }


    public static String formatAndPrint(String jsonString) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            return gson.toJson(jsonElement);
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return "";
    }
}
