package org.springframework.context.annotation;

import com.example.myapplication.DelegateConfigurableListableBeanFactory;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

import java.beans.PropertyDescriptor;

@Configuration("org.springframework.context.annotation.internalConfigurationAnnotationProcessor")
public class AndroidConfigurationClassPostProcessor extends ConfigurationClassPostProcessor {
    private static final String IMPORT_REGISTRY_BEAN_NAME =
            ConfigurationClassPostProcessor.class.getName() + ".importRegistry";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        DelegateConfigurableListableBeanFactory delegateConfigurableListableBeanFactory = new DelegateConfigurableListableBeanFactory(beanFactory, beanFactory instanceof BeanDefinitionRegistry?(BeanDefinitionRegistry)beanFactory:null) {
            @Override
            public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
                if (!"org.springframework.context.annotation.ConfigurationClassPostProcessor.ImportAwareBeanPostProcessor".equals(beanPostProcessor.getClass().getName())) {
                    super.addBeanPostProcessor(beanPostProcessor);
                }
            }
        };
        super.postProcessBeanFactory(delegateConfigurableListableBeanFactory);
        beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));
    }

    @Override
    public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {
        //todo: use https://github.com/zhangke3016/MethodInterceptProxy https://github.com/leo-ouyang/CGLib-for-Android https://github.com/linkedin/dexmaker
    }

    private static class ImportAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

        private final BeanFactory beanFactory;

        public ImportAwareBeanPostProcessor(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
            // Inject the BeanFactory before AutowiredAnnotationBeanPostProcessor's
            // postProcessPropertyValues method attempts to autowire other configuration beans.
            if (bean instanceof ConfigurationClassEnhancer.EnhancedConfiguration) {
                ((ConfigurationClassEnhancer.EnhancedConfiguration) bean).setBeanFactory(this.beanFactory);
            }
            return pvs;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName)  {
            if (bean instanceof ImportAware) {
                ImportRegistry importRegistry = this.beanFactory.getBean(IMPORT_REGISTRY_BEAN_NAME, ImportRegistry.class);
                String className = bean instanceof ConfigurationClassEnhancer.EnhancedConfiguration ? bean.getClass().getSuperclass().getName() : bean.getClass().getName();
                AnnotationMetadata importingClass = importRegistry.getImportingClassFor(className);
                if (importingClass != null) {
                    ((ImportAware) bean).setImportMetadata(importingClass);
                }
            }
            return bean;
        }
    }

}
