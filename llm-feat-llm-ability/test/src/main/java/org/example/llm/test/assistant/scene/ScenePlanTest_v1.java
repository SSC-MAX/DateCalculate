package org.example.llm.test.assistant.scene;

import com.alibaba.fastjson2.JSONArray;
import org.example.llm.ability.LlmAbilityCaller;
import org.example.llm.ability.handler.CollectThinkingAndContentHandler;
import org.example.llm.common.util.Checks;
import org.example.llm.common.util.ExcelUtil;
import org.example.llm.test.domain.QuestionAndScenes;
import org.example.llm.test.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScenePlanTest_v1 {

    static String userInfo = """
            用户姓名：张三
            用户角色：teacher
            用户学校id：1234
            """;

    static String sceneInfo =
            """
            #学生请假
            可以完成以下任务
            1. 班主任查询学生请假数据或记录、撤销学生请假、学生销假、学生情况、返校情况等；规则：除学生本人外，班主任、教师、家长也可帮学生进行请"
                        
            #教师请假
            可以完成以下任务
            1.教师查询教师请假数据或记录、撤销教师请假；规则：班主任或者教师可以为自己请假，不能帮其他教师同事请假
                        
            #教师调课
            教师调课，可以完成以下任务
            1. 教师申请调课
            2. 查询教师调课、教师调课情况查询等，且不包含请假相关要求
                        
            #失物招领
            失物招领，可以完成以下任务
            1. 用于用户登记和提交新的丢失物品信息，便于后续失物招领管理和查找。
                        
            #继续教育培训
            继续教育培训，可完成以下任务
            1. 用于教师查询和管理自己的培训任务列表，并支持按培训名称进行筛选。
            用车申请	"用车申请，可以完成以下任务
            1、 教师查询和管理用车申请记录"
                        
            #教学图片生成
            根据用户的要求和描述信息，可完成以下工作
            1. 根据用户文字描述要求来生成一张图片
            2. 根据用户上传的图片文件以及修改诉求来生成一张修改后的图片
            """;

    static String system =
            """
              # 角色
              你是一个场景规划助手，通过分析用户问题并结合上文对话内容，分解用户意图并并对每个意图定位到合适的场景
                    
              # 任务
              1. 你需要分析用户问题，分解出用户意图，如果用户问题不可拆分，则将原输入作为唯一的意图，首先判断用户问题和历史记录是否相关，如果相关则参考历史记录，如果无关则直接根据当前问题，给出最匹配的一个或者几个场景，如果没有匹配合适的，也可直接说明
              2. 对每个意图规划到合适的场景；以数组形式输出场景规划结果，如果用户输入信息不够精确定位到多个场景，则将多个场景一起输出
              3. 判断推理需要结合用户信息
              4. 每个场景描述中的能力都涵盖了其负责的责任范围，即该场景能完成的任务，如果场景能力无法完成用户要求，则不可以作为规划结果
              5. step by step地拆分用户问题，输出结果为数组，包含拆分的子问题question和最匹配的场景名称scenes，存在以下情况
                a. 多个子问题，各个子问题匹配单一场景：格式为[{"question": "子问题1", "scenes": ["命中场景1"]},{"question": "子问题2", "scenes": ["命中场景2"]}]
                b. 存在某个子问题未命中场景，则对应匹配场景为空，格式为[{"question": "子问题3", "scenes": ["命中场景3"]},{"question": "子问题4", "scenes": []}]
                c. 存在某个子问题对应最匹配场景有多个，则对应匹配场景为多个场景列表，格式为：[{"question": "子问题5", "scenes": ["命中场景5","命中场景6"]}]
                    
              # 用户信息
              {{userInfo}}
                    
              ============场景信息 开始================
              {{sceneList}}
              ============场景信息 结束================
                    
              ============输出格式示例 开始================
              [{"question": "子问题1", "scenes": ["命中场景1"]}]
              ============输出格式示例 结束================
                    
              # 回答格式
              1. 不要包含```json ```, 直接输出json字符串即可； 严格按照输出格式示例，禁止包含规划的结果等字眼，结果仅包含json数组即可不要包含任何其他无关内容
              2. 每个意图关联的场景列表内部元素为场景名称
              3. 输出格式为对象的数组，每个对象包含2个属性，分别是：question: 拆解用户意图后的子问题、scenes: 该意图相关的场景列表
              4. 拆解后的子问题如果长度超过50个字符，则精简成50字符以内作为输出结果；拆解后的子问题不可以包含文件或url等信息
              5. 输出内容应当总是包含至少一个question和其命中的scenes数组,如果该意图没有命中任何场景，则该意图的scenes数组值应当为空
              6. 思考过程的输出只需要关注场景命中的条件以及推理过程，要求性描述不必再思考过程中复述
              7. 思考过程输出禁止包含任何用户信息
            """
                    .replace("{{userInfo}}", userInfo)
                    .replace("{{sceneList}}", sceneInfo);


    static List<String> questions = List.of("""
            明天需要去市里开会，请安排一辆公车。
            下周二有个研讨会，需要申请一辆公车。
            这周五要去参加一个学术交流会，请帮我安排一辆公车。
            下周一要带学生去参观博物馆，请安排一辆大客车。
            下周三有个重要会议，需要提前一天申请一辆公车。
            这个周末要去外地调研，请安排一辆适合长途行驶的公车。
            下周四要去另一个校区讲课，请安排一辆公车接送。
            明天下午要去教育局办事，请尽快安排一辆公车。
            下周六有个校外活动，需要申请一辆公车。
            后天上午要去机场接一位专家，请安排一辆公车。
            请提供最近一个月内所有教师的用车记录。
            查询一下这学期教师用车的总次数和平均使用频率。
            能给我看看上周教师用车的具体情况吗？
            统计一下这个月各部门的用车情况。
            显示一下上个月教师用车最多的部门是哪个。
            查询一下最近三个月内教师用车的高峰时段。
            列出今年教师用车的详细统计报告。
            分析一下最近一年内教师用车的变化趋势。
            提供一份按周汇总的教师用车统计表。
            查看一下这学期教师用车的最频繁日期。
            打开车辆申请应用
            提交用车申请
            查看我的用车申请状态
            取消用车申请
            修改用车申请时间
            查看可用车辆列表
            预订特定车辆
            延长用车时间
            确认用车申请
            完成用车反馈
            """.split("\n"));


    public static void main(String[] args) {
        List<Supplier<CollectThinkingAndContentHandler>> tasks = new ArrayList<>();
        for (String question : questions) {
            tasks.add(() -> {
                CollectThinkingAndContentHandler collectThinkingAndContentHandler = new CollectThinkingAndContentHandler(question);
                LlmAbilityCaller.chat("scenePlan", system, null, question, true, false, collectThinkingAndContentHandler);
                return collectThinkingAndContentHandler;
            });
        }
        List<CollectThinkingAndContentHandler> outputThinkingWithContentHandlers = ThreadUtil.submitBatchTask(5, tasks);

        String fileName = "测试";
        List<String> titles = List.of("问题", "思考过程", "规划结果");
        List<List<String>> data = outputThinkingWithContentHandlers.stream()
                        .map(r -> {
                            String question = r.getQuestion();
                            String thinkingContent = r.getAllThinkingContent().toString();
                            String content = r.getAllContent().toString();
                            StringBuilder proceedContent = new StringBuilder();
                            try {
                                List<QuestionAndScenes> questionAndScenes = JSONArray.parseArray(content, QuestionAndScenes.class);
                                if (Checks.noNull( questionAndScenes )) {
                                    for (QuestionAndScenes questionAndScene : questionAndScenes) {
                                        proceedContent.append("意图：").append(questionAndScene.getQuestion()).append("\n");
                                        proceedContent.append("命中场景：").append(String.join(",", questionAndScene.getScenes())).append("\n\n");
                                    }
                                }
                            }
                            catch (Exception e) {
                                proceedContent = new StringBuilder("生成结果格式异常：" + content);
                            }
                            return List.of(question, thinkingContent, proceedContent.toString());
                        }).toList();
        ExcelUtil.gen("e://data//"+fileName+"-"+System.currentTimeMillis()+".xlsx", fileName, titles, data);

    }

}
