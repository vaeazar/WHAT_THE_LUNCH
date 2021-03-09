package com.example.mafia.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:/application2.properties")
public class LunchDatabaseConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.lunch")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean(name = "lunchJdbcDataSource", destroyMethod = "close")
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    @Bean(name = "lunchSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("lunchJdbcDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mappers/**/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "lunchSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("lunchSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

