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

        assertThat(names).containsExactly("importUsersJob", "reconcileLedgerJob", "sendDigestEmailJob");
    }

    @Test
    void countMatchesSeededInstances() {
        assertThat(jobInstanceRepository.count()).isEqualTo(4);
    }
}
