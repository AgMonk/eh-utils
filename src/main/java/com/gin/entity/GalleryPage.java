package com.gin.entity;

import com.gin.utils.GalleryIdTag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 画廊的某一页
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/13 17:20
 */
@Setter
@Getter
@NoArgsConstructor
public class GalleryPage {
    public static final Pattern URL_PATTERN = Pattern.compile("/g/(\\d+)/(.{10})");
    /**
     * 图片详情页链接
     */
    List<String> imagePageUrls;
    /**
     * 所有分页的链接
     */
    List<String> pages;
    /**
     * 画廊标题
     */
    String title;
    /**
     * 日文标题
     */
    String titleJp;
    /**
     * 当前页
     */
    int page;
    /**
     * 最大页
     */
    int maxPages;
    /**
     * 画廊ID
     */
    long id;
    /**
     * 画廊随机TAG
     */
    String tag;


    public GalleryPage(Document document) {
        {
            final Element titleElement = document.getElementById("gn");
            this.title = titleElement != null ? titleElement.ownText() : null;
        }
        {
            final Element titleElement = document.getElementById("gj");
            this.titleJp = titleElement != null ? titleElement.ownText() : null;
        }

        //翻页组件
        final Element pager = document.getElementsByClass("ptt").get(0);
        //当前页
        this.page = Integer.parseInt(pager.getElementsByClass("ptds").get(0).getElementsByTag("a").get(0).ownText());
        // 所有页链接
        final Elements pages = pager.getElementsByTag("a");
        //最大页
        this.maxPages = pages.stream().map(Element::ownText).mapToInt(s -> {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).filter(i -> i > 0).max().orElse(0);
        //第一页的url
        final String firstUrl = pages.stream().filter(e -> "1".equals(e.ownText())).map(e -> e.attr("href")).findFirst().orElse(null);
        if (firstUrl != null) {
            this.pages = new ArrayList<>();
            this.pages.add(firstUrl);
            for (int i = 1; i < this.maxPages; i++) {
                this.pages.add(firstUrl + "?p=" + i);
            }
            // 解析id和tag
            final GalleryIdTag galleryIdTag = new GalleryIdTag(firstUrl);
            this.id = galleryIdTag.getId();
            this.tag = galleryIdTag.getTag();
        }

        this.imagePageUrls = document.getElementsByClass("gdtl").stream().map(doc -> doc.getElementsByTag("a").get(0).attr("href")).collect(Collectors.toList());

    }
}
