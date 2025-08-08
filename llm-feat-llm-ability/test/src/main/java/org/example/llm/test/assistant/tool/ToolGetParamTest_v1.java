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
import org.example.llm.test.domain.ToolGetParamResult;
import org.example.llm.test.domain.ToolGetParamScript;
import org.example.llm.test.utils.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author : zybi
 * @date : 2025/7/3 11:12
 */
public class ToolGetParamTest_v1 {

    @Data
    @Accessors(chain = true)
    static class ScriptInfo {
        private String filePath;
        private String fileName;
    }

    static final String[] MODELS = {/*"deepseekV3", */"doubao1.5"/*, "qwen3-235b"*/};

    static List<ScriptInfo> scriptInfoList = List.of(
            /*new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\继教培训反馈\\")
                    .setFileName("测试用例_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\用车申请\\")
                    .setFileName("教师创建用车申请_v1"),*//*
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\校长信箱\\")
                    .setFileName("教师、学生、家长创建信件_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\失物招领\\")
                    .setFileName("增丢失物品_v1"),
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\调代课\\")
                    .setFileName("提交调课工具_v1"),*/
//            new ScriptInfo()
//                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\3. 抽槽\\巡课\\")
//                    .setFileName("单班巡课_v1")
            new ScriptInfo()
                    .setFilePath("D:\\data\\TestCase\\")
                    .setFileName("提交调课工具_v1")

    );



    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        for (ScriptInfo scriptInfo : scriptInfoList) {
            for (String model : MODELS) {
                run(scriptInfo.filePath, scriptInfo.fileName, model);
            }
        }
    }

    static void run(String filePath, String fileName, String modelName) throws ReflectiveOperationException, IOException {
        File file = new File(filePath + fileName + ".xlsx");
        List<ToolGetParamScript> toolGetParamScripts = ExcelUtil.readExcel(file, ToolGetParamScript.class);

        List<Supplier<ToolGetParamResult>> tasks = new ArrayList<>();
        for (ToolGetParamScript script : toolGetParamScripts) {
            String toolName = script.getToolName();
            String toolParam = script.getToolParam();
            String questions = script.getQuestions();
            String[] questionArr = questions.split("\n");
            for (String question : questionArr) {
                tasks.add(() -> {
                    String result = callAbility(toolName, toolParam, question, modelName);   // 模型调用
                    return new ToolGetParamResult()
                            .setSceneName(script.getSceneName())
                            .setSceneId(script.getSceneId())
                            .setRole(script.getRole())
                            .setToolName(script.getToolName())
                            .setToolParam(script.getToolParam())
                            .setToolRequired(script.getToolRequired())
                            .setQuestion(question)
                            .setResult(result);
                });
            }
        }
        List<ToolGetParamResult> toolGetParamResults = ThreadUtil.submitBatchTask(5, tasks);
        List<List<String>> data = toolGetParamResults.stream()
                .map(item -> {
                    String paramDef = filterToolParamDef(item.getToolParam());
                    String toolRequired = item.getToolRequired();
                    List<String> lackParams = getLackParams(toolRequired, item.getResult());
                    boolean completed = Checks.isNull(lackParams);
                    String paramResult;
                    try {
                        paramResult = JSONObject.toJSONString(JSONObject.parseObject(removeFormat(item.getResult())), JSONWriter.Feature.PrettyFormat);
                    }
                    catch (Exception e) {
                        paramResult = "";
                    }

                    return List.of(
                            item.getSceneName(),
                            item.getSceneId(),
                            item.getRole(),
                            item.getToolName(),
                            paramDef,
                            toolRequired,
                            item.getQuestion(),
                            item.getResult() == null? "" : paramResult,
                            completed? "完整" : "缺少必填",
                            completed? "" : JSONArray.toJSONString(lackParams, JSONWriter.Feature.PrettyFormat)
                    );
                }).toList();

        List<String> titles = List.of("场景名称", "场景id", "场景角色","工具名称", "工具参数", "工具必填参数", "问题", "抽槽结果", "是否抽槽完整", "缺少的参数");
        ExcelUtil.gen(filePath + fileName+"-"+modelName+"-测试结果-"+System.currentTimeMillis()+".xlsx", fileName, titles, data);
    }

    private static String filterToolParamDef(String toolParamDef) {
        JSONArray paramDefArray = JSONArray.parseArray(toolParamDef);
        List<Map<String, Object>> filteredParam = new ArrayList<>();
        for (int i = 0; i < paramDefArray.size(); i++) {
            JSONObject jsonObject = paramDefArray.getJSONObject(i);
            JSONObject def = jsonObject.getJSONObject("value");
            String name = jsonObject.getString("name");
            if (ToolParamConstants.ToolCommonParam.ALL.contains(name)) {
                continue;
            }
            filteredParam.add(jsonObject);
        }
        return JSONObject.toJSONString(filteredParam, JSONWriter.Feature.PrettyFormat);
    }

    static List<String> getLackParams(String toolRequired, String param) {
        if (Checks.isNull( toolRequired )) {
            return null;
        }

        JSONArray requestParams = JSONArray.parseArray(toolRequired);
        if (Checks.isNull( param )) {
            return requestParams.toList(String.class);
        }
        String result = removeFormat(param);
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(result);
        }
        catch (Exception e) {
            System.err.println("==>" + param);
            jsonObject = new JSONObject();
        }

        List<String> lackParams = new ArrayList<>();
        for (Object requestParam : requestParams) {
            if (!jsonObject.containsKey(requestParam.toString())) {
                lackParams.add(requestParam.toString());
            }
        }
        return lackParams;
    }

    static final String removeFormat(String rawContent) {
        return rawContent.replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "")
                .replace("# 规划的结果", "")
                .trim();
    }


    // 调用模型
    static String callAbility(String toolNameAndDesc, String toolParam, String question, String model) {
        StringBuilder result = new StringBuilder();
        StreamModelCaller.create(model)
                .setEnableThinking(false)
                .setStream(false)
                .setResponseFormat(new SourceModelRequest.ResponseFormat().setType("json_object"))
                .setMessages(
                        List.of(
                                new SourceModelRequest.Message().setRole("system").setContent(getSystemPrompt(toolNameAndDesc, toolParam)),
                                new SourceModelRequest.Message().setRole("user").setContent(question)
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
                .call();


        String s = result.toString();
        int start = s.indexOf("{");
        int last = s.lastIndexOf("}");

        return s.substring(start, last + 1);
    }


    static String getSystemPrompt(String toolNameAndDesc, String paramDef) {
        String rawSystemPrompt = SYSTEM_PROMPT
                .replace("{{userInfo}}", getTeacherUserInfo())
                .replace("{{toolParamsDefinition}}", getToolParamsDefinition(paramDef))
                .replace("{{toolNameAndDesc}}", toolNameAndDesc)
                ;

        //System.out.println(proceedPrompt);
        return rawSystemPrompt + "\n" + getTimeRule();
    }

    static String getToolParamsDefinition(String paramDef) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONArray paramDefArray = JSONArray.parseArray(paramDef);
        for (int i = 0; i < paramDefArray.size(); i++) {
            JSONObject jsonObject = paramDefArray.getJSONObject(i);
            JSONObject def = jsonObject.getJSONObject("value");
            String name = jsonObject.getString("name");
            if (ToolParamConstants.ToolCommonParam.ALL.contains(name)) {
                continue;
            }
            String cName = def.getString("cName");
            //String type = def.getString("type");
            String description = def.getString("description");
            stringBuilder.append(i).append(". ").append("参数名称：").append(name).append("; ");
            stringBuilder.append("参数描述：").append(cName == null ? "" : cName + ",").append(description).append("\n");
        }

        return stringBuilder.toString();
    }

    static final String SYSTEM_PROMPT =
            """
              # 角色
              你是一个工具参数提取助手
                    
              # 任务
              从用户个人信息，当前时间信息，历史对话，以及当前用户问题中提取工具的参数
                    
              # 参数提取要求
              1. 严禁使用不存在的信息构造工具参数
              2. 参数要求具体的时间值，但是输入未提供足够的信息是，用一下关联映射，自动补全
                  a. 凌晨：早上3点
                  b. 早上：早上8点
                  c. 上午：早上10点
                  d. 中午、午后：中午12点
                  e. 下午：下午3点
                  f. 晚上、傍晚：晚上6点
                  g. 晚间：晚上8点
                  h. 深夜：晚上23点
              3. 如果参数是一个时间段的起止时间，但是输入仅包含某天，如昨天，上周x；这种情况根据当前时间计算目标日期，开始时间取目标日期的0点0分，结束时间取目标日期的23点59分
              4. 如果历史对话和当前输入都未提及人物信息，但是参数需要人物信息（*userName、*personName），则从当前用户信息获取姓名和角色信息
              5. 如果历史对话中已经提取到了参数值，则直接使用，如果当前用户输入中有相同的参数，则使用当前用户输入的参数值
              6. 如果是丢失了某个物品那物品状态就是丢失，物品描述是丢失物品的描述信息
              7. 当用户问题出现本周、上周、下周等一周的维度信息时，应当总是从那一周的周一作为开始时间，周日作为结束时间
              8. 如果参数不明确或未提及，则输出结果json中不包含该参数的key
              9. 禁止使用参数描述及格式要求中的示例作为参数值
             
              # 输出格式要求
              1. 输出json格式；格式为{"参数名": "提取到的参数值"},禁止输出json之外的任何其他内容，禁止输出注意事项
              2. 不要包含```json ```, 直接输出json字符串即可,禁止输出“注：”等内容
                    
              # 当前用户信息
              {{userInfo}}
              
              # 工具名称和描述
              {{toolNameAndDesc}}
                         
              # 工具参数定义
              {{toolParamsDefinition}}
            """;



    private static String getTeacherUserInfo() {
        return """
                用户名称：张三
                用户角色：teacher
                """;
    }

    private static String getTimeRule() {
        return """
                # 当前与近期时间枚举
                {{timeList}}
                """
                .replace("{{timeList}}", String.join("\n", getRecentlyTime()));
    }


    private static List<String> getRecentlyTime() {
        // 设置日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 1. 今天
        LocalDate today = LocalDate.now();

        // 2. 昨天
        LocalDate yesterday = today.minusDays(1);

        // 3. 明天
        LocalDate tomorrow = today.plusDays(1);

        // 4. 后天
        LocalDate dayAfterTomorrow = today.plusDays(2);

        // 5. 大后天
        LocalDate dayAfterDayAfterTomorrow = today.plusDays(3);

        // 6. 前天
        LocalDate dayBeforeYesterday = today.minusDays(2);

        // 7. 本周一到本周日
        LocalDate mondayThisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate sundayThisWeek = today.with(DayOfWeek.SUNDAY);

        // 8. 上周一到上周日
        LocalDate mondayLastWeek = mondayThisWeek.minusWeeks(1);
        LocalDate sundayLastWeek = sundayThisWeek.minusWeeks(1);

        // 9. 下周一到下周日
        LocalDate mondayNextWeek = mondayThisWeek.plusWeeks(1);
        LocalDate sundayNextWeek = sundayThisWeek.plusWeeks(1);

        // 10. 上月
        YearMonth lastMonth = YearMonth.from(today).minusMonths(1);
        LocalDate firstDayOfLastMonth = lastMonth.atDay(1);
        LocalDate lastDayOfLastMonth = lastMonth.atEndOfMonth();

        // 11. 本月
        YearMonth thisMonth = YearMonth.from(today);
        LocalDate firstDayOfThisMonth = thisMonth.atDay(1);
        LocalDate lastDayOfThisMonth = thisMonth.atEndOfMonth();

        // 12. 下月
        YearMonth nextMonth = YearMonth.from(today).plusMonths(1);
        LocalDate firstDayOfNextMonth = nextMonth.atDay(1);
        LocalDate lastDayOfNextMonth = nextMonth.atEndOfMonth();

        // 额外示例：获取本周所有日期
        List<String> datesThisWeek = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String weekName;
            switch (i) {
                case 0 -> weekName = "一";
                case 1 -> weekName = "二";
                case 2 -> weekName = "三";
                case 3 -> weekName = "四";
                case 4 -> weekName = "五";
                case 5 -> weekName = "六";
                case 6 -> weekName = "日";
                default ->
                        weekName = ""+i;
            }
            datesThisWeek.add("本周"+weekName+": "+mondayThisWeek.plusDays(i));
        }
        List<String> strings = new ArrayList<>();
        strings.add("当前时间： " + curFormatter.format(new Date()));
        strings.add("前天: " + dayBeforeYesterday.format(formatter));
        strings.add("昨天: " + yesterday.format(formatter));
        strings.add("明天: " + tomorrow.format(formatter));
        strings.add("后天: " + dayAfterTomorrow.format(formatter));
        strings.add("大后天: " + dayAfterDayAfterTomorrow.format(formatter));
        strings.add("上周一: " + mondayLastWeek.format(formatter));
        strings.add("上周日: " + sundayLastWeek.format(formatter));
        strings.add("下周一: " + mondayNextWeek.format(formatter));
        strings.add("下周日: " + sundayNextWeek.format(formatter));
        strings.addAll(datesThisWeek);
        strings.add("上月第一天: " + firstDayOfLastMonth.format(formatter));
        strings.add("上月最后一天: " + lastDayOfLastMonth.format(formatter));
        strings.add("本月第一天: " + firstDayOfThisMonth.format(formatter));
        strings.add("本月最后一天: " + lastDayOfThisMonth.format(formatter));
        strings.add("下月第一天: " + firstDayOfNextMonth.format(formatter));
        strings.add("下月最后一天: " + lastDayOfNextMonth.format(formatter));
        return strings;
    }
}
