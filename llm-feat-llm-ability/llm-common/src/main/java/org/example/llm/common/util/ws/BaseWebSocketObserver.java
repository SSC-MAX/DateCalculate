package org.example.llm.common.util.ws;

import lombok.Data;
import okio.ByteString;
import org.example.llm.common.model.domain.RequestContext;

/**
 * @author : zybi
 * @date : 2024/10/30 22:25
 */
@Data
public abstract class BaseWebSocketObserver<T> {
    private RequestContext context;

    public BaseWebSocketObserver(RequestContext context) {
        this.context = context;
    }


    abstract public void onOpen();

    abstract public T onMessage(String message);

    abstract public T onMessage(ByteString bytes);

    abstract public void onError(Throwable throwable);

    abstract public void onCompleted(int code, String reason);
}
