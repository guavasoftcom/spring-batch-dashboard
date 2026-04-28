package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.DurationSummary;
import com.guavasoft.springbatch.dashboard.model.IoSummary;
import com.guavasoft.springbatch.dashboard.model.JobExecutionStepCounts;
import com.guavasoft.springbatch.dashboard.model.StepDetail;
import com.guavasoft.springbatch.dashboard.model.StepDetailPage;
import com.guavasoft.springbatch.dashboard.model.StepDuration;
import com.guavasoft.springbatch.dashboard.service.JobExecutionStepsService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobExecutionStepsController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobExecutionStepsControllerTest {

    private static final long EXEC_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobExecutionStepsService service;

    @Test
    void returnsStepCounts() throws Exception {
        when(service.getStepCounts(EXEC_ID)).thenReturn(new JobExecutionStepCounts(5, 3, 1, 1));

        mockMvc.perform(get("/api/job-executions/{id}/summary/steps", EXEC_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSteps").value(5))
            .andExpect(jsonPath("$.completed").value(3))
            .andExpect(jsonPath("$.failed").value(1))
            .andExpect(jsonPath("$.active").value(1));
    }

    @Test
    void returnsIoSummary() throws Exception {
        when(service.getIoSummary(EXEC_ID)).thenReturn(new IoSummary(500, 450));

        mockMvc.perform(get("/api/job-executions/{id}/summary/io", EXEC_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRead").value(500))
            .andExpect(jsonPath("$.totalWrite").value(450));
    }

    @Test
    void returnsDurationSummary() throws Exception {
        when(service.getDurationSummary(EXEC_ID)).thenReturn(new DurationSummary(720L));

        mockMvc.perform(get("/api/job-executions/{id}/summary/duration", EXEC_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDurationSeconds").value(720));
    }

    @Test
    void returnsStepDurations() throws Exception {
        when(service.getStepDurations(EXEC_ID)).thenReturn(List.of(
            new StepDuration("step1", 30),
            new StepDuration("step2", 45)));

        mockMvc.perform(get("/api/job-executions/{id}/step-durations", EXEC_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].stepName").value("step1"))
            .andExpect(jsonPath("$[1].durationSeconds").value(45));
    }

    @Test
    void returnsStepDetailsWithDefaultPaging() throws Exception {
        StepDetail detail = new StepDetail(1, "step1", "COMPLETED", 100, 95, 1, 0,
            30, "2026-04-27T10:00:00Z", "2026-04-27T10:00:30Z", "COMPLETED", null, Map.of());
        when(service.getStepDetails(eq(EXEC_ID), eq("startTime"), eq("desc"), eq(0), eq(10)))
            .thenReturn(new StepDetailPage(List.of(detail), 0, 10, 1));

        mockMvc.perform(get("/api/job-executions/{id}/steps", EXEC_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].stepName").value("step1"));

        verify(service).getStepDetails(EXEC_ID, "startTime", "desc", 0, 10);
    }

    @Test
    void clampsStepDetailsPagingBounds() throws Exception {
        when(service.getStepDetails(eq(EXEC_ID), eq("status"), eq("asc"), eq(0), eq(100)))
            .thenReturn(new StepDetailPage(List.of(), 0, 100, 0));

        mockMvc.perform(get("/api/job-executions/{id}/steps", EXEC_ID)
                .param("sortBy", "status")
                .param("sortDir", "asc")
                .param("page", "-5")
                .param("size", "9999"))
            .andExpect(status().isOk());

        verify(service).getStepDetails(EXEC_ID, "status", "asc", 0, 100);
    }
}
