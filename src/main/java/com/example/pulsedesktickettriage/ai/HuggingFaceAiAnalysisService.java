package com.example.pulsedesktickettriage.ai;

import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Service
@Profile("!stub")
public class HuggingFaceAiAnalysisService implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceAiAnalysisService.class);

    private static final String SYSTEM_PROMPT = """
        You triage user feedback for a support platform called PulseDesk.
        Given a single user comment, decide whether it describes an actionable
        problem or request that should become a support ticket, as opposed to
        a compliment or a comment with nothing actionable in it.
        If it should become a ticket, write a short title (max 8 words),
        pick the category and priority that best fit, and write a one-sentence summary.
        If it should NOT become a ticket, still fill in every field with your
        best guess - the caller ignores them when shouldCreateTicket is false.
        Respond with ONLY one JSON object - no markdown, no code fences, no text
        around it - in exactly this shape:
        {"shouldCreateTicket": true or false,
         "title": "string",
         "category": "BUG" or "FEATURE" or "BILLING" or "ACCOUNT" or "OTHER",
         "priority": "LOW" or "MEDIUM" or "HIGH",
         "summary": "string"}
        """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public HuggingFaceAiAnalysisService(@Value("${huggingface.api.url}") String apiUrl,
                                        @Value("${huggingface.api.token}") String apiToken,
                                        @Value("${huggingface.api.model}") String model,
                                        @Value("${huggingface.api.read-timeout-ms}") long readTimeoutMs,
                                        ObjectMapper objectMapper) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public TicketAnalysisResult analyze(String commentText) {
        var request = new ChatCompletionRequest(
                model,
                List.of(
                        new ChatCompletionRequest.Message("system", SYSTEM_PROMPT),
                        new ChatCompletionRequest.Message("user", commentText)
                ),
                0.2
        );

        String rawBody = restClient.post()
                .body(request)
                .retrieve()
                .body(String.class);

        try {
            ChatCompletionResponse response = objectMapper.readValue(rawBody, ChatCompletionResponse.class);
            String content = response.choices().get(0).message().content();
            JsonNode node = objectMapper.readTree(extractJson(content));
            return new TicketAnalysisResult(
                    node.get("shouldCreateTicket").asBoolean(),
                    node.get("title").asText(),
                    TicketCategory.valueOf(node.get("category").asText()),
                    TicketPriority.valueOf(node.get("priority").asText()),
                    node.get("summary").asText()
            );
        } catch (Exception e) {
            log.warn("Could not parse Hugging Face response, treating as no ticket. Raw response: {}", rawBody, e);
            return TicketAnalysisResult.noTicket();
        }
    }

    private static String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        return (start >= 0 && end > start) ? content.substring(start, end + 1) : content;
    }
}
