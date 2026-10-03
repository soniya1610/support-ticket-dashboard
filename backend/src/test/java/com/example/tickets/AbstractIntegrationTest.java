package com.example.tickets;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import com.example.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

/**
 * Shared setup: full application context on H2 (profile "test").
 * The Flyway seed data is wiped before every test so each test controls its own dataset.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    protected static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected TicketRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    protected Ticket save(String title, String email, TicketStatus status, TicketPriority priority, LocalDateTime createdAt) {
        Ticket t = new Ticket(title, "Description for " + title, email, priority);
        t.setStatus(status);
        t.setCreatedAt(createdAt);
        return repository.saveAndFlush(t);
    }

    protected Ticket save(String title, TicketStatus status, TicketPriority priority) {
        return save(title, "someone@example.com", status, priority, BASE_TIME);
    }
}
