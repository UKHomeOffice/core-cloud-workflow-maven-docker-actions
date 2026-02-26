package uk.gov.homeoffice.corecloud.dummyservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CCL-6306 – Unit tests for StatusController.
 *
 * These tests exercise the Maven 'test' phase of the pipeline and produce
 * Surefire XML reports consumed by the pipeline's test result publisher.
 */
@WebMvcTest(StatusController.class)
class StatusControllerTest {

    private static final String STATUS_PATH = "/api/status";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/status returns HTTP 200")
    void statusReturnsOk() throws Exception {
        mockMvc.perform(get(STATUS_PATH).accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/status returns expected JSON body")
    void statusBodyContainsExpectedFields() throws Exception {
        mockMvc.perform(get(STATUS_PATH).accept(MediaType.APPLICATION_JSON))
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.status").value("UP"))
               .andExpect(jsonPath("$.team").value("core-cloud-platform"))
               .andExpect(jsonPath("$.service").value("dummy-service"))
               .andExpect(jsonPath("$.ticket").value("CCL-6306"));
    }

    @Test
    @DisplayName("GET /api/status response is not empty")
    void statusBodyNotEmpty() throws Exception {
        mockMvc.perform(get(STATUS_PATH))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isNotEmpty());
    }
}