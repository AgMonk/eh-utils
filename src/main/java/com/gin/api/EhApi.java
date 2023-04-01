package com.gin.api;

import com.gin.entity.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主类
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 09:44
 */
@RequiredArgsConstructor
public class EhApi {
    private final EhClient client;
    /**
     * 异步请求一个画廊的所有页
     * @param url 画廊任意页链接
     * @return map
     */
    public Gallery getGallery(String url) throws InterruptedException, IOException {
        final GalleryPage page = getGalleryPage(url);
        // 所有分页链接
        final List<String> pages = page.getAllPages();
        // 返回结果
        final HashMap<String, GalleryPage> resultMap = new HashMap<>(pages.size());
        // 放入第一页
        resultMap.put(url, page);
        // 缺少的页
        List<String> lackPages = pages.stream().filter(u -> !resultMap.containsKey(u)).collect(Collectors.toList());
        // 当还有缺少页时，继续请求
        if  (lackPages.size() > 0) {
            // 请求缺少页, 放入返回结果
            resultMap.putAll(getGalleryPage(lackPages));
        }
        // 将所有数据组合为一个对象
        return new Gallery(resultMap.values());
    }

    /**
     * 异步请求多个画廊页, 阻塞到所有请求成功
     * @param urls 画廊页地址
     * @return 画廊对象
     */
    public HashMap<String, GalleryPage> getGalleryPage(Collection<String> urls) throws InterruptedException {
        return client.getContext(urls, context -> new GalleryPage(context.getDocument()));
    }

    /**
     * 同步请求一个画廊页
     * @param url url
     * @return 画廊页
     */
    public GalleryPage getGalleryPage(String url) throws IOException {
        return new GalleryPage(client.getContext(url).getDocument());
    }

    /**
     * 同步请求原始图片地址
     * @param imagePageUrl 图片详情页地址
     * @return 图片原址地址
     */
    public String getOriginalUrl(String imagePageUrl) throws IOException {
        final GalleryImagePage imagePage = new GalleryImagePage(client.getContext(imagePageUrl).getDocument());
        if (imagePage.hasOriginalUrl()) {
            // 有原图地址，请求原图地址
            return client.getContext(imagePage.getRedirectUrl()).getRedirectUrl();
        }else{
            // 无原图地址，返回预览图地址
            return imagePage.getSrc();
        }
    }

    /**
     * 请求图像限额
     * @return 限额
     */
    @Nullable
    public ImageLimit getImageLimit() throws IOException {
        String home = "https://e-hentai.org/home.php";
        final ResponseContext context = client.getContext(home);
        @SuppressWarnings("SpellCheckingInspection")
        final Elements elements = context.getDocument().select(".homebox strong");
        if (elements.size()>=2) {
            final int current = Integer.parseInt(elements.get(0).ownText());
            final int max = Integer.parseInt(elements.get(1).ownText());
            return new ImageLimit(current, max);
        }
        return null;
    }

}
