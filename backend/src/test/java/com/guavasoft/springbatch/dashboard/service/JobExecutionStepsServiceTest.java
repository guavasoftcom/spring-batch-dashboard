package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobExecutionStepsServiceTest {

    private static final long EXEC_ID = 42L;

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @InjectMocks
    private JobExecutionStepsService service;

    @Test
    void getStepCountsDelegatesToRepository() {
        JobExecutionStepCounts counts = new JobExecutionStepCounts(5, 3, 1, 1);
        when(stepExecutionRepository.countsByJobExecutionId(EXEC_ID)).thenReturn(counts);

        assertThat(service.getStepCounts(EXEC_ID)).isSameAs(counts);
    }

    @Test
    void getIoSummaryDelegatesToRepository() {
        IoSummary io = new IoSummary(500, 450);
        when(stepExecutionRepository.ioSummaryByJobExecutionId(EXEC_ID)).thenReturn(io);

        assertThat(service.getIoSummary(EXEC_ID)).isSameAs(io);
    }

    @Test
    void getDurationSummaryDelegatesToRepository() {
        DurationSummary duration = new DurationSummary(720L);
        when(stepExecutionRepository.durationSummaryByJobExecutionId(EXEC_ID)).thenReturn(duration);

        assertThat(service.getDurationSummary(EXEC_ID)).isSameAs(duration);
    }

    @Test
    void getStepDurationsDelegatesToRepository() {
        List<StepDuration> durations = List.of(new StepDuration("step1", 30));
        when(stepExecutionRepository.stepDurationsByJobExecutionId(EXEC_ID)).thenReturn(durations);

        assertThat(service.getStepDurations(EXEC_ID)).isSameAs(durations);
    }

    @Test
    void getStepDetailsCombinesDetailsAndTotalIntoPage() {
        StepDetail detail = new StepDetail(1, "step1", "COMPLETED", 100, 95, 1, 0,
            30, "2026-04-27T10:00:00Z", "2026-04-27T10:00:30Z", "COMPLETED", null, Map.of());
        when(stepExecutionRepository.stepDetailsByJobExecutionId(EXEC_ID, "startTime", "desc", 0, 10))
            .thenReturn(List.of(detail));
        when(stepExecutionRepository.countStepsByJobExecutionId(EXEC_ID)).thenReturn(7L);

        StepDetailPage page = service.getStepDetails(EXEC_ID, "startTime", "desc", 0, 10);

        assertThat(page).isEqualTo(new StepDetailPage(List.of(detail), 0, 10, 7));
    }
}
