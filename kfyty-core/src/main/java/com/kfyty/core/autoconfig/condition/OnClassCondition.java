package com.kfyty.core.autoconfig.condition;

import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.core.support.AnnotationMetadata;

import static com.kfyty.core.utils.ReflectUtil.isPresent;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/17 11:43
 * @email kfyty725@hotmail.com
 */
public class OnClassCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        for (String conditionName : this.conditionNames(metadata)) {
            if (!isPresent(conditionName)) {
                return false;
            }
        }
        return true;
    }

    protected String[] conditionNames(AnnotationMetadata<?> metadata) {
        return ((ConditionalOnClass) metadata.get()).value();
    }
}
