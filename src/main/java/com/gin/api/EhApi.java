package com.gin.api;

import com.gin.callback.CountDownCallback;
import com.gin.callback.ResponseHandler;
import com.gin.entity.Gallery;
import com.gin.entity.GalleryPage;
import com.gin.interceptor.CookieInterceptor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 主类
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 09:44
 */
public class EhApi {
    final OkHttpClient client;

    public EhApi(OkHttpClient client, String cookie, Proxy proxy) {
        final OkHttpClient.Builder builder = client.newBuilder();

        builder.addInterceptor(new CookieInterceptor(cookie));
        builder.followRedirects(false);
        if (proxy != null) {
            builder.proxy(proxy);
        }
        this.client = builder.build();
    }

    /**
     * 默认配置创建api
     * @param cookie cookie
     * @return api
     */
    public static EhApi defaultApi(String cookie) {
        return new EhApi(defaultClient(), cookie, defaultProxy());
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

    @NotNull
    private static Request getRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    /**
     * 异步请求一个画廊的所有页
     * @param url 画廊任意页链接
     * @return map
     */
    public Gallery gallery(String url) throws InterruptedException, IOException {
        final GalleryPage page = getGalleryPage(url);
        // 所有分页链接
        final List<String> pages = page.getPages();
        // 返回结果
        final HashMap<String, GalleryPage> resultMap = new HashMap<>(pages.size());
        // 放入第一页
        resultMap.put(url, page);
        // 缺少的页
        List<String> lackPages = pages.stream().filter(u -> !resultMap.containsKey(u)).collect(Collectors.toList());
        // 当还有缺少页时，继续请求
        while (lackPages.size() > 0) {
            // 请求缺少页, 放入返回结果
            resultMap.putAll(getGalleryPage(lackPages));
            // 重新计算缺少页
            lackPages = pages.stream().filter(u -> !resultMap.containsKey(u)).collect(Collectors.toList());
        }
        // 将所有数据组合为一个对象
        return new Gallery(resultMap.values());
    }

    /**
     * 异步请求一个地址
     * @param url      地址
     * @param callback 响应处理方法
     */
    public void get(String url, Callback callback) throws InterruptedException {
        get(Collections.singleton(url), callback);
    }

    /**
     * 同步请求一个地址
     * @param url 地址
     * @return Document
     */
    public Response get(String url) throws IOException {
        return client.newCall(getRequest(url)).execute();
    }

    /**
     * 异步请求多个地址
     * @param urls     urls
     * @param callback 回调
     */
    public void get(Collection<String> urls, Callback callback) throws InterruptedException {
        urls.forEach(url -> client.newCall(getRequest(url)).enqueue(callback));
    }

    /**
     * 异步请求多个地址, 阻塞到所有请求完成, 转换为指定类型返回
     * @param urls urls
     * @return map
     */
    public <T> HashMap<String, T> get(Collection<String> urls, ResponseHandler<T> handler) throws InterruptedException {
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
            public void handleResponse(Call call, String url, Response response) throws IOException {
                map.put(url, handler.handle(response));
                response.close();
            }
        });
        latch.await();
        return map;
    }

    /**
     * 异步请求多个网页, 阻塞到所有请求完成
     * @param urls urls
     * @return map
     */
    public HashMap<String, Document> getDocument(Collection<String> urls) throws InterruptedException {
        return getUrls(urls, response -> {
            assert response.body() != null;
            return Jsoup.parse(response.body().string());
        });
    }

    /**
     * 同步请求一个网页地址
     * @param url url
     * @return document
     */
    public Document getDocument(String url) throws IOException {
        try (Response response = get(url)) {
            assert response.body() != null;
            return Jsoup.parse(response.body().string());
        }
    }

    /**
     * 异步请求多个画廊页, 阻塞到所有请求完成
     * @param urls 画廊页地址
     * @return 画廊对象
     */
    public HashMap<String, GalleryPage> getGalleryPage(Collection<String> urls) throws InterruptedException {
        return getUrls(urls, response -> {
            assert response.body() != null;
            return new GalleryPage(Jsoup.parse(response.body().string()));
        });
    }

    /**
     * 同步请求一个画廊页
     * @param url url
     * @return 画廊页
     */
    public GalleryPage getGalleryPage(String url) throws IOException {
        return new GalleryPage(getDocument(url));
    }

    /**
     * 请求多个图片详情页地址,返回原图地址
     * @param imageUrls 图片详情页地址
     * @return 原图地址
     */
    public HashMap<String, String> getOriginalUrl(Collection<String> imageUrls) throws InterruptedException {
        final HashMap<String, Document> documentMap = getDocument(imageUrls);
        final HashMap<String, String> result = new HashMap<>(documentMap.size());
        documentMap.forEach((s, document) -> {
            final Elements select = document.select("#i7 > a");
            if (!select.isEmpty()) {
                result.put(s, select.get(0).attr("href"));
            }else{
                final Element img = document.getElementById("img");
                if (img!=null) {
                    result.put(s, img.attr("src"));
                }
            }
        });
        return result;
    }

    /**
     * 请求多个原图地址，返回重定向的真实地址
     * @param originalUrls urls
     * @return 重定向地址
     */
    public HashMap<String, String> getRedirectUrl(Collection<String> originalUrls) throws InterruptedException {
        final HashMap<String, String> res = new HashMap<>(originalUrls.size());
        final String fullImg = "fullimg.php";
        // 没有原图的，直接用预览图
        originalUrls.stream().filter(u->!u.contains(fullImg)).collect(Collectors.toList()).forEach(u->res.put(u,u));
        // 有原图的，请求原图
        final List<String> hasOriginal = originalUrls.stream().filter(u -> u.contains(fullImg)).collect(Collectors.toList());
        res.putAll(getUrls(hasOriginal, response -> {
            if (response.code() == 302) {
                return response.header("Location");
            }
            throw new IOException("请求失败, 请重试");
        }));
        return res;
    }

    /**
     * 异步请求多个地址, 阻塞到所有请求成功, 转换为指定类型返回
     * @param urls urls
     * @return map
     */
    public <T> HashMap<String, T> getUrls(Collection<String> urls, ResponseHandler<T> handler) throws InterruptedException {
        final HashMap<String, T> map = new HashMap<>(urls.size());
        // 缺少的页
        List<String> lackUrls = new ArrayList<>(urls);
        do {
            // 请求缺少页, 放入返回结果
            map.putAll(get(lackUrls, handler));
            // 重新计算缺少页
            lackUrls = urls.stream().filter(u -> !map.containsKey(u)).collect(Collectors.toList());
            // 当还有缺少页时，继续请求
        } while (lackUrls.size() > 0);
        return map;
    }
}
