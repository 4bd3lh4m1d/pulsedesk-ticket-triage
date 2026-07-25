package com.example.pulsedesktickettriage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stub")
@Transactional
class CommentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode postComment(String text, String channel) throws Exception {
        String body = mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"" + text + "\", \"channel\": \"" + channel + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    @Test
    void actionable_comment_creates_ticket() throws Exception {
        JsonNode comment = postComment("the app keeps crashing when I open settings", "email");
        long commentId = comment.get("id").asLong();

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].commentId").value(commentId))
                .andExpect(jsonPath("$[0].category").value("BUG"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void non_actionable_comment_does_not_create_ticket() throws Exception {
        JsonNode comment = postComment("love the new design, great job", "chat");

        assertThat(comment.get("status").asString()).isEqualTo("ANALYZED");
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void created_comments_appear_in_comment_list() throws Exception {
        postComment("please add a dark mode feature", "email");

        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("please add a dark mode feature"))
                .andExpect(jsonPath("$[0].channel").value("email"))
                .andExpect(jsonPath("$[0].status").value("ANALYZED"));
    }

    @Test
    void unknown_ticket_returns_404_with_message() throws Exception {
        mockMvc.perform(get("/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket 999 not found"));
    }
}
