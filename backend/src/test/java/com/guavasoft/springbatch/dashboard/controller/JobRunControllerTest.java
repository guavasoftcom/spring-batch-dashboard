package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.AvgDuration;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.model.JobRunPage;
import com.guavasoft.springbatch.dashboard.model.RunCounts;
import com.guavasoft.springbatch.dashboard.model.SuccessRate;
import com.guavasoft.springbatch.dashboard.service.JobRunService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobRunController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobRunControllerTest {

    private static final String JOB_ID = "importUsers";
    private static final int DEFAULT_WINDOW = 7;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobRunService jobRunService;

    @Test
    void returnsCounts() throws Exception {
        when(jobRunService.getCounts(JOB_ID, DEFAULT_WINDOW)).thenReturn(new RunCounts(20, 18, 1, 19));

        mockMvc.perform(get("/api/jobs/{jobId}/runs/counts", JOB_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(20))
            .andExpect(jsonPath("$.completed").value(18))
            .andExpect(jsonPath("$.failed").value(1))
            .andExpect(jsonPath("$.finished").value(19));
    }

    @Test
    void returnsCountsWithExplicitWindow() throws Exception {
        when(jobRunService.getCounts(JOB_ID, 30)).thenReturn(new RunCounts(5, 5, 0, 5));

        mockMvc.perform(get("/api/jobs/{jobId}/runs/counts", JOB_ID).param("window", "30"))
            .andExpect(status().isOk());

        verify(jobRunService).getCounts(JOB_ID, 30);
    }

    @Test
    void returnsSuccessRate() throws Exception {
        when(jobRunService.getSuccessRate(JOB_ID, DEFAULT_WINDOW)).thenReturn(SuccessRate.of(18, 19));

        mockMvc.perform(get("/api/jobs/{jobId}/runs/success-rate", JOB_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(18))
            .andExpect(jsonPath("$.finished").value(19))
            .andExpect(jsonPath("$.successRate").value(95));
    }

    @Test
    void returnsAvgDuration() throws Exception {
        when(jobRunService.getAvgDuration(JOB_ID, DEFAULT_WINDOW)).thenReturn(new AvgDuration(120));

        mockMvc.perform(get("/api/jobs/{jobId}/runs/avg-duration", JOB_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageSeconds").value(120));
    }

    @Test
    void returnsLastRun() throws Exception {
        JobRun run = new JobRun(7, "COMPLETED",
            "2026-04-27T09:00:00Z", "2026-04-27T09:01:00Z",
            60, 100, 95, "COMPLETED");
        when(jobRunService.getLastRun(JOB_ID, DEFAULT_WINDOW)).thenReturn(run);

        mockMvc.perform(get("/api/jobs/{jobId}/runs/last", JOB_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.executionId").value(7))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.durationSeconds").value(60));
    }

    @Test
    void returnsLastRunNullWhenAbsent() throws Exception {
        when(jobRunService.getLastRun(JOB_ID, DEFAULT_WINDOW)).thenReturn(null);

        mockMvc.perform(get("/api/jobs/{jobId}/runs/last", JOB_ID))
            .andExpect(status().isOk());
    }

    @Test
    void listsRunsWithDefaults() throws Exception {
        JobRun run = new JobRun(1, "COMPLETED",
            "2026-04-27T09:00:00Z", "2026-04-27T09:01:00Z",
            60, 100, 95, "COMPLETED");
        when(jobRunService.getRuns(eq(JOB_ID), eq("executionId"), eq("desc"), eq(0), eq(20), eq(DEFAULT_WINDOW)))
            .thenReturn(new JobRunPage(List.of(run), 0, 20, 1));

        mockMvc.perform(get("/api/jobs/{jobId}/runs", JOB_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].executionId").value(1));

        verify(jobRunService).getRuns(JOB_ID, "executionId", "desc", 0, 20, DEFAULT_WINDOW);
    }

    @Test
    void clampsRunListPaging() throws Exception {
        when(jobRunService.getRuns(eq(JOB_ID), eq("startTime"), eq("asc"), eq(0), eq(100), eq(DEFAULT_WINDOW)))
            .thenReturn(new JobRunPage(List.of(), 0, 100, 0));

        mockMvc.perform(get("/api/jobs/{jobId}/runs", JOB_ID)
                .param("sortBy", "startTime")
                .param("sortDir", "asc")
                .param("page", "-1")
                .param("size", "5000"))
            .andExpect(status().isOk());

        verify(jobRunService).getRuns(JOB_ID, "startTime", "asc", 0, 100, DEFAULT_WINDOW);
    }

    @Test
    void listsRunsWithExplicitWindow() throws Exception {
        when(jobRunService.getRuns(eq(JOB_ID), eq("executionId"), eq("desc"), eq(0), eq(20), eq(60)))
            .thenReturn(new JobRunPage(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/jobs/{jobId}/runs", JOB_ID).param("window", "60"))
            .andExpect(status().isOk());

        verify(jobRunService).getRuns(JOB_ID, "executionId", "desc", 0, 20, 60);
    }

    @Test
    void rejectsCountsWindowBelowMin() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/runs/counts", JOB_ID).param("window", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectsCountsWindowAboveMax() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/runs/counts", JOB_ID).param("window", "91"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsTrendWithDefaultWindow() throws Exception {
        when(jobRunService.getRunsTrend(JOB_ID, DEFAULT_WINDOW)).thenReturn(List.of());

        mockMvc.perform(get("/api/jobs/{jobId}/runs/trend", JOB_ID))
            .andExpect(status().isOk());

        verify(jobRunService).getRunsTrend(JOB_ID, DEFAULT_WINDOW);
    }

    @Test
    void rejectsTrendWindowBelowMin() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/runs/trend", JOB_ID).param("window", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectsTrendWindowAboveMax() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/runs/trend", JOB_ID).param("window", "91"))
            .andExpect(status().isBadRequest());
    }
}
