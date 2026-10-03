package com.example.tickets;

import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketSummaryTest extends AbstractIntegrationTest {

    @Test
    void emptyDatabaseReturnsZeroCounts() throws Exception {
        mockMvc.perform(get("/api/tickets/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.open", is(0)))
                .andExpect(jsonPath("$.inProgress", is(0)))
                .andExpect(jsonPath("$.resolved", is(0)));
    }

    @Test
    void summaryCountsEntireDatasetAndIgnoresFilters() throws Exception {
        save("A", TicketStatus.OPEN, TicketPriority.HIGH);
        save("B", TicketStatus.OPEN, TicketPriority.LOW);
        save("C", TicketStatus.OPEN, TicketPriority.MEDIUM);
        save("D", TicketStatus.IN_PROGRESS, TicketPriority.HIGH);
        save("E", TicketStatus.IN_PROGRESS, TicketPriority.LOW);
        save("F", TicketStatus.RESOLVED, TicketPriority.HIGH);

        mockMvc.perform(get("/api/tickets/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(6)))
                .andExpect(jsonPath("$.open", is(3)))
                .andExpect(jsonPath("$.inProgress", is(2)))
                .andExpect(jsonPath("$.resolved", is(1)));

        // The list narrows down, the summary does not - even if filters are sent to it.
        mockMvc.perform(get("/api/tickets").param("status", "RESOLVED").param("search", "F"))
                .andExpect(jsonPath("$.total", is(1)));

        mockMvc.perform(get("/api/tickets/summary").param("status", "RESOLVED").param("priority", "HIGH").param("search", "F"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(6)))
                .andExpect(jsonPath("$.open", is(3)))
                .andExpect(jsonPath("$.inProgress", is(2)))
                .andExpect(jsonPath("$.resolved", is(1)));
    }
}
