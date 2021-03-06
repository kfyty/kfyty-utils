package com.kfyty.support.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS;

/**
 * 功能描述: json 工具类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 15:26
 * @since JDK 1.8
 */
public abstract class JsonUtil {

    private static final ObjectMapper OBJECT_READ_MAPPER = new ObjectMapper();

    private static final ObjectMapper OBJECT_WRITE_MAPPER = new ObjectMapper();

    static {
        OBJECT_READ_MAPPER.setTimeZone(TimeZone.getDefault());
        OBJECT_WRITE_MAPPER.setTimeZone(TimeZone.getDefault());
        OBJECT_WRITE_MAPPER.configure(WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        OBJECT_WRITE_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return OBJECT_WRITE_MAPPER.writeValueAsString(o);
    }

    public static String toJson(Object o, String dateFormat) throws JsonProcessingException {
        OBJECT_WRITE_MAPPER.setDateFormat(new SimpleDateFormat(dateFormat));
        return OBJECT_WRITE_MAPPER.writeValueAsString(o);
    }

    public static <T> T toObject(String json, Class<T> clazz) throws IOException {
        return OBJECT_READ_MAPPER.readValue(json, clazz);
    }
}
