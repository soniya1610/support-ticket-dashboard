package com.example.tickets.repository;

import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    /** Single grouped query: one row per status that has at least one ticket. */
    @Query("select t.status as status, count(t) as total from Ticket t group by t.status")
    List<StatusCount> countGroupedByStatus();

    interface StatusCount {
        TicketStatus getStatus();

        long getTotal();
    }
}
