package com.gin.api;

import com.gin.callback.CountDownCallback;
import com.gin.callback.ResponseHandler;
import com.gin.entity.ResponseContext;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Eh客户端
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/4/1 09:35
 */
public class EhClient {
    private final OkHttpClient client;
    private final String cookie;


    public EhClient(OkHttpClient client, String cookie, Proxy proxy) {
        final OkHttpClient.Builder builder = client.newBuilder();
        builder.followRedirects(false);
        if (proxy != null) {
            builder.proxy(proxy);
        }

        this.client = builder.build();
        this.cookie = cookie;
    }

    /**
     * 默认配置创建client
     * @param cookie cookie
     * @return api
     */
    public static EhClient createDefault(String cookie) {
        return new EhClient(defaultClient(), cookie, defaultProxy());
    }

    /**
     * 默认客户端
     * @return okHttpclient
     */
    public static OkHttpClient defaultClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(3, 30, TimeUnit.SECONDS))
                .build();
    }

    /**
     * 默认代理
     * @return 代理
     */
    public static Proxy defaultProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809));
    }

    /**
     * 异步请求一个地址
     * @param url      地址
     * @param callback 响应处理方法
     */
    public void get(String url, Callback callback) {
        getCall(url).enqueue(callback);
    }

    /**
     * 同步请求一个地址
     * @param url 地址
     * @return Document
     */
    public Response get(String url) throws IOException {
        return getCall(url).execute();
    }

    /**
     * 异步请求多个地址
     * @param urls     urls
     * @param callback 回调
     */
    public void get(Collection<String> urls, Callback callback) {
        urls.forEach(url -> get(url, callback));
    }

    /**
     * 同步请求一个地址
     * @param url 地址
     * @return ResponseContext
     */
    public ResponseContext getContext(String url) throws IOException {
        return new ResponseContext(get(url));
    }

    /**
     * 异步请求多个地址, 阻塞到所有请求成功, 转换为指定类型返回
     * @param urls urls
     * @return map
     */
    public <T> HashMap<String, T> getContext(Collection<String> urls, ResponseHandler<T> handler) throws InterruptedException {
        final HashMap<String, T> map = new HashMap<>(urls.size());
        // 缺少的页
        List<String> lackUrls = new ArrayList<>(urls);
        do {
            // 请求缺少页, 放入返回结果
            map.putAll(getContextAwait(lackUrls, handler));
            // 重新计算缺少页
            lackUrls = urls.stream().filter(u -> !map.containsKey(u)).collect(Collectors.toList());
            // 当还有缺少页时，继续请求
        } while (lackUrls.size() > 0);
        return map;
    }

    @NotNull
    private Call getCall(String url) {
        return client.newCall(getRequest(url));
    }

    /**
     * 异步请求多个地址, 阻塞到所有请求完成或超时, 转换为指定类型返回
     * @param urls urls
     * @return map
     */
    private <T> HashMap<String, T> getContextAwait(Collection<String> urls, ResponseHandler<T> handler) throws InterruptedException {
        final HashMap<String, T> map = new HashMap<>(urls.size());
        final CountDownLatch latch = new CountDownLatch(urls.size());
        get(urls, new CountDownCallback(latch) {
            @Override
            public void handleFailure(Call call, String url, IOException e) {
                if (e instanceof SocketTimeoutException) {
                    System.err.println("[WARN] " + e.getLocalizedMessage() + " : " + url);
                } else {
                    e.printStackTrace();
                }
            }

            @Override
            public void handleResponse(Call call, ResponseContext context) throws IOException {
                map.put(context.getUrl(), handler.handle(context));
            }
        });
        latch.await();
        return map;
    }

    @NotNull
    private Request getRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("cookie", this.cookie)
                .build();
    }
}
