package org.example.llm.ability.util;

import org.example.llm.ability.model.domain.Tool;
import org.example.llm.ability.model.protocol.SourceModelRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : zybi
 * @date : 2025/5/28 9:59
 */
public class ToolConvertUtil {

    public static List<SourceModelRequest.Tool> convert(List<Tool> tools) {
        if (tools == null || tools.isEmpty()) {
            return Collections.emptyList();
        }

        return tools.stream().map(t -> {
            Map<String, Object> params = new HashMap<>();
            t.getProperties().forEach(p ->
                    params.put(p.getName(), Map.ofEntries(
                            Map.entry("type", p.getType()),
                            Map.entry("description", p.getDesc())
            )));
            return new SourceModelRequest.Tool()
                    .setType("function")
                    .setFunction(
                            new SourceModelRequest.Function()
                                    .setName(t.getName())
                                    .setDescription(t.getDesc())
                                    .setParameters( params )
                    );
        }).toList();
    }
}
