package org.example.llm.test.domain;

import lombok.Data;

@Data
public class ToolGetTimeScript {
    private String sceneName;
    private String sceneId;
    private String role;
    private String toolName;
    private String toolParam;
    private String toolRequired;
    private String questions;
    private String predicatePlanResult;
    private String judgeType;
    private String targets;
}
