package com.mentalhealth.config;

import com.mentalhealth.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器
 * 作用：拦截需要登录的请求，验证 token 是否有效
 * 流程：请求头取token → 解析用户ID → 存到request属性 → 放行
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从请求头获取 token
        String authHeader = request.getHeader("Authorization");

        // 2. 检查 token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "未登录，请先登录");
            return false;
        }

        // 3. 去掉 "Bearer " 前缀获取真正的 token
        String token = authHeader.substring(7);

        // 4. 验证 token 是否有效
        if (!jwtUtil.validateToken(token)) {
            sendError(response, "登录已过期，请重新登录");
            return false;
        }

        // 5. 解析用户信息，存到 request 中，方便后续使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        return true;
    }

    /** 返回 401 错误给前端 */
    private void sendError(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
