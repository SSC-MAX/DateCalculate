package org.example.llm.test.assistant.tool;

import java.util.Set;

/**
 * @author : zybi
 * @date : 2025/7/3 14:41
 */
public interface ToolParamConstants {
    /**
     * 工具参数
     */
    interface ToolCommonParam {
        String TRACE_ID = "traceId";
        String USER_ID = "userId";
        String ROLE_EN_NAME = "roleEnName";
        String SCHOOL_ID = "schoolId";
        String APP_KEY = "appKey";
        String TOKEN = "token";
        String CLIENT_TYPE = "clientType";
        String DEVICE_ID = "deviceId";
        String PAGE_NO = "pageNo";
        String PAGE_SIZE = "pageSize";
        String SESSION_ID = "sessionId";
        String REQUEST_TYPE = "requestType";

        String X_APP_KEY = "X-APPKEY";
        String X_TOKEN = "X-TOKEN";
        String X_USER_ID = "X-USERID";

        Set<String> ALL = Set.of(
                TRACE_ID, USER_ID, ROLE_EN_NAME, SCHOOL_ID,
                APP_KEY, TOKEN, CLIENT_TYPE, DEVICE_ID,
                PAGE_NO, PAGE_SIZE,
                SESSION_ID, REQUEST_TYPE,
                X_APP_KEY, X_TOKEN, X_USER_ID
        );
    }
}
