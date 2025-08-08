package org.example.llm.ability.service;

import lombok.Data;
import lombok.experimental.Accessors;
import org.example.llm.ability.model.domain.ModelBasicParam;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.common.model.domain.ChatRequestContext;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author : zybi
 * @date : 2025/5/19 14:14
 */
public class StreamModelCallerUtil {



    public static Builder create(ModelBasicParam modelBasicParam) {
        return new Builder(modelBasicParam);
    }




    @Data
    @Accessors(chain = true)
    public static class Builder {
        private final ModelBasicParam modelBasicParam;
        private String model;
        private Boolean stream;
        private Integer maxTokens;
        private Double temperature;
        private String stop;
        private Boolean enableThinking;
        private List<SourceModelRequest.Tool> tools;
        private SourceModelRequest.ResponseFormat responseFormat;
        private List<SourceModelRequest.Message> messages;

        private Consumer<ChatRequestContext> onOpen;
        private BiFunction<ChatRequestContext, String, SourceModelResponse> onMessage;
        private BiConsumer<ChatRequestContext, Throwable> onError;
        private Consumer<ChatRequestContext> onComplete;

        public Builder(ModelBasicParam modelBasicParam) {
            this.modelBasicParam = modelBasicParam;
        }

        public void call() {
            StreamModelCaller.call(this);
        }
    }
}
