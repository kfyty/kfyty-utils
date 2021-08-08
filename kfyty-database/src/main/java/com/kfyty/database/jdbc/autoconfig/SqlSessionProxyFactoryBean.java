package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.session.Configuration;
import com.kfyty.database.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.utils.BeanUtil;
import lombok.Data;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;

/**
 * 描述: SqlSessionProxyFactoryBean
 *
 * @author kfyty725
 * @date 2021/8/8 11:07
 * @email kfyty725@hotmail.com
 */
@Data
public class SqlSessionProxyFactoryBean implements FactoryBean<SqlSessionProxyFactory>, InitializingBean {
    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private List<Interceptor> interceptors;

    @Override
    public Class<?> getBeanType() {
        return SqlSessionProxyFactory.class;
    }

    @Override
    public SqlSessionProxyFactory getObject() {
        Configuration configuration = new Configuration()
                .setDataSource(this.getDataSource())
                .setInterceptors(this.getInterceptors());
        return new SqlSessionProxyFactory(configuration);
    }

    @Override
    public void afterPropertiesSet() {
        if (this.dataSource == null) {
            throw new IllegalStateException("data source can't null");
        }
        if (this.interceptors != null) {
            interceptors.sort(Comparator.comparing(BeanUtil::getBeanOrder));
        }
    }
}
