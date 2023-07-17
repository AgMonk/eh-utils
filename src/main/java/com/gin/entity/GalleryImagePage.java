package com.gin.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 画廊的图片详情页
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/4/1 10:13
 */
@Getter
@NoArgsConstructor
public class GalleryImagePage {
    public static final String ERROR_509 = "https://exhentai.org/img/509.gif";
    /**
     * 标题
     */
    String title;
    /**
     * 预览图地址
     */
    String src;
    /**
     * 跳转地址
     */
    String redirectUrl;

    public GalleryImagePage(Document document) {
        // 解析标题
        {
            final Elements elements = document.select("#i1 > h1");
            this.title = !elements.isEmpty() ? elements.get(0).ownText() : null;
        }
        // 解析预览图地址
        {
            final Element img = document.getElementById("img");
            this.src = img != null ? img.attr("src") : null;
        }
        // 解析跳转地址
        {
            final Elements elements = document.select("#i7 > a");
            this.redirectUrl = !elements.isEmpty() ? elements.get(0).attr("href") : null;
        }
    }

    public boolean hasOriginalUrl(){
        return redirectUrl !=null;
    }
}
