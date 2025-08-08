package org.example.llm.common.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/5/13 9:31
 */
@Data
@Accessors(chain = true)
public class ChatResponseData {

    private String userId;          // 用户id

    private String chatId;          // 会话id
    private String dialogId;        // 对话id
    private String messageId;       // 消息（回答）id

    private String sessionId;       // 任务关联的session
    private String sceneId;         // 匹配到的场景码
    private String taskId;          // 任务id

    private String contentType;     // 数据类型
    private String content;         // 数据内容

    private Boolean messageEnd;     // 是否消息结束
    private Boolean dialogEnd;      // 是否对话结束
    private Boolean taskEnd;        // 是否任务结束
    private Boolean sceneEnd;       // 是否场景结束
    private Boolean sessionEnd;     // 任务关联的session完成状态
    private List<TypeAndContent> mixedContent; // 混合内容

}