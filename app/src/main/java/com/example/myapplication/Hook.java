package com.example.myapplication;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.dx.Code;
import com.android.dx.Local;
import com.android.dx.TypeId;
import com.example.myapplication.dx.Dx;
import com.example.myapplication.dx.OverDexMaker;
import com.example.myapplication.javassist.SpringAnnotationsAttribute;
import com.example.myapplication.scan.ApkEntry;
import com.example.myapplication.scan.ApkFile;
import com.example.myapplication.scan.Config;
import com.example.myapplication.util.FileUtils;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfigurationEmbeddedTomcat;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.annotation.AndroidConfigurationClassPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;

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
    @Pointcut("execution(void javassist.bytecode.ClassFile.read(java.io.DataInputStream))")
    public void classFileRead(){}
    @Pointcut("execution(java.lang.Class org.springframework.cglib.core.ReflectUtils.defineClass(java.lang.String, byte[], java.lang.ClassLoader, java.security.ProtectionDomain))")
    public void defineClass(){}
    @Pointcut("execution(java.lang.Class javassist.util.proxy.FactoryHelper.toClass2(java.lang.reflect.Method, java.lang.ClassLoader, java.lang.Object[]))")
    public void javassistToClass2(){}
    @Pointcut("execution(org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.new())")
    public void newDefaultMethodInvokingMethodInterceptor(){}
    @Pointcut("execution(java.lang.Object org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(org.aopalliance.intercept.MethodInvocation))")
    public void invokeDefaultMethodInvokingMethodInterceptor(){}
    @Pointcut("call(javax.xml.parsers.SAXParserFactory javax.xml.parsers.SAXParserFactory.newInstance())")
    public void callSAXParserFactoryNew(){}
    /**
     * {@link org.springframework.boot.SpringApplication#run}
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("springApplicationRun()")
    public Object springApplicationRun(ProceedingJoinPoint joinPoint) throws Throwable {
        Log.d("HOOK", joinPoint.getSignature().toString());
        SpringApplication springApplication = (SpringApplication) joinPoint.getThis();
        springApplication.setResourceLoader(new AndroidResourcePatternResolver(AndroidClassResource.getClassLoader()));
        Set<Object> sources = springApplication.getSources();
//        sources.add(AndroidConfigurationClassPostProcessor.class);
        for (Object source:sources) {
            if (source instanceof Class) {
                Config.basePackage = ((Class) source).getPackage().getName();
                break;
            }
        }
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

    //@Around("requestMappingHandlerAdapter()")
    public Object requestMappingHandlerAdapter(ProceedingJoinPoint joinPoint) throws Throwable {
        return getBean(joinPoint);
    }

    @Around("resolveJarFileReference()")
    public Object resolveJarFileReference(ProceedingJoinPoint joinPoint) throws Throwable {
        String file = getArchiveUrl(joinPoint.getThis()).getFile();
        if (AndroidClassResource.getSourceDir().equals(file)) {
            return new ApkFile(file);
        } else {
            Log.e("resolveJarFileReference", file);
            return joinPoint.proceed();
        }
    }

    /**
     * {@link javassist.bytecode.ClassFile#read}
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("classFileRead()")
    public void classFileRead(ProceedingJoinPoint joinPoint) throws Throwable {
        ClassFile file = (ClassFile) joinPoint.getThis();
        DataInputStream dis = (DataInputStream) joinPoint.getArgs()[0];
        InputStream remain = ApkEntry.judge(dis);
        if (remain == null) {
            String clz = ApkEntry.read(dis);
            Field thisclassname = ClassFile.class.getDeclaredField("thisclassname");
            thisclassname.setAccessible(true);
            thisclassname.set(file, clz);
            /** {@link org.hibernate.boot.archive.scan.spi.ClassFileArchiveEntryHandler#toClassDescriptor} */
            Field attributes = ClassFile.class.getDeclaredField("attributes");
            attributes.setAccessible(true);
            attributes.set(file, new ArrayList<>(Collections.singletonList(getAnnotation(clz))));
        } else {
            joinPoint.proceed(new Object[]{remain});
        }
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
        return defineClass(classLoader, className, (byte[])joinPoint.getArgs()[1]);
    }

    /**
     * {@link javassist.util.proxy.FactoryHelper#toClass2}(Method ignore, ClassLoader loader, Object[] args)
     * @param joinPoint
     * @return java.lang.Class
     * @throws Throwable
     */
    @Around("javassistToClass2()")
    public Object javassistToClass2(ProceedingJoinPoint joinPoint) throws Throwable {
        ClassLoader classLoader = (ClassLoader) joinPoint.getArgs()[1];
        // { className, b, new Integer(0), new Integer(b.length), domain }
        Object[] args = (Object[]) joinPoint.getArgs()[2];
        String className = (String) args[0];
        byte[] bytecode = (byte[]) args[1];
        return defineClass(classLoader, className, bytecode);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Around("newDefaultMethodInvokingMethodInterceptor()")
    public Object newDefaultMethodInvokingMethodInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        DefaultMethodInvokingMethodInterceptor self = (DefaultMethodInvokingMethodInterceptor) joinPoint.getThis();

        Field field = DefaultMethodInvokingMethodInterceptor.class.getDeclaredField("constructor");
        field.setAccessible(true);
        Constructor<MethodHandles.Lookup> constructor = null;
        try {
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
        } catch (NoSuchMethodException e){
            constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        }
        field.set(self, constructor);

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Around("invokeDefaultMethodInvokingMethodInterceptor()")
    public Object invokeDefaultMethodInvokingMethodInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        DefaultMethodInvokingMethodInterceptor self = (DefaultMethodInvokingMethodInterceptor) joinPoint.getThis();
        MethodInvocation invocation = (MethodInvocation) joinPoint.getArgs()[0];
        Field field = DefaultMethodInvokingMethodInterceptor.class.getDeclaredField("constructor");
        field.setAccessible(true);
        Constructor constructor = (Constructor) field.get(self);

        Method method = invocation.getMethod();

        if (!org.springframework.data.util.ReflectionUtils.isDefaultMethod(method)) {
            return invocation.proceed();
        }

        Object[] arguments = invocation.getArguments();
        Class<?> declaringClass = method.getDeclaringClass();
        Object proxy = ((ProxyMethodInvocation) invocation).getProxy();
        MethodHandles.Lookup lookup = (MethodHandles.Lookup) (constructor.getParameterCount() == 1 ?
                constructor.newInstance(declaringClass) :
                constructor.newInstance(declaringClass, (MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE)));
        return lookup.unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(arguments);
    }

    /**
     * {@link javax.xml.parsers.SAXParserFactory#newInstance}
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("callSAXParserFactoryNew()")
    public Object callSAXParserFactoryNew(ProceedingJoinPoint joinPoint) throws Throwable {
        return new SAXParserFactoryImpl();
    }

    private Class defineClass(ClassLoader classLoader, String className, byte[] bytecode) throws IOException, ClassNotFoundException {
        Dx dx = new Dx();
        dx.addClass(bytecode, className);
        dx.setSharedClassLoader(classLoader);
        return dx.generateAndLoad(classLoader, null).loadClass(className);
    }

    private AnnotationsAttribute getAnnotation(String clz) throws IOException {
        return new SpringAnnotationsAttribute(clz, AnnotationsAttribute.visibleTag, getMetadataReader0(clz).getAnnotationMetadata());
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

    private static final String EnhancerClass = "org.springframework.cglib.proxy.Enhancer";

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

//    private Class proxyEnhancer(File dexCacheDir) throws IOException, ClassNotFoundException {
//        String superClsName = DexEnhancer.class.getName().replace(".", "/");
//        String subClsName = EnhancerClass.replace(".", "/");
//
//        TypeId<?> superType = TypeId.get("L" + superClsName + ";");
//        TypeId<?> subType = TypeId.get("L" + subClsName + ";");
//
//        OverDexMaker dexMaker = new OverDexMaker();
//        dexMaker.declare(subType, subClsName, Modifier.PUBLIC, superType);
//        Code code = dexMaker.declare(subType.getConstructor(), Modifier.PUBLIC);
//        Local thisRef = code.getThis(subType);
//        code.invokeDirect(superType.getConstructor(), null, thisRef);
//        code.returnVoid();
//        ClassLoader loader = dexMaker.generateAndLoad(DexEnhancer.class.getClassLoader(), dexCacheDir);
//        return loader.loadClass(EnhancerClass);
//    }

    public static ClassLoader init(Context context) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        cleanCache(context);
        Dx.init(new File(context.getDir("tmpdexfiles", Context.MODE_PRIVATE).getAbsolutePath()));
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AndroidClassResource.setSourceDir(context.getApplicationInfo().sourceDir, loader);
        return loader;
    }
}
