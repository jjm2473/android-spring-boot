package com.example.myapplication;


import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;

public class AndroidMetadataReader implements MetadataReader {
    private AnnotationMetadata annotationMetadata;
    private Resource resource;

    public AndroidMetadataReader(Class<?> aClass) {
        this.annotationMetadata = new StandardAnnotationMetadata(aClass);
        this.resource = new DummyClassResource(aClass.getName());
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

    private static class DummyClassResource extends DescriptiveResource {

        /**
         * Create a new DescriptiveResource.
         *
         * @param description the resource description
         */
        public DummyClassResource(String description) {
            super(description);
        }

        @Override
        public boolean isReadable() {
            return super.isReadable();
        }
    }
}
