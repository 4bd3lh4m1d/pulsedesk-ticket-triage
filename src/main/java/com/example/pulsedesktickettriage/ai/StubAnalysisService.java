package com.example.pulsedesktickettriage.ai;

import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("stub")
public class StubAnalysisService implements AiAnalysisService {

    @Override
    public TicketAnalysisResult analyze(String commentText) {
        String lower = commentText.toLowerCase();

        if (lower.contains("crash") || lower.contains("error") || lower.contains("broken")) {
            return new TicketAnalysisResult(true, "Bug report: " + truncate(commentText),
                    TicketCategory.BUG, TicketPriority.HIGH,
                    "User reported a crash/error: " + truncate(commentText));
        }
        if (lower.contains("charge") || lower.contains("bill") || lower.contains("refund")) {
            return new TicketAnalysisResult(true, "Billing issue: " + truncate(commentText),
                    TicketCategory.BILLING, TicketPriority.MEDIUM,
                    "User reported a billing concern: " + truncate(commentText));
        }
        if (lower.contains("feature") || lower.contains("please add") || lower.contains("would be nice")) {
            return new TicketAnalysisResult(true, "Feature request: " + truncate(commentText),
                    TicketCategory.FEATURE, TicketPriority.LOW,
                    "User requested a feature: " + truncate(commentText));
        }
        return TicketAnalysisResult.noTicket();
    }

    private String truncate(String text) {
        return text.length() <= 60 ? text : text.substring(0, 60) + "...";
    }
}
