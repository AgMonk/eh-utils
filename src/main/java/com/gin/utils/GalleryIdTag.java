package com.gin.utils;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 画廊的ID和tag
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/3/14 15:41
 */
@Getter
public class GalleryIdTag {
    public static final Pattern URL_PATTERN = Pattern.compile("/g/(\\d+)/(.{10})");

    final long id;
    final String tag;

    public GalleryIdTag(String url) {
        // 解析id和tag
        final Matcher matcher = URL_PATTERN.matcher(url);
        if (matcher.find()) {
            this.id = Long.parseLong(matcher.group(1));
            this.tag = matcher.group(2);
        } else {
            throw new RuntimeException("解析失败, 不是合法的地址:" + url);

        }
    }
}
