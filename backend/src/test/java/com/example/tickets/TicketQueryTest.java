package com.example.tickets;

import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketQueryTest extends AbstractIntegrationTest {

    @BeforeEach
    void seedData() {
        // 13 tickets that match search=login & status=OPEN & priority=HIGH
        for (int i = 1; i <= 13; i++) {
            save("Login failure #" + i, "user" + i + "@acme.com", TicketStatus.OPEN, TicketPriority.HIGH,
                    BASE_TIME.plusHours(i));
        }
        // near misses: each one violates exactly one criterion
        save("Login problem resolved", "a@other.com", TicketStatus.RESOLVED, TicketPriority.HIGH, BASE_TIME.plusDays(1));
        save("Login low priority", "b@other.com", TicketStatus.OPEN, TicketPriority.LOW, BASE_TIME.plusDays(2));
        save("Billing question", "c@other.com", TicketStatus.OPEN, TicketPriority.HIGH, BASE_TIME.plusDays(3));
    }

    @Test
    void searchStatusPriorityAndPaginationWorkTogether() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .param("search", "LOGIN")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .param("sort", "newest")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(13)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.data[0].title", is("Login failure #13")))
                .andExpect(jsonPath("$.data[9].title", is("Login failure #4")));

        mockMvc.perform(get("/api/tickets")
                        .param("search", "login").param("status", "OPEN").param("priority", "HIGH")
                        .param("sort", "newest").param("page", "2").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(13)))
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].title", is("Login failure #3")))
                .andExpect(jsonPath("$.data[2].title", is("Login failure #1")));
    }

    @Test
    void sortOldestReversesOrder() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .param("search", "login").param("status", "OPEN").param("priority", "HIGH")
                        .param("sort", "oldest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title", is("Login failure #1")))
                .andExpect(jsonPath("$.data[9].title", is("Login failure #10")));
    }

    @Test
    void defaultsAreNewestFirstPage1Size10() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(16)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.data[0].title", is("Billing question")));
    }

    @Test
    void searchMatchesCustomerEmailCaseInsensitivelyAndPartially() throws Exception {
        mockMvc.perform(get("/api/tickets").param("search", "USER5@ACME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].customerEmail", is("user5@acme.com")));

        mockMvc.perform(get("/api/tickets").param("search", "other.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(3)));
    }

    @Test
    void statusAndPriorityFiltersWithoutSearch() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "RESOLVED"))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].title", is("Login problem resolved")));

        mockMvc.perform(get("/api/tickets").param("priority", "LOW"))
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].title", is("Login low priority")));
    }

    @Test
    void likeWildcardsInSearchAreTreatedLiterally() throws Exception {
        mockMvc.perform(get("/api/tickets").param("search", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
        mockMvc.perform(get("/api/tickets").param("search", "_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    void pageBeyondLastReturnsEmptyData() throws Exception {
        mockMvc.perform(get("/api/tickets").param("page", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.total", is(16)))
                .andExpect(jsonPath("$.page", is(9)));
    }

    @Test
    void badParametersReturn400() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details[0].field", is("status")));

        mockMvc.perform(get("/api/tickets").param("priority", "URGENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("priority")));

        mockMvc.perform(get("/api/tickets").param("sort", "random"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("sort")));

        mockMvc.perform(get("/api/tickets").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("page")));

        mockMvc.perform(get("/api/tickets").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("page")));

        mockMvc.perform(get("/api/tickets").param("pageSize", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field", is("pageSize")));
    }
}
