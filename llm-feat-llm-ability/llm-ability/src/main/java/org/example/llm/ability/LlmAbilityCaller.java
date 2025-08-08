package org.example.llm.ability;

import com.alibaba.fastjson2.JSONObject;
import org.example.llm.ability.handler.ChatResponseHandler;
import org.example.llm.ability.handler.OutputResponseContentHandler;
import org.example.llm.ability.model.domain.RoleAndContent;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.ability.service.StreamModelCaller;
import org.example.llm.ability.util.SseMessageProcessor;
import org.example.llm.common.util.Checks;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型能力调用器
 */
public class LlmAbilityCaller {

    public static void chat(String userInput)
    {
        chat(null, null, null, userInput, false, false, new OutputResponseContentHandler());
    }

    public static void chatWithThinking(String userInput)
    {
        chat(null, null, null, userInput, true, false, new OutputResponseContentHandler());
    }

    public static void chatOutputJson(String userInput)
    {
        chat(null, null, null, userInput, false, true, new OutputResponseContentHandler());
    }


    public static void chat(String scene,
                             String system,
                             List<RoleAndContent> histories,
                             String userInput,
                             boolean enableThinking,
                             boolean outputJson,
                             ChatResponseHandler chatResponseHandler)
    {
        List<SourceModelRequest.Message> messages = new ArrayList<>();
        // 系统提示词
        if (Checks.noNull( system )) {
            messages.add(new SourceModelRequest.Message().setRole("system").setContent(system));
        }
        // 历史对话
        if (Checks.noNull( histories )) {
            for (RoleAndContent history : histories) {
                messages.add(new SourceModelRequest.Message().setRole(history.getRole()).setContent(history.getContent()));
            }
        }
        // 当前问题
        messages.add(new SourceModelRequest.Message().setRole("user").setContent(userInput));

        StreamModelCaller.create(Checks.isNull( scene )? "common" : scene)
                .setEnableThinking(!Boolean.FALSE.equals(enableThinking))
                .setStream(!Boolean.FALSE.equals(enableThinking))
                .setResponseFormat(new SourceModelRequest.ResponseFormat().setType(outputJson? "json_object" : "text"))
                .setMessages(messages)
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    chatResponseHandler.handle(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }

}
