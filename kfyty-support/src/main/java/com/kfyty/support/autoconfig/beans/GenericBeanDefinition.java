package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.generic.ActualGeneric;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import com.kfyty.support.utils.ScopeUtil;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 描述: 简单的通用 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 10:29
 * @email kfyty725@hotmail.com
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class GenericBeanDefinition implements BeanDefinition {
    /**
     * 该 bean 注册的名称
     */
    protected String beanName;

    /**
     * 该 bean 注册的类型
     */
    protected Class<?> beanType;

    /**
     * bean 作用域
     */
    protected String scope;

    /**
     * 是否是自动装配的候选者
     */
    protected boolean isAutowireCandidate;

    /**
     * 构造器
     */
    protected Constructor<?> constructor;

    /**
     * 默认构造器参数
     */
    protected Map<Class<?>, Object> defaultConstructorArgs;

    /**
     * 自动注入处理器，所有实例共享，以处理循环依赖
     */
    protected static AutowiredProcessor autowiredProcessor = null;

    public GenericBeanDefinition() {
        this.setAutowireCandidate(true);
    }

    public GenericBeanDefinition(Class<?> beanType) {
        this(BeanUtil.getBeanName(beanType), beanType);
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType) {
        this(beanName, beanType, ScopeUtil.resolveScope(beanType).value());
    }

    public GenericBeanDefinition(String beanName, Class<?> beanType, String scope) {
        this();
        this.setBeanName(beanName);
        this.setBeanType(beanType);
        this.setScope(scope);
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = Objects.requireNonNull(beanName);
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanType;
    }

    @Override
    public void setBeanType(Class<?> beanType) {
        this.beanType = Objects.requireNonNull(beanType);
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = Objects.requireNonNull(scope);
    }

    @Override
    public boolean isSingleton() {
        return this.getScope().equals(SCOPE_SINGLETON);
    }

    @Override
    public boolean isAutowireCandidate() {
        return this.isAutowireCandidate;
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.isAutowireCandidate = autowireCandidate;
    }

    @Override
    public BeanDefinition addConstructorArgs(Class<?> argType, Object arg) {
        if(this.defaultConstructorArgs == null) {
            this.defaultConstructorArgs = new LinkedHashMap<>(4);
        }
        this.defaultConstructorArgs.put(argType, arg);
        return this;
    }

    @Override
    public Map<Class<?>, Object> getConstructArgs() {
        return this.prepareConstructorArgs();
    }

    @Override
    public Class<?>[] getConstructArgTypes() {
        this.ensureConstructor();
        return this.constructor.getParameterTypes();
    }

    @Override
    public Object[] getConstructArgValues() {
        return this.getConstructArgs().values().toArray();
    }

    @Override
    public Object createInstance(ApplicationContext context) {
        if(context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        this.ensureAutowiredProcessor(context);
        Object bean = ReflectUtil.newInstance(this.beanType, this.getConstructArgs());
        if(log.isDebugEnabled()) {
            log.debug("instantiate bean: {} !", bean);
        }
        return bean;
    }

    protected void ensureConstructor() {
        if(this.constructor == null) {
            this.constructor = ReflectUtil.searchSuitableConstructor(this.beanType, e -> AnnotationUtil.hasAnnotation(e, Autowired.class));
        }
    }

    protected void ensureAutowiredProcessor(ApplicationContext context) {
        if(autowiredProcessor == null || autowiredProcessor.getContext() != context) {
            autowiredProcessor = new AutowiredProcessor(context);
        }
    }

    protected Map<Class<?>, Object> prepareConstructorArgs() {
        this.ensureConstructor();
        if(this.constructor.getParameterCount() == 0) {
            return Collections.emptyMap();
        }
        Parameter[] parameters = this.constructor.getParameters();
        Map<Class<?>, Object> constructorArgs = Optional.ofNullable(this.defaultConstructorArgs).map(LinkedHashMap::new).orElse(new LinkedHashMap<>(4));
        for (int i = constructorArgs.size(); i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Autowired autowired = AnnotationUtil.findAnnotation(parameter, Autowired.class);
            Object resolveBean = autowiredProcessor.doResolveBean(BeanUtil.getBeanName(parameter), ActualGeneric.from(this.beanType, parameter), autowired != null ? autowired : AnnotationUtil.findAnnotation(this.constructor, Autowired.class));
            constructorArgs.put(parameter.getType(), resolveBean);
        }
        return constructorArgs;
    }
}
