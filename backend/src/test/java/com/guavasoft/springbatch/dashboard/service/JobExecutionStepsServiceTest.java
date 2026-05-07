package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.entity.JobExecutionEntity;
import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.JobExecutionTiming;
import com.guavasoft.springbatch.dashboard.model.StepCountsSummary;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.model.StepExecutionDetail;
import com.guavasoft.springbatch.dashboard.repository.JobExecutionRepository;
import com.guavasoft.springbatch.dashboard.repository.StepExecutionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class JobExecutionStepsServiceTest {

    private static final long EXEC_ID = 42L;

    @Mock
    private StepExecutionRepository stepExecutionRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

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
    void getStepCountsSummaryDelegatesToRepository() {
        StepCountsSummary counts = new StepCountsSummary(500, 450, 50, 5, 1, 2, 3, 1);
        when(stepExecutionRepository.stepCountsSummaryByJobExecutionId(EXEC_ID)).thenReturn(counts);

        assertThat(service.getStepCountsSummary(EXEC_ID)).isSameAs(counts);
    }

    @Test
    void getExecutionTimingFormatsLifecycleTimestamps() {
        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setCreateTime(LocalDateTime.of(2026, 4, 30, 9, 15, 29));
        entity.setStartTime(LocalDateTime.of(2026, 4, 30, 9, 15, 30));
        entity.setEndTime(LocalDateTime.of(2026, 4, 30, 9, 16, 0));
        when(jobExecutionRepository.findById(EXEC_ID)).thenReturn(Optional.of(entity));

        JobExecutionTiming timing = service.getExecutionTiming(EXEC_ID);

        assertThat(timing.createTime()).isEqualTo("2026-04-30 09:15:29");
        assertThat(timing.startTime()).isEqualTo("2026-04-30 09:15:30");
        assertThat(timing.endTime()).isEqualTo("2026-04-30 09:16:00");
    }

    @Test
    void getExecutionTimingPassesThroughNullStartAndEnd() {
        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setCreateTime(LocalDateTime.of(2026, 4, 30, 9, 15, 29));
        when(jobExecutionRepository.findById(EXEC_ID)).thenReturn(Optional.of(entity));

        JobExecutionTiming timing = service.getExecutionTiming(EXEC_ID);

        assertThat(timing.createTime()).isEqualTo("2026-04-30 09:15:29");
        assertThat(timing.startTime()).isNull();
        assertThat(timing.endTime()).isNull();
    }

    @Test
    void getExecutionTimingThrows404WhenMissing() {
        when(jobExecutionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExecutionTiming(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void getDurationSummaryDelegatesToRepository() {
        DurationSummary duration = new DurationSummary(720L);
        when(stepExecutionRepository.durationSummaryByJobExecutionId(EXEC_ID)).thenReturn(duration);

        assertThat(service.getDurationSummary(EXEC_ID)).isSameAs(duration);
    }

    @Test
    void getStepExecutionDetailReturnsRepositoryValue() {
        StepExecutionDetail detail = new StepExecutionDetail(
                1L, EXEC_ID, "step1", "COMPLETED",
                100, 95, 10, 0, 0, 0, 0, 0, 30,
                "2026-04-27 09:59:59", "2026-04-27 10:00:00", "2026-04-27 10:00:30", "2026-04-27 10:00:30",
                "COMPLETED", null, Map.of("checkpoint", 50));
        when(stepExecutionRepository.findStepExecutionDetail(1L)).thenReturn(Optional.of(detail));

        assertThat(service.getStepExecutionDetail(1L)).isSameAs(detail);
    }

    @Test
    void getStepExecutionDetailThrows404WhenMissing() {
        when(stepExecutionRepository.findStepExecutionDetail(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStepExecutionDetail(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void getStepDetailsCombinesDetailsAndTotalIntoPage() {
        StepDetail detail = new StepDetail(1, "step1", "COMPLETED", 100, 95, 1, 0,
            30, "2026-04-27T10:00:00Z", "2026-04-27T10:00:30Z");
        when(stepExecutionRepository.stepDetailsByJobExecutionId(EXEC_ID, "startTime", "desc", 0, 10))
            .thenReturn(List.of(detail));
        when(stepExecutionRepository.countStepsByJobExecutionId(EXEC_ID)).thenReturn(7L);

        StepDetailPage page = service.getStepDetails(EXEC_ID, "startTime", "desc", 0, 10);

        assertThat(page).isEqualTo(new StepDetailPage(List.of(detail), 0, 10, 7));
    }
}
