package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ToolGetTimeResult {
    private String sceneName;
    private String sceneId;
    private String role;
    private String toolName;
    private String toolParam;
    private String toolRequired;
    private String question;
    private String result;
    private String target;
}
