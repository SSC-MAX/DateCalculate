package org.example.llm.ability.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author : zybi
 * @date : 2025/5/28 9:58
 */
@Data
@Accessors(chain = true)
public class Tool {

    private String name;
    private String desc;
    private List<Property> properties;

    @Data
    @Accessors(chain = true)
    public static class Property {
        private String type;
        private String name;
        private String desc;
    }
}
