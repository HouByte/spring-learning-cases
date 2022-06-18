package cn.flowboot.multisource.source;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <h1>切面实现切换数据源</h1>
 *
 * @author Vincent Vic
 * @version 1.0
 */
@Order(1)
@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(cn.flowboot.multisource.source.MultipleDataSource)"
            +"||@within(cn.flowboot.multisource.source.MultipleDataSource)")
    public void myDS(){};

    @Around("myDS()")
    public Object around(ProceedingJoinPoint point)throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        MultipleDataSource multipleDataSource = AnnotationUtils.findAnnotation(signature.getMethod(), MultipleDataSource.class);
        if(Objects.nonNull(multipleDataSource)){
            DynamicMultipleDataSourceContextHolder.setDataSourceName(multipleDataSource.dataSourceName());
        }
        try{
            return point.proceed();
        } finally {
            // 清空数据源
            DynamicMultipleDataSourceContextHolder.clearDataSourceName();
        }
    }
}