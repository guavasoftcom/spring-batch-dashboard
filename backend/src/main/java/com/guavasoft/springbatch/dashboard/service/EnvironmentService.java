package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final DatasourcesProperties datasourcesProperties;

    public List<String> getDatasourceNames() {
        return datasourcesProperties.getDatasources().stream()
            .map(DatasourcesProperties.DatasourceEntry::getName)
            .sorted()
            .toList();
    }
}
