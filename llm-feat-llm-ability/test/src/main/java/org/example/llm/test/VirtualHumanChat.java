package org.example.llm.test;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.common.enums.ChatEntranceEnum;
import org.example.llm.common.enums.WSContentTypeEnum;
import org.example.llm.common.model.domain.*;
import org.example.llm.common.util.JsonUtil;
import org.example.llm.common.util.ws.WebSocketClientUtil;
import org.example.llm.test.domain.Context;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * @author : zybi
 * @date : 2025/6/1 22:19
 */
@Slf4j
public class VirtualHumanChat {
    // 创建 Scanner 对象用于读取控制台输入
    static Scanner scanner = new Scanner(System.in);
    static final String ENV =
            Map.of(
                            "local", "localhost:19271",
                            "dev", "172.29.236.100:19271",
                            "pre", "agent-test.ceshiservice.cn"
                    )
                    .get("local");
    static final String TOKEN = "testToken002";
    static final String APP_KEY = "jydmx-llm-web";
    static final String CLIENT_TYPE = "ios";
    static final String DEVICE_ID = "DDA9442F-AE55-43C7-BD78-C257948C1C4B";
    //static final String ASST_ID = "asst-02bc5f30e32f428d83d8c64a3500a194";
    static final String ASST_ID = "asst-advertisingDesignCompanion";
    static final String ROLE = "teacher";
    static final String SCHOOL_ID = "1500000100266983834"; // 1500000200069226983

    static final String URL = "ws://"+ENV+"/beta/api/assistant/chat/virtualHuman?X-TOKEN="+TOKEN+"&X-APPKEY="+APP_KEY+"&X-DEVICEID="+DEVICE_ID+"&X-CLIENTTYPE="+CLIENT_TYPE+"&chatId=test-asst-chatId-1748440994";

    static final String EXIT = "exit";

    public static void main(String[] args) {
        ChatMessageRequest request = new ChatMessageRequest();

        // 输入必填参数
        inputRequired(request);

        // 开始对话
        while (true) {
            System.out.println("输入类型（i-input | e-event）");
            String line = scanner.nextLine();
            request.setMessageType("e".equals(line)? "event" : "input");

            System.out.println("输入问题");
            line = scanner.nextLine();

            if (EXIT.equalsIgnoreCase(line.trim())) {
                System.out.println("会话结束");
                break;
            }

            System.out.println(JSONObject.toJSONString(request));

            request.setMessage(List.of(new TypeAndContent()
                    .setContentType(WSContentTypeEnum.TEXT.getType())
                    .setContent(line)))
                    //.setEntrance(ChatEntranceEnum.V.getEntrance())
                    .setEvent(new ChatMessageRequest.Event())
                    .setTest(new ChatMessageRequest.Test().setEnable(Boolean.TRUE));
            Context cont = new Context().setLastTypeEnum(null);
            WebSocketClientUtil.<ApiResponse<ChatResponseData>>create()
                    .traceId(UUID.randomUUID().toString())
                    .url(URL)
                    .param(request)
                    .responseEndFlag(response -> response == null || response.getCode() != 0 || response.getData() == null || response.getData().getDialogEnd())
                    .onOpen(context -> {
                        log.info("客户端建连成功");
                    })
                    .onTextMessage((context, stringResponse) -> {
                        ApiResponse<ChatResponseData> response = JsonUtil.parseWrapper(stringResponse, new TypeReference<ApiResponse<ChatResponseData>>() {
                        }.getType());
                        Common.handleResponse( request, response, cont );
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

    static void inputRequired(ChatMessageRequest request) {
        //System.out.println("输入助理ID（assistantId）");
        //String line = scanner.nextLine();
        request.setAssistantId(ASST_ID);

        //.out.println("输入会话ID（chatId）");
        //String line = scanner.nextLine();
        request.setChatId(UUID.randomUUID().toString());

        UserBasicInfo userBasicInfo = new UserBasicInfo();
        //System.out.println("输入用户角色（roleEnName）");
        //line = scanner.nextLine();
        userBasicInfo.setRoleEnName(ROLE);

        //System.out.println("输入用户机构ID（schoolId）");
        //line = scanner.nextLine();
        userBasicInfo.setSchoolId(SCHOOL_ID);

        request.setUserInfo(userBasicInfo);

        /*System.out.println("是否开启测试（true | false）");
        boolean enableTest = scanner.nextBoolean();
        request.setTest(new ChatMessageRequest.Test().setEnable(enableTest));*/
    }

}
