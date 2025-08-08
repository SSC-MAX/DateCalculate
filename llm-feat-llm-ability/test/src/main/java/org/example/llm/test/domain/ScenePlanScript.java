package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScenePlanScript {
    private String question;
    private String role;
    private String predicatePlanResult;
    private String judgeType;

}