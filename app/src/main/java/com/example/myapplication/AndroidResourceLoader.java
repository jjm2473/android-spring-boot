package com.example.myapplication;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.net.MalformedURLException;

public class AndroidResourceLoader extends DefaultResourceLoader {
    public AndroidResourceLoader(ClassLoader classLoader) {
        super(classLoader);
        addProtocolResolver(new AndroidClassResolver());
    }

    private static class AndroidClassResolver implements ProtocolResolver {
        @Override
        public Resource resolve(String location, ResourceLoader resourceLoader) {
            if (location.startsWith(CLASSPATH_URL_PREFIX) && location.endsWith(".class")) {
                try {
                    return new AndroidClassResource(location.substring(CLASSPATH_URL_PREFIX.length(), location.length()-6).replace('/', '.'));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
    }
}
