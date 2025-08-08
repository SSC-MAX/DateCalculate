package org.example.llm.common.util.ws;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;
import org.apache.commons.lang3.function.TriConsumer;
import org.example.llm.common.enums.WebsocketSendTypeEnum;
import org.example.llm.common.model.domain.RequestContext;
import org.example.llm.common.util.http.OkHttpClientFactory;

import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author : zybi
 * @date : 2024/10/30 22:52
 */
@Slf4j
public class WebSocketClientUtil {

    /**
     * 最大等待首帧返回数据时间
     */
    private static final Long MAX_WAIT_TIME_OF_FIRST_FRAME_DATA = 60L;

    /**
     * 最大等待ws完成时间
     */
    private static final Long MAX_WAIT_TIME_OF_COMPLETED = 300L;

    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 5;
    private static final Integer DEFAULT_READ_TIMEOUT = 180;
    private static final Integer DEFAULT_MAX_IDLE_CONNECTIONS = 200;
    private static final Integer DEFAULT_KEEP_ALIVE_DURATION = 5;

    private static OkHttpClient okHttpClient;

    private static final Object LOCK = new Object();

    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient != null) {
            return okHttpClient;
        }

        synchronized ( LOCK ) {
            if ( okHttpClient == null ) {
                okHttpClient = OkHttpClientFactory.getInstance(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_MAX_IDLE_CONNECTIONS, DEFAULT_KEEP_ALIVE_DURATION);
            }
        }
        return okHttpClient;
    }

    public static <T> WebSocketBuilder<T> create() {
        return new WebSocketBuilder<>();
    }


    public static class WebSocketBuilder<T> {
        private BaseWebSocketBlockSwitcher switcher;
        private String traceId;
        private String url;
        private WebsocketSendTypeEnum sendTypeEnum;
        private Object param;
        private Function<T, Boolean> responseEndFlagJudge;
        private Consumer<RequestContext> onOpenConsumer;
        private BiFunction<RequestContext, String, T> onMessageConsumer;
        private BiFunction<RequestContext, ByteString, T> onMessageByteStringConsumer;
        private BiConsumer<RequestContext, Throwable> onErrorConsumer;
        private TriConsumer<RequestContext, Integer, String> onCloseConsumer;

        public WebSocketBuilder<T> switcher(BaseWebSocketBlockSwitcher switcher) {
            this.switcher = switcher;
            return this;
        }
        public WebSocketBuilder<T> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        public WebSocketBuilder<T> url(String url) {
            this.url = url;
            return this;
        }
        public WebSocketBuilder<T> sendType(WebsocketSendTypeEnum sendTypeEnum) {
            this.sendTypeEnum = sendTypeEnum;
            return this;
        }

        public WebSocketBuilder<T> param(Object param) {
            this.param = param;
            return this;
        }
        public WebSocketBuilder<T> responseEndFlag(Function<T, Boolean> responseEndFlagJudge) {
            this.responseEndFlagJudge = responseEndFlagJudge;
            return this;
        }
        public WebSocketBuilder<T> onOpen(Consumer<RequestContext> onOpenConsumer) {
            this.onOpenConsumer = onOpenConsumer;
            return this;
        }
        public WebSocketBuilder<T> onTextMessage(BiFunction<RequestContext, String, T> onMessageConsumer) {
            this.onMessageConsumer = onMessageConsumer;
            return this;
        }
        public WebSocketBuilder<T> onBytesMessage(BiFunction<RequestContext, ByteString, T> onMessageConsumer) {
            this.onMessageByteStringConsumer = onMessageConsumer;
            return this;
        }
        public WebSocketBuilder<T> onError(BiConsumer<RequestContext, Throwable> onErrorConsumer) {
            this.onErrorConsumer = onErrorConsumer;
            return this;
        }
        public WebSocketBuilder<T> onClose(TriConsumer<RequestContext, Integer, String> onCloseConsumer) {
            this.onCloseConsumer = onCloseConsumer;
            return this;
        }

        public void request() {
            // 请求对象构建
            Request request = new Request.Builder().url(this.url).build();
            // 上下文对象构建
            if (this.sendTypeEnum == null) {
                this.sendTypeEnum = WebsocketSendTypeEnum.Text;
            }
            RequestContext context = RequestContext.builder()
                    .traceId(traceId)
                    .requestUrl(this.url)
                    .sendTypeEnum(this.sendTypeEnum)
                    .param(this.param)
                    .requestStartTime(System.currentTimeMillis())
                    .build();
            // 开关默认
            if (switcher == null) {
                switcher = new BaseWebSocketBlockSwitcher(MAX_WAIT_TIME_OF_FIRST_FRAME_DATA, MAX_WAIT_TIME_OF_COMPLETED);
            }
            // 观察者构建
            BaseWebSocketObserver<T> baseWebSocketObserver = new BaseWebSocketObserver<T>(context) {
                @Override
                public void onOpen() {
                    if (onOpenConsumer != null) {
                        onOpenConsumer.accept(getContext());
                    }
                }
                @Override
                public T onMessage(String message) {
                    if (onMessageConsumer != null) {
                        return onMessageConsumer.apply(getContext(), message);
                    }
                    return null;
                }
                @Override
                public T onMessage(ByteString bytes) {
                    if (onMessageByteStringConsumer != null) {
                        return onMessageByteStringConsumer.apply(getContext(), bytes);
                    }
                    return null;
                }
                @Override
                public void onError(Throwable throwable) {
                    if (onErrorConsumer != null) {
                        onErrorConsumer.accept(getContext(), throwable);
                    }
                }
                @Override
                public void onCompleted(int code, String reason) {
                    if (onCloseConsumer != null) {
                        onCloseConsumer.accept(getContext(), code, reason);
                    }
                }
            };
            // websocket监听器构建
            BaseWebSocketListener<T> baseWebSocketListener = new BaseWebSocketListener<>(switcher, baseWebSocketObserver, responseEndFlagJudge);

            // web
            WebSocket webSocket = null;
            int closeCode = 1000;
            String closeMessage = "success";
            try {
                // 开启websocket链接
                webSocket = getOkHttpClient().newWebSocket(request, baseWebSocketListener);

                // 打开阻塞开关
                switcher.turnOn();
            }
            catch (TimeoutException te) {
                // 阻塞开关超时异常
                log.error("websocket链接超时", te);
                closeCode = 2000;
                closeMessage = "error";
                // 释放阻塞开关
                switcher.turnOffAll();
                // 将异常传递给观察者
                baseWebSocketObserver.onError(te);
            }
            catch (Exception e) {
                log.error("websocket链接异常", e);
                closeCode = 3000;
                closeMessage = "error";
                // 释放阻塞开关
                switcher.turnOffAll();
                // 将异常传递给观察者
                baseWebSocketObserver.onError(e);
            }
            finally {
                // 关闭websocket链接
                if (null != webSocket) {
                    webSocket.close(closeCode, closeMessage);
                    //log.info("工具层 => finally 关闭websocket链接");
                }
                // 释放阻塞开关
                switcher.turnOffAll();
            }
        }
    }

}
