package com.mynote.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解
 */
@Target(ElementType.METHOD)//方法注解
@Retention(RetentionPolicy.RUNTIME)//运行时触发器
@Documented
public @interface Log {
    /**
     * 操作描述
     */
    String value() default "";
}
