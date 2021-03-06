package com.kfyty.mvc.annotation;

import com.kfyty.mvc.request.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping(value = "", requestMethod = RequestMethod.GET)
public @interface GetMapping {
    String value();
}
