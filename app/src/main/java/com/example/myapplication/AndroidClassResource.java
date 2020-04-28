package com.example.myapplication;

import androidx.annotation.Nullable;

import org.springframework.core.io.DescriptiveResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;

public class AndroidClassResource extends DescriptiveResource {
    private static String sourceDir;
    private static String filePrefix;
    private static ClassLoader classLoader;

    public static String getSourceDir() {
        return sourceDir;
    }

    public static String getFilePrefix() {
        return filePrefix;
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 包名
     * @param prefix
     * @return
     * @throws IOException
     */
    public static List<String> getClasses(@Nullable String prefix) throws IOException {
        PackageClassCollector collector = new PackageClassCollector(prefix);
        try {
            AndroidClassesScanner.classes(classLoader, collector);
            return collector.getCollection();
        } catch (Exception e) {
            throw new IOException("scan classes failed", e);
        }
    }

    public static void setSourceDir(String sourceDir, ClassLoader classLoader) {
        AndroidClassResource.sourceDir = sourceDir;
        AndroidClassResource.filePrefix = "file:"+sourceDir+"!/";
        AndroidClassResource.classLoader = classLoader;
    }

    private URL url;

    public AndroidClassResource(String className) throws MalformedURLException {
        super(className);
        this.url = new URL("jar", null, filePrefix + className.replace('.', '/') + ".class");
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public URL getURL() throws IOException {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AndroidClassResource) {
            return this.url.equals(((AndroidClassResource) obj).url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }


    private static class PackageClassCollector implements AndroidClassesScanner.Consumer {
        private String prefix;
        private List<String> collection;

        private PackageClassCollector(String prefix) {
            this.prefix = prefix;
            this.collection = new ArrayList<>();
        }

        @Override
        public void accept(DexFile dexFile, String className) {
            if (prefix == null || className.startsWith(prefix)) {
                collection.add(className);
            }
        }

        private List<String> getCollection() {
            return collection;
        }
    }
}
