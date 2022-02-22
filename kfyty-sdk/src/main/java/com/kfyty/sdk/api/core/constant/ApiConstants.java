package com.kfyty.sdk.api.core.constant;

import java.util.regex.Pattern;

/**
 * 描述: api 常量
 *
 * @author kun.zhang
 * @date 2021/11/11 15:01
 * @email kfyty725@hotmail.com
 */
public interface ApiConstants {
    /**
     * {} 正则匹配
     */
    Pattern PARAMETERS_PATTERN = Pattern.compile("(\\{.*?})");

    /**
     * 默认连接超时时间
     */
    int DEFAULT_CONNECT_REQUEST_TIME_OUT = 2000;

    /**
     * 默认请求超时时间
     */
    int DEFAULT_READ_REQUEST_TIME_OUT = 3000;

    /**
     * content type
     */
    String CONTENT_TYPE_DEFAULT = "application/x-www-form-urlencoded";

    /**
     * content type
     */
    String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    /**
     * content type
     */
    String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
}