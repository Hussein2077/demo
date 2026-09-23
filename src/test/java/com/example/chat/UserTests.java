package com.example.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchUsers_shouldFindMatchingUsers_andExcludeCurrentUser() throws Exception {
        // Current user is Ahmed (id = 1). Searching for "Mohamed" should return Mohamed Ali (id = 2)
        // and should exclude Ahmed even though Ahmed's displayName is "Ahmed Mohamed"
        mockMvc.perform(get("/api/users/search")
                        .param("q", "mohamed")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[*].id", not(hasItem(1))))
                .andExpect(jsonPath("$.data[*].username", hasItem("mohamed")));
    }

    @Test
    void searchUsers_missingHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("q", "ali"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
