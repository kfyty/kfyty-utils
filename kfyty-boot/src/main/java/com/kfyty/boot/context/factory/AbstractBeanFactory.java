package com.kfyty.boot.context.factory;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.BeanFactoryAware;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.BeanDefinitionRegistry;
import com.kfyty.support.autoconfig.beans.BeanFactory;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.autoconfig.beans.InstantiatedBeanDefinition;
import com.kfyty.support.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.support.exception.BeansException;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 描述: bean 工厂基础实现
 *
 * @author kfyty725
 * @date 2021/7/3 10:36
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractBeanFactory implements ApplicationContextAware, BeanDefinitionRegistry, BeanFactory {
    /**
     * bean 定义
     */
    protected final Map<String, BeanDefinition> beanDefinitions;

    /**
     * bean 单例
     */
    protected final Map<String, Object> beanInstances;

    /**
     * bean 后置处理器
     */
    protected final List<BeanPostProcessor> beanPostProcessors;

    /**
     * 应用上下文
     */
    protected ApplicationContext applicationContext;

    public AbstractBeanFactory() {
        this.beanDefinitions = Collections.synchronizedMap(new LinkedHashMap<>());
        this.beanInstances = new ConcurrentHashMap<>();
        this.beanPostProcessors = new ArrayList<>();
    }

    public void registerBeanPostProcessors(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessors.add(beanPostProcessor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return beanPostProcessors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        if(this.containsBeanDefinition(beanDefinition.getBeanName())) {
            throw new BeansException("conflicting bean definition: " + beanDefinition.getBeanName());
        }
        this.beanDefinitions.putIfAbsent(beanDefinition.getBeanName(), beanDefinition);
        if(FactoryBean.class.isAssignableFrom(beanDefinition.getBeanType())) {
            this.registerBeanDefinition(GenericBeanDefinition.from(beanDefinition));
        }
        for (Method method : beanDefinition.getBeanType().getMethods()) {
            Bean beanAnnotation = AnnotationUtil.findAnnotation(method, Bean.class);
            if(beanAnnotation != null) {
                this.registerBeanDefinition(GenericBeanDefinition.from(beanDefinition, method, beanAnnotation));
            }
        }
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitions.containsKey(beanName);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return this.beanDefinitions;
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.getBeanDefinitions().get(beanName);
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions(Class<?> beanType) {
        return this.getBeanDefinitions().entrySet()
                .parallelStream()
                .filter(e -> beanType.isAssignableFrom(e.getValue().getBeanType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> {
                    throw new IllegalStateException("duplicate key " + k2);
                }, LinkedHashMap::new));
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName, Class<?> beanType) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(beanType);
        return beanDefinitions.size() == 1 ? beanDefinitions.values().iterator().next() : beanDefinitions.get(beanName);
    }

    @Override
    public boolean contains(String name) {
        return this.beanInstances.containsKey(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Map<String, BeanDefinition> beanDefinitions = this.getBeanDefinitions(clazz);
        if(beanDefinitions.size() > 1) {
            throw new BeansException("more than one instance of type: " + clazz.getName());
        }
        return beanDefinitions.isEmpty() ? null : this.getBean(beanDefinitions.values().iterator().next().getBeanName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        if(this.contains(name)) {
            return (T) this.beanInstances.get(name);
        }
        return (T) this.registerBean(this.getBeanDefinition(name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanOfType(Class<T> clazz) {
        Map<String, Object> beans = new LinkedHashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions(clazz).values()) {
            beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
        }
        return (Map<String, T>) beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeanWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<String, Object> beans = new LinkedHashMap<>(2);
        for (BeanDefinition beanDefinition : this.getBeanDefinitions().values()) {
            if(AnnotationUtil.hasAnnotationElement(beanDefinition.getBeanType(), annotationClass)) {
                beans.put(beanDefinition.getBeanName(), this.registerBean(beanDefinition));
            }
        }
        return (Map<String, T>) beans;
    }

    /**
     * 根据 BeanDefinition 注册一个 bean
     * 由于创建 bean 实例的过程中可能会触发代理，因此需对返回的 bean 实例做二次判断
     * @param beanDefinition BeanDefinition
     * @return bean 实例
     */
    @Override
    public Object registerBean(BeanDefinition beanDefinition) {
        if(this.contains(beanDefinition.getBeanName())) {
            return this.getBean(beanDefinition.getBeanName());
        }
        Object bean = this.doCreateBean(beanDefinition);
        if(!this.contains(beanDefinition.getBeanName())) {
            return this.registerBean(beanDefinition.getBeanName(), bean);
        }
        return this.getBean(beanDefinition.getBeanName());
    }

    @Override
    public Object registerBean(Class<?> clazz, Object bean) {
        return this.registerBean(BeanUtil.convert2BeanName(clazz), bean);
    }

    @Override
    public Object registerBean(String name, Object bean) {
        synchronized (this) {
            if(this.contains(name)) {
                throw new BeansException("conflicting bean name: " + name);
            }
            if(!this.containsBeanDefinition(name)) {
                this.registerBeanDefinition(InstantiatedBeanDefinition.from(name, bean.getClass()));
            }
            BeanDefinition beanDefinition = this.getBeanDefinition(name);
            if(beanDefinition.isSingleton()) {
                this.beanInstances.put(name, bean);
            }
            this.invokeAwareMethod(name, bean);
            bean = this.invokeBeanPostProcessAfterInstantiation(name, getExposedBean(beanDefinition, bean));
            this.doAutowiredBean(name, getExposedBean(beanDefinition, bean));
            bean = this.invokeLifecycleMethod(name, getExposedBean(beanDefinition, bean));
            return bean;
        }
    }

    @Override
    public void replaceBean(Class<?> clazz, Object bean) {
        this.replaceBean(BeanUtil.convert2BeanName(clazz), bean);
    }

    @Override
    public void replaceBean(String name, Object bean) {
        if(bean != null && this.contains(name)) {
            this.beanInstances.put(name, bean);
        }
    }

    @Override
    public void forEach(BiConsumer<String, Object> bean) {
        for (Map.Entry<String, Object> entry : this.beanInstances.entrySet()) {
            bean.accept(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 创建 bean 实例
     * @param beanDefinition bean 定义
     * @return bean
     */
    public abstract Object doCreateBean(BeanDefinition beanDefinition);

    /**
     * 对 bean 执行依赖注入
     * @param beanName bean 名称
     * @param bean bean 实例
     */
    public abstract void doAutowiredBean(String beanName, Object bean);

    protected Object getExposedBean(BeanDefinition beanDefinition, Object bean) {
        return beanDefinition.isSingleton() ? this.getBean(beanDefinition.getBeanName()) : bean;
    }

    private void invokeAwareMethod(String beanName, Object bean) {
        if(bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        if(bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
        }
    }

    private Object invokeBeanPostProcessAfterInstantiation(String beanName, Object bean) {
        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            if(beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                Object newBean = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessAfterInstantiation(bean, beanName);
                if(newBean != null && newBean != bean) {
                    bean = newBean;
                    this.replaceBean(beanName, newBean);
                }
            }
        }
        return bean;
    }

    private Object invokeLifecycleMethod(String beanName, Object bean) {
        if(bean instanceof ApplicationContext) {
            return bean;
        }
        bean = this.initializingBean(beanName, bean);
        return bean;
    }

    private Object initializingBean(String beanName, Object bean) {
        BeanDefinition beanDefinition = this.getBeanDefinition(beanName);

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            if(newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        if(bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        bean = getExposedBean(beanDefinition, bean);

        if(beanDefinition instanceof MethodBeanDefinition) {
            final Object finalBean = bean;
            MethodBeanDefinition methodBeanDefinition = (MethodBeanDefinition) beanDefinition;
            Optional.ofNullable(methodBeanDefinition.getInitMethod(this.applicationContext)).ifPresent(e -> ReflectUtil.invokeMethod(finalBean, e));
        }

        bean = getExposedBean(beanDefinition, bean);

        for (BeanPostProcessor beanPostProcessor : this.getBeanPostProcessors()) {
            Object newBean = beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            if(newBean != null && newBean != bean) {
                bean = newBean;
                this.replaceBean(beanName, newBean);
            }
        }

        return bean;
    }
}
