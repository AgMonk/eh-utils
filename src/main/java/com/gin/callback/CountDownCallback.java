package com.gin.callback;

import com.gin.entity.ResponseContext;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 倒计时Callback
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 14:22
 */
@RequiredArgsConstructor
public abstract class CountDownCallback implements Callback {
    private final CountDownLatch countDownLatch;

    @Override
    public final void onFailure(@NotNull Call call, @NotNull IOException e) {
        final String url = call.request().url().toString();
        handleFailure(call, url, e);
        countDownLatch.countDown();
    }

    @Override
    public final void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        handleResponse(call,new ResponseContext(response));
        countDownLatch.countDown();
    }


    /**
     * 处理失败
     * @param call call
     * @param url  url
     * @param e    异常
     */
    public abstract void handleFailure(Call call, String url, IOException e);

    /**
     * 处理响应
     * @param call     call
     * @param context 响应
     * @throws IOException 异常
     */

    public abstract void handleResponse(Call call,  ResponseContext context) throws IOException;

}   
