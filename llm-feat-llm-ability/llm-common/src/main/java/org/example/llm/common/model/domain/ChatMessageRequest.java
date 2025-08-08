package org.example.llm.common.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author : zybi
 * @date : 2025/5/13 19:57
 */
@Data
@Accessors(chain = true)
public class ChatMessageRequest {

    private String assistantId;         // 助理id
    private String sceneId;           // 场景码
    private String sessionId;           // 场景任务执行关联时使用
    private String taskId;              // 任务id
    private UserBasicInfo userInfo;     // 用户信息
    private String chatId;              // 会话id
    private String dialogId;            // 会话id
    private String entrance;            // 入口
    private String messageType;         // 消息内容类型 input：输入，event：事件
    private List<TypeAndContent> message;  // 消息内容
    private Event event;  // 拓展参数
    private Test test;  // 测试参数

    @Data
    @Accessors(chain = true)
    public static class Event {
        private String type;
        private Map<String, Object> formData;
    }

    @Data
    @Accessors(chain = true)
    public static class Test {
        private Boolean enable;
    }
}
