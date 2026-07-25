package com.example.pulsedesktickettriage.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stub")
class CommentApiErrorHandlingTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void malformed_json_body_returns_400_not_500() throws Exception {
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{text: }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is not valid JSON"));
    }

    @Test
    void blank_text_returns_400_with_field_error() throws Exception {
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.text").value("text must not be blank"));
    }
}
