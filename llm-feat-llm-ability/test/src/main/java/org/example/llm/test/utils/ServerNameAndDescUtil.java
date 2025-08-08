package org.example.llm.test.utils;

import org.example.llm.common.util.ExcelUtil;
import org.example.llm.test.domain.ServerInfoScript;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zybi
 * @date : 2025/7/19 15:05
 */
public class ServerNameAndDescUtil {

    public static Map<String, String> getNameWithDescMap(String filePath) throws ReflectiveOperationException, IOException {
        File file = new File(filePath);
        List<ServerInfoScript> scenePlanScripts = ExcelUtil.readExcel(file, ServerInfoScript.class);

        Map<String, String> result = new HashMap<>();
        for (ServerInfoScript scenePlanScript : scenePlanScripts) {
            result.put( scenePlanScript.getServerName(), scenePlanScript.getServerDesc() );
        }
        return result;
    }

    public static void main(String[] args) throws ReflectiveOperationException, IOException {
        getNameWithDescMap("C:\\Users\\zybi\\Desktop\\zybi-files\\记录\\大模型产品\\智能助理\\效果测试自测\\服务+工具描述 -- 0179.xlsx")
                .forEach((k, v) -> {
                    if (k == null || v == null) {
                        return;
                    }
                    System.out.println("# 场景名称： " + k.trim());
                    System.out.println( v.trim() );
                    System.out.println();
                });
    }

}
