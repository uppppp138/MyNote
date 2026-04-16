package com.mynote.config;

import com.mynote.interceptors.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    private final UserInfoInterceptor userInfoInterceptor;//注入拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //默认拦截所有的路径
        registry.addInterceptor(userInfoInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/doc.html",
                        "/doc.html/**",
                        "/v3/api-docs",      // OpenAPI3 是 v3
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/note-share/noValid/**"//通过分享链接访问笔记不进行拦截
                );
    }
}
