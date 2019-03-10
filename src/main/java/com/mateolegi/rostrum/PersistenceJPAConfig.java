package com.mateolegi.rostrum;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class PersistenceJPAConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(){
        LocalContainerEntityManagerFactoryBean factoryBean
                = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource( this.restDataSource() );
        factoryBean.setPackagesToScan( "org.rest" );

        JpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter() {
            {
                // JPA properties ...
            }
        };
        factoryBean.setJpaVendorAdapter( vendorAdapter );
        // factoryBean.setJpaProperties( this.additionlProperties() );

        return factoryBean;
    }

    @Bean
    public DataSource restDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        // dataSource.setDriverClassName( this.driverClassName );
        // dataSource.setUrl( this.url );
        dataSource.setUsername( "restUser" );
        dataSource.setPassword( "restmy5ql" );
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                this.entityManagerFactoryBean().getObject() );

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }
}
