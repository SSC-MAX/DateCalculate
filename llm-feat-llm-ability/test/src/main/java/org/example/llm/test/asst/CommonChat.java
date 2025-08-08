package org.example.llm.test.asst;

import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.common.enums.WSContentTypeEnum;
import org.example.llm.common.model.domain.*;
import org.example.llm.common.util.JsonUtil;
import org.example.llm.common.util.ws.WebSocketClientUtil;
import org.example.llm.test.Common;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author : zybi
 * @date : 2025/5/29 19:39
 */
@Slf4j
public class CommonChat {


    static final String ENV =
            Map.of(
                "local", "localhost:19270",
                "dev", "agent-dev.ceshiservice.cn",
                "pre", "agent-test.ceshiservice.cn"
            )
            .get("test");
    static final String TOKEN = "testToken002";
    static final String ASST_ID = "asst-3f717080c99a4f0c875ab1cf2cfe2e06";
    static final String ROLE = "student";
    static final String SCHOOL_ID = "1500000200069226983";
    static final String URL = "ws://"+ENV+"/api/assistant/chat?X-TOKEN="+TOKEN+"&X-APPKEY=ifly-szjz-saas&chatId=test-asst-chatId-1748440994";



    static WSContentTypeEnum contentTypeEnum = null;
    public static void main(String[] args) {
        ChatMessageRequest chatMessageRequest = new ChatMessageRequest()
                .setAssistantId(ASST_ID)
                .setChatId(System.currentTimeMillis() + "")
                .setUserInfo(new UserBasicInfo()
                        .setRoleEnName(ROLE)
                        .setSchoolId(SCHOOL_ID))
                .setMessageType("input")
                .setMessage(List.of(
                        new TypeAndContent().setContentType(WSContentTypeEnum.TEXT.getType()).setContent("你好")
                ))
                .setEvent(new ChatMessageRequest.Event().setType(null).setFormData(null))
                .setTest(new ChatMessageRequest.Test().setEnable(Boolean.TRUE));

        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                .traceId(UUID.randomUUID().toString())
                .url(URL)
                .param(chatMessageRequest)
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
                    System.out.println("\n\n");
                })
                .request();
    }
}
