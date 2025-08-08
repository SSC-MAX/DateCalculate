package org.example.llm.common.enums;

import lombok.Getter;

/**
 * @author : zybi
 * @date : 2024/12/25 23:50
 */
@Getter
public enum WSContentTypeEnum {

    TEXT("1010", "text", Boolean.FALSE),
    THINKING("1020", "thinking", Boolean.FALSE),
    PLANNING("1021", "planning", Boolean.FALSE),
    PLAN_COST_TIME("1022", "planCostTime", Boolean.FALSE),

    FILE_URL("2010", "fileUrl", Boolean.FALSE),
    EDITABLE_FILE("2020", "editableFile", Boolean.FALSE),
    LIST("5010", "list", Boolean.FALSE),

    SELECT("5020", "select", Boolean.TRUE),
    FORM("5030", "form", Boolean.TRUE),

    STEP_CHAIN("5040", "stepChain", Boolean.FALSE),
    UPDATE("5041", "update content", Boolean.FALSE),
    STEP("5042", "step", Boolean.FALSE),
    TABLE("5050", "table", Boolean.FALSE),
    OPEN_LINK("5060", "openLink", Boolean.FALSE),
    LOADING("5070", "loading", Boolean.FALSE),
    STOP_TASK("5080", "stopTask", Boolean.FALSE),
    RECOMMEND_QUESTION("5090", "recommendQuestion", Boolean.FALSE),
    VIRTUAL_HUMAN("5100", "virtualHuman", Boolean.FALSE),
    CLASS_INSPECTION_COURSE("6001", "classInspectionCourse", Boolean.FALSE),

    MIXED("x", "mixed", Boolean.FALSE),
    EVENT("e", "event", Boolean.FALSE),
    TEST("t", "test", Boolean.FALSE),
    ;

    private final String type;
    private final String name;
    private final Boolean needUserReInput;

    WSContentTypeEnum(String type, String name, Boolean needUserReInput) {
        this.type = type;
        this.name = name;
        this.needUserReInput = needUserReInput;
    }

    /**
     * 根据类型获取枚举值
     * @param type 类型码
     * @return 枚举对象
     */
    public static WSContentTypeEnum getByType(String type) {
        if (type == null) {
            return null;
        }

        for (WSContentTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }

        return null;
    }

    /**
     * 判断是否包含
     * @param val 目标值
     * @return true: 包含该目标值
     */
    public static boolean contains(String val) {
        if (val == null) {
            return false;
        }

        for (WSContentTypeEnum value : values()) {
            if (value.getType().equals(val) || value.getName().equals(val)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否相等
     * @param type 目标值
     * @return true: 相等
     */
    public boolean typeEquals(String type) {
        return this.type.equals(type);
    }

}
