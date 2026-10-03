package com.example.tickets.service;

import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.PagedResponse;
import com.example.tickets.dto.SummaryResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.dto.UpdateTicketRequest;
import com.example.tickets.exception.InvalidRequestException;
import com.example.tickets.exception.TicketNotFoundException;
import com.example.tickets.model.Ticket;
import com.example.tickets.model.TicketPriority;
import com.example.tickets.model.TicketStatus;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.repository.TicketSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class TicketService {

    static final int MAX_PAGE_SIZE = 100;

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        Ticket ticket = new Ticket(
                request.title().trim(),
                request.description().trim(),
                request.customerEmail().trim(),
                request.priority());
        return TicketResponse.from(repository.save(ticket));
    }

    public PagedResponse<TicketResponse> list(String search, TicketStatus status, TicketPriority priority,
                                              String sort, int page, int pageSize) {
        if (page < 1) {
            throw new InvalidRequestException("page", "page must be 1 or greater");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("pageSize", "pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        Sort.Direction direction;
        if ("newest".equalsIgnoreCase(sort)) {
            direction = Sort.Direction.DESC;
        } else if ("oldest".equalsIgnoreCase(sort)) {
            direction = Sort.Direction.ASC;
        } else {
            throw new InvalidRequestException("sort", "sort must be 'newest' or 'oldest'");
        }
        // id as tie-breaker keeps pagination stable when created_at values are equal
        Sort order = Sort.by(direction, "createdAt").and(Sort.by(direction, "id"));

        Page<Ticket> result = repository.findAll(
                TicketSpecifications.withFilters(search, status, priority),
                PageRequest.of(page - 1, pageSize, order));
        return PagedResponse.from(result.map(TicketResponse::from));
    }

    public TicketResponse get(Long id) {
        return TicketResponse.from(find(id));
    }

    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest request) {
        if (request.status() == null && request.priority() == null) {
            throw new InvalidRequestException(null, "Provide at least one of 'status' or 'priority'");
        }
        Ticket ticket = find(id);
        if (request.status() != null) {
            ticket.setStatus(request.status());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        return TicketResponse.from(repository.saveAndFlush(ticket));
    }

    public SummaryResponse summary() {
        Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
        for (TicketRepository.StatusCount row : repository.countGroupedByStatus()) {
            counts.put(row.getStatus(), row.getTotal());
        }
        long open = counts.getOrDefault(TicketStatus.OPEN, 0L);
        long inProgress = counts.getOrDefault(TicketStatus.IN_PROGRESS, 0L);
        long resolved = counts.getOrDefault(TicketStatus.RESOLVED, 0L);
        return new SummaryResponse(open + inProgress + resolved, open, inProgress, resolved);
    }

    private Ticket find(Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }
}
