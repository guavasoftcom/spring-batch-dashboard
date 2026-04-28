package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties.DatasourceEntry;
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
    void getDatasourceNamesReturnsAlphabeticallySortedNames() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of(
            entry("prod"), entry("dev"), entry("staging")));

        assertThat(environmentService.getDatasourceNames()).containsExactly("dev", "prod", "staging");
    }

    @Test
    void getDatasourceNamesReturnsEmptyListWhenNoneConfigured() {
        when(datasourcesProperties.getDatasources()).thenReturn(List.of());

        assertThat(environmentService.getDatasourceNames()).isEmpty();
    }

    private static DatasourceEntry entry(String name) {
        DatasourceEntry e = new DatasourceEntry();
        e.setName(name);
        return e;
    }
}
