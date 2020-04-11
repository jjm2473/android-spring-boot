package com.example.myapplication;


import android.util.Log;

import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfigurationEmbeddedTomcat;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Configuration("org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory")
public class AndroidMetadataReaderFactory implements MetadataReaderFactory {
    private MetadataReader defaultReader =  new AndroidMetadataReader(Object.class);
    @Override
    public MetadataReader getMetadataReader(String className) throws IOException {
//        if ("org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration$EmbeddedTomcat".equals(className)) {
//            return new AndroidMetadataReader(EmbeddedServletContainerAutoConfigurationEmbeddedTomcat.class);
//        }
        AndroidMetadataReader metadataReader = null;
        try {
            metadataReader = new AndroidMetadataReader(Class.forName(className));
        } catch (ClassNotFoundException e) {
            Log.e("AndroidMetadataReader", className + " not found");
            throw new IOException(e);
        } catch (NoClassDefFoundError error) {
            Log.d("AndroidMetadataReader", className + " def error", error);
            return defaultReader;
        }
        if ("org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration".equals(className)) {
            metadataReader.setAnnotationMetadata(new AnnotationMetadataProxy(metadataReader.getAnnotationMetadata()) {
                @Override
                public String[] getMemberClassNames() {
                    String[] memberClassNames = annotationMetadata.getMemberClassNames();
                    for (int i=0;i<memberClassNames.length;++i) {
                        if ("org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration$EmbeddedTomcat".equals(memberClassNames[i])) {
                            memberClassNames[i] = EmbeddedServletContainerAutoConfigurationEmbeddedTomcat.class.getName();
                        }
                    }
                    return memberClassNames;
                }
            });
        }
        return metadataReader;
    }

    @Override
    public MetadataReader getMetadataReader(Resource resource) throws IOException {
        return getMetadataReader(resource.getDescription());
    }
}
