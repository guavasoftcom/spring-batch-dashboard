package com.guavasoft.springbatch.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.guavasoft.springbatch.dashboard.model.EnvironmentInfo;
import com.guavasoft.springbatch.dashboard.service.EnvironmentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EnvironmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentService environmentService;

    @Test
    void returnsEnvironmentInfoList() throws Exception {
        when(environmentService.getEnvironments()).thenReturn(List.of(
            new EnvironmentInfo("prod", "POSTGRESQL"),
            new EnvironmentInfo("staging", "MYSQL")));

        mockMvc.perform(get("/api/environments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("prod"))
            .andExpect(jsonPath("$[0].type").value("POSTGRESQL"))
            .andExpect(jsonPath("$[1].name").value("staging"))
            .andExpect(jsonPath("$[1].type").value("MYSQL"));
    }
}
