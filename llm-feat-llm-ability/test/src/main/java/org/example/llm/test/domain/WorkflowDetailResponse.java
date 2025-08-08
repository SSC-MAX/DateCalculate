package org.example.llm.test.domain;

import lombok.Data;

import java.util.List;

/**
 * @author jzxiong
 * @version V1.0
 * @description
 * @date 2025/6/28 18:42
 */
@Data
public class WorkflowDetailResponse {

    private String workflowName;
    private String workflowDesc;
    private String workflowVersionId;
    private String workflowId;

    private List<WorkflowVersionInputDto> inputs;
    private List<WorkflowVersionOutputDto> outputs;

    private String agentId;
    private String versionId;

    @Data
    public static class WorkflowVersionInputDto{
        private String name;
        private String type;
        private Boolean required;
        private String description;
    }

    @Data
    public static class WorkflowVersionOutputDto{
        private String name;
        private String type;
    }
}
