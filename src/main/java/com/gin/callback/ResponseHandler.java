package com.gin.callback;

import com.gin.entity.ResponseContext;

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
     * @param context 响应
     * @return T
     * @throws IOException 异常
     */
    T handle(ResponseContext context) throws IOException;
}   
