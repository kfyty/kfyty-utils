package com.kfyty.mvc.annotation;

import com.kfyty.mvc.request.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求映射路径
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
    /**
     * 请求映射路径
     * 默认为方法名称
     */
    String value() default "";

    /**
     * 请求方法
     *
     * @return 默认 GET 方法
     */
    RequestMethod requestMethod() default RequestMethod.GET;

    /**
     * 设置响应的 content-type
     *
     * @return content-type
     */
    String produces() default "text/plain; charset=utf-8";
}
