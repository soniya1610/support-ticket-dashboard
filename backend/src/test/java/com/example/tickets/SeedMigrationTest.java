package com.example.tickets;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import com.example.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uses its own in-memory database (separate Spring context) so the seed data written by
 * Flyway V2 is still intact, regardless of what other test classes delete.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:seedcheck;MODE=MySQL;DB_CLOSE_DELAY=-1")
class SeedMigrationTest {

    @Autowired
    private TicketRepository repository;

    @Test
    void flywaySeedsAtLeast25VariedTickets() {
        List<Ticket> all = repository.findAll();
        assertTrue(all.size() >= 25, "expected at least 25 seeded tickets but found " + all.size());

        for (TicketStatus s : TicketStatus.values()) {
            assertTrue(all.stream().anyMatch(t -> t.getStatus() == s), "no seeded ticket with status " + s);
        }
        for (TicketPriority p : TicketPriority.values()) {
            assertTrue(all.stream().anyMatch(t -> t.getPriority() == p), "no seeded ticket with priority " + p);
        }
        long distinctDates = all.stream().map(t -> t.getCreatedAt().toLocalDate()).distinct().count();
        assertTrue(distinctDates > 10, "seed data should have varied created dates");
        assertEquals(all.size(), repository.countGroupedByStatus().stream().mapToLong(TicketRepository.StatusCount::getTotal).sum());
    }
}
