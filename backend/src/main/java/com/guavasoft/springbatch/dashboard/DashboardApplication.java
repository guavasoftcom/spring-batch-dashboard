package com.guavasoft.springbatch.dashboard;

import com.guavasoft.springbatch.dashboard.config.AuthProperties;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.OAuth2Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DatasourcesProperties.class, AuthProperties.class, OAuth2Properties.class})
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
