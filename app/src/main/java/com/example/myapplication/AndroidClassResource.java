package com.example.myapplication;

import org.springframework.core.io.DescriptiveResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AndroidClassResource extends DescriptiveResource {
    private static String sourceDir;
    private static String filePrefix;

    public static String getSourceDir() {
        return sourceDir;
    }

    public static String getFilePrefix() {
        return filePrefix;
    }

    public static void setSourceDir(String sourceDir) {
        AndroidClassResource.sourceDir = sourceDir;
        AndroidClassResource.filePrefix = "file:"+sourceDir+"!/";
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
}
