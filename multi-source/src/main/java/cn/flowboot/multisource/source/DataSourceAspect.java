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

    @Pointcut("@annotation(cn.flowboot.multisource.source.MyDataSource)"
            +"||@within(cn.flowboot.multisource.source.MyDataSource)")
    public void myDS(){};

    @Around("myDS()")
    public Object around(ProceedingJoinPoint point)throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();
        MyDataSource myDataSource = AnnotationUtils.findAnnotation(signature.getMethod(), MyDataSource.class);
        if(Objects.nonNull(myDataSource)){
            DynamicMultipleDataSourceContextHolder.setDataSourceName(myDataSource.dataSourceName());
        }
        try{
            return point.proceed();
        } finally {
            // 清空数据源
            DynamicMultipleDataSourceContextHolder.clearDataSourceName();
        }
    }
}