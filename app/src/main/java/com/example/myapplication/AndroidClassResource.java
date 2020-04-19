package com.example.myapplication;

import org.springframework.core.io.DescriptiveResource;

public class AndroidClassResource extends DescriptiveResource {

    public AndroidClassResource(String className) {
        super(className);
    }

    @Override
    public boolean isReadable() {
        return true;
    }
}
