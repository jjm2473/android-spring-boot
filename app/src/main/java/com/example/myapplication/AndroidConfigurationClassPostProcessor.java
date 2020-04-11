package com.example.myapplication;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

@Configuration("org.springframework.context.annotation.internalConfigurationAnnotationProcessor")
public class AndroidConfigurationClassPostProcessor extends ConfigurationClassPostProcessor {
    public AndroidConfigurationClassPostProcessor() {
        super();
        this.setMetadataReaderFactory(new AndroidMetadataReaderFactory());
    }

    @Override
    public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {

    }
}
