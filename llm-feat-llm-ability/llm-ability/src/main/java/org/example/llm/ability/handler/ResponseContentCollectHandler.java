package org.example.llm.ability.handler;

import org.example.llm.ability.model.protocol.SourceModelResponse;

public class ResponseContentCollectHandler implements ChatResponseHandler {
    @Override
    public void handle(SourceModelResponse response) {
        System.out.println(response);
    }
}
