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
            .map(entry -> new EnvironmentInfo(entry.getName(), databaseTypeFromUrl(entry.getUrl())))
            .sorted(Comparator.comparing(EnvironmentInfo::name))
            .toList();
    }

    private static String databaseTypeFromUrl(String url) {
        if (url == null || !url.startsWith("jdbc:")) {
            return "UNKNOWN";
        }
        int colonAfterPrefix = url.indexOf(':', 5);
        if (colonAfterPrefix < 0) {
            return "UNKNOWN";
        }
        return url.substring(5, colonAfterPrefix).toUpperCase();
    }
}
