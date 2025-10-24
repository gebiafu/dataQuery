package com.example.dataquery.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 * Quartz Job工厂 - 支持Spring依赖注入
 */
@Component
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
    
    private AutowireCapableBeanFactory beanFactory;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }
    
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object jobInstance = super.createJobInstance(bundle);
        beanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}
