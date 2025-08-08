package org.example.llm.ability.handler;

import org.example.llm.ability.model.protocol.SourceModelResponse;

public interface ChatResponseHandler {

    void handle(SourceModelResponse response);
}
