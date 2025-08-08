package org.example.llm.common.util.sse;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.example.llm.common.model.domain.ChatRequestContext;
import org.example.llm.common.util.Checks;
import org.example.llm.common.util.http.OkHttpClientUtil;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author : zybi
 * @date : 2024/11/12 11:19
 */
@Slf4j
public class SSEUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 从spring容器中读取okHttpClient客户端
     */
    private static OkHttpClient getOkHttpClient() {
        return OkHttpClientUtil.getOkHttpClient();
    }


    /**
     * sse请求构建器
     */
    @Getter
    public static class Builder<T> {
        private final String url;                   // 请求url
        private Map<String, String> headers;        // 请求头
        private Map<String, String> nameValuePairs; // 请求参数 url-encode方式
        private Map<String, String> queryParams;    // 查询参数
        private Object bodyParam;                   // 请求体参数(内部转换为json）
        private Boolean printRequestParams = true;  // 打印请求参数
        private Boolean printResponseStr = true;    // 打印响应结果
        private Integer connectionTimeout;          // 链接超时时间
        private Integer readTimeout;                // 读取结果超时时间
        private BaseSSEObserver<T> observer;        // 观察者
        private Function<T, Boolean> isEndFlag;     // 结束帧返回


        Builder(String url) {
            this.url = url;
        }

        public Builder<T> addHeader(String key, String val) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key, val);
            return this;
        }

        public Builder<T> addAllHeaders(Map<String, String> headers) {
            if (headers == null) {
                return this;
            }
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
            return this;
        }

        public Builder<T> addNamePair(String key, String val) {
            if (this.nameValuePairs == null) {
                this.nameValuePairs = new HashMap<>();
            }
            this.nameValuePairs.put(key, val);
            return this;
        }

        public Builder<T> addAllNamePairs(Map<String, String> nameValuePairs) {
            if (nameValuePairs == null) {
                return this;
            }
            if (this.nameValuePairs == null) {
                this.nameValuePairs = new HashMap<>();
            }
            this.nameValuePairs.putAll(nameValuePairs);
            return this;
        }

        public Builder<T> addQueryParam(String key, String val) {
            if (this.queryParams == null) {
                this.queryParams = new HashMap<>();
            }
            this.queryParams.put(key, val);
            return this;
        }

        public Builder<T> addAllQueryParams(Map<String, String> queryParams) {
            if (queryParams == null) {
                return this;
            }
            if (this.queryParams == null) {
                this.queryParams = new HashMap<>();
            }
            this.queryParams.putAll(queryParams);
            return this;
        }

        public Builder<T> setBodyParam(Object bodyParam) {
            this.bodyParam = bodyParam;
            return this;
        }

        public Builder<T> setPrintRequestParams(Boolean printRequestParams) {
            this.printRequestParams = printRequestParams;
            return this;
        }

        public Builder<T> setPrintResponseStr(Boolean printResponseStr) {
            this.printResponseStr = printResponseStr;
            return this;
        }

        public Builder<T> setConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder<T> setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder<T> setObserver(BaseSSEObserver<T> observer) {
            this.observer = observer;
            return this;
        }

        public Builder<T> setIsEndFlag(Function<T, Boolean> isEndFlag) {
            this.isEndFlag = isEndFlag;
            return this;
        }

        private void validateParam() {
            if (this.url == null) {
                throw new IllegalArgumentException("sse请求url不能为空");
            }
            if (this.observer == null) {
                throw new IllegalArgumentException("sse请求消费者不能为空");
            }
            if (this.isEndFlag == null) {
                throw new IllegalArgumentException("sse请求结束帧判断不能为空");
            }
        }

        /**
         * 使用构建器中的参数构建一个request请求
         */
        private Request generateRequest() {
            Request.Builder requestBuilder = new Request.Builder().url(url);
            if ( printRequestParams ) {
                log.info("==> sse url: {}", url);
            }
            // headers
            if (Checks.noNull( headers )) {
                log.info("==> headers: {}", headers);
                for (String header:headers.keySet()){
                    requestBuilder.addHeader(header, headers.get(header));
                }
            }

            // form-body
            if (Checks.noNull( nameValuePairs )) {
                if (printRequestParams) {
                    log.info("==> body-form: {}", nameValuePairs);
                }
                FormBody.Builder builder = new FormBody.Builder();
                for (String key : nameValuePairs.keySet()) {
                    builder.add(key, nameValuePairs.get(key));
                }
                requestBuilder.post(builder.build());
            }

            // json-body
            if (Checks.noNull( bodyParam )) {
                String bodyJson = JSONObject.toJSONString(bodyParam);
                if (printRequestParams) {
                    log.info("==> body-json: {}", bodyJson);
                }
                RequestBody requestBody = RequestBody.create(bodyJson, JSON);
                requestBuilder.post(requestBody);
            }
            if (Checks.noNull(queryParams)) {
                HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
                for (String key : queryParams.keySet()) {
                    httpUrlBuilder.addQueryParameter(key, queryParams.get(key));
                }
                if (printRequestParams) {
                    log.info("==> url-param: {}", queryParams);
                }
                requestBuilder.url(httpUrlBuilder.build());
            }
            return requestBuilder.build();
        }


        public void execute() {
            // 0. 校验参数
            validateParam();

            // 1. 利用参数构建request
            Request request = generateRequest();

            // 2. 初始化上下文对象
            ChatRequestContext context = ChatRequestContext.builder()
                    .requestUrl(this.url)
                    .param(this.bodyParam)
                    .requestStartTime(System.currentTimeMillis())
                    .build();
            
            // 会话中断：会话开始前
            if (Thread.currentThread().isInterrupted()) {
                this.observer.onError(context, new RuntimeException("中断会话，会话建连开始前。"));
            }

            try {
                // 3. 请求并消
                requestThenConsume(request, this.connectionTimeout, this.readTimeout, context, this.observer, this.isEndFlag, this.printResponseStr);
            }
            catch (Exception e) {
                log.error("错误", e);
                // 观察者监听到异常事件
                this.observer.onError(context, e);
            }
            finally {
                context.setRequestEndTime(System.currentTimeMillis());
                // 观察者监听到结束事件
                this.observer.onCompleted(context);
            }
        }
    }


    /**
     * 创建一个请求构建器
     * @param url 请求地址
     */
    public static <T> Builder<T> create(String url) {
        return new Builder<>(url);
    }


    /**
     * 请求并消费
     * @param request 请求
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读取超时时间
     * @param context sse上下文
     * @param observer 观察者
     * @param isEndFlag 是否到结束帧
     * @param printResponse 是否打印响应结果
     */
    private static <T> void requestThenConsume(Request request,
                                               Integer connectTimeout,
                                               Integer readTimeout,
                                               ChatRequestContext context,
                                               BaseSSEObserver<T> observer,
                                               Function<T, Boolean> isEndFlag,
                                               Boolean printResponse)
    {
        try {
            // 1. 构建client
            OkHttpClient okHttpClient = getClient(connectTimeout, readTimeout);

            // 2. 请求并消费response
            try (Response response = okHttpClient.newCall(request).execute()) {
                // 2.0 建连成功事件
                context.setConnectedTime(System.currentTimeMillis());
                observer.onOpen(context);

                // 2.1 异常
                checkResponse(response);

                // 2.2 消费
                consumeResponse(response, context, observer, isEndFlag, printResponse);
            }
            catch (InterruptedIOException iie) {
                throw new RuntimeException("中断会话");
            }
        }
        catch (Exception e) {
            log.error("进行sse请求异常: " + request.url(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取httpClient
     * @param connectTimeout 连接超时时间
     * @param readTimeout 读取数据超时时间
     */
    private static OkHttpClient getClient(Integer connectTimeout, Integer readTimeout) {
        OkHttpClient okHttpClient = getOkHttpClient();
        if (connectTimeout != null || readTimeout != null) {
            OkHttpClient.Builder builder = okHttpClient.newBuilder();
            if (connectTimeout != null && connectTimeout > 0) {
                builder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
            }
            if (readTimeout != null && readTimeout > 0) {
                builder.readTimeout(readTimeout, TimeUnit.SECONDS);
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    /**
     * 响应结果基础校验
     * @param response 响应结果
     * @throws IOException io异常
     */
    private static void checkResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            ResponseBody body = response.body();
            if (body != null) {
                BufferedSource source = body.source();
                String errorMsg = source.readByteString().utf8();
                throw new RuntimeException(errorMsg);
            }
            throw new RuntimeException(response.message());
        }
    }

    /**
     * 响应结果消费
     * @param response 响应结果
     * @param observer 观察者
     * @throws IOException 异常
     */
    private static <T> void consumeResponse(Response response,
                                            ChatRequestContext context,
                                            BaseSSEObserver<T> observer,
                                            Function<T, Boolean> isEndFlag,
                                            Boolean printResponse) throws IOException
    {
        ResponseBody body = response.body();
        if (body == null) {
            log.info("响应body为空");
            return;
        }
        BufferedSource source = body.source();
        while (true) {
            // 会话中断：会话开始前
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("中断会话，会话过程中。");
            }
            
            // 预期内的中断操作
            if (Boolean.TRUE.equals(context.getStopped())) {
                log.info("预期内中断操作（主动调用停止会话操作）");
                break;
            }
            
            String line = source.readUtf8Line();
            if (printResponse) {
                log.info("工具层 => sse返回： {}", line);
            }

            if (context.getFirstResponseTime() == null) {
                context.setFirstResponseTime(System.currentTimeMillis());
            }

            /* 空字符串返回是正常现象跳过即可 */
            if (line != null && line.isEmpty()) {
                continue;
            }

            /* 观察者消费事件 */
            T t = observer.onMessage(context, line);

            /* 结束帧 */
            if (isEndFlag.apply(t)) {
                break;
            }
        }
    }
}
