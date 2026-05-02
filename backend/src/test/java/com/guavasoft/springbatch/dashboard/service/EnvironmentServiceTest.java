package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties.DatasourceEntry;
import com.guavasoft.springbatch.dashboard.model.EnvironmentInfo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvironmentServiceTest {

    @Mock
    private DatasourcesProperties datasourcesProperties;

    @InjectMocks
    private EnvironmentService environmentService;

    @Test
    void getEnvironmentsReturnsAlphabeticallySortedEntriesWithDerivedType() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(
            entry("prod", "jdbc:postgresql://host/db"),
            entry("dev", "jdbc:mysql://host/db"),
            entry("staging", "jdbc:oracle:thin:@host:1521:db")));

        assertThat(environmentService.getEnvironments())
            .extracting(EnvironmentInfo::name, EnvironmentInfo::type)
            .containsExactly(
                tuple("dev", "MYSQL"),
                tuple("prod", "POSTGRESQL"),
                tuple("staging", "ORACLE"));
    }

    @Test
    void getEnvironmentsReturnsEmptyListWhenNoneConfigured() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of());

        assertThat(environmentService.getEnvironments()).isEmpty();
    }

    @Test
    void getEnvironmentsReturnsUnknownTypeForMalformedUrl() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(entry("oddball", "not-a-jdbc-url")));

        assertThat(environmentService.getEnvironments())
            .extracting(EnvironmentInfo::type)
            .containsExactly("UNKNOWN");
    }

    @Test
    void getEnvironmentsReturnsUnknownTypeForNullUrl() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(entry("nullUrl", null)));

        assertThat(environmentService.getEnvironments())
            .extracting(EnvironmentInfo::type)
            .containsExactly("UNKNOWN");
    }

    @Test
    void getEnvironmentsReturnsUnknownTypeForPrefixOnlyUrl() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(entry("prefixOnly", "jdbc:")));

        assertThat(environmentService.getEnvironments())
            .extracting(EnvironmentInfo::type)
            .containsExactly("UNKNOWN");
    }

    @Test
    void getEnvironmentsUppercasesLowercaseEngineToken() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(entry("camel", "jdbc:postgresql://h/d")));

        assertThat(environmentService.getEnvironments())
            .extracting(EnvironmentInfo::type)
            .containsExactly("POSTGRESQL");
    }

    private static DatasourceEntry entry(String name, String url) {
        DatasourceEntry e = new DatasourceEntry();
        e.setName(name);
        e.setUrl(url);
        return e;
    }
}
