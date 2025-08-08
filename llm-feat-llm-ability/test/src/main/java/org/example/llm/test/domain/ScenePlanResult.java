package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScenePlanResult {
    private String role;
    private String question;
    private String predicatePlanResult;
    private String judgeType;
    private String thinkingContent;
    private String planResult;
    private String correct;
}
