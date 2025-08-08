package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/7/3 15:23
 */
@Data
@Accessors(chain = true)
public class ToolGetParamScript {

    private String sceneName;
    private String sceneId;
    private String role;
    private String toolName;
    private String toolParam;
    private String toolRequired;
    private String questions;
    private String predicatePlanResult;
    private String judgeType;
}
