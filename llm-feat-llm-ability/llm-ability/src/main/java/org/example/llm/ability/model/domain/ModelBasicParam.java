package org.example.llm.ability.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/5/19 16:33
 */
@Data
@Accessors(chain = true)
public class ModelBasicParam {

    private String url;
    private String appKey;
    private String model;
    private Integer maxTokens;
    private Double temperature;

}
