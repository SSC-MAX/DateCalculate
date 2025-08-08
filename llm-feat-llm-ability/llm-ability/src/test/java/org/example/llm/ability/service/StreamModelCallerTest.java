package org.example.llm.ability.service;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.llm.ability.handler.StreamResponseHandler;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.ability.util.SseMessageProcessor;
import org.example.llm.common.util.ResourceUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
class StreamModelCallerTest {

    @Test
    public void test() {
        StreamResponseHandler streamResponseHandler = new StreamResponseHandler();
        StreamModelCaller.create("intentionRecognize")
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("user").setContent("你好")
                        )
                )
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    streamResponseHandler.output(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }


    @Test
    public void testScenePosition() {
        String prompt = """
                        # 角色
                          你是一个场景规划助手，通过分析用户问题并结合上文对话内容，分解用户意图并并对每个意图定位到合适的场景
                                            
                          # 任务
                          1. 你需要分析用户问题，分解出用户意图，如果用户问题不可拆分，则将原输入作为唯一的意图，首先判断用户问题和历史记录是否相关，如果相关则参考历史记录，如果无关则直接根据当前问题，给出最匹配的一个或者几个场景，如果没有匹配合适的，也可直接说明
                          2. 对每个意图规划到合适的场景；以数组形式输出场景规划结果，如果用户输入信息不够精确定位到多个场景，则将多个场景一起输出
                          3. 判断推理需要结合用户信息
                          4. 每个场景描述中的能力都涵盖了其负责的责任范围，即该场景能完成的任务，如果场景能力无法完成用户要求，则不可以作为规划结果
                                            
                          # 用户信息
                          用户姓名：毕震圆
                          用户角色：teacher
                          用户学校id：1500000200043255097
                                            
                          ============场景信息 开始================
                          # 场景能力列表
                           场景名称： 继教培训反馈
                           场景描述： 继续教育培训，可完成以下任务
                           1. 用于教师查询和管理自己的培训任务列表，并支持按培训名称进行筛选。
                           
                           场景名称： 失物招领
                           场景描述： 失物招领，可以完成以下任务
                           1. 用户描述自己丢失物品或者想要登记自己的丢失物品，则可以记录相关丢失物品数据
                           2. 查询和管理已登记的捡到物品信息
                           3. 查询所有登记在案的丢失物品信息，以找到自己丢失的物
                           
                           场景名称： 用车申请
                           场景描述： 用车申请，可以完成以下任务
                           1、 教师查询和管理用车申请记录
                           
                           场景名称： 教师请假
                           场景描述： 教师请假，可以完成以下任务
                           1. 教师为自己新增请假
                          ============场景信息 结束================
                                            
                          ============示例 开始======================
                          # 用户输入
                          我明天下午有事，帮我请个假，然后把明天下午的课调整到后天上午,最后帮我查询一下明天下午的课表
                                            
                          # 输出
                          [
                          {"intention": "我明天下午有事，帮我请个假", "scenes": ["命中场景1", "命中场景2"]},
                          {"intention": "把我明天下午的课调整到后天上午","scenes": ["命中场景3"]},
                          {"intention": "帮我查询一下明天下午的课表","scenes": []}
                          ]
                          ============示例 结束======================
                                            
                          # 回答格式
                          1. 不要包含```json ```, 直接输出json字符串即可
                          2. 每个意图关联的场景列表内部元素为场景名称
                          3. 输出格式为对象的数组，每个对象包含2个属性，分别是：intention: 拆解用户意图后的子问题、scenes: 该意图相关的场景列表
                          5. 输出内容应当总是包含至少一个intention和其命中的scenes数组,如果该意图没有命中任何场景，则该意图的scenes数组值应当为空
                          6. 思考过程的输出只需要关注场景命中的条件以及推理过程，要求性描述不必再思考过程中复述
                          7. 思考过程输出禁止包含任何用户信息
                        """;
        StreamResponseHandler streamResponseHandler = new StreamResponseHandler();
        StreamModelCaller.create("intentionRecognize")
                .setEnableThinking(Boolean.TRUE)
                .setStream(Boolean.TRUE)
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("system").setContent(prompt),
                                new SourceModelRequest.Message().setRole("user").setContent("明天下午有事情请个假，然后把明天下午的课调整到后天上午,最后帮我查询一下明天下午的课表")
                        )
                )
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    streamResponseHandler.output(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }


    @Test
    public void test_tools() throws IOException {
        String sceneListJson = ResourceUtil.readJsonFromResource("scene_list.json");

        StreamResponseHandler streamResponseHandler = new StreamResponseHandler();
        StreamModelCaller.create("sceneMatch")
                .setMessages(
                        List.of(
                                //new SourceModelRequest.Message().setRole("user").setContent("我明天下午有事，帮我请个假"),
                                //new SourceModelRequest.Message().setRole("assistant").setContent("请问你的教师id是多少？"),
                                new SourceModelRequest.Message().setRole("user").setContent("你好")
                        )
                )
                .setEnableThinking(Boolean.FALSE)
                .setStream(Boolean.TRUE)
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    System.out.println(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }

    @Test
    public void test2() {
        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 方法1：获取DayOfWeek枚举值
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        System.out.println("今天是星期: " + dayOfWeek);

        // 方法2：获取数字形式的星期几 (1=Monday, 7=Sunday)
        int dayOfWeekValue = dayOfWeek.getValue();
        System.out.println("星期的数值表示: " + dayOfWeekValue);

        // 方法3：获取本地化的星期几名称（完整形式）
        String dayName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
        System.out.println("本地化的星期名称(完整): " + dayName);

        // 方法4：获取本地化的星期几名称（缩写形式）
        String shortDayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
        System.out.println("本地化的星期名称(缩写): " + shortDayName);

        // 方法5：获取中文星期几
        String chineseDayName = getChineseDayOfWeek(dayOfWeek);
        System.out.println("中文星期几: " + chineseDayName);

    }
    // 自定义方法：获取中文星期几
    private static String getChineseDayOfWeek(DayOfWeek dayOfWeek) {
        String[] chineseDays = {"", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return chineseDays[dayOfWeek.getValue()];
    }
    @Test
    public void test_tools_2() throws IOException {
        String sceneListJson = ResourceUtil.readJsonFromResource("scene_list.json");

        StreamResponseHandler streamResponseHandler = new StreamResponseHandler();
        StreamModelCaller.create("sceneMatch")
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("system").setContent(
                                        """
                                        # 角色
                                        你是一个工具参数提取助手
                                                                                
                                        # 任务
                                        1. 从用户个人信息，当前时间信息，上下文信息，以及当前用户问题中提取工具的参数
                                        2. 抽槽提取的参数必须严格按照工具描述中的格式要求来
                                                                                
                                        # 参数提取要求
                                        1. 严禁使用不存在的信息构造工具参数
                                                                                
                                        # 用户信息
                                        用户姓名：喻建兰ls
                                        用户角色：teacher
                                        用户学校id：1500000200043255097
                                                                                
                                        # 当前时间
                                        {{currentTime}}
                                        
                                        # 工具参数定义
                                        {{toolDefinition}}
                                        
                                        # 示例输出json格式
                                        {"参数名": "参数值", "参数名": "参数值"}
                                        """.replace("{{currentTime}}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                                            .replace("{{toolDefinition}}", """
                                                    丢了一个钥匙
                                                    参数名称： lostItemName
                                                    参数描述要求：丢失物品名称

                                                    参数名称： lostTime
                                                    参数描述要求： 丢失时间 数据格式为：yyyy-MM-dd HH:mm

                                                    参数名称： entryTime
                                                    参数描述要求： 录入时间 数据格式为：yyyy-MM-dd HH:mm

                                                    参数名称： lostPersonName
                                                    参数描述要求： 丢失人姓名

                                                    参数名称： lostItemStatus
                                                    参数描述要求： 丢失物品状态

                                                    参数名称： itemDescription
                                                    参数描述要求： 物品描述

                                                    参数名称： lostLocation
                                                    参数描述要求： 丢失地点""")
                                ),
                                new SourceModelRequest.Message().setRole("user")
                                        .setContent("上午丢了个钥匙")
                        )
                )
                .setEnableThinking(Boolean.FALSE)
                .setStream(Boolean.FALSE)
                .setResponseFormat(new SourceModelRequest.ResponseFormat().setType("json_object"))
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    System.out.println(sourceModelResponse);
                    return sourceModelResponse;
                })
                .call();
    }
}
