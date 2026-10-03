package com.example.tickets.controller;

import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.PagedResponse;
import com.example.tickets.dto.SummaryResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.dto.UpdateTicketRequest;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import com.example.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/tickets/" + created.id())).body(created);
    }

    @GetMapping
    public PagedResponse<TicketResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return service.list(search, status, priority, sort, page, pageSize);
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        return service.summary();
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @RequestBody UpdateTicketRequest request) {
        return service.update(id, request);
    }
}
