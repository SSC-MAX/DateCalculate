package org.example.llm.test.assistant.scene;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.llm.ability.LlmAbilityCaller;
import org.example.llm.ability.handler.CollectThinkingAndContentHandler;
import org.example.llm.common.util.Checks;
import org.example.llm.common.util.ExcelUtil;
import org.example.llm.test.domain.QuestionAndScenes;
import org.example.llm.test.domain.ScenePlanResult;
import org.example.llm.test.domain.ScenePlanScript;
import org.example.llm.test.utils.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ScenePlanTest_v2 {

    @Data
    @Accessors(chain = true)
    static class ScriptInfo {
        private String filePath;
        private String fileName;
    }

    static List<ScriptInfo> scriptInfoList = List.of(
            /*new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_学生请假_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_失物招领_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_教师请假_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_继教培训反馈_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_用车申请_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_直播巡课_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_教学图片生成_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_创作_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_校长信箱_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_学校规章制度_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_场馆预约_v1"),*/
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_all")/*,
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_创作_v2"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_教育公文_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_教学反思_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_活动策划_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_项目式学习_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\1. 场景规划\\")
                    .setFileName("测试用例_英语单词教学_v1")*/
    );

    static final String[] MODELS = {/*"deepseekV3","doubao1.5",*/ "qwen3-235b"};



    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        for (ScriptInfo scriptInfo : scriptInfoList) {
            for (String model : MODELS) {
                run(scriptInfo.getFilePath(), scriptInfo.getFileName(), model);
            }
        }
    }

    static void run(String filePath, String fileName, String model) throws ReflectiveOperationException, IOException {
        File file = new File(filePath + fileName + ".xlsx");
        List<ScenePlanScript> scenePlanScripts = ExcelUtil.readExcel(file, ScenePlanScript.class);

        List<Supplier<ScenePlanResult>> tasks = new ArrayList<>();
        for (ScenePlanScript script : scenePlanScripts) {
            tasks.add(() -> {
                CollectThinkingAndContentHandler collectThinkingAndContentHandler = new CollectThinkingAndContentHandler(script.getQuestion());
                LlmAbilityCaller.chat(model, getSystem(script.getRole()), null, script.getQuestion(), true, false, collectThinkingAndContentHandler);
                return new ScenePlanResult()
                        .setRole(script.getRole())
                        .setQuestion(script.getQuestion())
                        .setPredicatePlanResult(script.getPredicatePlanResult())
                        .setJudgeType(script.getJudgeType())
                        .setThinkingContent(collectThinkingAndContentHandler.getAllThinkingContent().toString())
                        .setPlanResult(collectThinkingAndContentHandler.getAllContent().toString())
                        ;
            });
        }
        List<ScenePlanResult> outputThinkingWithContentHandlers = ThreadUtil.submitBatchTask(7, tasks);
        List<String> titles = List.of("角色", "问题", "预期结果", "判断逻辑","思考过程", "规划结果", "对错");
        List<List<String>> data = outputThinkingWithContentHandlers.stream()
                .map(r -> {
                    String question = r.getQuestion();
                    String thinkingContent = r.getThinkingContent();
                    String content = r.getPlanResult();
                    StringBuilder proceedContent = new StringBuilder();
                    String predicatePlanResult = r.getPredicatePlanResult();
                    String judgeType = r.getJudgeType();
                    String correct = "";
                    Set<String> allScenes = new HashSet<>();
                    try {
                        List<QuestionAndScenes> questionAndScenes = JSONArray.parseArray(content, QuestionAndScenes.class);
                        if (Checks.noNull( questionAndScenes )) {
                            for (QuestionAndScenes questionAndScene : questionAndScenes) {
                                proceedContent.append("意图：").append(questionAndScene.getQuestion()).append("\n");
                                proceedContent.append("命中场景：").append(String.join(",", questionAndScene.getScenes())).append("\n\n");
                                if (Checks.noNull( questionAndScene.getScenes() )) {
                                    allScenes.addAll( questionAndScene.getScenes() );
                                }
                            }
                        }
                        if (null == judgeType || "空".equals(judgeType)) {
                            correct = Checks.isNull(allScenes)? "对" : "错";
                        } else if ("等于".equals(judgeType)) {
                            correct = (Checks.noNull(allScenes) && allScenes.size() == 1 && allScenes.contains(predicatePlanResult))? "对" : "错";
                        } else if ("包含".equals(judgeType)) {
                            String[] predicates = predicatePlanResult.split("&&");
                            correct = "对";
                            for (String predicate : predicates) {
                                if (!allScenes.contains(predicate)) {
                                    correct = "错";
                                    break;
                                }
                            }
                        } else {
                            correct = "对";
                        }
                    }
                    catch (Exception e) {
                        proceedContent = new StringBuilder("生成结果格式异常：" + content);
                    }
                    return List.of(r.getRole()
                            , question
                            , r.getPredicatePlanResult() == null? "" : r.getPredicatePlanResult()
                            , r.getJudgeType() == null? "" : r.getJudgeType()
                            , thinkingContent == null? "" : thinkingContent
                            , proceedContent.toString()
                            , correct);
                }).toList();

        ExcelUtil.gen(filePath + fileName+"-"+model+"-测试结果-"+System.currentTimeMillis()+".xlsx", fileName, titles, data);
    }


    static String getSystem(String role) {
        return system
                .replace("{{userInfo}}", userInfo.replace("{{role}}", role))
                .replace("{{sceneList}}", sceneInfo);
    }

    static String userInfo = """
            用户姓名：张三
            用户角色：{{role}}
            用户学校id：1234
            """;

    static String sceneInfo =
                    """
                    # 场景名称： 学校规章制度
                    可以完成以下任务
                    1、支持管理员设置制度类别，发布制度文件，发布时可以选择可查看的用户权限范围、制度施行开始结束日期、附件是否可下载等；
                    2、支持教师、学生、家长查看自己权限范围的制度文件。
                                        
                    # 场景名称： 智能巡课
                    可以完成以下任务
                    1. 查看某年级班级的班级、课堂、巡课、监控、设备、摄像头
                                        
                    # 场景名称： 活动策划
                    可以策划各种创意的活动，可以评估预算，可以生成各种活动创意。
                                        
                    # 场景名称： 失物招领
                    可以完成以下任务
                    1. 教师角色、学生角色创建丢失物品信息、创建捡到物品信息
                    2. “唤醒”、“打开”失物招领应用
                    3. 教师角色、学生角色查询全校丢失物品列表、捡到物品列表
                                        
                    # 场景名称： 用车申请
                    可以完成以下任务
                    1. 查询和管理自己的用车申请记录
                    2. 教师创建用车申请
                    3. 教师和车辆管理员角色可以查询用全部车辆使用情况
                                        
                    # 场景名称： 英语张老师
                    帮助英语老师解答英语问题，包括语法、时态、句式等各种英语问题
                                        
                    # 场景名称： 关键词提取与验证
                    关键词提取与验证  将用户提供的关键词进行拓展和解释
                                        
                    # 场景名称： 调代课
                    可以完成以下任务
                    1. 教师申请调课
                    2. 查询教师调课、教师调课情况查询等，且不包含请假相关要求
                                        
                    # 场景名称： 项目式学习
                    智能体专为教师设计，帮助围绕给定主题生成一套跨学科、结构完整的项目式学习方案。用户只需输入一个教学主题，智能体即可结合学段与学科特点，输出包含“标题、驱动问题、项目目标、项目活动安排、成果展示与评价、成效反思”等关键环节的教学设计。方案中活动任务分阶段展开，安排合理，兼顾知识融合与学生发展需求，适合在2周内完成。
                                        
                    # 场景名称： 创作
                    可以完成以下任务
                    1. 若用户问题以“编写”、“撰写”、“设计”、“改写”、“扩写”、“丰富”等词语开头
                    2. 用户问题要求输出超过300字符，则进行创作
                    3. 用户想要进行文章撰写、生成文案、写作、写经验分享等文本创作类的意图
                    4. 用户问题是一篇中文汉字的文章、段落、感悟、笔记、散文，则可使用创作应用进一步处理
                                        
                    # 场景名称： 教育公文
                    教育公文助手是专门为教育领域定制的智能写作辅助软件，它极大地提升了教育工作者撰写各类文档的效率和质量。无论是需要制定教学公告、制定或修改管理规范、还是创作宣传文稿，这款工具都能够提供强大的支持。用户只需输入相关的关键词或主题，教育公文助手便会基于输入的信息，自动生成结构完整、内容丰富的文本草案。
                                        
                    # 场景名称： 英语单词教学
                    输入英文单词，会为您提供单词的教学内容
                                        
                    # 场景名称： 教学反思
                    请填写需要教学反思的课文，以及教学目标是否达成和遇到的问题，学生的课堂反应，小助手会为您初拟一份教学反思
                                        
                    # 场景名称： 继教培训反馈
                    可完成以下任务
                    1. 用于教师查询和管理自己的培训任务列表，并支持按培训名称进行筛选。
                    2. “唤醒”、“打开”继续教育培训应用
                    3. 培训管理员查询教师提交的某个任务情况
                                        
                    # 场景名称： 数学李老师
                    帮助数学老师解答数学问题
                                        
                    # 场景名称： 教学图片生成
                    可完成以下工作
                    1. 根据用户文字描述要求来生成一张图片
                    2. 根据用户上传的图片文件以及修改诉求来生成一张修改后的图片
                                        
                    # 场景名称： 设计任务规划
                    设计任务规划  帮助用户设计广告设计的任务规划
                                        
                    # 场景名称： 前期设计调研
                    前期设计调研  帮助用户进行广告设计调研
                    常用问法：帮我搜索一些扫地机器人相关的广告设计
                                        
                    # 场景名称： AI搜
                    教师信息的查询、统计和分析，用户指令的操作对象需要是教师，指令中可能包含教师年龄、工龄、职称、编制、学历、教育和工作经历、教师荣誉称号、教学比赛获奖、课题研究成果、论文学术成果、公开课、专题研讨、集体备课、培训学时等关键字。
                    通常用户会说帮我查询符合xx条件的教师，或帮我分析学校教师的xx分布，让大模型根据查询条件自动给出统计分析结果。
                                        
                    # 场景名称： 教师请假
                    可以完成以下任务
                    1.教师查自己的请假数据、统计教师请假数据；查询销假、未审批、已审批、未完成、已完成等各种状态的请假记录
                    2.教师可记录、撤销教师请假；规则：班主任或者教师仅可以为自己请假或销假，不可以操作学生、教师等任何他人的或的请假数据
                    3.“唤醒”、“打开”教师请假应用
                                        
                    # 场景名称： 学生请假
                    可以完成以下任务
                    1. 班主任角色查询学生请假数据或记录、撤销学生请假、学生销假、学生情况、返校情况等，调用“班主任：查询学生请假信息”
                    2. 班主任角色“唤醒”、“打开”学生请假应用，则调用“班主任打开学生请假”
                    3. 家长角色查询学生请假数据或记录、撤销学生请假、学生销假、学生情况、返校情况等，调用“家长：查询学生请假信息”
                    4. 家长角色“唤醒”、“打开”学生请假应用，则调用“家长：打开学生请假”
                                        
                    # 场景名称： 德育评比
                    德育班级评分总分报表
                                        
                    # 场景名称： 校长信箱
                    可以完成以下任务
                    1、支持师生、家长匿名发布信件；
                    2、支持校长信箱管理员指定回复人进行回复，对回复人回复的内容进行审核，审核通过后将回复内容发送给发件人；
                    3、支持多次回复信件、支持校长信箱管理员删除不合理的回复；
                    4、提供信件记录的导出功能。
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
            """;
}
