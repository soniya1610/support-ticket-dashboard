package com.example.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketValidationTest extends AbstractIntegrationTest {

    private static String body(String title, String description, String email, String priority) {
        return """
                {"title": %s, "description": %s, "customerEmail": %s, "priority": %s}
                """.formatted(quote(title), quote(description), quote(email), quote(priority));
    }

    private static String quote(String v) {
        return v == null ? "null" : "\"" + v + "\"";
    }

    @Test
    void createsTicketWithDefaultOpenStatus() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body("  Printer is broken  ", "Paper jam every time", "jane@example.com", "HIGH")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/tickets/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Printer is broken")))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        assertEquals(1, repository.count());
    }

    @Test
    void missingTitleReturns400WithFieldDetails() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body(null, "Some description", "jane@example.com", "LOW")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details", hasSize(1)))
                .andExpect(jsonPath("$.error.details[0].field", is("title")))
                .andExpect(jsonPath("$.error.details[0].message", is("Title is required")));

        assertEquals(0, repository.count());
    }

    @Test
    void titleLongerThan120CharactersReturns400() throws Exception {
        String tooLong = "x".repeat(121);
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body(tooLong, "Some description", "jane@example.com", "LOW")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details[0].field", is("title")))
                .andExpect(jsonPath("$.error.details[0].message", is("Title must be at most 120 characters")));
    }

    @Test
    void titleOfExactly120CharactersIsAccepted() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body("x".repeat(120), "Some description", "jane@example.com", "LOW")))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidEmailReturns400() throws Exception {
        for (String bad : new String[]{"not-an-email", "missing-tld@host", "two@@example.com"}) {
            mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                            .content(body("Title", "Some description", bad, "MEDIUM")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.error.details[0].field", is("customerEmail")))
                    .andExpect(jsonPath("$.error.details[0].message", is("Customer email must be a valid email address")));
        }
    }

    @Test
    void multipleInvalidFieldsAreAllReported() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "   ", "bad", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details", hasSize(4)))
                .andExpect(jsonPath("$.error.details[?(@.field=='title')]", hasSize(1)))
                .andExpect(jsonPath("$.error.details[?(@.field=='description')]", hasSize(1)))
                .andExpect(jsonPath("$.error.details[?(@.field=='customerEmail')]", hasSize(1)))
                .andExpect(jsonPath("$.error.details[?(@.field=='priority')]", hasSize(1)));
    }

    @Test
    void invalidPriorityEnumReturns400() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Title", "Some description", "jane@example.com", "URGENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details[0].field", is("priority")));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code", is("MALFORMED_REQUEST")));
    }
}
