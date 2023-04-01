package com.gin.entity;

import lombok.Getter;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * 响应上下文
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/4/1 10:48
 */
@Getter
public class ResponseContext {
    private final String url;
    private final int code;
    /**
     * 重定向地址(原图地址)
     */
    private final String redirectUrl;
    private final Document document;
    private final String bodyString;

    public ResponseContext(Response response) throws IOException {
        this.url = response.request().url().toString();
        this.code = response.code();
        final ResponseBody body = response.body();
        this.bodyString = body != null ? body.string() : null;
        this.document = this.bodyString != null ? Jsoup.parse(this.bodyString) : null;
        this.redirectUrl = response.header("Location");
        response.close();
    }
}
