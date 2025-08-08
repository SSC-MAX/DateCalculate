package org.example.llm.test;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.common.enums.WSContentTypeEnum;
import org.example.llm.common.model.domain.ApiResponse;
import org.example.llm.common.model.domain.ChatResponseData;
import org.example.llm.common.util.JsonUtil;
import org.example.llm.common.util.ws.BaseWebSocketBlockSwitcher;
import org.example.llm.common.util.ws.WebSocketClientUtil;

import java.util.UUID;

@Slf4j
public class WebSocket {
    static WSContentTypeEnum contentTypeEnum = null;

    public static void main(String[] args) {
        JSONObject jsonObject = JSONObject.parseObject("""
                {
                  "assistantId": "asst-3f717080c99a4f0c875ab1cf2cfe2e06",
                  "sceneId": "",
                  "sessionId": "",
                  "taskId": "",
                  "userInfo": {
                    "roleEnName": "student",
                    "schoolId": "1500000200069226983"
                  },
                  "chatId": "test-asst-chatId-{{chatId}}",
                  "dialogId": "",
                  "messageType": "input",
                  "message": [
                    {
                      "contentType": "1010",
                      "content": "下午有事我要请假"
                    }
                  ],
                  "event": {
                    "type": "",
                    "formData": {
                    }
                  }
                }
                """.replace("{{chatId}}", System.currentTimeMillis() + ""));


        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                .traceId(UUID.randomUUID().toString())
                .url("ws://localhost:19270/api/assistant/chat?X-TOKEN=98ef35f0-aab9-4be6-b981-692d71d90911&X-APPKEY=jydmx-llm-web&X-CLIENTTYPE=ios&X-DEVICEID=mock-deviceId&chatId=test-asst-chatId-1748440994")
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
