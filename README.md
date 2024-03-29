# kfyty-framework
    造轮子~~ 注解式 ioc/aop、自动装配、异步事件、动态代理、注解式 mvc（支持 Restful）、嵌入式 tomcat、
    注解式 jdbc 框架、javafx mvvm 框架，并基于此开发了一套支持 java/enjoy/freemarker/jsp 模板的代码生成器。

## kfyty-core
项目核心，包含一些通用工具类、jar index 启动引导、泛型推断工具、jdbc 访问工具、bean 封装工具、包读取工具、json 转换工具以及自动配置接口、自动配置注解等

## kfyty-database
数据库访问工具，基于代理实现了接口式 jdbc 访问，支持注解编写 SQL 或者基于模板引擎(如：enjoy/freemarker) 的动态 SQL；
内置代码生成器和默认生成模板，支持 java 编程模板、enjoy/freemarker 模板以及 jsp 模板（需要 k-jte 支持），或者自定义模板引擎。

## kfyty-mvc
注解式 mvc，支持嵌入式 tomcat、复杂参数自动转换绑定、 restful/ant 风格路径匹配、全局异常处理、请求拦截器等。

## kfyty-aop
AOP 模块，支持 ant 路径匹配、支持注解类型匹配、集成 AspectJ，可以单独使用，也可以集成到 kfyty-boot 自动配置。

## kfyty-sdk
用于快速封装 sdk，只需编写请求及响应模型，支持拦截器，易于拓展，支持命令式、响应式风格

## kfyty-javafx
javafx mvvm 框架，实现了视图和数据模型的双向绑定。代码中只需操作数据即可反应到视图上，视图上编辑数据即可反应到模型里。详见 javafx 模块 README.md.

## kfyty-boot
注解式 ioc、自动装配、作用域代理(单例/原型/刷新)、懒加载代理、配置文件属性自动绑定(支持嵌套的复杂类型绑定)、条件注解、jsr 条件注解校验器、异步事件、动态代理、spi、自定义 jar index 类加载器 等。
##### 集成独立 spring-tx 事务管理器、quartz、xxl-job、redisson、druid、HikariCP、tomcat-jdbc、jakarta.validation、百度 uid 生成器的自动配置 starter。

```xml
<dependency>
    <groupId>com.kfyty</groupId>
    <artifactId>kfyty-boot</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
```java
package com.kfyty.demo;

import com.kfyty.boot.K;
import com.kfyty.boot.validator.annotation.Condition;
import com.kfyty.core.autoconfig.annotation.Async;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.mvc.annotation.GetMapping;
import com.kfyty.mvc.autoconfig.annotation.EnableWebMvc;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableWebMvc
@BootApplication
public class Main {

    public static void main(String[] args) {
        K.run(Main.class, args);
    }

    @GetMapping
    public User hello(@Valid User user) {
        return user;
    }

    @Async
    @EventListener
    public void onStarted(ContextRefreshedEvent event) {
        log.info("started succeed !");
    }

    @Data
    public static class User {
        @Condition(when = "type == 1", then = "photo != null", message = "type=1时，图片不能为空")
        private Integer type;

        private String photo;
    }
}
```
