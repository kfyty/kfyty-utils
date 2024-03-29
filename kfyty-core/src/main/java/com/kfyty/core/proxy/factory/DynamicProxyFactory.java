package com.kfyty.core.proxy.factory;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.core.utils.ReflectUtil.hasAnyInterfaces;

/**
 * 描述: 动态代理工厂，用于创建代理对象
 *
 * @author kfyty725
 * @date 2021/6/19 11:50
 * @email kfyty725@hotmail.com
 */
@NoArgsConstructor
public abstract class DynamicProxyFactory {
    protected List<MethodInterceptorChainPoint> points;

    public static DynamicProxyFactory create(Object bean, ApplicationContext context) {
        if (AopUtil.isJdkProxy(bean)) {
            return create();
        }
        if (!hasAnyInterfaces(bean.getClass()) || AnnotationUtil.hasAnnotationElement(bean, Configuration.class)) {
            return create(true);
        }
        BootApplication annotation = AnnotationUtil.findAnnotation(context.getPrimarySource(), BootApplication.class);
        return annotation == null ? create() : create(annotation.proxyTargetClass());
    }

    public static DynamicProxyFactory create() {
        return create(false);
    }

    public static DynamicProxyFactory create(boolean proxyTargetClass) {
        return !proxyTargetClass ? new JdkDynamicProxyFactory() : new CglibDynamicProxyFactory();
    }

    public DynamicProxyFactory addInterceptorPoint(MethodInterceptorChainPoint point) {
        if (this.points == null) {
            this.points = new LinkedList<>();
        }
        this.points.add(Objects.requireNonNull(point));
        return this;
    }

    public <T> T createProxy(T source) {
        return createProxy(source, CommonUtil.EMPTY_CLASS_ARRAY, CommonUtil.EMPTY_OBJECT_ARRAY);
    }

    public <T> T createProxy(Class<T> targetClass) {
        return createProxy(targetClass, CommonUtil.EMPTY_CLASS_ARRAY, CommonUtil.EMPTY_OBJECT_ARRAY);
    }

    public <T> T createProxy(T source, Class<?>[] argTypes, Object[] argValues) {
        //noinspection unchecked
        return createProxy(source, (Class<T>) source.getClass(), argTypes, argValues);
    }

    public <T> T createProxy(Class<T> targetClass, Class<?>[] argTypes, Object[] argValues) {
        return createProxy(null, targetClass, argTypes, argValues);
    }

    public abstract <T> T createProxy(T source, BeanDefinition beanDefinition);

    public abstract <T> T createProxy(T source, Class<T> targetClass, Class<?>[] argTypes, Object[] argValues);
}
