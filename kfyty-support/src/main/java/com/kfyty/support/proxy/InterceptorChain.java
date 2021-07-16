package com.kfyty.support.proxy;

import com.kfyty.support.utils.BeanUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 描述: 方法拦截链
 *
 * @author kfyty725
 * @date 2021/6/19 11:10
 * @email kfyty725@hotmail.com
 */
public class InterceptorChain extends MethodInvocationInterceptor {
    private int currentChainIndex;
    private MethodProxyWrapper intercepting;
    private final List<InterceptorChainPoint> chainPoints;
    private final ThreadLocal<InterceptorChain> threadInterceptorChain;

    public InterceptorChain(Object source) {
        super(source);
        this.currentChainIndex = -1;
        this.chainPoints = new ArrayList<>(4);
        this.threadInterceptorChain = new ThreadLocal<>();
    }

    public InterceptorChain(Object source, List<InterceptorChainPoint> chainPoints) {
        this(source);
        this.chainPoints.addAll(chainPoints);
        this.sortInterceptorChain();
    }

    public InterceptorChain addInterceptorPoint(InterceptorChainPoint chainPoint) {
        this.chainPoints.add(chainPoint);
        this.sortInterceptorChain();
        return this;
    }

    public void sortInterceptorChain() {
        this.chainPoints.sort(Comparator.comparing(BeanUtil::getBeanOrder));
    }

    @Override
    protected Object process(MethodProxyWrapper methodProxy) throws Throwable {
        InterceptorChain threadChain = this.threadInterceptorChain.get();
        if (threadChain != null && threadChain.intercepting.equals(methodProxy)) {
            return threadChain.proceed(methodProxy);
        }
        try {
            threadChain = new InterceptorChain(this.getSource(), this.chainPoints);
            threadChain.intercepting = methodProxy;
            this.threadInterceptorChain.set(threadChain);
            return threadChain.proceed(methodProxy);
        } finally {
            this.threadInterceptorChain.remove();
        }
    }

    public Object proceed(MethodProxyWrapper methodProxy) throws Throwable {
        if(++this.currentChainIndex == this.chainPoints.size()) {
            this.currentChainIndex = -1;
            return methodProxy.invoke();
        }
        return this.chainPoints.get(this.currentChainIndex).proceed(methodProxy, this);
    }
}
