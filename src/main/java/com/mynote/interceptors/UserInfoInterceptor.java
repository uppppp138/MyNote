package com.mynote.interceptors;

import com.mynote.common.BusinessException;
import com.mynote.util.JwtUtil;
import com.mynote.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@RequiredArgsConstructor
@Component
public class UserInfoInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.从请求头中获取token
        String token = request.getHeader("authorization");
        // 2. 判断token是否存在
        if (token == null) {
            response.setStatus(401);
            response.getWriter().write("未登录，请先登录");
            return false;  //没有token，不放行
        }
        // 3. 解析token（处理异常情况）
        try {
            Long userId = jwtUtil.parseToken(token);
            UserContext.setUser(userId);
            return true;  // 解析成功，放行
        } catch (Exception e) {
            // token过期或无效
            throw new BusinessException(404, "token无效或已过期");
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清理用户
        UserContext.removeUser();
    }
}
