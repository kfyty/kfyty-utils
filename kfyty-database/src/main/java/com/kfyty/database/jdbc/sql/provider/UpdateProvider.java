package com.kfyty.database.jdbc.sql.provider;

import com.kfyty.core.lang.Value;
import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.core.method.MethodParameter;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 描述: 更新 sql
 *
 * @author kfyty725
 * @date 2021/6/2 16:30
 * @email kfyty725@hotmail.com
 */
public interface UpdateProvider {

    String updateByPk(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params);

    String updateBatch(Class<?> mapperClass, Method sourceMethod, Value<Execute> annotation, Map<String, MethodParameter> params);
}
