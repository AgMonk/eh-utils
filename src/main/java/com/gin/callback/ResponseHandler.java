package com.gin.callback;

import okhttp3.Response;

import java.io.IOException;

/**
 * 响应处理方法
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 15:03
 */
public interface ResponseHandler<T> {
    /**
     * 将响应转换为指定类型
     * @param response 响应
     * @return T
     * @throws IOException 异常
     */
    T handle(Response response) throws IOException;
}   
