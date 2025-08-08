package org.example.llm.ability.service;

import com.alibaba.fastjson2.JSONObject;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.ability.util.SseMessageProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/8/3 22:59
 */
public class Qwen3Test {

    @Test
    public void planTest() {
        StreamModelCaller.create("sceneMatch")
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("user").setContent("你好")
                        )
                )
                .setEnableThinking(Boolean.TRUE)
                .setStream(Boolean.TRUE)
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    System.out.println(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }
}
