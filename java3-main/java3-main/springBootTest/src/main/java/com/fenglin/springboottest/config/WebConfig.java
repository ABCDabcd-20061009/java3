package com.fenglin.springboottest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * 注册 JWT 拦截器：拦截 /api/** 解析登录身份。
 * 跨域配置在 CorsConfig，两者都是 WebMvcConfigurer，Spring 会一起生效。
 * 另外注册 /media/** 静态媒体目录（广播剧视频/音频），该路径不经过 JWT 拦截器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/**");
    }

    /**
     * 媒体静态资源：/media/drama/xxx.mp4
     * 依次查找：
     *   1) 项目运行目录下的 ./media/  （放真实视频，无需重新打包）
     *   2) classpath:/static/media/    （随 jar 一起打包的默认示例视频）
     * Spring 的 ResourceHttpRequestHandler 原生支持 Range 请求，可正常拖动进度条。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:./media/", "classpath:/static/media/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
    }
}
