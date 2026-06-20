package com.ticket.support_management_system_api.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Removes springdoc's QueryDSL bean that is incompatible with Spring Data 4.x (Spring Boot 4.x).
 * springdoc-openapi 2.x references TypeInformation which was removed in Spring Data 4.x.
 */
@Component
public class SpringDocQueryDslCompatibilityFix implements BeanDefinitionRegistryPostProcessor, Ordered {

    private static final String BEAN_NAME = "queryDslQuerydslPredicateOperationCustomizer";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition(BEAN_NAME)) {
            registry.removeBeanDefinition(BEAN_NAME);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
