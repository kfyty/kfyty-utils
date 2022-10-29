package com.kfyty.condition;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.event.ContextRefreshedEvent;
import org.junit.Assert;

import java.util.List;

/**
 * 描述: 条件注解测试
 *
 * @author kfyty725
 * @date 2022/4/23 11:06
 * @email kfyty725@hotmail.com
 */
@Component
public class ConditionTest {
    private boolean isOverride;

    @Autowired(required = false)
    private List<Inter> cons;

    @Bean
    public BB bbOverride() {
        this.isOverride = true;
        return new BB();
    }

    @EventListener
    public void onComplete(ContextRefreshedEvent event) {
        Assert.assertSame(this.isOverride, true);
        Assert.assertSame(this.cons.size(), 5);
    }
}

interface Inter {}

@Component
@ConditionalOnBean({CC.class, BB.class})
@ConditionalOnClass("com.kfyty.condition.ConditionTest")
class AA implements Inter {}

@Component
@ConditionalOnMissingBean(BB.class)
class BB implements Inter {}

@Component
@ConditionalOnBean(BB.class)
class CC implements Inter {

    @Bean
    @ConditionalOnBean(AA.class)
    public EE ee() {
        return new EE();
    }
}

class DD implements Inter {}

@Component
@ConditionalOnMissingBean
class DDF implements FactoryBean<DD> {

    @Override
    public Class<?> getBeanType() {
        return DD.class;
    }

    @Override
    public DD getObject() {
        return new DD();
    }
}

class EE implements Inter {}
