package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jzxiong
 * @version V1.0
 * @description
 * @date 2025/6/28 18:41
 */
@Data
@Accessors(chain = true)
public class WorkflowDetailRequest {

    private String workflowName;
    private String workflowVersionId;
    private String version;

}
