package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.dx.Dx;
import com.example.myapplication.util.FileUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfigurationEmbeddedTomcat;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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
    @Pointcut("execution(java.lang.Class org.springframework.cglib.core.ReflectUtils.defineClass(java.lang.String, byte[], java.lang.ClassLoader, java.security.ProtectionDomain))")
    public void defineClass(){}

    //@Around("springApplicationRun()")//无cglib时启用
//    public Object springApplicationRun(ProceedingJoinPoint joinPoint) throws Throwable {
//        Log.d("HOOK", joinPoint.getSignature().toString());
//        SpringApplication springApplication = (SpringApplication) joinPoint.getThis();
//        Set<Object> sources = springApplication.getSources();
//        sources.add(AndroidConfigurationClassPostProcessor.class);
//        return joinPoint.proceed();
//    }

    /**
     * {@link ClassPathScanningCandidateComponentProvider#findCandidateComponents}
     */
    @Around("findCandidateComponents()")
    public Object findCandidateComponents(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.d("HOOK", joinPoint.getSignature().toString());
        return joinPoint.proceed();
    }

    /**
     * {@link ClassPathScanningCandidateComponentProvider#setResourceLoader}
     * @return
     */
    @Around("setResourceLoader()")
    public Object setResourceLoader(ProceedingJoinPoint joinPoint) throws Throwable {
        AndroidResourcePatternResolver resourcePatternResolver = new AndroidResourcePatternResolver();
        return joinPoint.proceed(new Object[]{resourcePatternResolver});
    }

    /**
     *
     * @return
     * @see ClassPathScanningCandidateComponentProvider#resolveBasePackage(java.lang.String)
     */
    @Around("resolveBasePackage()")
    public Object resolveBasePackage(ProceedingJoinPoint joinPoint) throws Throwable {
        return "/" + ((String)joinPoint.getArgs()[0]);
    }


    @Around("metadataByName()")
    public Object metadataByName(ProceedingJoinPoint joinPoint) throws Throwable {
        return getMetadataReader0((String)joinPoint.getArgs()[0]);
    }

    @Around("metadataByRes()")
    public Object metadataByRes(ProceedingJoinPoint joinPoint) throws Throwable {
        return getMetadataReader1((Resource)joinPoint.getArgs()[0]);
    }

    //@Around("requestMappingHandlerAdapter()")//无cglib时启用
    public Object requestMappingHandlerAdapter(ProceedingJoinPoint joinPoint) throws Throwable {
        return getBean(joinPoint);
    }

    /**
     * {@link ReflectUtils#defineClass(java.lang.String, byte[], java.lang.ClassLoader, java.security.ProtectionDomain)}
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("defineClass()")
    public Object defineClass(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = (String) joinPoint.getArgs()[0];
        ClassLoader classLoader = (ClassLoader) joinPoint.getArgs()[2];
        Dx dx = new Dx();
        dx.addClass((byte[])joinPoint.getArgs()[1], className);
        dx.setSharedClassLoader(classLoader);
        return dx.generateAndLoad(classLoader, null).loadClass(className);
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
            throw new IOException(e);
        } catch (NoClassDefFoundError error) {
            Log.d("AndroidMetadataReader", className + " def error");
            return defaultReader;
        }
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

    private static void cleanCache(Context context) throws IOException {
        for (File f:context.getCacheDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("tomcat");
            }
        })) {
            FileUtils.rm(f);
        }
    }

    public static ClassLoader init(Context context) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        cleanCache(context);
        Dx.init(new File(context.getDir("tmpdexfiles", Context.MODE_PRIVATE).getAbsolutePath()));
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //AndroidClassResource.setSourceDir(context.getApplicationInfo().sourceDir, loader);
        return loader;
    }

}
