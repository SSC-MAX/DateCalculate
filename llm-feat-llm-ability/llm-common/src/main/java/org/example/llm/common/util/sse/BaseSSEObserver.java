package org.example.llm.common.util.sse;

import org.example.llm.common.model.domain.ChatRequestContext;

/**
 * @author : zybi
 * @date : 2024/11/12 14:22
 */
public abstract class BaseSSEObserver<T> {

    abstract public void onOpen(ChatRequestContext context);

    abstract public T onMessage(ChatRequestContext context, String message);

    abstract public void onError(ChatRequestContext context, Throwable error);

    abstract public void onCompleted(ChatRequestContext context);
}
