package com.example.tickets.dto;

import com.example.tickets.model.TicketPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        @NotBlank(message = "Customer email is required")
        @Size(max = 254, message = "Customer email must be at most 254 characters")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Customer email must be a valid email address")
        String customerEmail,

        @NotNull(message = "Priority is required (LOW, MEDIUM or HIGH)")
        TicketPriority priority
) {
}
