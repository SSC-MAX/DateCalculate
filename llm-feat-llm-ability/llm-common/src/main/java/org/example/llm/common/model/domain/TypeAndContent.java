package org.example.llm.common.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : zybi
 * @date : 2025/5/13 20:20
 */
@Data
@Accessors(chain = true)
public class TypeAndContent {
    private String contentType;     // 数据类型
    private String content;         // 数据内容
}
