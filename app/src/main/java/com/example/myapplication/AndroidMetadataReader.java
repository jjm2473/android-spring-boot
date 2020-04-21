package com.example.myapplication;

import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;

import java.net.MalformedURLException;

public class AndroidMetadataReader implements MetadataReader {
    private AnnotationMetadata annotationMetadata;
    private Resource resource;

    public AndroidMetadataReader(Class<?> aClass) throws MalformedURLException {
        this.annotationMetadata = new StandardAnnotationMetadata(aClass);
        this.resource = new AndroidClassResource(aClass.getName());
    }

    void setAnnotationMetadata(AnnotationMetadata annotationMetadata) {
        this.annotationMetadata = annotationMetadata;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return annotationMetadata;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return annotationMetadata;
    }
}
