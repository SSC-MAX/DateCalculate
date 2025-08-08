package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/6/18 17:54
 */
@Data
@Accessors(chain = true)
public class ToolPlanScriptV2 {
    private String sceneName;
    private String role;
    private String toolName;
    private String toolDesc;
    private String predicatePlanResult;
    private String judgeType;
    private String userInput;
}
