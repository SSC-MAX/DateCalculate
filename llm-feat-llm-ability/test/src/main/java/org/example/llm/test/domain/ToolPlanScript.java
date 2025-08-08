package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/6/18 17:54
 */
@Data
@Accessors(chain = true)
public class ToolPlanScript {
    private String address;
    private String sseEndpoint;
    private String toolPlanRule;
    private String predicatePlanResult;
    private String judgeType;
    private String userInput;
}
