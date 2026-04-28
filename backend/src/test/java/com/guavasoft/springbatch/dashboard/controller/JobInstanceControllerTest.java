package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.service.JobInstanceService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JobInstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobInstanceService jobInstanceService;

    @Test
    void returnsJobNames() throws Exception {
        when(jobInstanceService.getJobNames()).thenReturn(List.of("importUsers", "billingJob"));

        mockMvc.perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0]").value("importUsers"))
            .andExpect(jsonPath("$[1]").value("billingJob"));
    }
}
