package com.example.myapplication;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.security.AccessControlContext;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DelegateConfigurableListableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {
    private ConfigurableListableBeanFactory delegate;
    private BeanDefinitionRegistry beanDefinitionRegistry;

    public DelegateConfigurableListableBeanFactory(ConfigurableListableBeanFactory delegate, BeanDefinitionRegistry beanDefinitionRegistry) {
        this.delegate = delegate;
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }

    @Override
    public void ignoreDependencyType(Class<?> type) {
        delegate.ignoreDependencyType(type);
    }

    @Override
    public void ignoreDependencyInterface(Class<?> ifc) {
        delegate.ignoreDependencyInterface(ifc);
    }

    @Override
    public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
        delegate.registerResolvableDependency(dependencyType, autowiredValue);
    }

    @Override
    public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {
        return delegate.isAutowireCandidate(beanName, descriptor);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return delegate.getBeanDefinition(beanName);
    }

    @Override
    public Iterator<String> getBeanNamesIterator() {
        return delegate.getBeanNamesIterator();
    }

    @Override
    public void clearMetadataCache() {
        delegate.clearMetadataCache();
    }

    @Override
    public void freezeConfiguration() {
        delegate.freezeConfiguration();
    }

    @Override
    public boolean isConfigurationFrozen() {
        return delegate.isConfigurationFrozen();
    }

    @Override
    public void preInstantiateSingletons() throws BeansException {
        delegate.preInstantiateSingletons();
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return delegate.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return delegate.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return delegate.getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        return delegate.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return delegate.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return delegate.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return delegate.getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        return delegate.getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return delegate.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        return delegate.getBeansWithAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return delegate.findAnnotationOnBean(beanName, annotationType);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return delegate.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return delegate.getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return delegate.getBean(requiredType);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return delegate.getBean(name, args);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return delegate.getBean(requiredType, args);
    }

    @Override
    public boolean containsBean(String name) {
        return delegate.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return delegate.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return delegate.isPrototype(name);
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, typeToMatch);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, typeToMatch);
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return delegate.getType(name);
    }

    @Override
    public String[] getAliases(String name) {
        return delegate.getAliases(name);
    }

    @Override
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        return delegate.createBean(beanClass);
    }

    @Override
    public void autowireBean(Object existingBean) throws BeansException {
        delegate.autowireBean(existingBean);
    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        return delegate.configureBean(existingBean, beanName);
    }

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        return delegate.createBean(beanClass, autowireMode, dependencyCheck);
    }

    @Override
    public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        return delegate.autowire(beanClass, autowireMode, dependencyCheck);
    }

    @Override
    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException {
        delegate.autowireBeanProperties(existingBean, autowireMode, dependencyCheck);
    }

    @Override
    public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
        delegate.applyBeanPropertyValues(existingBean, beanName);
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) throws BeansException {
        return delegate.initializeBean(existingBean, beanName);
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {
        return delegate.applyBeanPostProcessorsBeforeInitialization(existingBean, beanName);
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        return delegate.applyBeanPostProcessorsAfterInitialization(existingBean, beanName);
    }

    @Override
    public void destroyBean(Object existingBean) {
        delegate.destroyBean(existingBean);
    }

    @Override
    public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
        return delegate.resolveNamedBean(requiredType);
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName) throws BeansException {
        return delegate.resolveDependency(descriptor, requestingBeanName);
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {
        return delegate.resolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
    }

    @Override
    public void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException {
        delegate.setParentBeanFactory(parentBeanFactory);
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        delegate.setBeanClassLoader(beanClassLoader);
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return delegate.getBeanClassLoader();
    }

    @Override
    public void setTempClassLoader(ClassLoader tempClassLoader) {
        delegate.setTempClassLoader(tempClassLoader);
    }

    @Override
    public ClassLoader getTempClassLoader() {
        return delegate.getTempClassLoader();
    }

    @Override
    public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
        delegate.setCacheBeanMetadata(cacheBeanMetadata);
    }

    @Override
    public boolean isCacheBeanMetadata() {
        return delegate.isCacheBeanMetadata();
    }

    @Override
    public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
        delegate.setBeanExpressionResolver(resolver);
    }

    @Override
    public BeanExpressionResolver getBeanExpressionResolver() {
        return delegate.getBeanExpressionResolver();
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        delegate.setConversionService(conversionService);
    }

    @Override
    public ConversionService getConversionService() {
        return delegate.getConversionService();
    }

    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
        delegate.addPropertyEditorRegistrar(registrar);
    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
        delegate.registerCustomEditor(requiredType, propertyEditorClass);
    }

    @Override
    public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
        delegate.copyRegisteredEditorsTo(registry);
    }

    @Override
    public void setTypeConverter(TypeConverter typeConverter) {
        delegate.setTypeConverter(typeConverter);
    }

    @Override
    public TypeConverter getTypeConverter() {
        return delegate.getTypeConverter();
    }

    @Override
    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        delegate.addEmbeddedValueResolver(valueResolver);
    }

    @Override
    public boolean hasEmbeddedValueResolver() {
        return delegate.hasEmbeddedValueResolver();
    }

    @Override
    public String resolveEmbeddedValue(String value) {
        return delegate.resolveEmbeddedValue(value);
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        delegate.addBeanPostProcessor(beanPostProcessor);
    }

    @Override
    public int getBeanPostProcessorCount() {
        return delegate.getBeanPostProcessorCount();
    }

    @Override
    public void registerScope(String scopeName, Scope scope) {
        delegate.registerScope(scopeName, scope);
    }

    @Override
    public String[] getRegisteredScopeNames() {
        return delegate.getRegisteredScopeNames();
    }

    @Override
    public Scope getRegisteredScope(String scopeName) {
        return delegate.getRegisteredScope(scopeName);
    }

    @Override
    public AccessControlContext getAccessControlContext() {
        return delegate.getAccessControlContext();
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        delegate.copyConfigurationFrom(otherFactory);
    }

    @Override
    public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
        delegate.registerAlias(beanName, alias);
    }

    @Override
    public void resolveAliases(StringValueResolver valueResolver) {
        delegate.resolveAliases(valueResolver);
    }

    @Override
    public BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return delegate.getMergedBeanDefinition(beanName);
    }

    @Override
    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        return delegate.isFactoryBean(name);
    }

    @Override
    public void setCurrentlyInCreation(String beanName, boolean inCreation) {
        delegate.setCurrentlyInCreation(beanName, inCreation);
    }

    @Override
    public boolean isCurrentlyInCreation(String beanName) {
        return delegate.isCurrentlyInCreation(beanName);
    }

    @Override
    public void registerDependentBean(String beanName, String dependentBeanName) {
        delegate.registerDependentBean(beanName, dependentBeanName);
    }

    @Override
    public String[] getDependentBeans(String beanName) {
        return delegate.getDependentBeans(beanName);
    }

    @Override
    public String[] getDependenciesForBean(String beanName) {
        return delegate.getDependenciesForBean(beanName);
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        delegate.destroyBean(beanName, beanInstance);
    }

    @Override
    public void destroyScopedBean(String beanName) {
        delegate.destroyScopedBean(beanName);
    }

    @Override
    public void destroySingletons() {
        delegate.destroySingletons();
    }

    @Override
    public BeanFactory getParentBeanFactory() {
        return delegate.getParentBeanFactory();
    }

    @Override
    public boolean containsLocalBean(String name) {
        return delegate.containsLocalBean(name);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        delegate.registerSingleton(beanName, singletonObject);
    }

    @Override
    public Object getSingleton(String beanName) {
        return delegate.getSingleton(beanName);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return delegate.containsSingleton(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        return delegate.getSingletonNames();
    }

    @Override
    public int getSingletonCount() {
        return delegate.getSingletonCount();
    }

    @Override
    public Object getSingletonMutex() {
        return delegate.getSingletonMutex();
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        beanDefinitionRegistry.removeBeanDefinition(beanName);
    }

    @Override
    public boolean isBeanNameInUse(String beanName) {
        return beanDefinitionRegistry.isBeanNameInUse(beanName);
    }

    @Override
    public void removeAlias(String alias) {
        beanDefinitionRegistry.removeAlias(alias);
    }

    @Override
    public boolean isAlias(String name) {
        return beanDefinitionRegistry.isAlias(name);
    }
}
