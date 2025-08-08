package org.example.llm.common.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author gbzhou
 * @desc 统一响应体
 * @date 2024/4/24
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // 操作是否成功
    private Integer code;

    // 返回的消息
    private String message;

    // 细节信息
    private String subMessage;

    // 要返回的数据
    private T data;

    // traceId
    private String traceId;

}

