package org.example.llm.test.domain;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/7/19 15:11
 */
@Data
@Accessors(chain = true)
public class ServerInfoScript {
    private String serverName;
    private String serverDesc;
    private String source;
    private String type;
    private String callMethod;
    private String role;
    private String availableTools;
    private String planToolRule;
}
