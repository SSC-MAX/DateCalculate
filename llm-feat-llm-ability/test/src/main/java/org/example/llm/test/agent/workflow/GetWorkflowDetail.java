package org.example.llm.test.agent.workflow;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.example.llm.common.constants.CommonConstants;
import org.example.llm.common.model.domain.ApiResponse;
import org.example.llm.common.util.http.OkHttpClientUtil;
import org.example.llm.test.domain.WorkflowDetailRequest;
import org.example.llm.test.domain.WorkflowDetailResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zybi
 * @date : 2025/7/19 13:55
 */
@Slf4j
public class GetWorkflowDetail {
    static final String AGENT_PLATFORM_HOST = "https://www.sparktutor.cn/cerebra-agent";
    static final String AGENT_PLATFORM_PATH = "/api/v1/workflow/detail";

    static final String TENANT_ID = "1500000100266983834";
    static final String TENANT_TYPE = "school";
    static final String TOKEN = "36f410be-a0ff-42a5-8525-289b56e6a82f";
    static final String USER_ID = "1500000100328972692";
    static final String ROLE_ID = "schoolAdministrator";
    static final String CUSTOM_APP_KEY = "jydmx-llm-web";

    static final List<String> WORKFLOW_IDS = List.of(
            "6877642f5cf275948ddf80a3",
            "687764c55cf275948ddf80a4",
            "687857b55cf275948ddf80ad",
            "687745ee5cf275948ddf8092",
            "67ff64a75cf2c4aed3847558",
            "6877493a5cf275948ddf8096",
            "687744315cf2c46bffc28c32",
            "6877483f5cf2c46bffc28c36",
            "6878bc595cf275948ddf80bc",
            "6878bd7c5cf275948ddf80bd",
            "6878dbc45cf2c46bffc28c59"
    );

    public static void main(String[] args) {
        for (String workflowId : WORKFLOW_IDS) {
            WorkflowDetailResponse workflowDetailResponse = getWorkflowDetail(
                    Map.of(
                            CommonConstants.TENANT_ID, TENANT_ID,
                            CommonConstants.TENANT_TYPE, TENANT_TYPE,
                            CommonConstants.TOKEN, TOKEN,
                            CommonConstants.USER_ID, USER_ID,
                            CommonConstants.ROLE_ID, ROLE_ID,
                            CommonConstants.CUSTOM_APPKEY, CUSTOM_APP_KEY
                    ),
                    new WorkflowDetailRequest()
                            .setVersion("001")
                            .setWorkflowName("英语张老师")
                            .setWorkflowVersionId(workflowId)
            );
            assert workflowDetailResponse != null;
            List<WorkflowDetailResponse.WorkflowVersionInputDto> inputs = workflowDetailResponse.getInputs();
            List<String> required = new ArrayList<>();
            for (WorkflowDetailResponse.WorkflowVersionInputDto input : inputs) {
                if (Boolean.TRUE.equals(input.getRequired())) {
                    required.add(input.getName());
                }
            }
            System.out.println("工作流名称：" + workflowDetailResponse.getWorkflowName());
            System.out.println("工作流描述：" + workflowDetailResponse.getWorkflowDesc());
            System.out.println("工作流参数：" + JSONObject.toJSONString(inputs));
            System.out.println("工作流必填：" + required);
        }
    }

    public static WorkflowDetailResponse getWorkflowDetail(Map<String, String> headers, WorkflowDetailRequest workflowDetailRequest) {
        try {
            Map<String, String> params = buildWorkflowDetailParams(workflowDetailRequest);
            String exec = OkHttpClientUtil.createGet(String.format("%s%s", AGENT_PLATFORM_HOST, AGENT_PLATFORM_PATH))
                    .addAllHeaders(headers)
                    .addAllParam(params)
                    .exec();
            ApiResponse<WorkflowDetailResponse> streamWorkflowResponse = JSONObject.parseObject(exec, new TypeReference<ApiResponse<WorkflowDetailResponse>>() {
            });
            return streamWorkflowResponse.getData();
        }catch (Exception e){
            log.error("获取工作流详情失败", e);
        }
        return null;
    }

    private static Map<String, String> buildWorkflowDetailParams(WorkflowDetailRequest workflowDetailRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("workflowName", workflowDetailRequest.getWorkflowName());
        params.put("version", workflowDetailRequest.getVersion());
        params.put("workflowVersionId", workflowDetailRequest.getWorkflowVersionId());
        return params;
    }
}
