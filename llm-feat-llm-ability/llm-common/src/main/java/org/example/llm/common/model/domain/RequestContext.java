package org.example.llm.common.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.llm.common.enums.WebsocketSendTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zybi
 * @date : 2024/11/13 21:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    private String traceId;             // 请求链路id
    private String requestUrl;          // 请求地址
    private WebsocketSendTypeEnum sendTypeEnum; // 请求参数类型
    private Object param;               // 请求参数

    private Long requestStartTime;      // 请求开始时间
    private Long connectedTime;         // 建连成功事件
    private Long requestEndTime;        // 请求结束时间
    private Long firstResponseTime;     // 第一帧响应时间
    
    private Boolean stopped;        // 是否被中断过

    private Map<String, Object> attributes; // 自定义属性

    public void setAttribute(String key, Object val) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, val);
    }

    public Object getAttribute(String key) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        return this.attributes.get(key);
    }
    
    public void stopChat() {
        this.stopped = true;
    }

}
