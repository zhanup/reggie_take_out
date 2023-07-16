package com.itheima.reggie.filter;

import com.alibaba.fastjson2.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经dengl
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获取本次请求的URI
        String requestURI = request.getRequestURI();
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",  // 移动端发送短信
                "/user/login",  // 移动端登录
        };

        // 2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        // 3.如果不需要出来，直接放行
        if (check) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4.1.判断登录状态，已登录，直接放行
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }

        // 4.2.判断登录状态，已登录，直接放行
        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }

        // 5.如果未登录返回未登录结果
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls this url array
     * @param requestURI the request URI
     * @return boolean
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url: urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
