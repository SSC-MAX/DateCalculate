package org.example.llm.test.ddm;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.common.enums.WSContentTypeEnum;
import org.example.llm.common.model.domain.ApiResponse;
import org.example.llm.common.model.domain.ChatResponseData;
import org.example.llm.common.util.JsonUtil;
import org.example.llm.common.util.ws.BaseWebSocketBlockSwitcher;
import org.example.llm.common.util.ws.WebSocketClientUtil;
import org.example.llm.test.Common;

import java.util.UUID;

/**
 * @author : zybi
 * @date : 2025/5/29 10:30
 */
@Slf4j
public class StudentLeave {

    static final String ADDRESS = "localhost:19270";
    //static final String ADDRESS = "172.29.236.100:19270";
    static final String TOKEN = "7af5c459-87ec-4a85-876b-6953a96cf560";
    static final String ASST_ID = "asst-3f717080c99a4f0c875ab1cf2cfe2e06";
    static final String ROLE = "student";
    static final String SCHOOL_ID = "1500000200069226983";





    static WSContentTypeEnum contentTypeEnum = null;
    public static void main(String[] args) {
        // 小华，由于感冒发烧，想申请请假，请假一天
        // 五年级二班的男生小华，由于感冒发烧，想申请请假，请假一天
        JSONObject jsonObject = JSONObject.parseObject("""
                {
                  "assistantId": "{{asstId}}",
                  "sceneId": "",
                  "sessionId": "",
                  "taskId": "",
                  "userInfo": {
                    "roleEnName": "{{role}}",
                    "schoolId": "{{schoolId}}"
                  },
                  "chatId": "test-asst-chatId-{{chatId}}",
                  "dialogId": "",
                  "messageType": "input",
                  "message": [
                    {
                      "contentType": "1010",
                      "content": "我是小华同学，由于感冒发烧，想申请请假，请假一天"
                    }
                  ],
                  "event": {
                    "type": "",
                    "formData": {
                    }
                  },
                  "test": {
                    "enable": true
                  }
                }
                """
                .replace("{{asstId}}", ASST_ID)
                .replace("{{chatId}}", System.currentTimeMillis() + "")
                .replace("{{role}}", ROLE)
                .replace("{{schoolId}}", SCHOOL_ID)
        );


        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                .traceId(UUID.randomUUID().toString())
                .url("ws://"+ADDRESS+"/api/assistant/chat?X-TOKEN="+TOKEN+"&X-APPKEY=jydmx-llm-web&chatId=test-asst-chatId-1748440994")
                //.url("ws://172.29.236.100:19270/api/assistant/chat?X-TOKEN=98ef35f0-aab9-4be6-b981-692d71d90911&X-APPKEY=jydmx-llm-web&X-CLIENTTYPE=ios&X-DEVICEID=mock-deviceId&chatId=test-asst-chatId-1748440994")
                .param(jsonObject)
                .switcher(new BaseWebSocketBlockSwitcher(5L, 20L))
                .responseEndFlag(response -> response == null || response.getCode() != 0 || response.getData() == null || response.getData().getDialogEnd())
                .onOpen(context -> {
                    log.info("客户端建连成功");
                })
                .onTextMessage((context, stringResponse) -> {
                    //log.info("客户端接收到字符串消息: {}", stringResponse);
                    ApiResponse<ChatResponseData> response = JsonUtil.parseWrapper(stringResponse, new TypeReference<ApiResponse<ChatResponseData>>() {
                    }.getType());
                    contentTypeEnum = Common.handleResponse( response, contentTypeEnum );
                    return response;
                })
                .onError((context, err) -> {
                    log.error("客户端监听到异常", err);
                })
                .onClose((context, code, msg) -> {
                    //log.info("客户端关闭连接, code: {}, msg: {}", code, msg);
                    System.out.println("\n\n");
                })
                .request();
    }

}
