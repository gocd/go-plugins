package com.tw.go.plugin.common.util;

import com.google.gson.GsonBuilder;

public class JsonUtil {

    public static String toJsonString(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.create().toJson(object);
    }

    public static <T> T fromJsonString(String json, Class<T> type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.create().fromJson(json, type);
    }
}
