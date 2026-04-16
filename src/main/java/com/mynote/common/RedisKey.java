package com.mynote.common;


public class RedisKey {
    // 用户笔记浏览次数
    public static final String USER_NOTE_VIEW_COUNT_PREFIX = "user:note:count:";
    //热门笔记
    public static final String HOT_KEY_PREFIX = "note:hot:";

    //笔记分享
    public static final String NOTE_SHARE_PREFIX = "note:share:";
    public static final String BASE_URL_PREFIX = "http://localhost:8080/note-share/noValid/";
    //分享笔记的查看次数
    public static final String NOTE_SHARE_VIEW_COUNT_PREFIX = "note:share:view:";




}
