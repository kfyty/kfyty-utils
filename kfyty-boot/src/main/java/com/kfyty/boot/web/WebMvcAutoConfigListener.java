package com.kfyty.boot.web;

import com.kfyty.boot.K;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.SneakyThrows;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 描述: mvc 监听器，用于从 tomcat 中启动自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:52
 * @email kfyty725@hotmail.com
 */
@ComponentScan
@EnableAutoConfiguration
public class WebMvcAutoConfigListener implements ServletContextListener {
    private static final String BASE_PACKAGE_PARAM_NAME = "basePackage";

    private ApplicationContext applicationContext;

    @Override
    @SneakyThrows
    public void contextInitialized(ServletContextEvent sce) {
        if(this.applicationContext == null) {
            synchronized (this) {
                if(this.applicationContext == null) {
                    String basePackage = sce.getServletContext().getInitParameter(BASE_PACKAGE_PARAM_NAME);
                    ComponentScan annotation = AnnotationUtil.findAnnotation(this, ComponentScan.class);
                    ReflectUtil.setAnnotationValue(annotation, "value", new String[] {basePackage});
                    this.applicationContext = K.run(WebMvcAutoConfigListener.class);
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
