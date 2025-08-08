package org.example.llm.common.util.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
/**
 * @author : zybi
 * @date : 2024/7/13 0:13
 */
@Slf4j
public class OkHttpClientFactory {

    /**
     * 创建一个httpClient
     * @param connectionTimeout 建连超时时间 单位秒
     * @param readTimeout 请求超时时间 单位秒
     * @param maxIdleConnections 最大连接数
     * @param keepAliveDuration 存活时间 单位分钟
     * @return OkHttpClient
     */
    public static OkHttpClient getInstance(Integer connectionTimeout, Integer readTimeout, Integer maxIdleConnections, Integer keepAliveDuration) {

        log.info("创建HttpClient, connectionTimeout：{}s, readTimeout：{}s, maxIdleConnections：{}, keepAliveDuration：{}min",
                connectionTimeout, readTimeout, maxIdleConnections, keepAliveDuration);
        return okHttpClient(connectionTimeout, readTimeout, maxIdleConnections, keepAliveDuration);
    }

    private static OkHttpClient okHttpClient(Integer connectionTimeout, Integer readTimeout, Integer maxIdleConnections, Integer keepAliveDuration) {
        return new OkHttpClient.Builder()
                .sslSocketFactory(Objects.requireNonNull(sslSocketFactory()), x509TrustManager())
                .retryOnConnectionFailure(true)
                .connectionPool(pool(maxIdleConnections, keepAliveDuration))//连接池
                .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }


    private static X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }


    private static SSLSocketFactory sslSocketFactory() {
        try {
            //信任任何链接
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            log.error("算法异常", e);
        } catch (KeyManagementException e) {
            log.error("key异常", e);
        }
        return null;
    }
    /**
     * Create a new connection pool with tuning parameters appropriate for a single-user application.
     * The tuning parameters in this pool are subject to change in future OkHttp releases. Currently
     */
    private static ConnectionPool pool(Integer maxIdleConnections, Integer keepAliveDuration) {
        return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES);
    }

}