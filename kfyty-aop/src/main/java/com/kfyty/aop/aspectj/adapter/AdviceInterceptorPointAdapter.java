package com.kfyty.aop.aspectj.adapter;

import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import org.aopalliance.aop.Advice;

/**
 * 描述: 通知适配器
 *
 * @author kfyty725
 * @date 2022/3/20 16:47
 * @email kfyty725@hotmail.com
 */
public interface AdviceInterceptorPointAdapter {
    /**
     * 将 {@link Advice} 适配为 {@link MethodInterceptorChainPoint}
     * 返回 null 时将尝试下一个适配器
     *
     * @param advice 通知
     * @return 代理拦截点
     */
    MethodInterceptorChainPoint adapt(Advice advice);
}
