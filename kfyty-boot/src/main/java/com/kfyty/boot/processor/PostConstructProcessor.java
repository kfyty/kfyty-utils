package com.kfyty.boot.processor;

import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.ReflectUtil;
import jakarta.annotation.PostConstruct;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/7 17:14
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MAX_VALUE)
public class PostConstructProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> sourceClass = AopUtil.getTargetClass(bean);
        for (Method method : ReflectUtil.getMethods(sourceClass)) {
            if(AnnotationUtil.hasAnnotation(method, PostConstruct.class)) {
                ReflectUtil.invokeMethod(bean, method);
            }
        }
        return null;
    }
}
