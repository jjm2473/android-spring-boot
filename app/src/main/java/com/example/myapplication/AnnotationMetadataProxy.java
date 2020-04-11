package com.example.myapplication;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Set;

public class AnnotationMetadataProxy implements AnnotationMetadata {
    protected AnnotationMetadata annotationMetadata;

    public AnnotationMetadataProxy(AnnotationMetadata annotationMetadata) {
        this.annotationMetadata = annotationMetadata;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationMetadata.getAnnotationTypes();
    }

    @Override
    public Set<String> getMetaAnnotationTypes(String annotationName) {
        return annotationMetadata.getMetaAnnotationTypes(annotationName);
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return annotationMetadata.hasAnnotation(annotationName);
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        return annotationMetadata.hasMetaAnnotation(metaAnnotationName);
    }

    @Override
    public boolean hasAnnotatedMethods(String annotationName) {
        return annotationMetadata.hasAnnotatedMethods(annotationName);
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        return annotationMetadata.getAnnotatedMethods(annotationName);
    }

    @Override
    public String getClassName() {
        return annotationMetadata.getClassName();
    }

    @Override
    public boolean isInterface() {
        return annotationMetadata.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return annotationMetadata.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return annotationMetadata.isAbstract();
    }

    @Override
    public boolean isConcrete() {
        return annotationMetadata.isConcrete();
    }

    @Override
    public boolean isFinal() {
        return annotationMetadata.isFinal();
    }

    @Override
    public boolean isIndependent() {
        return annotationMetadata.isIndependent();
    }

    @Override
    public boolean hasEnclosingClass() {
        return annotationMetadata.hasEnclosingClass();
    }

    @Override
    public String getEnclosingClassName() {
        return annotationMetadata.getEnclosingClassName();
    }

    @Override
    public boolean hasSuperClass() {
        return annotationMetadata.hasSuperClass();
    }

    @Override
    public String getSuperClassName() {
        return annotationMetadata.getSuperClassName();
    }

    @Override
    public String[] getInterfaceNames() {
        return annotationMetadata.getInterfaceNames();
    }

    @Override
    public String[] getMemberClassNames() {
        return annotationMetadata.getMemberClassNames();
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return annotationMetadata.isAnnotated(annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return annotationMetadata.getAnnotationAttributes(annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return annotationMetadata.getAnnotationAttributes(annotationName, classValuesAsString);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
        return annotationMetadata.getAllAnnotationAttributes(annotationName);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return annotationMetadata.getAllAnnotationAttributes(annotationName, classValuesAsString);
    }
}
