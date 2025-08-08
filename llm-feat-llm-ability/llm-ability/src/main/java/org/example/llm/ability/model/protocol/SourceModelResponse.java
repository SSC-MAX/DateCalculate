package org.example.llm.ability.model.protocol;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/5/19 16:33
 */
@Data
@Accessors(chain = true)
public class SourceModelResponse {
    @JSONField(name = "id")
    @JsonProperty("id")
    private String id;   // 本次请求的唯一标识

    private String model;  // 本次请求实际使用的模型名称和版本

    /**
     * 是否结束
     */
    private Boolean isEnd;

    /**
     * 本次请求的模型输出内容
     */
    private List<Choice> choices;

    /**
     * 本次请求的 tokens 用量
     */
    private Usage usage;


    @Data
    public static class Choice {
        /**
         * 当前元素在 choices 列表的索引
         */
        private Integer index;
        /**
         * 模型停止生成 token 的原因。可能的值包括：
         * <p>
         * stop：模型输出自然结束，或因命中请求参数 stop 中指定的字段而被截断
         * length：模型输出因达到请求参数 max_token 指定的最大 token 数量而被截断
         * content_filter：模型输出被内容审核拦截
         * tool_calls：模型调用了工具
         */
        @JSONField(name = "finish_reason")
        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * 模型处理问题的思维链内容。
         * 仅深度推理模型支持返回此字段
         */
        @JSONField(name = "reasoning_content")
        @JsonProperty("reasoning_content")
        private String reasoningContent;

        /**
         * 模型输出的内容
         */
        private ChoiceDelta delta;

        /**
         * 非流式返回数据对象（千问）
         */
        private message message;

    }

    @Data
    public static class ChoiceDelta {
        /**
         * 角色 固定为 assistant
         */
        private String role;
        /**
         * 模型生成的消息内容，content 与 tool_calls 字段二者至少有一个为非空
         */
        private String content;

        @JSONField(name = "reasoning_content")
        @JsonProperty("reasoning_content")
        private String reasoningContent;

    }

    @Data
    public static class message {
        /**
         * 角色 固定为 assistant
         */
        private String role;
        /**
         * 模型生成的消息内容，content 与 tool_calls 字段二者至少有一个为非空
         */
        private String content;

        @JSONField(name = "tool_calls")
        @JsonProperty("tool_calls")
        private List<toolCall> toolCalls;

    }

    @Data
    public static class toolCall {
        private String id;
        private String type;
        private Function function;
        private Integer index;
    }

    @Data
    private static class Function {
        private String name;
        private String arguments;
    }


    @Data
    public static class Usage {
        @JSONField(name = "prompt_tokens")
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JSONField(name = "completion_tokens")
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JSONField(name = "total_tokens")
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
