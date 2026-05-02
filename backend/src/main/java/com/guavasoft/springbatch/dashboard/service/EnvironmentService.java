package com.guavasoft.springbatch.dashboard.service;

import com.guavasoft.springbatch.dashboard.config.DatasourcesProperties;
import com.guavasoft.springbatch.dashboard.model.EnvironmentInfo;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final DatasourcesProperties datasourcesProperties;

    public List<EnvironmentInfo> getEnvironments() {
        return datasourcesProperties.getDatasources().stream()
            .map(entry -> new EnvironmentInfo(entry.getName(), entry.getType().name()))
            .sorted(Comparator.comparing(EnvironmentInfo::name))
            .toList();
    }
}
