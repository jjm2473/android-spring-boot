package org.springframework.core.type.classreading;

import android.util.Log;

import com.example.myapplication.AndroidMetadataReader;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

public class SimpleMetadataReaderFactory implements MetadataReaderFactory {

    private final ResourceLoader resourceLoader;


    /**
     * Create a new SimpleMetadataReaderFactory for the default class loader.
     */
    public SimpleMetadataReaderFactory() {
        this.resourceLoader = new DefaultResourceLoader();
    }

    /**
     * Create a new SimpleMetadataReaderFactory for the given resource loader.
     * @param resourceLoader the Spring ResourceLoader to use
     * (also determines the ClassLoader to use)
     */
    public SimpleMetadataReaderFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    }

    /**
     * Create a new SimpleMetadataReaderFactory for the given class loader.
     * @param classLoader the ClassLoader to use
     */
    public SimpleMetadataReaderFactory(ClassLoader classLoader) {
        this.resourceLoader =
                (classLoader != null ? new DefaultResourceLoader(classLoader) : new DefaultResourceLoader());
    }


    /**
     * Return the ResourceLoader that this MetadataReaderFactory has been
     * constructed with.
     */
    public final ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    @Override
    public MetadataReader getMetadataReader(String className) throws IOException {
        try {
            return new AndroidMetadataReader(Class.forName(className));
        } catch (ClassNotFoundException e) {
            Log.e("AndroidMetadataReader", className+" not found");
            throw new IOException(e);
        } catch (NoClassDefFoundError error) {
            Log.e("AndroidMetadataReader", className + " def error");
            throw error;
        }
    }

    @Override
    public MetadataReader getMetadataReader(Resource resource) throws IOException {
        return getMetadataReader(resource.getDescription());
    }
}