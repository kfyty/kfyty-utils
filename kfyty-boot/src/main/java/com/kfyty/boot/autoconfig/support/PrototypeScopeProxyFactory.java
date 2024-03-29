package com.kfyty.boot.autoconfig.support;

import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.scope.ScopeProxyFactory;

/**
 * 描述: 原型代理工厂
 *
 * @author kfyty725
 * @date 2022/10/22 10:17
 * @email kfyty725@hotmail.com
 */
public class PrototypeScopeProxyFactory implements ScopeProxyFactory {

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return beanFactory.registerBean(beanDefinition);
    }
}
