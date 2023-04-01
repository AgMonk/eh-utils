package com.gin.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    List<GalleryImageThumbnail> thumbnails;
    /**
     * 画廊标题
     */
    String title;
    /**
     * 日文标题
     */
    String titleJp;
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
        this.thumbnails = list.stream().flatMap(i -> i.getThumbnails().stream()).collect(Collectors.toList());

        final GalleryPage firstPage = list.get(0);
        this.title = firstPage.getTitle();
        this.titleJp = firstPage.getTitleJp();
        this.maxPages = firstPage.getMaxPages();
        this.id = firstPage.getId();
        this.tag = firstPage.getTag();
    }

    /**
     * 获取所有分页链接
     * @return 分页链接
     */
    public List<String> findAllPages() {
        return getAllPages(id, tag, this.maxPages);
    }

    @NotNull
    public static List<String> getAllPages(long id, String tag, int maxPages) {
        final String firstUrl = String.format("https://exhentai.org/g/%d/%s/", id, tag);
        final ArrayList<String> list = new ArrayList<>();
        list.add(firstUrl);
        for (int i = 1; i < maxPages; i++) {
            list.add(firstUrl + "?p=" + i);
        }
        return list;
    }
}
