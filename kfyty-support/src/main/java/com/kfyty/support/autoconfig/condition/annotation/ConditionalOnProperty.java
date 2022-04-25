package com.kfyty.support.autoconfig.condition.annotation;

import com.kfyty.support.autoconfig.condition.OnPropertyCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 配置属性条件
 *
 * @author kfyty725
 * @date 2022/4/24 19:36
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnPropertyCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnProperty {
    /**
     * 属性 key
     *
     * @return key
     */
    String value();

    /**
     * 属性前缀
     *
     * @return prefix
     */
    String prefix() default "";

    /**
     * 期望属性值
     *
     * @return 期望值
     */
    String havingValue() default "";

    /**
     * 配置中不存在属性 key 时，条件是否成立
     *
     * @return 默认 false
     */
    boolean matchIfMissing() default false;
}