package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.Durations;
import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.JobStatusSlice;
import com.guavasoft.springbatch.dashboard.service.JobExecutionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobExecutionControllerTest {

    private static final int DEFAULT_WINDOW = 7;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobExecutionService jobExecutionService;

    @Test
    void returnsJobCounts() throws Exception {
        when(jobExecutionService.getCounts(DEFAULT_WINDOW)).thenReturn(new ExecutionCounts(50, 40, 5, 5));

        mockMvc.perform(get("/api/overview/job-counts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(50))
            .andExpect(jsonPath("$.completed").value(40))
            .andExpect(jsonPath("$.failed").value(5))
            .andExpect(jsonPath("$.started").value(5));
    }

    @Test
    void returnsJobCountsWithExplicitWindow() throws Exception {
        when(jobExecutionService.getCounts(30)).thenReturn(new ExecutionCounts(0, 0, 0, 0));

        mockMvc.perform(get("/api/overview/job-counts").param("window", "30"))
            .andExpect(status().isOk());

        verify(jobExecutionService).getCounts(30);
    }

    @Test
    void rejectsJobCountsWindowAboveMax() throws Exception {
        mockMvc.perform(get("/api/overview/job-counts").param("window", "91"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsRuntime() throws Exception {
        when(jobExecutionService.getRuntime(DEFAULT_WINDOW)).thenReturn(new Durations(120L, 600L));

        mockMvc.perform(get("/api/overview/runtime"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageSeconds").value(120))
            .andExpect(jsonPath("$.longestSeconds").value(600));
    }

    @Test
    void returnsJobStatusChart() throws Exception {
        when(jobExecutionService.getStatusChart(DEFAULT_WINDOW)).thenReturn(List.of(
            new JobStatusSlice(0, "Completed", 40, "#0a0"),
            new JobStatusSlice(1, "Failed", 5, "#a00")));

        mockMvc.perform(get("/api/overview/job-status-chart"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].label").value("Completed"))
            .andExpect(jsonPath("$[0].value").value(40))
            .andExpect(jsonPath("$[1].label").value("Failed"));
    }
}
