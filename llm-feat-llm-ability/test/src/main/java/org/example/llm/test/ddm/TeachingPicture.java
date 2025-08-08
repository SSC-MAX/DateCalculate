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
public class TeachingPicture {

    //static final String ADDRESS = "agent-test.ceshiservice.cn";
    // static final String ADDRESS = "localhost:19270";
    static final String ADDRESS = "172.29.236.100:19270";
    static final String TOKEN = "testToken002";

    static final String ROLE = "student";
    static final String SCHOOL_ID = "1500000200069226983";





    static WSContentTypeEnum contentTypeEnum = null;
    public static void main(String[] args) {
        textToImage();
        //iamgeToImage();
    }


    static void textToImage() {
        JSONObject jsonObject = JSONObject.parseObject("""
                {
                  "assistantId": "asst-3f717080c99a4f0c875ab1cf2cfe2e06",
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
                  "message": [{"contentType":"1010","content":"给图中增加几颗数"},{"contentType":"2010","content":"{\\"url\\":\\"https://test.download.cycore.cn/ecsp-iflytek/20250607/4ba39340c051435da6e623411d926e4e/20250423_205206.jpg\\",\\"format\\":\\"img\\"}"}
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
                """.replace("{{chatId}}", System.currentTimeMillis() + "")
                .replace("{{role}}", ROLE)
                .replace("{{schoolId}}", SCHOOL_ID)
        );


        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                .traceId(UUID.randomUUID().toString())
                .url("ws://"+ADDRESS+"/api/assistant/chat?X-TOKEN="+TOKEN+"&X-APPKEY=jydmx-llm-web&chatId=test-asst-chatId-1748440994")
                //.url("ws://172.29.236.100:19270/api/assistant/chat?X-TOKEN=98ef35f0-aab9-4be6-b981-692d71d90911&X-APPKEY=jydmx-llm-web&X-CLIENTTYPE=ios&X-DEVICEID=mock-deviceId&chatId=test-asst-chatId-1748440994")
                .param(jsonObject)
                .responseEndFlag(response -> response == null || response.getCode() != 0 || response.getData() == null || response.getData().getDialogEnd())
                .onOpen(context -> {
                    log.info("客户端建连成功");
                })
                .onTextMessage((context, stringResponse) -> {
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


    static void iamgeToImage() {
        JSONObject jsonObject = JSONObject.parseObject("""
                {
                  "assistantId": "asst-3f717080c99a4f0c875ab1cf2cfe2e06",
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
                      "content": "改成黑白的"
                    },
                    {
                      "contentType": "2010",
                      "content": "https://bj.download.cycore.cn/szjz-saas-epas-proxy/2025/04/14/15/44efc2f8db-86aa-4d76-8b43-f72b1a493510.png"
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
                """.replace("{{chatId}}", System.currentTimeMillis() + "")
                .replace("{{role}}", ROLE)
                .replace("{{schoolId}}", SCHOOL_ID)
        );


        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                .traceId(UUID.randomUUID().toString())
                .url("ws://"+ADDRESS+"/api/assistant/chat?X-TOKEN="+TOKEN+"&X-APPKEY=jydmx-llm-web&X-CLIENTTYPE=ios&X-DEVICEID=mock-deviceId&chatId=test-asst-chatId-1748440994")
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
