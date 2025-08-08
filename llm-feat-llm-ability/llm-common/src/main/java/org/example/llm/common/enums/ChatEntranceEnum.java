package org.example.llm.common.enums;

import lombok.Getter;

/**
 * @author : zybi
 * @date : 2025/6/23 16:28
 */
@Getter
public enum ChatEntranceEnum {

    /** 规划执行入口会话 */
    PLAN_AND_EXEC("plan", true),

    /** 在某个场景（卡片）下对话 */
    IN_SCENE("scene", true),

    /** 独立组件形式的对话入口（悬浮球） */
    STAR_ASST("starAsst", true),

    /** 通用对话（裸调大模型） */
    COMMON("common", false),
    ;

    private final String entrance;
    private final Boolean saveChatData;

    ChatEntranceEnum(String entrance, Boolean saveChatData) {
        this.entrance = entrance;
        this.saveChatData = saveChatData;
    }


    public static ChatEntranceEnum getByCode(String entrance) {
        for (ChatEntranceEnum value : values()) {
            if (value.entrance.equals(entrance)) {
                return value;
            }
        }

        return null;
    }
}
