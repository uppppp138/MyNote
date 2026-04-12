package com.mynote.util;

import java.util.Collections;
import java.util.List;

public class CollUtils {
    
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
    
    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }
}