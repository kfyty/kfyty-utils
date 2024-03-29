package com.kfyty.mvc.annotation;

import com.kfyty.mvc.annotation.bind.ResponseBody;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个控制器
 *
 * @see Controller
 * @see ResponseBody
 */
@Controller
@Documented
@ResponseBody
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
    String value() default "";
}
