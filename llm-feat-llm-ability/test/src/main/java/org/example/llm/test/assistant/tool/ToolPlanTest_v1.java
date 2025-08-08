package org.example.llm.test.assistant.tool;

import lombok.Data;
import lombok.experimental.Accessors;
import org.example.llm.common.util.ExcelUtil;
import org.example.llm.common.util.http.OkHttpClientUtil;
import org.example.llm.test.domain.ToolPlanResult;
import org.example.llm.test.domain.ToolPlanScript;
import org.example.llm.test.utils.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author : zybi
 * @date : 2025/6/18 16:23
 */
public class ToolPlanTest_v1 {

    @Data
    @Accessors(chain = true)
    static class ScriptInfo {
        private String filePath;
        private String fileName;
    }

    static List<ScriptInfo> scriptInfoList = List.of(
            new ScriptInfo()
                    .setFilePath("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\工具规划\\1. 教学图片生成\\")
                    .setFileName("测试用例v1")
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
        List<ToolPlanScript> toolPlanScripts = ExcelUtil.readExcel(file, ToolPlanScript.class);
        ToolPlanScript toolPlanScript = toolPlanScripts.get(0);
        String address = toolPlanScript.getAddress();
        String sseEndpoint = toolPlanScript.getSseEndpoint();
        String toolPlanRule = toolPlanScript.getToolPlanRule();

        List<Supplier<ToolPlanResult>> tasks = new ArrayList<>();
        for (ToolPlanScript script : toolPlanScripts) {
            tasks.add(() -> {
                OkHttpClientUtil.createPost("http://agent-test.ceshiservice.cn/mcp/test/planTool")
                        .setRequestBody(Map.of(
                                "address", address,
                                "sseEndpoint", sseEndpoint,
                                "userInput", script.getUserInput(),
                                "toolPlanRule", toolPlanRule
                        ))
                        .exec();
                return new ToolPlanResult()
                        .setUserInput(script.getUserInput())
                        .setPredicatePlanResult(script.getPredicatePlanResult())
                        .setJudgeType(script.getJudgeType())
                        .setToolList(null)
                        .setPlanResult(null)
                        .setCorrect("错");
            });
        }
        List<ToolPlanResult> toolPlanResults = ThreadUtil.submitBatchTask(5, tasks);
        List<String> titles = List.of("问题", "预期结果", "判断逻辑", "规划结果", "对错");

        List<List<String>> lists = toolPlanResults.stream().map(pr -> {
            return List.of(
                    pr.getUserInput(),
                    pr.getPredicatePlanResult(),
                    pr.getJudgeType(),
                    pr.getPlanResult(),
                    "错"
            );
        }).toList();

        ExcelUtil.gen(filePath + fileName+"-测试结果-"+System.currentTimeMillis()+".xlsx", fileName, titles, lists);
    }
}
