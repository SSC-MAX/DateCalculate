package org.example.llm.ability.model.protocol;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/5/19 11:23
 */
@Data
@Accessors(chain = true)
public class SourceModelRequest {

    private String model;                   // 模型版本
    private List<Message> messages;         // 消息列表
    private Boolean stream;                 // 是否流式
    @JSONField(name = "max_tokens")
    @JsonProperty("max_tokens")
    private Integer maxTokens;              // 模型回复最大长度（单位 token），取值范围为 [0, 4096] 输入 token 和输出 token 的总长度还受模型的上下文长度限制。最大模型输出[2,4096]
    private Double temperature;             // 较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定 默认0.8
    private List<String> stop;              //模型遇到 stop 字段所指定的字符串时将停止继续生成，这个词语本身不会输出。最多支持 4 个字符串。

    // ===============通义千问专有====================== //
    @JSONField(name = "enable_thinking")
    @JsonProperty("enable_thinking")
    private Boolean enableThinking;        // 是否开启思考模式，适用于Qwen3模型
    @JSONField(name = "thinking_budget")
    @JsonProperty("thinking_budget")
    private Integer thinkingBudget;        // 是否开启思考模式，适用于Qwen3模型
    private List<Tool> tools;
    @JSONField(name = "response_format")
    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @Data
    public static class Message {
        /**
         * 发出该消息的对话参与者角色，可选值包括：
         * system：System Message 系统消息
         * user：User Message 用户消息
         * assistant：Assistant Message 对话助手消息
         * tool：Tool Message 工具调用消息
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }

    @Data
    public static class Tool{
        /**
         * tools的类型，当前仅支持function
         */
        private String type;
        public Function function;
    }

    @Data
    public static class Function{
        /**
         * 工具函数的名称，必须是字母、数字，可以包含下划线和短划线，最大长度为64
         */
        private String name;
        /**
         * 工具函数的描述
         */
        private String description;
        /**
         * 工具的参数描述，需要是一个合法的JSON Schema
         */
        private Object parameters;
    }


    @Data
    @Accessors(chain = true)
    public static class ResponseFormat {
        private String type;
    }
}
