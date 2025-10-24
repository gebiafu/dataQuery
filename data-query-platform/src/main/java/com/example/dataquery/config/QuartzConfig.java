package com.example.dataquery.config;

import com.example.dataquery.quartz.AutowiringSpringBeanJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz配置
 */
@Configuration
public class QuartzConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(autowiringSpringBeanJobFactory);
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(10);
        factory.setAutoStartup(true);
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "DataQueryScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "false");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        factory.setQuartzProperties(properties);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        return factory;
    }
}
