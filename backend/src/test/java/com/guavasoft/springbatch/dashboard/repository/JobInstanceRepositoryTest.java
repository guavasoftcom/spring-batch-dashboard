package com.guavasoft.springbatch.dashboard.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@BatchRepositoryTest
class JobInstanceRepositoryTest {

    @Autowired
    private JobInstanceRepository jobInstanceRepository;

    @Test
    void findDistinctJobNamesReturnsAlphabeticallyOrderedNames() {
        List<String> names = jobInstanceRepository.findDistinctJobNames();

        assertThat(names).containsExactly("dailyImportJob", "reconcileLedgerJob", "sendDigestEmailJob");
    }

    @Test
    void countMatchesSeededInstances() {
        // 90 dailyImportJob + 30 reconcileLedgerJob + 12 sendDigestEmailJob = 132
        assertThat(jobInstanceRepository.count()).isEqualTo(132);
    }
}
