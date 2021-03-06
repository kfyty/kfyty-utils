package com.kfyty.database.generate.configuration;

import com.kfyty.database.generate.database.AbstractDataBaseMapper;
import com.kfyty.support.autoconfig.beans.FactoryBean;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/12 12:41
 * @email kfyty725@hotmail.com
 */
public class DataBaseMapperFactory implements FactoryBean<Class<AbstractDataBaseMapper>> {
    private final Class<AbstractDataBaseMapper> databaseClass;

    public DataBaseMapperFactory(Class<AbstractDataBaseMapper> databaseClass) {
        this.databaseClass = databaseClass;
    }

    @Override
    public Class<?> getBeanType() {
        return this.databaseClass;
    }

    @Override
    public Class<AbstractDataBaseMapper> getObject() {
        return this.databaseClass;
    }
}
