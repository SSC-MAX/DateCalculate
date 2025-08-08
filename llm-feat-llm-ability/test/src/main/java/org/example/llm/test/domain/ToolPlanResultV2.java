package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/6/18 18:02
 */
@Data
@Accessors(chain = true)
public class ToolPlanResultV2 {
    private String sceneName;
    private String role;
    private String toolList;
    private String userInput;
    private String predicatePlanResult;
    private String judgeType;
    private String planResult;
    private String correct;
}
