package com.gin.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 画廊
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 11:51
 */
@Getter
@Setter
@NoArgsConstructor
public class Gallery {
    /**
     * 图片详情页链接
     */
    List<String> images;
    /**
     * 所有分页的链接
     */
    List<String> pages;
    /**
     * 画廊标题
     */
    String title;
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

    public Gallery(Collection<GalleryPage> galleryPages) {
        if (galleryPages == null || galleryPages.size() == 0) {
            throw new RuntimeException("画廊页不能为空");
        }
        final List<GalleryPage> list = galleryPages.stream().sorted(Comparator.comparingInt(o -> o.page)).collect(Collectors.toList());
        this.images = list.stream().flatMap(i->i.getImages().stream()).collect(Collectors.toList());

        final GalleryPage firstPage = list.get(0);
        this.pages = firstPage.getPages();
        this.title = firstPage.getTitle();
        this.maxPages = firstPage.getMaxPages();
        this.id = firstPage.getId();
        this.tag = firstPage.getTag();
    }
}
