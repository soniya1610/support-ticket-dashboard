package com.example.tickets.dto;

import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;

/** Partial update: only fields that are present are applied. At least one is required. */
public record UpdateTicketRequest(TicketStatus status, TicketPriority priority) {
}
