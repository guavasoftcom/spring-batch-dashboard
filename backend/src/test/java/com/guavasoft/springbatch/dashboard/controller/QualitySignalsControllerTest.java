package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.ProcessingTotals;
import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.service.QualitySignalsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = QualitySignalsController.class)
@AutoConfigureMockMvc(addFilters = false)
class QualitySignalsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QualitySignalsService qualitySignalsService;

    @Test
    void returnsQualitySignals() throws Exception {
        ProcessingTotals totals = new ProcessingTotals(100, 90, 10, 5, 1, 2);
        QualitySignals signals = new QualitySignals("FailureLabel", totals, "2026-04-27T10:00:00Z");
        when(qualitySignalsService.getSignals()).thenReturn(signals);

        mockMvc.perform(get("/api/overview/quality-signals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastFailure").value("FailureLabel"))
            .andExpect(jsonPath("$.latestUpdate").value("2026-04-27T10:00:00Z"))
            .andExpect(jsonPath("$.processing.readCount").value(100))
            .andExpect(jsonPath("$.processing.writeCount").value(90))
            .andExpect(jsonPath("$.processing.commitCount").value(10))
            .andExpect(jsonPath("$.processing.skipCount").value(2));
    }
}
