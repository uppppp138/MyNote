package com.mynote.util;

import cn.hutool.core.bean.BeanUtil;
import java.util.List;
import java.util.stream.Collectors;

public class BeanUtils {
    
    public static <T, R> List<T> copyList(List<R> source, Class<T> targetClass) {
        if (source == null || source.isEmpty()) {
            return CollUtils.emptyList();
        }
        return source.stream()
                .map(item -> BeanUtil.copyProperties(item, targetClass))
                .collect(Collectors.toList());
    }
    
    public static <T, R> List<T> copyList(List<R> source, Class<T> targetClass, Convert<R, T> convert) {
        if (source == null || source.isEmpty()) {
            return CollUtils.emptyList();
        }
        return source.stream()
                .map(convert::convert)
                .collect(Collectors.toList());
    }
}