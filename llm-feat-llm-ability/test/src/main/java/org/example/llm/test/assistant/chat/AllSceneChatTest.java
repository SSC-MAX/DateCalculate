//package org.example.llm.test.assistant.chat;
//
//import com.alibaba.fastjson2.TypeReference;
//import lombok.extern.slf4j.Slf4j;
//import org.example.llm.common.enums.ChatEntranceEnum;
//import org.example.llm.common.enums.WSContentTypeEnum;
//import org.example.llm.common.model.domain.*;
//import org.example.llm.common.util.JsonUtil;
//import org.example.llm.common.util.ws.WebSocketClientUtil;
//import org.example.llm.test.Common;
//import org.example.llm.test.domain.Context;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.UUID;
//
///**
// * @author : zybi
// * @date : 2025/7/19 15:54
// */
//@Slf4j
//public class AllSceneChatTest {
//    static Scanner scanner = new Scanner(System.in);
//    static final String ENV =
//            Map.of(
//                            "local", "localhost:19271",
//                            "dev", "172.29.236.100:19271",
//                            "pre", "agent-test.ceshiservice.cn"
//                    )
//                    .get("local");
//    static final String TOKEN = "testToken002";
//    static final String APP_KEY = "jydmx-llm-web";
//    static final String CLIENT_TYPE = "web";
//    static final String DEVICE_ID = "DDA9442F-AE55-43C7-BD78-C257948C1C4B";
//    static final String URL = "ws://"+ENV+"/beta/api/assistant/chat?X-TOKEN="+TOKEN+"&X-APPKEY="+APP_KEY+"&X-DEVICEID="+DEVICE_ID+"&X-CLIENTTYPE="+CLIENT_TYPE+"&chatId=test-asst-chatId-1748440994";
//
//    static final String ROLE = "teacher";
//    static final String SCHOOL_ID = "1500000200043255097";
//
//    void chat() {
//        ChatMessageRequest request = new ChatMessageRequest();
//        request.setUserInfo(new UserBasicInfo()
//                .setRoleEnName(ROLE)
//                .setSchoolId(SCHOOL_ID));
//        request.setMessage(List.of(new TypeAndContent()
//                        .setContentType(WSContentTypeEnum.TEXT.getType())
//                        .setContent(line)))
//                .setSceneId(sceneId)
//                .setEntrance(ChatEntranceEnum.PLAN_AND_EXEC.getEntrance())
//                .setEvent(new ChatMessageRequest.Event())
//                .setTest(new ChatMessageRequest.Test().setEnable(Boolean.TRUE));
//        Context cont = new Context().setLastTypeEnum(null);
//        WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
//                .traceId(UUID.randomUUID().toString())
//                .url(URL)
//                .param(request)
//                .responseEndFlag(response -> response == null || response.getCode() != 0 || response.getData() == null || response.getData().getDialogEnd())
//                .onOpen(context -> {
//                    log.info("客户端建连成功");
//                })
//                .onTextMessage((context, stringResponse) -> {
//                    ApiResponse<ChatResponseData> response = JsonUtil.parseWrapper(stringResponse, new TypeReference<ApiResponse<ChatResponseData>>() {
//                    }.getType());
//                    Common.handleResponse( request, response, cont );
//                    return response;
//                })
//                .onError((context, err) -> {
//                    log.error("客户端监听到异常", err);
//                })
//                .onClose((context, code, msg) -> {
//                    System.out.println("\n\n");
//                })
//                .request();
//    }
//}
