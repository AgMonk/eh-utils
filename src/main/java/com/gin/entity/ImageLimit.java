package com.gin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 图片限额
 * @author : ginstone
 * @version : v1.0.0
 * @since : 2023/4/1 14:41
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageLimit {
    Integer current;
    Integer max;
}   
