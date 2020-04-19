package com.example.myapplication;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;

public class AndroidResourcePatternResolver implements ResourcePatternResolver {

    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        int idx = locationPattern.indexOf('/');
        String packageName = locationPattern.substring(idx+1, locationPattern.indexOf('/', idx+1));
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        PackageClassCollector collector = new PackageClassCollector(packageName);
        try {
            AndroidClassesScanner.classes(contextClassLoader, collector);
        } catch (Exception e) {
            throw new IOException("scan classes failed", e);
        }
        List<String> collection = collector.getCollection();
        Resource[] resources = new Resource[collection.size()];
        for (int i=0;i<resources.length;++i) {
            resources[i] = new AndroidClassResource(collection.get(i));
        }
        return resources;
    }

    @Override
    public Resource getResource(String location) {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
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
