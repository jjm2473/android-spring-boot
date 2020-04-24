package com.example.myapplication;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfigurationEmbeddedTomcat;
import org.springframework.context.annotation.AndroidConfigurationClassPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Aspect
@SuppressWarnings("Unused")
public class Hook {
    private MetadataReader defaultReader =  new AndroidMetadataReader(Object.class);
    /**
     * 无cglib时，临时方案，缓存requestMappingHandlerAdapter
     */
    private Map<String, Object> beanCache = new HashMap<>();

    public Hook() throws MalformedURLException {
    }

    @Pointcut("execution(* org.springframework.boot.SpringApplication.run(java.lang.String...))")
    public void springApplicationRun() {}
    @Pointcut("execution(* org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.setResourceLoader(..))")
    public void setResourceLoader(){}
    @Pointcut("execution(* org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.resolveBasePackage(..))")
    public void resolveBasePackage(){}
    @Pointcut("execution(* org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.findCandidateComponents(..))")
    public void findCandidateComponents(){}
    @Pointcut("execution(* org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(java.lang.String))")
    public void metadataByName(){}
    @Pointcut("execution(* org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(org.springframework.core.io.Resource))")
    public void metadataByRes(){}
    @Pointcut("execution(* org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.EnableWebMvcConfiguration.requestMappingHandlerAdapter())")
    public void requestMappingHandlerAdapter() {}
    @Pointcut("execution(* org.springframework.context.support.GenericApplicationContext.getResources(java.lang.String))")
    public void appGetResources(){}
    @Pointcut("execution(* org.hibernate.boot.archive.internal.JarFileBasedArchiveDescriptor.resolveJarFileReference())")
    public void resolveJarFileReference(){}

    @Around("springApplicationRun()")
    public Object springApplicationRun(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.d("HOOK", joinPoint.getSignature().toString());
        SpringApplication springApplication = (SpringApplication) joinPoint.getThis();
        springApplication.setResourceLoader(new AndroidResourcePatternResolver(Hook.class.getClassLoader()));
        Set<Object> sources = springApplication.getSources();
        sources.add(AndroidConfigurationClassPostProcessor.class);
        return joinPoint.proceed();
    }

    /**
     * {@link ClassPathScanningCandidateComponentProvider#findCandidateComponents}
     */
    //@Around("findCandidateComponents()")
    public Object findCandidateComponents(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.d("HOOK", joinPoint.getSignature().toString());
        return joinPoint.proceed();
    }

    /**
     * {@link ClassPathScanningCandidateComponentProvider#setResourceLoader}
     * @return
     */
//    @Around("setResourceLoader()")
    public Object setResourceLoader(ProceedingJoinPoint joinPoint) throws Throwable {
        AndroidResourcePatternResolver resourcePatternResolver = new AndroidResourcePatternResolver(Hook.class.getClassLoader());
        return joinPoint.proceed(new Object[]{resourcePatternResolver});
    }

    /**
     *
     * @return
     * @see ClassPathScanningCandidateComponentProvider#resolveBasePackage(java.lang.String)
     */
//    @Around("resolveBasePackage()")
    public Object resolveBasePackage(ProceedingJoinPoint joinPoint) throws Throwable {
        return "/" + ((String)joinPoint.getArgs()[0]);
    }

    /**
     * {@link org.springframework.context.support.GenericApplicationContext#getResources}
     * @return
     */
//    @Around("appGetResources()")
    public Object appGetResources(ProceedingJoinPoint joinPoint) throws Throwable {
        String locationPattern = (String) joinPoint.getArgs()[0];
        AndroidResourcePatternResolver resourcePatternResolver = new AndroidResourcePatternResolver(Hook.class.getClassLoader());
        return resourcePatternResolver.getResources(locationPattern);
    }

    /**
     * {@link org.springframework.core.type.classreading.SimpleMetadataReaderFactory#getMetadataReader(java.lang.String)}
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("metadataByName()")
    public Object metadataByName(ProceedingJoinPoint joinPoint) throws Throwable {
        return getMetadataReader0((String)joinPoint.getArgs()[0]);
    }

    @Around("metadataByRes()")
    public Object metadataByRes(ProceedingJoinPoint joinPoint) throws Throwable {
        return getMetadataReader1((Resource)joinPoint.getArgs()[0]);
    }

    @Around("requestMappingHandlerAdapter()")
    public Object requestMappingHandlerAdapter(ProceedingJoinPoint joinPoint) throws Throwable {
        return getBean(joinPoint);
    }

    @Around("resolveJarFileReference()")
    public Object resolveJarFileReference(ProceedingJoinPoint joinPoint) throws Throwable {
        String file = getArchiveUrl(joinPoint.getThis()).getFile();
        if (AndroidClassResource.getSourceDir().equals(file)) {
            return new ApkFile(file);
        } else {
            return joinPoint.proceed();
        }
    }

    private Object getBean(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = joinPoint.getSignature().toString();
        Object o = beanCache.get(key);
        if (o == null) {
            o = joinPoint.proceed();
            beanCache.put(key, o);
        }
        return o;
    }

    private MetadataReader getMetadataReader0(String className) throws IOException {
        AndroidMetadataReader metadataReader = null;
        try {
            metadataReader = new AndroidMetadataReader(Class.forName(className));
        } catch (ClassNotFoundException e) {
            Log.e("AndroidMetadataReader", className + " not found");
            return defaultReader;
        } catch (NoClassDefFoundError error) {
            Log.d("AndroidMetadataReader", className + " def error");
            return defaultReader;
        }
        // 创建临时文件夹避免tomcat搜索文档根目录
        if ("org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration".equals(className)) {
            metadataReader.setAnnotationMetadata(new AnnotationMetadataProxy(metadataReader.getAnnotationMetadata()) {
                @Override
                public String[] getMemberClassNames() {
                    String[] memberClassNames = annotationMetadata.getMemberClassNames();
                    for (int i=0;i<memberClassNames.length;++i) {
                        if ("org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration$EmbeddedTomcat".equals(memberClassNames[i])) {
                            memberClassNames[i] = EmbeddedServletContainerAutoConfigurationEmbeddedTomcat.class.getName();
                        }
                    }
                    return memberClassNames;
                }
            });
        }
        return metadataReader;
    }

    private MetadataReader getMetadataReader1(Resource resource) throws IOException {
        return getMetadataReader0(resource.getDescription());
    }

    /**
     * {@link org.hibernate.boot.archive.spi.AbstractArchiveDescriptor#getArchiveUrl}
     * @param obj
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static URL getArchiveUrl(Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Method getArchiveUrl = Class.forName("org.hibernate.boot.archive.spi.AbstractArchiveDescriptor").getDeclaredMethod("getArchiveUrl");
        getArchiveUrl.setAccessible(true);
        return (URL) getArchiveUrl.invoke(obj);
    }
}
