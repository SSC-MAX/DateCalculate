package org.example.llm.common.util.ws;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.example.llm.common.enums.WebsocketSendTypeEnum;
import org.example.llm.common.model.domain.RequestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author : zybi
 * @date : 2024/10/29 23:49
 */
@Slf4j
public class BaseWebSocketListener<T> extends WebSocketListener {

    private final BaseWebSocketBlockSwitcher switcher;
    private final BaseWebSocketObserver<T> observer;
    private final Function<T, Boolean> responseEndFlagJudge;

    public BaseWebSocketListener(BaseWebSocketBlockSwitcher switcher, BaseWebSocketObserver<T> observer, Function<T, Boolean> responseEndFlagJudge) {
        this.switcher = switcher;
        this.observer = observer;
        this.responseEndFlagJudge = responseEndFlagJudge;
    }

    private String getTraceId() {
        return observer.getContext().getTraceId();
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        try {
            log.info("工具层 => websocket连接建立成功，url: {}", observer.getContext().getRequestUrl());

            // 记录
            observer.getContext().setConnectedTime(System.currentTimeMillis());

            // 观察者建连事件
            observer.onOpen();

            super.onOpen(webSocket, response);

            // 发送消息
            WebsocketSendTypeEnum sendTypeEnum = observer.getContext().getSendTypeEnum();
            String paramJson = JSONObject.toJSONString(observer.getContext().getParam());
            if (WebsocketSendTypeEnum.Text.equals(sendTypeEnum)) {
                webSocket.send(paramJson);
            } else {
                webSocket.send(ByteString.of((byte[]) observer.getContext().getParam()));
            }
            log.info("消息发送成功");
        }
        catch (Exception e) {
            this.onFailure(webSocket, e, response);
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        try {
            RequestContext context = observer.getContext();
            if (context.getFirstResponseTime() == null) {
                context.setFirstResponseTime(System.currentTimeMillis());

                // 通知者通知调用者已接收到首帧数据
                switcher.turnOffFirstFrameDataWait();
            }

            // 观察者处理消息
            T t = observer.onMessage(text);

            // 结束帧
            if (responseEndFlagJudge.apply(t)) {
                this.onClosed(webSocket, 1000, "success");
            }
        }
        catch (Exception e) {
            log.error("", e);
            this.onFailure(webSocket, e, null);
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        try {
            RequestContext context = observer.getContext();
            if (context.getFirstResponseTime() == null) {
                context.setFirstResponseTime(System.currentTimeMillis());

                // 通知者通知调用者已接收到首帧数据
                switcher.turnOffFirstFrameDataWait();
            }

            // 观察者处理消息
            T t = observer.onMessage(bytes);

            // 结束帧
            if (responseEndFlagJudge.apply(t)) {
                this.onClosed(webSocket, 1000, "success");
            }
        }
        catch (Exception e) {
            this.onFailure(webSocket, e, null);
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.info("工具层 => 监听到websocket异常", t);
        try {
            // 观察者的异常事件
            observer.onError(t);
        }
        catch (Exception e) {
            log.error("处理websocket异常过程再次发生异常", e);
        }
        finally {
            this.onClosed(webSocket, 1000, t.getMessage());
        }
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
        //log.info("工具层 => 链接已关闭，url: {}, code: {}, reason: {}", observer.getContext().getRequestUrl(), code, reason);

        // 记录
        observer.getContext().setRequestEndTime(System.currentTimeMillis());

        // 观察者的完成事件
        observer.onCompleted(code, reason);

        // 通知者 通知父线程任务已完成
        switcher.turnOffAll();
    }

}
