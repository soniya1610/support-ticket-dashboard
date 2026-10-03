package com.example.tickets;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketUpdateTest extends AbstractIntegrationTest {

    @Test
    void patchStatusAndPriorityPersists() throws Exception {
        Ticket ticket = save("Broken login", TicketStatus.OPEN, TicketPriority.LOW);
        Long id = ticket.getId();

        mockMvc.perform(patch("/api/tickets/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.priority", is("HIGH")));

        // a fresh read through the API and straight from the database both see the change
        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.priority", is("HIGH")));

        Ticket reloaded = repository.findById(id).orElseThrow();
        assertEquals(TicketStatus.IN_PROGRESS, reloaded.getStatus());
        assertEquals(TicketPriority.HIGH, reloaded.getPriority());
        assertFalse(reloaded.getUpdatedAt().isBefore(reloaded.getCreatedAt()));
    }

    @Test
    void patchOnlyStatusLeavesPriorityUntouched() throws Exception {
        Ticket ticket = save("Slow page", TicketStatus.OPEN, TicketPriority.MEDIUM);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")));
    }

    @Test
    void patchOnlyPriorityLeavesStatusUntouched() throws Exception {
        Ticket ticket = save("Slow page", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.priority", is("LOW")));
    }

    @Test
    void patchUnknownIdReturns404() throws Exception {
        mockMvc.perform(patch("/api/tickets/{id}", 999999).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
    }

    @Test
    void patchInvalidEnumValueReturns400AndChangesNothing() throws Exception {
        Ticket ticket = save("Broken login", TicketStatus.OPEN, TicketPriority.LOW);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details[0].field", is("status")));

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("priority")));

        assertEquals(TicketStatus.OPEN, repository.findById(ticket.getId()).orElseThrow().getStatus());
    }

    @Test
    void patchWithNoUpdatableFieldReturns400() throws Exception {
        Ticket ticket = save("Broken login", TicketStatus.OPEN, TicketPriority.LOW);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
    }

    @Test
    void patchCannotChangeOtherFields() throws Exception {
        Ticket ticket = save("Broken login", TicketStatus.OPEN, TicketPriority.LOW);

        mockMvc.perform(patch("/api/tickets/{id}", ticket.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hacked\",\"status\":\"RESOLVED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("title")));

        assertEquals("Broken login", repository.findById(ticket.getId()).orElseThrow().getTitle());
    }
}
