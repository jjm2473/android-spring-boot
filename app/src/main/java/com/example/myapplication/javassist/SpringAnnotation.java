package com.example.myapplication.javassist;

import java.util.Map;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

public class SpringAnnotation extends Annotation {
    private String type;
    private Map<String, Object> attr;
    public SpringAnnotation(String type, Map<String, Object> attr) {
        super(0, null);
        this.type = type;
        this.attr = attr;
    }

    @Override
    public String getTypeName() {
        return type;
    }
}
