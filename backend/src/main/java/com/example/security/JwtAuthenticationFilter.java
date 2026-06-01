package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.example.entity.SysUser;
import com.example.service.SysUserService;
import com.example.service.impl.UserDetailServiceImpl;
import com.example.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserDetailServiceImpl userDetailService;

    @Autowired
    SysUserService sysUserService;

    // 白名单路径（与SecurityConfig保持一致）
    private static final String[] WHITE_LIST = {
            "/login",
            "/logout",
            "/captcha",
            "/favicon.ico"
    };

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        // 1. 检查是否是白名单路径
        String requestURI = request.getRequestURI();
        if (isWhiteList(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 获取token
        String jwt = request.getHeader(jwtUtils.getHeader());

        if (StrUtil.isBlankOrUndefined(jwt)) {
            // 对于非白名单路径但没有token的情况，交给异常处理器处理
            chain.doFilter(request, response);
            return;
        }

        try {
            // 3. 解析和验证token
            Claims claim = jwtUtils.getClaimByToken(jwt);
            if (claim == null) {
                throw new JwtException("token 异常");
            }

            if (jwtUtils.isTokenExpired(claim)) {
                throw new JwtException("token 已过期");
            }

            // 4. 设置认证信息
            String username = claim.getSubject();
            SysUser sysUser = sysUserService.getByUsername(username);

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            userDetailService.getUserAuthority(sysUser.getId())
                    );
            SecurityContextHolder.getContext().setAuthentication(token);

        } catch (JwtException e) {
            // token异常，清除安全上下文，然后继续过滤器链
            // 异常将由JwtAuthenticationEntryPoint处理
            SecurityContextHolder.clearContext();
        }

        // 5. 继续过滤器链
        chain.doFilter(request, response);
    }

    /**
     * 判断请求是否在白名单中
     */
    private boolean isWhiteList(String requestURI) {
        for (String whitePath : WHITE_LIST) {
            if (requestURI.equals(whitePath) ||
                    requestURI.startsWith(whitePath.replace("/*", ""))) {
                return true;
            }
        }
        return false;
    }
}