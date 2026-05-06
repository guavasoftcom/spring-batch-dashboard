package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.JobLastRun;
import com.guavasoft.springbatch.dashboard.model.JobRun;
import com.guavasoft.springbatch.dashboard.service.JobLastRunsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobLastRunsController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobLastRunsControllerTest {

    private static final int DEFAULT_WINDOW = 7;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLastRunsService jobLastRunsService;

    @Test
    void returnsJobLastRuns() throws Exception {
        JobRun importRun = new JobRun(101L, "COMPLETED", "2026-04-30 09:15:30", "2026-04-30 09:16:30",
                60L, 1000L, 950L, "COMPLETED");
        List<JobLastRun> jobLastRuns = List.of(
                new JobLastRun("importUsersJob", importRun),
                new JobLastRun("reconcileLedgerJob", null));
        when(jobLastRunsService.getJobLastRuns(DEFAULT_WINDOW)).thenReturn(jobLastRuns);

        mockMvc.perform(get("/api/overview/job-last-runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].jobName").value("importUsersJob"))
            .andExpect(jsonPath("$[0].run.executionId").value(101))
            .andExpect(jsonPath("$[0].run.status").value("COMPLETED"))
            .andExpect(jsonPath("$[0].run.readCount").value(1000))
            .andExpect(jsonPath("$[0].run.writeCount").value(950))
            .andExpect(jsonPath("$[1].jobName").value("reconcileLedgerJob"))
            .andExpect(jsonPath("$[1].run").doesNotExist());
    }

    @Test
    void usesExplicitWindow() throws Exception {
        when(jobLastRunsService.getJobLastRuns(30)).thenReturn(List.of());

        mockMvc.perform(get("/api/overview/job-last-runs").param("window", "30"))
            .andExpect(status().isOk());

        verify(jobLastRunsService).getJobLastRuns(30);
    }

    @Test
    void rejectsWindowAboveMax() throws Exception {
        mockMvc.perform(get("/api/overview/job-last-runs").param("window", "91"))
            .andExpect(status().isBadRequest());
    }
}
