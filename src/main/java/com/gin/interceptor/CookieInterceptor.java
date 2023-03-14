package com.gin.interceptor;

import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2022/10/10 14:34
 **/
@RequiredArgsConstructor
public class CookieInterceptor implements Interceptor {
    final String cookie;
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final Request.Builder builder = request.newBuilder()
                .header("cookie", cookie);

        return chain.proceed(builder.build());
    }
}
