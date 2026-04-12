package com.mynote.config;

import com.mynote.interceptors.UserInfoInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
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
                        "/user/login",      // 登录不拦截
                        "/user/register",   // 注册不拦截
                        "/swagger-ui/**",   // Swagger UI 静态资源
                        "/v3/api-docs/**",  // Swagger API 文档
                        "/doc.html"         // 如果用 Knife4j
                );
    }
}
