package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.repository.JobInstanceRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobInstanceServiceTest {

    @Mock
    private JobInstanceRepository jobInstanceRepository;

    @InjectMocks
    private JobInstanceService jobInstanceService;

    @Test
    void getJobNamesDelegatesToRepository() {
        List<String> expected = List.of("a", "b");
        when(jobInstanceRepository.findDistinctJobNames()).thenReturn(expected);

        assertThat(jobInstanceService.getJobNames()).isSameAs(expected);
    }
}
