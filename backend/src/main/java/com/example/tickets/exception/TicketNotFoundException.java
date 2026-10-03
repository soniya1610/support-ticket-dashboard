package com.example.tickets.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long id) {
        super("Ticket with id " + id + " was not found");
    }
}
