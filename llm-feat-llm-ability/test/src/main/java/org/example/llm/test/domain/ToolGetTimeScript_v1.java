package org.example.llm.test.domain;

import lombok.Data;

@Data
public class ToolGetTimeScript_v1 {
    private String sceneName;
    private String sceneId;
    private String role;
    private String toolName;
    private String toolParam;
    private String toolRequired;
    private String question;
    private String lastTermStartDate;
    private String lastTermEndDate;
    private String currentTermStartDate;
    private String currentTermEndDate;
    private String nextTermStartDate;
    private String nextTermEndDate;
    private String targets;
}
