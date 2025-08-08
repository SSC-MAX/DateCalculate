package org.example.llm.test.assistant.tool;


import lombok.extern.slf4j.Slf4j;
import org.example.llm.common.util.ExcelUtil;
import org.example.llm.test.domain.ToolExtractParam;
import org.example.llm.test.domain.ToolGetTimeResult;
import org.example.llm.test.domain.ToolGetParamScript;
import org.example.llm.test.domain.ToolGetTimeScript_v1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.example.llm.test.assistant.tool.ToolTest_v1.callAbility;

@Slf4j
public class Test {

    // ToolTest_v1 toolTest_v1;

    public List<ToolExtractParam> readXlsx(String filePath, String fileName) throws ReflectiveOperationException, IOException {
        File file = new File(filePath + fileName + ".xlsx");
        List<ToolExtractParam> toolGetParamScripts = ExcelUtil.readExcel(file, ToolExtractParam.class);      // 读取文件
        return toolGetParamScripts;
    }

    static void runSingle(String filePath, String fileName, String modelName) throws ReflectiveOperationException, IOException {
        File file = new File(filePath + fileName + ".xlsx");
        List<ToolGetParamScript> toolGetParamScripts = ExcelUtil.readExcel(file, ToolGetParamScript.class);      // 读取文件
        ToolExtractParam toolExtractParam = ExcelUtil.readExcel(file, ToolExtractParam.class).get(0);
        List<Supplier<ToolGetTimeResult>> tasks1 = new ArrayList<>();
        String toolNameExtract = toolExtractParam.getToolName();
        String toolParamExtract = toolExtractParam.getToolParam();
        String resultExtract = callAbility(toolNameExtract, toolParamExtract, "", modelName);
        log.error("===\n"+resultExtract+"\n===");
    }

    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        File file = new File("D:\\data\\TestCase\\时间提取_v1\\时间提取工具v1.xlsx");
        List<ToolGetTimeScript_v1> toolGetTimeScriptV1s = ExcelUtil.readExcelCrossRow(file, ToolGetTimeScript_v1.class);
        System.out.println(toolGetTimeScriptV1s.size()+"\n==========\n"+toolGetTimeScriptV1s.get(1).toString()+"\n==========\n");


    }
}
