package com.kfyty.boot.data.jdbc.autoconfig;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;

/**
 * 描述: 数据源自动配置类
 *
 * @author kfyty725
 * @date 2022/6/1 10:58
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.datasource")
@ConditionalOnProperty(prefix = "k.datasource", value = "type", matchIfNonNull = true)
public class DataSourceProperties {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * jdbc 连接 url
     */
    private String url;

    /**
     * 驱动类全限定名
     */
    private String driverClassName;
}
