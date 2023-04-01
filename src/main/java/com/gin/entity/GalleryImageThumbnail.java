package com.gin.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片缩略图
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/4/1 17:05
 */
@Getter
@Setter
@NoArgsConstructor
public class GalleryImageThumbnail {
    public static final Pattern PATTERN = Pattern.compile("Page (\\d+): (.+)");
    int index;
    /**
     * 图片详情页url
     */
    String imagePageUrl;
    /**
     * 文件名
     */
    String filename;
    /**
     * 缩略图url
     */
    String thumbnailUrl;

    /**
     * 用 gdtl 类型的 element构造
     * @param element 元素
     */
    public GalleryImageThumbnail(Element element) {
        // 图片详情页
        final Elements a = element.getElementsByTag("a");
        if (!a.isEmpty()) {
            this.imagePageUrl = a.get(0).attr("href");
        }
        // 图片标签
        final Elements img = element.getElementsByTag("img");
        if (!img.isEmpty()) {
            final Element e = img.get(0);
            // 缩略图地址
            this.thumbnailUrl = e.attr("src");
            final Matcher matcher = PATTERN.matcher(e.attr("title"));
            if (matcher.find()) {
                this.filename = matcher.group(2);
                this.index = Integer.parseInt(matcher.group(1));
            }
        }
    }
}
