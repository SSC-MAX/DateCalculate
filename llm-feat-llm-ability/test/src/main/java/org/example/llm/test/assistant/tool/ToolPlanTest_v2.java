package org.example.llm.test.assistant.tool;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.llm.ability.model.protocol.SourceModelRequest;
import org.example.llm.ability.model.protocol.SourceModelResponse;
import org.example.llm.ability.service.StreamModelCaller;
import org.example.llm.ability.util.SseMessageProcessor;
import org.example.llm.common.util.Checks;
import org.example.llm.common.util.ExcelUtil;
import org.example.llm.test.domain.ToolPlanResultV2;
import org.example.llm.test.domain.ToolPlanScriptV2;
import org.example.llm.test.utils.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author : zybi
 * @date : 2025/6/18 16:23
 */
public class ToolPlanTest_v2 {

    @Data
    @Accessors(chain = true)
    static class ScriptInfo {
        private String filePath;
        private String fileName;
    }

    @Data
    @Accessors(chain = true)
    static class ToolNameWithDesc {
        private String name;
        private String desc;
    }

    static List<ScriptInfo> scriptInfoList = List.of(
            /*new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\2. 学生请假\\")
                    .setFileName("学生请假_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\3. 校长信箱\\")
                    .setFileName("校长信箱_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\4. 用车申请\\")
                    .setFileName("用车申请_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\5. 继教培反馈\\")
                    .setFileName("继教培训反馈_v2"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\6. 教师请假\\")
                    .setFileName("教师请假_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\7. 规章制度\\")
                    .setFileName("规章制度"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\8. 失物招领\\")
                    .setFileName("失物招领_v1"),*/
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\2. 工具规划\\9. 调代课\\")
                    .setFileName("调代课_v1")
    );


    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        try {
            for (ScriptInfo scriptInfo : scriptInfoList) {
                run(scriptInfo.filePath, scriptInfo.fileName);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run(String filePath, String fileName) throws ReflectiveOperationException, IOException {
        File file = new File(filePath + fileName + ".xlsx");
        List<ToolPlanScriptV2> toolPlanScripts = ExcelUtil.readExcel(file, ToolPlanScriptV2.class);

        List<ToolNameWithDesc> toolNameWithDescList = toolPlanScripts.stream()
                .map(t -> new ToolNameWithDesc()
                        .setName(t.getToolName())
                        .setDesc(t.getToolDesc())
                )
                .toList();
        String toolNameWithDescListJson = JSONObject.toJSONString(toolNameWithDescList, JSONWriter.Feature.PrettyFormat);

        List<Supplier<ToolPlanResultV2>> tasks = new ArrayList<>();
        for (ToolPlanScriptV2 script : toolPlanScripts) {
            if (Checks.isNull( script.getSceneName() )) {
                continue;
            }
            String questions = script.getUserInput();
            String[] questionArr = questions.split("\n");
            for (String question : questionArr) {
                tasks.add(() -> {
                    String planResult = callAbility("qwen3-235b", toolNameWithDescList,
                            """
                                    # 工具选取规则
                                    1. 调课应当总是先使用“请假时间内调课推荐工具”或“调课推荐工具”工具
                                    2. 用户问题不包含课节次时（如第二节课）, 调用 “请假时间内调课推荐工具” 工具；
                                    3. 用户问题包含课节次时（如第二节课）, 调用 “请假时间内调课推荐工具” 工具
                                    4. 调课的请求应当总是需要规划到“提交调课工具”请求，哪怕当前参数还不足够（部分参数需要前置工具执行结果）
                                    5. 查询历史调课数据时应当使用 “查询调课结果工具”
                                    """
                            , script.getRole(), question);
                    JSONArray planResultArray = JSONArray.parseArray(planResult);
                    String predicatePlanResult = script.getPredicatePlanResult();
                    String judgeType = script.getJudgeType();
                    String correct;
                    if ("等于".equals(judgeType)) {
                        correct = (planResultArray.size() == 1 && predicatePlanResult.equals(planResultArray.get(0).toString()))? "对" : "错";
                    } else {
                        correct = "对";
                    }
                    return new ToolPlanResultV2()
                            .setSceneName(script.getSceneName())
                            .setRole(script.getRole())
                            .setToolList(toolNameWithDescListJson)
                            .setUserInput(question)
                            .setPredicatePlanResult(script.getPredicatePlanResult())
                            .setJudgeType(script.getJudgeType())
                            .setPlanResult(JSONObject.toJSONString(planResultArray, JSONWriter.Feature.PrettyFormat))
                            .setCorrect(correct);
                });
            }
        }
        List<ToolPlanResultV2> toolPlanResults = ThreadUtil.submitBatchTask(5, tasks);
        List<String> titles = List.of("场景名称", "角色", "工具列表", "问题", "预期结果", "判断逻辑", "规划结果", "对错");

        List<List<String>> lists = toolPlanResults.stream().map(pr -> List.of(
                pr.getSceneName(),
                pr.getRole(),
                pr.getToolList(),
                pr.getUserInput(),
                pr.getPredicatePlanResult(),
                pr.getJudgeType(),
                pr.getPlanResult(),
                pr.getCorrect()
        )).toList();

        ExcelUtil.gen(filePath + fileName+"-测试结果-"+System.currentTimeMillis()+".xlsx", fileName, titles, lists);
    }

    static String callAbility(String model, List<ToolNameWithDesc> toolNameWithDescLis, String toolPlanRole, String role, String userInput) {
        StringBuilder result = new StringBuilder();
        StreamModelCaller.create(model)
                .setEnableThinking(false)
                .setStream(false)
                .setResponseFormat(new SourceModelRequest.ResponseFormat().setType("json_object"))
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("user").setContent(getPrompt(toolNameWithDescLis, toolPlanRole, role, userInput))
                        )
                )
                .setOnMessage((chatRequestContext, s) -> {
                    if (s == null) {
                        return null;
                    }
                    SourceModelResponse sourceModelResponse = JSONObject.parseObject(SseMessageProcessor.removeSsePrefixes(s), SourceModelResponse.class);
                    result.append(sourceModelResponse.getChoices().get(0).getMessage().getContent());
                    return sourceModelResponse;
                })
                .setOnError((chatRequestContext, throwable) -> {
                    System.err.println("Error occurred: " + throwable.getMessage());
                })
                .call();

        String s = result.toString();
        int start = s.indexOf("[");
        int last = s.lastIndexOf("]");
        System.out.println(s);
        return s.substring(start, last + 1);
    }


    private static String getPrompt(List<ToolNameWithDesc> toolNameWithDescLis, String toolPlanRule, String role, String userInput) {
        String replace = TOOL_PLAN_PROMPT
                .replace("{{toolList}}", getToolList(toolNameWithDescLis))
                .replace("{{toolPlanRule}}", toolPlanRule == null ? "" : toolPlanRule)
                .replace("{{userInfo}}", getUserInfo(role))
                .replace("{{userInput}}", userInput);
        System.out.println(replace);
        return replace;
    }

    private static String getToolList(List<ToolNameWithDesc> toolNameWithDescList) {
        StringBuilder toolListBuilder = new StringBuilder();
        for (ToolNameWithDesc toolNameWithDesc : toolNameWithDescList) {
            if (Checks.isNull( toolNameWithDesc.getName() )) {
                continue;
            }
            toolListBuilder.append(
                    """
                    工具名称：{{name}}
                    工具描述：{{desc}}
                    """
                    .replace("{{name}}", toolNameWithDesc.name)
                    .replace("{{desc}}", toolNameWithDesc.desc)
            );
        }
        return toolListBuilder.toString();
    }

    private static String getUserInfo(String role) {
        return """
                用户名称：张三
                用户角色：{{role}}
                """
                .replace("{{role}}", role)
                ;
    }

    static final String TOOL_PLAN_PROMPT =
        """
        # 角色
        你是一个工具规划助手
                
        # 任务
        从工具列表中选取一个或多个工具，处理用户的问题；如果有工具选取规则的话，则必须按照规则选取。
                
        #工具列表
        =========工具列表开始===============
        {{toolList}}
        =========工具列表结束===============
                
        {{toolPlanRule}}
        
        # 用户信息
        {{userInfo}}
                
        # 用户问题
        {{userInput}}
                
        ===示例json数组输出格式==
        ["工具1", "工具2"]
        =====
                
        # 回答要求
        1. 根据用户问题，给出规划，要求串行调用工具
        2. 输出内容为必须是一个数组，数值中每个字符串值必须是工具的名称
        3. 仅像示例输出的格式那样回答，禁止回复任何其他无关内容或推理信息
        """;

}
