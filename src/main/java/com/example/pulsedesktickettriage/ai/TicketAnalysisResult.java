package com.example.pulsedesktickettriage.ai;

import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;

public record TicketAnalysisResult(
        boolean shouldCreateTicket,
        String title,
        TicketCategory category,
        TicketPriority priority,
        String summary
) {
    public static TicketAnalysisResult noTicket(){
        return new TicketAnalysisResult(false,null,null,null,null);
    }
}
