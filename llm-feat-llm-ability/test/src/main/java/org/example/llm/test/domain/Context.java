package org.example.llm.test.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.example.llm.common.enums.WSContentTypeEnum;

/**
 * @author : zybi
 * @date : 2025/6/1 22:53
 */
@Data
@Accessors(chain = true)
public class Context {
    private WSContentTypeEnum lastTypeEnum;
}
