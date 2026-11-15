package main.java.com.blog.config;

import com.blog.interceptor.AntiCrawlerInterceptor;
import com.blog.interceptor.JwtAuthInterceptor;
import com.blog.interceptor.RateLimitInterceptor;
import com.blog.interceptor.SessionAuthInterceptor;
import com.blog.interceptor.VisitLogInterceptor;
import com.blog.service.VisitLogService;
import com.blog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JwtUtil jwtUtil;
    private final VisitLogService visitLogService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final SessionAuthInterceptor sessionAuthInterceptor;

    @Value("${blog.upload.path}")
    private String uploadPath;

    public WebConfig(JwtUtil jwtUtil,
                     VisitLogService visitLogService,
                     org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
                     SessionAuthInterceptor sessionAuthInterceptor) {
        this.jwtUtil = jwtUtil;
        this.visitLogService = visitLogService;
        this.redisTemplate = redisTemplate;
        this.sessionAuthInterceptor = sessionAuthInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AntiCrawlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/articles/*/og", "/sitemap.xml", "/robots.txt");

        registry.addInterceptor(new RateLimitInterceptor(redisTemplate))
                .addPathPatterns("/**")
                .excludePathPatterns("/sitemap.xml", "/robots.txt");

        registry.addInterceptor(new JwtAuthInterceptor(jwtUtil))
                .addPathPatterns("/admin/**", "/series/admin", "/series/admin/**")
                .excludePathPatterns("/admin/login");

        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/user/**", "/comments", "/interview-comments",
                        "/forum/posts", "/forum/posts/**", "/forum/replies", "/forum/replies/**");

        registry.addInterceptor(new VisitLogInterceptor(visitLogService))
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                ObjectMapper mapper = jacksonConverter.getObjectMapper();
                JavaTimeModule module = new JavaTimeModule();
                module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(FORMATTER));
                module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(FORMATTER));
                module.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
                module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
                mapper.registerModule(module);
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            }
        }
    }
}
