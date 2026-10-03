package com.example.tickets.dto;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;

import java.time.Instant;
import java.time.ZoneOffset;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String customerEmail,
        TicketPriority priority,
        TicketStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getCustomerEmail(),
                t.getPriority(),
                t.getStatus(),
                t.getCreatedAt().toInstant(ZoneOffset.UTC),
                t.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
