package com.kfyty.boot.proxy;

import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.MethodProxy;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 延迟初始化代理
 *
 * @author kfyty725
 * @date 2021/7/11 12:30
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class LazyProxyInterceptorProxy implements MethodInterceptorChainPoint {
    private final String beanName;
    private final BeanFactory beanFactory;

    @Override
    public Object proceed(MethodProxy methodProxy, MethodInterceptorChain chain) throws Throwable {
        String requiredBeanName = BeanMethodInterceptorProxy.getCurrentRequiredBeanName();
        try {
            BeanMethodInterceptorProxy.setCurrentRequiredBeanName(this.beanName);
            methodProxy.setTarget(this.beanFactory.getBean(this.beanName));
            return chain.proceed(methodProxy);
        } finally {
            BeanMethodInterceptorProxy.setCurrentRequiredBeanName(requiredBeanName);
        }
    }
}
