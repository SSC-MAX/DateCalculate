package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ToolExtractParam {
    private String toolName;
    private String toolParam;
    private String input;
    private String target;
}
