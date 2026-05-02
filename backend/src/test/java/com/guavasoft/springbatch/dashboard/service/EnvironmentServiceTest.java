package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties.DatasourceEntry;
import com.guavasoft.springbatch.dashboard.dialect.DialectType;
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
    void getEnvironmentsReturnsAlphabeticallySortedEntriesWithDeclaredType() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(
            entry("prod", DialectType.POSTGRESQL),
            entry("dev", DialectType.MYSQL),
            entry("staging", DialectType.ORACLE)));

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

    private static DatasourceEntry entry(String name, DialectType type) {
        DatasourceEntry e = new DatasourceEntry();
        e.setName(name);
        e.setType(type);
        return e;
    }
}
