package com.example.pulsedesktickettriage.ai;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuggingFaceAiAnalysisServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpServer server;
    private volatile long bodyDelayMillis;
    private volatile boolean wrapContentInFences;

    @BeforeEach
    void startFakeHuggingFace() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            byte[] body = chatCompletionJson().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try {
                Thread.sleep(bodyDelayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopFakeHuggingFace() {
        server.stop(0);
    }

    @Test
    void analyze_parses_result_when_body_arrives_within_read_timeout() {
        bodyDelayMillis = 200;

        TicketAnalysisResult result = service(5_000).analyze("the app crashes");

        assertThat(result.shouldCreateTicket()).isTrue();
        assertThat(result.title()).isEqualTo("App crashes");
    }

    @Test
    void analyze_strips_code_fences_around_the_json_content() {
        bodyDelayMillis = 0;
        wrapContentInFences = true;

        TicketAnalysisResult result = service(5_000).analyze("the app crashes");

        assertThat(result.shouldCreateTicket()).isTrue();
        assertThat(result.title()).isEqualTo("App crashes");
    }

    @Test
    void analyze_throws_rest_client_exception_when_body_slower_than_read_timeout() {
        bodyDelayMillis = 2_000;

        assertThatThrownBy(() -> service(300).analyze("the app crashes"))
                .isInstanceOf(RestClientException.class);
    }

    private HuggingFaceAiAnalysisService service(long readTimeoutMs) {
        String url = "http://localhost:" + server.getAddress().getPort() + "/v1/chat/completions";
        return new HuggingFaceAiAnalysisService(url, "test-token", "test-model", readTimeoutMs, MAPPER);
    }

    private String chatCompletionJson() {
        String content = """
                {"shouldCreateTicket": true, "title": "App crashes", "category": "BUG",
                 "priority": "HIGH", "summary": "User reported a crash"}""";
        if (wrapContentInFences) {
            content = "```json\n" + content + "\n```";
        }
        return MAPPER.writeValueAsString(Map.of(
                "choices", List.of(Map.of(
                        "message", Map.of("role", "assistant", "content", content)))));
    }
}
