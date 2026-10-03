package com.example.tickets.repository;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TicketSpecifications {

    /** Escape character used in LIKE patterns. '!' is used because '\' is awkward across MySQL modes. */
    private static final char ESCAPE = '!';

    private TicketSpecifications() {
    }

    /**
     * Combines optional free-text search (title OR customer email, case-insensitive, partial match)
     * with optional status and priority filters. Absent criteria are simply not applied.
     */
    public static Specification<Ticket> withFilters(String search, TicketStatus status, TicketPriority priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + escapeLike(search.trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("title")), pattern, ESCAPE),
                        cb.like(cb.lower(root.<String>get("customerEmail")), pattern, ESCAPE)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Makes user input literal: '%' and '_' must not act as wildcards. */
    static String escapeLike(String raw) {
        String e = String.valueOf(ESCAPE);
        return raw
                .replace(e, e + e)
                .replace("%", e + "%")
                .replace("_", e + "_");
    }
}
