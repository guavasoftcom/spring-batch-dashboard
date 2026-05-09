package com.guavasoft.springbatch.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SpaController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void overviewForwardsToSpaShell() throws Exception {
        mockMvc.perform(get("/overview"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void jobDetailForwardsToSpaShell() throws Exception {
        mockMvc.perform(get("/jobs/daily-import-job"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void jobExecutionForwardsToSpaShell() throws Exception {
        mockMvc.perform(get("/jobs/daily-import-job/executions/42"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/index.html"));
    }
}
