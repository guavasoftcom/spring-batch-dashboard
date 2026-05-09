package com.guavasoft.springbatch.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.guavasoft.springbatch.dashboard.config.TimestampFormatter;
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
import org.springframework.web.server.ResponseStatusException;

class JobExecutionStepsServiceTest {

    private static final long EXEC_ID = 42L;

    // Real TimestampFormatter with empty zone map; DataSourceContext is not set so it
    // falls back to UTC for all conversions in this test class.
    private static final TimestampFormatter TIMESTAMP_FORMATTER = new TimestampFormatter(Map.of());

    private final StepExecutionRepository stepExecutionRepository = mock(StepExecutionRepository.class);
    private final JobExecutionRepository jobExecutionRepository = mock(JobExecutionRepository.class);
    private final JobExecutionStepsService service =
            new JobExecutionStepsService(stepExecutionRepository, jobExecutionRepository, TIMESTAMP_FORMATTER);

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

        assertThat(timing.createTime()).isEqualTo("2026-04-30T09:15:29Z");
        assertThat(timing.startTime()).isEqualTo("2026-04-30T09:15:30Z");
        assertThat(timing.endTime()).isEqualTo("2026-04-30T09:16:00Z");
    }

    @Test
    void getExecutionTimingPassesThroughNullStartAndEnd() {
        JobExecutionEntity entity = new JobExecutionEntity();
        entity.setCreateTime(LocalDateTime.of(2026, 4, 30, 9, 15, 29));
        when(jobExecutionRepository.findById(EXEC_ID)).thenReturn(Optional.of(entity));

        JobExecutionTiming timing = service.getExecutionTiming(EXEC_ID);

        assertThat(timing.createTime()).isEqualTo("2026-04-30T09:15:29Z");
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
                "2026-04-27T09:59:59Z", "2026-04-27T10:00:00Z", "2026-04-27T10:00:30Z", "2026-04-27T10:00:30Z",
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
