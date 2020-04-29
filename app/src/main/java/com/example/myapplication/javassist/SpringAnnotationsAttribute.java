package com.example.myapplication.javassist;

import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class SpringAnnotationsAttribute extends AnnotationsAttribute {
    private AnnotationMetadata annotationMetadata;
    public SpringAnnotationsAttribute(String clz, String attrname, AnnotationMetadata annotationMetadata) {
        super(new ConstPool(clz), attrname, null);
        this.annotationMetadata = annotationMetadata;
    }

    @Override
    public Annotation getAnnotation(String type) {
        Map<String, Object> attr = annotationMetadata.getAnnotationAttributes(type);
        if (attr == null) {
            return null;
        }
        return new SpringAnnotation(type, attr);
    }

    @Override
    public Annotation[] getAnnotations() {
        Set<String> annotationTypes = annotationMetadata.getAnnotationTypes();
        Annotation[] annotations = new Annotation[annotationTypes.size()];
        int i=0;
        for (String type: annotationTypes) {
            annotations[i] = getAnnotation(type);
            ++i;
        }
        return annotations;
    }
}
