package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.ExecutionCounts;
import com.guavasoft.springbatch.dashboard.model.ThroughputBar;
import com.guavasoft.springbatch.dashboard.model.ThroughputSummary;
import com.guavasoft.springbatch.dashboard.service.StepExecutionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StepExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class StepExecutionControllerTest {

    private static final int DEFAULT_WINDOW = 7;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StepExecutionService stepExecutionService;

    @Test
    void returnsStepCounts() throws Exception {
        when(stepExecutionService.getCounts(DEFAULT_WINDOW)).thenReturn(new ExecutionCounts(200, 180, 10, 10));

        mockMvc.perform(get("/api/overview/step-counts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(200))
            .andExpect(jsonPath("$.completed").value(180))
            .andExpect(jsonPath("$.failed").value(10))
            .andExpect(jsonPath("$.started").value(10));
    }

    @Test
    void returnsStepCountsWithExplicitWindow() throws Exception {
        when(stepExecutionService.getCounts(60)).thenReturn(new ExecutionCounts(0, 0, 0, 0));

        mockMvc.perform(get("/api/overview/step-counts").param("window", "60"))
            .andExpect(status().isOk());

        verify(stepExecutionService).getCounts(60);
    }

    @Test
    void rejectsStepCountsWindowAboveMax() throws Exception {
        mockMvc.perform(get("/api/overview/step-counts").param("window", "91"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsThroughput() throws Exception {
        when(stepExecutionService.getThroughput(DEFAULT_WINDOW)).thenReturn(new ThroughputSummary(1000, 950));

        mockMvc.perform(get("/api/overview/throughput"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.readCount").value(1000))
            .andExpect(jsonPath("$.writeCount").value(950));
    }

    @Test
    void returnsProcessingMetrics() throws Exception {
        when(stepExecutionService.getProcessingMetrics(DEFAULT_WINDOW)).thenReturn(List.of(
            new ThroughputBar("read", 1000),
            new ThroughputBar("write", 950),
            new ThroughputBar("commit", 100)));

        mockMvc.perform(get("/api/overview/processing-metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].metric").value("read"))
            .andExpect(jsonPath("$[0].value").value(1000))
            .andExpect(jsonPath("$[2].metric").value("commit"));
    }
}
