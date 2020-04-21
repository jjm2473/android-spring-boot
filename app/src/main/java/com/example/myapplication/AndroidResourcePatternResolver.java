package com.example.myapplication;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.PathMatcher;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexFile;

public class AndroidResourcePatternResolver extends PathMatchingResourcePatternResolver {

    private ClassLoader classLoader;
    private Set<String> entries;

    public AndroidResourcePatternResolver(ClassLoader classLoader) throws IOException {
        super(new AndroidResourceLoader(classLoader));
        this.classLoader = classLoader;
        Set<String> entries0 = new LinkedHashSet<>(64);
        try (JarFile jarFile = new JarFile(AndroidClassResource.getSourceDir())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                entries0.add(jarEntry.getName());
            }
        }
        this.entries = new HashSet<>(entries0);
    }

    @Override
    protected Set<Resource> doFindAllClassPathResources(String path) throws IOException {
        if (path.endsWith("/") || entries.contains(path)) {
            return Collections.singleton(new UrlResource(new URL("jar", null, AndroidClassResource.getFilePrefix() + path)));
        } else {
            return Collections.emptySet();
        }
    }
    @Override
    protected boolean isJarResource(Resource resource) throws IOException {
        return resource instanceof AndroidClassResource;
    }
    @Override
    protected Set<Resource> doFindPathMatchingJarResources(Resource rootDirResource, URL rootDirURL, String subPattern)
            throws IOException {
        if (ResourceUtils.URL_PROTOCOL_JAR.equals(rootDirURL.getProtocol()) && rootDirURL.getFile().startsWith(AndroidClassResource.getFilePrefix())) {
            String rootDir = rootDirURL.getFile().substring(AndroidClassResource.getFilePrefix().length());
            Set<Resource> resources = lsPrefix(rootDir);
            int prelen = (AndroidClassResource.getFilePrefix() + rootDir).length();
            PathMatcher pathMatcher = getPathMatcher();
            Set<Resource> result = new LinkedHashSet<Resource>(8);
            for (Resource res:resources) {
                if (pathMatcher.match(subPattern, res.getURL().getFile().substring(prelen))) {
                    result.add(res);
                }
            }
            return result;
        }
        return super.doFindPathMatchingJarResources(rootDirResource, rootDirURL, subPattern);
    }

    private Set<Resource> lsPrefix(String path) throws IOException {
        Set<Resource> ret = new HashSet<>();
        for (String name:entries) {
            if (name.startsWith(path)) {
                ret.add(new UrlResource(new URL("jar", null, AndroidClassResource.getFilePrefix()+name)));
            }
        }
        PackageClassCollector collector = new PackageClassCollector(path.replace('/', '.'));
        try {
            AndroidClassesScanner.classes(classLoader, collector);
        } catch (Exception e) {
            throw new IOException("scan classes failed", e);
        }
        for (String cls:collector.getCollection()) {
            ret.add(new AndroidClassResource(cls));
        }
        return ret;
    }

    private static class PackageClassCollector implements AndroidClassesScanner.Consumer {
        private String prefix;
        private List<String> collection;

        public PackageClassCollector(String prefix) {
            this.prefix = prefix;
            this.collection = new ArrayList<>();
        }

        @Override
        public void accept(DexFile dexFile, String className) {
            if (className.startsWith(prefix)) {
                collection.add(className);
            }
        }

        public List<String> getCollection() {
            return collection;
        }
    }
}
