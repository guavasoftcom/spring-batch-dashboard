package com.guavasoft.springbatch.dashboard.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class DatasourcesProperties {

    private List<DatasourceEntry> datasources = List.of();

    @Getter
    @Setter
    public static class DatasourceEntry {
        private String name;
        private String url;
        private String username;
        private String password;
    }
}
