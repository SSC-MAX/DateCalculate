package org.example.llm.common.util.http;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.llm.common.enums.RequestMethodEnum;
import org.example.llm.common.util.Checks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author luoqifeng
 */
@Slf4j
public class OkHttpClientUtil {

    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 5;
    private static final Integer DEFAULT_READ_TIMEOUT = 60;
    private static final Integer DEFAULT_MAX_IDLE_CONNECTIONS = 200;
    private static final Integer DEFAULT_KEEP_ALIVE_DURATION = 5;


    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient okHttpClient;
    private static final Object LOCK = new Object();

    public static OkHttpClient getOkHttpClient() {
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

    public static Builder create(final String url, RequestMethodEnum method) {
        return new Builder(url, method);
    }

    /**
     * 构建一个 http - post请求
     */
    public static Builder createPost(final String url) {
        return create(url, RequestMethodEnum.POST);
    }

    /**
     * 构建一个 http - post请求
     */
    public static Builder createGet(final String url) {
        return create(url, RequestMethodEnum.GET);
    }

    /**
     * http请求参数构建者
     */
    public static class Builder {
        private final String url;                   // 请求url
        private final RequestMethodEnum method;         // 请求方式
        private Map<String, String> headers;        // 请求头
        private Map<String, String> nameValuePairs; // 请求参数 url-encode方式
        private Object bodyParam;                   // 请求体参数(内部转换为json）
        private Boolean retryOnTimeout;             // 超时重试（默认true)
        private Boolean printRequestParams = true;  // 打印请求参数
        private Boolean printResponseStr = true;    // 打印响应结果
        private Integer connectionTimeout;          // 链接超时时间
        private Integer readTimeout;                // 读取结果超时时间
        private Map<String, String> params;         // 请求参数 query params

        /**
         * 构造器
         */
        public Builder(String url, RequestMethodEnum method) {
            this.url = url;
            this.method = method;
            this.retryOnTimeout = true;
        }

        /**
         * 添加请求头
         */
        public Builder addHeader(String key, String value) {
            if (headers == null) {
                headers =  new HashMap<>();
            }
            headers.put(key, value);
            return this;
        }

        public Builder addAllHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * 添加url-encode参数
         */
        public Builder addNameValuePair(String key, String value) {
            if (nameValuePairs == null) {
                nameValuePairs = new HashMap<>();
            }
            nameValuePairs.put(key, value);
            return this;
        }

        public Builder addAllNameValuePair(Map<String, String> forms) {
            this.nameValuePairs = forms;
            return this;
        }

        public Builder addParam(String key, String value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(key, value);
            return this;
        }

        public Builder addAllParam(Map<String, String> params) {
            this.params = params;
            return this;
        }

        /**
         * 设置请求体参数对象
         */
        public Builder setRequestBody(Object bodyParam) {
            this.bodyParam = bodyParam;
            return this;
        }

        /**
         * 设置是否超时重试
         */
        public Builder setRetryOnTimeout(boolean retryOnTimeout) {
            this.retryOnTimeout = retryOnTimeout;
            return this;
        }

        /**
         * 设置打印请求参数
         */
        public Builder setPrintParams(boolean print) {
            this.printRequestParams = print;
            return this;
        }

        /**
         * 设置打印响应结果
         */
        public Builder setPrintResponse(boolean print) {
            this.printResponseStr = print;
            return this;
        }

        public Builder setConnectTimeout(Integer timeout) {
            this.connectionTimeout = timeout;
            return this;
        }
        public Builder setReadTimeout(Integer timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public String exec() {
            switch (this.method) {
                case GET: return get(url, headers, nameValuePairs, params, connectionTimeout, readTimeout, printRequestParams, printResponseStr);
                case POST: return post(url, headers, nameValuePairs, params, bodyParam, connectionTimeout, readTimeout, printRequestParams, printResponseStr);
                default:
                    throw new UnsupportedOperationException("暂不支持此种类型的http请求");
            }
        }
    }

    private static String get(String url,
                               Map<String, String> headers,
                               Map<String, String> nameValuePairs,
                               Map<String, String> params,
                               Integer connectTimeout,
                               Integer readTimeout,
                               boolean printRequestParams,
                               boolean printResponseStr) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
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
        if (Checks.noNull(params)) {
            HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            for (String key : params.keySet()) {
                httpUrlBuilder.addQueryParameter(key, params.get(key));
            }
            requestBuilder.url(httpUrlBuilder.build());
        }

        Request request = requestBuilder.build();
        if ( printRequestParams ) {
            log.info("==> http url: {}", request.url());
        }

        String responseStr = execute(request, connectTimeout, readTimeout);
        if ( printResponseStr ) {
            log.info("<== 结束http请求。响应结果：{}", responseStr);
        }
        return responseStr;
    }

    private static String post(String url,
                              Map<String, String> headers,
                              Map<String, String> nameValuePairs,
                              Map<String, String> params,
                              Object bodyParam,
                              Integer connectTimeout,
                              Integer readTimeout,
                              boolean printRequestParams,
                              boolean printResponseStr) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
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
        if (Checks.noNull(params)) {
            HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            for (String key : params.keySet()) {
                httpUrlBuilder.addQueryParameter(key, params.get(key));
            }
            requestBuilder.url(httpUrlBuilder.build());
        }

        Request request = requestBuilder.build();
        if ( printRequestParams ) {
            log.info("==> http url: {}", request.url());
        }

        String responseStr = execute(request, connectTimeout, readTimeout);
        if ( printResponseStr ) {
            log.info("<== 结束http请求。响应结果：{}", responseStr);
        }
        return responseStr;
    }

    private static String execute(Request request, Integer connectTimeout, Integer readTimeout) {
        try {
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
            try (Response response = okHttpClient.newCall(request).execute();) {
                if (response.isSuccessful()) {
                    return Objects.requireNonNull(response.body()).string();
                }
                throw new RuntimeException(response.message());
            }
        }
        catch (Exception e) {
            log.error("进行http请求异常: " + request.url(), e);
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 获取响应体header里面的key
     * @param request
     * @param headerKey
     * @return
     */
    private static String execute(Request request, String headerKey) {
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.header(headerKey));
            }
            throw new RuntimeException(response.message());
        }
        catch (Exception e) {
            log.error("进行http请求异常: " + request.url(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getHeaderResponseByPost(String url,
                                                 Map<String, String> headers,
                                                 Map<String, String> nameValuePairs,
                                                 Map<String, String> responseHeader,
                                                 Object bodyParam,
                                                 Integer connectTimeout,
                                                 Integer readTimeout,
                                                 boolean printRequestParams,
                                                 boolean printResponseStr)
    {
        log.info("开始http请求。 ==> url: {}", url);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        // headers
        if (Checks.noNull( headers )) {
            log.info("  ==> headers: {}", headers);
            for (String header:headers.keySet()){
                requestBuilder.addHeader(header, headers.get(header));
            }
        }

        // form-body
        if (Checks.noNull( nameValuePairs )) {
            if (printRequestParams) {
                log.info("  ==> body-form: {}", nameValuePairs);
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
                log.info("  ==> body-json: {}", bodyJson);
            }
            RequestBody requestBody = RequestBody.create(bodyJson, JSON);
            requestBuilder.post(requestBody);
        }
        String responseStr = "";
        // 获取响应header 对应的key-val
        String headerKey = responseHeader.get("hwToken");
        if (Checks.isNull(responseHeader)){
            responseStr = execute(requestBuilder.build(), connectTimeout, readTimeout);
        }else{
            responseStr = execute(requestBuilder.build(), headerKey);
        }
        if ( printResponseStr ) {
            log.info("<== 结束http请求。响应结果：{}", responseStr);
        } else {
            log.info("<== 结束http请求。响应长度：{}", responseStr.length());
        }
        return responseStr;
    }


}
