package com.example.tickets.dto;

public record SummaryResponse(long total, long open, long inProgress, long resolved) {
}
