package com.kfyty.aop.processor;

import com.kfyty.aop.Advisor;
import com.kfyty.aop.MethodMatcher;
import com.kfyty.aop.PointcutAdvisor;
import com.kfyty.aop.aspectj.adapter.AdviceInterceptorPointAdapter;
import com.kfyty.aop.aspectj.creator.AdvisorCreator;
import com.kfyty.aop.proxy.AspectMethodInterceptorProxy;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.proxy.AbstractProxyCreatorProcessor;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 描述: 切面处理器
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class AspectJBeanPostProcessor extends AbstractProxyCreatorProcessor {
    /**
     * 是否已解析切面配置
     */
    private boolean hasResolveAspect;

    /**
     * advisor 创建器
     */
    @Autowired
    private AdvisorCreator advisorCreator;

    /**
     * 切面
     */
    @Autowired(required = false)
    private List<Advisor> aspectAdvisor;

    /**
     * 通知适配器
     */
    @Autowired(required = false)
    private List<AdviceInterceptorPointAdapter> adviceInterceptorPointAdapters;

    @Override
    public Object postProcessAfterInstantiation(Object bean, String beanName) {
        Class<?> beanClass = this.getBeanDefinition(beanName).getBeanType();
        List<Advisor> advisors = this.findAvailableAdvisor(beanClass);
        if (CommonUtil.empty(advisors)) {
            return null;
        }
        return this.createProxy(bean, beanName, new AspectMethodInterceptorProxy(advisors, this.adviceInterceptorPointAdapters));
    }

    /**
     * 查找该 bean class 中是否有方法适合 Advisor
     * 查找到存在一个方法适合即可返回，在代理中再搜索全部
     *
     * @param beanClass bean class
     * @return Advisor
     */
    protected List<Advisor> findAvailableAdvisor(Class<?> beanClass) {
        this.prepareAspectJAnnotationAdvisor();
        List<Advisor> advisors = new ArrayList<>();
        List<Method> methods = ReflectUtil.getMethods(beanClass);
        for (Advisor advisor : this.aspectAdvisor) {
            if (advisor instanceof PointcutAdvisor) {
                MethodMatcher methodMatcher = ((PointcutAdvisor) advisor).getPointcut().getMethodMatcher();
                for (Method targetMethod : methods) {
                    if (methodMatcher.matches(targetMethod, beanClass)) {
                        advisors.add(advisor);
                        break;
                    }
                }
            }
        }
        return advisors;
    }

    /**
     * 解析 @Aspect 注解的 Bean 为 Advisor
     */
    protected void prepareAspectJAnnotationAdvisor() {
        if (this.hasResolveAspect) {
            return;
        }
        this.hasResolveAspect = true;
        Collection<BeanDefinition> aspectJAnnotationBeanDefinition = this.applicationContext.getBeanDefinitionWithAnnotation(Aspect.class).values();
        for (BeanDefinition beanDefinition : aspectJAnnotationBeanDefinition) {
            Pair<String, Class<?>> namesAspectClass = new Pair<>(beanDefinition.getBeanName(), beanDefinition.getBeanType());
            List<Advisor> advisors = this.advisorCreator.createAdvisor(e -> this.applicationContext.getBean(e.getAspectName()), namesAspectClass);
            this.aspectAdvisor.addAll(advisors);
        }
    }
}
