# Support Ticket Dashboard

Full-stack app for a small support team: create tickets, track their status, and quickly find the ones that need attention.

* **Backend:** Java 17, Spring Boot 3.3 (Web, Data JPA, Validation), Flyway, MySQL (H2 for tests), Maven
* **Frontend:** React 18 + Vite, React Router, plain CSS, `fetch`
* No authentication / Spring Security (out of scope)


## Live demo
* Frontend: https://support-ticket-dashboard-nine.vercel.app

* Backend API: https://support-ticket-dashboard-production.up.railway.app/api/tickets

(The backend may take a few seconds to respond on the first request.)


## Folder Structure 

```
support-ticket-dashboard/
├── README.md
├── backend/
│   ├── pom.xml
│   ├── .env.example
│   └── src/
│       ├── main/java/com/example/tickets/
│       │   ├── TicketsApplication.java
│       │   ├── config/        CorsConfig
│       │   ├── controller/    TicketController
│       │   ├── dto/           CreateTicketRequest, UpdateTicketRequest, TicketResponse,
│       │   │                  PagedResponse, SummaryResponse, ErrorResponse
│       │   ├── exception/     TicketNotFoundException, InvalidRequestException, GlobalExceptionHandler
│       │   ├── model/         Ticket, TicketStatus, TicketPriority
│       │   ├── repository/    TicketRepository, TicketSpecifications
│       │   └── service/       TicketService
│       ├── main/resources/
│       │   ├── application.properties
│       │   └── db/migration/  V1__create_tickets_table.sql, V2__seed_tickets.sql
│       └── test/
│           ├── java/com/example/tickets/   (5 test classes, see "Tests")
│           └── resources/application-test.properties
└── frontend/
    ├── package.json, vite.config.js, index.html, .env.example
    └── src/
        ├── main.jsx, App.jsx, styles.css
        ├── api/client.js
        ├── hooks/useDebounce.js
        ├── utils/             validation.js, format.js
        ├── components/        SummaryCards, TicketFilters, TicketTable, Pagination,
        │                      TicketForm, StatusBadge, StateViews
        └── pages/             TicketListPage, CreateTicketPage, TicketDetailPage, NotFoundPage
```

## Prerequisites

* JDK 17 or newer (the project targets Java 17)
* Maven 3.9+
* Node.js 18+ (20+ recommended) and npm
* MySQL 8.x running locally (only needed to run the app; tests use in-memory H2)

## 1. Database setup (MySQL)

Create the database (the default JDBC URL also has `createDatabaseIfNotExist=true`, so this step is optional if your user may create databases):

```sql
CREATE DATABASE support_tickets CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Migrations and seed run automatically on backend startup** through Flyway:

| File | What it does |
|------|--------------|
| `V1__create_tickets_table.sql` | Creates `tickets` plus indexes on `status`, `priority`, `created_at` |
| `V2__seed_tickets.sql` | Inserts 27 realistic tickets (all statuses/priorities, dates spread over ~5 weeks) |

Flyway records applied versions in `flyway_schema_history`, so restarting never re-seeds. To start over: `DROP DATABASE support_tickets;` and recreate it. Do not edit V1/V2 after they have been applied; add `V3__...sql` instead.

## 2. Environment variables

Backend (`backend/.env.example`):

| Variable | Default | Meaning |
|----------|---------|---------|
| `DB_URL` | `jdbc:mysql://localhost:3306/support_tickets?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | *(empty)* | DB password |
| `CORS_ALLOWED_ORIGIN` | `http://localhost:5173` | Allowed browser origin(s), comma separated |
| `SERVER_PORT` | `8080` | API port |

Spring Boot does not read `.env` files by itself. Either export the variables or load the file into your shell:

```bash
cd backend
cp .env.example .env        # then edit DB_PASSWORD etc.
set -a; source .env; set +a # bash/zsh; values with '&' are quoted in the example
```

Frontend (`frontend/.env.example`): `VITE_API_BASE_URL=http://localhost:8080`. Copy it to `frontend/.env` if your API is not on that address.

## 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

API: `http://localhost:8080/api/tickets`. First startup creates the schema and seed data.

## 4. Run the frontend

```bash
cd frontend
cp .env.example .env     # optional when using the default API URL
npm install
npm run dev
```

Open `http://localhost:5173`. Production build: `npm run build` (output in `frontend/dist`).

## 5. Run the tests

```bash
cd backend
mvn test
```

Tests use `@SpringBootTest` + MockMvc on an in-memory **H2** database (`application-test.properties`, MySQL compatibility mode) with the **same Flyway migrations** as production. No MySQL is needed.

| Test class | Covers |
|------------|--------|
| `TicketValidationTest` | missing title, title > 120 (and exactly 120 accepted), invalid emails, several errors at once, invalid enum, malformed JSON, successful create (201, default `OPEN`, `Location` header) |
| `TicketQueryTest` | search + status + priority + sort + pagination together (13 matches over 2 pages), oldest/newest, defaults, case-insensitive partial match on title **and** email, literal `%`/`_` in search, out-of-range page, bad params -> 400 |
| `TicketUpdateTest` | PATCH status/priority persists (checked via API and DB), partial patches, unknown id -> 404, invalid enum -> 400 (nothing changed), empty body -> 400, unknown fields (e.g. `title`) rejected |
| `TicketSummaryTest` | grouped counts, zero counts on empty DB, counts **not** affected by filters (also when filters are sent to `/summary`) |
| `SeedMigrationTest` | Flyway seed has >= 25 tickets with varied status/priority/dates (own isolated H2 database) |

## API reference and sample curl commands

Base URL `http://localhost:8080`. `page` is **1-based**; `pageSize` is 1-100 (default 10); `sort` is `newest` (default) or `oldest`.

```bash
# Create (201)
curl -i -X POST http://localhost:8080/api/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"Cannot export report","description":"Export fails with a timeout","customerEmail":"jane@example.com","priority":"HIGH"}'

# Create with validation errors (400, VALIDATION_ERROR with field details)
curl -i -X POST http://localhost:8080/api/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"","description":"x","customerEmail":"nope","priority":"HIGH"}'

# List: defaults (newest first, page 1, 10 per page)
curl 'http://localhost:8080/api/tickets'

# List: search + filters + sort + pagination together
curl 'http://localhost:8080/api/tickets?search=password&status=OPEN&priority=HIGH&sort=oldest&page=1&pageSize=10'

# List: search by customer email fragment (case-insensitive)
curl 'http://localhost:8080/api/tickets?search=NORTHWIND'

# List: bad enum value (400)
curl -i 'http://localhost:8080/api/tickets?status=DONE'

# Get one (200) / missing (404)
curl http://localhost:8080/api/tickets/1
curl -i http://localhost:8080/api/tickets/999999

# Update status and/or priority (200)
curl -X PATCH http://localhost:8080/api/tickets/1 \
  -H 'Content-Type: application/json' -d '{"status":"IN_PROGRESS","priority":"HIGH"}'

# Update with invalid enum (400) / unknown id (404)
curl -i -X PATCH http://localhost:8080/api/tickets/1 -H 'Content-Type: application/json' -d '{"status":"DONE"}'
curl -i -X PATCH http://localhost:8080/api/tickets/999999 -H 'Content-Type: application/json' -d '{"status":"RESOLVED"}'

# Summary counts for the entire dataset (200)
curl http://localhost:8080/api/tickets/summary
```

Response shapes:

```jsonc
// GET /api/tickets
{ "data": [ { "id": 1, "title": "...", "description": "...", "customerEmail": "...",
              "priority": "HIGH", "status": "OPEN",
              "createdAt": "2026-09-30T08:42:00Z", "updatedAt": "2026-09-30T08:42:00Z" } ],
  "page": 1, "pageSize": 10, "total": 27, "totalPages": 3 }

// GET /api/tickets/summary
{ "total": 27, "open": 11, "inProgress": 7, "resolved": 9 }

// any error
{ "error": { "code": "VALIDATION_ERROR", "message": "Request validation failed",
             "details": [ { "field": "title", "message": "Title is required" } ] } }
```

Error codes: `VALIDATION_ERROR` (400), `MALFORMED_REQUEST` (400), `NOT_FOUND` (404), `METHOD_NOT_ALLOWED` (405), `UNSUPPORTED_MEDIA_TYPE` (415), `INTERNAL_ERROR` (500).

## Manual test checklist

**Create ticket**
- [ ] `/tickets/new`: submitting an empty form shows "Title is required", "Description is required", "Customer email is required".
- [ ] A title of 121 characters shows the length error and the counter turns red; 120 is accepted.
- [ ] `abc` and `abc@host` are rejected as invalid emails; `abc@host.com` is accepted.
- [ ] A valid submit redirects to the new ticket's detail page; the ticket has status Open.
- [ ] Backend-side errors: stop validating on the client (e.g. use the curl "validation errors" command) and confirm the API returns field details; in the UI, field messages from the API appear next to the matching inputs.

**List page**
- [ ] Summary cards show Total / Open / In Progress / Resolved; they do **not** change when you search or filter.
- [ ] Typing in search waits ~400 ms before the request (watch the Network tab) and matches title or email, case-insensitive, partial.
- [ ] Status and priority filters narrow the list; combined with search they all apply together.
- [ ] Sort "Oldest first"/"Newest first" reverses the order.
- [ ] Pagination: 10 per page, Previous/Next disabled at the ends, "Showing X-Y of N" is correct; changing a filter returns to page 1.
- [ ] The URL reflects search, status, priority, sort, page; reloading or pasting the URL restores the same view; browser Back/Forward works.
- [ ] Empty state: search for `zzzzzz` -> "No tickets match..." with a "Clear filters" button.
- [ ] Loading state: throttle the network in dev tools -> spinner. Error state: stop the backend and reload -> error message with "Try again"; restart the backend and click it.
- [ ] Resize to a phone width: the table becomes stacked cards; no horizontal scrolling.

**Detail page**
- [ ] Clicking a ticket shows all fields (customer, status, priority, created, updated, description).
- [ ] Change status and/or priority -> "Save changes" enables -> "Changes saved."; refresh the page and the values persist; "Last updated" changes.
- [ ] `/tickets/999999` shows "Ticket not found".

## Technical choices

* **Filtering/sorting/paging are 100% backend:** `JpaSpecificationExecutor` + `Pageable`. `TicketSpecifications.withFilters` builds optional predicates that are AND-ed; the search predicate is `lower(title) LIKE ... OR lower(customer_email) LIKE ...`. User input is escaped (`%`, `_`) so wildcards in the search box are literal. `createdAt` + `id` is used for ordering so pages are stable when timestamps tie.
* **Summary** uses one grouped JPQL query (`group by status`), independent of any filter.
* **Schema owned by Flyway**, Hibernate `ddl-auto=none`. DDL is kept to the common subset of MySQL and H2 so the same migrations run in tests (e.g. `VARCHAR(5000)` rather than `TEXT`).
* **Timestamps** are set with `@PrePersist`/`@PreUpdate`, stored as UTC `DATETIME(6)` and returned as ISO-8601 instants (`...Z`), so the browser shows them in the user's local time zone.
* **Validation** with Bean Validation on request DTOs (Java records); the frontend mirrors the same rules in `utils/validation.js` (including the same email regex). A single `@RestControllerAdvice` produces the uniform error body for validation errors, bad enum values (body and query params), bad `page`/`sort`, 404s, and unexpected errors (logged, generic message returned).
* **PATCH strictness:** unknown JSON fields are rejected (`spring.jackson.deserialization.fail-on-unknown-properties=true`), so a PATCH that tries to change `title` fails loudly instead of being silently ignored.
* **Frontend:** the URL query string is the single source of truth for list state; the search box keeps local state and a `useDebounce` hook pushes it to the URL. In-flight requests are aborted when the query changes, which avoids out-of-order responses. No state library or UI kit.

## Assumptions

* `priority` is required when creating a ticket (the form defaults to Medium); `status` always starts as `OPEN` and cannot be set on create.
* `description` is limited to 5000 characters (the spec gave no limit); `customerEmail` to 254.
* "Valid email" means `something@something.tld` (no spaces, exactly one `@`, a dot in the domain), applied identically in frontend and backend.
* `pageSize` accepts 1-100; the UI always uses 10.
* The summary endpoint ignores any query parameters sent to it.
* Titles/descriptions are trimmed before saving.

## Screenshots

| Ticket list | Create ticket (validation) |
|---|---|
| ![List](docs/screenshots/list.png) | ![Create](docs/screenshots/create.png) |

| Ticket detail | Mobile view |
|---|---|
| ![Detail](docs/screenshots/detail.png) | ![Mobile](docs/screenshots/mobile.png) |


## Known limitations

* No authentication, users, assignees, comments or ticket editing beyond status/priority (as specified).
* Search is a `LIKE '%term%'` scan; fine for thousands of rows, but it cannot use the B-tree indexes. A full-text index would be the next step for large data sets.
* The frontend has no automated tests, and the list does not auto-refresh (reload or navigate to see other users' changes). Concurrent edits are last-write-wins.
* The seed uses fixed dates (Aug-Sep 2026), so "newest" is relative to that data.
* No Maven wrapper is included; install Maven or generate one with `mvn wrapper:wrapper`.

## Verification status

* **Frontend:** `npm install` + `npm run build` succeed; the validation rules were unit-checked with Node (valid input, empty fields, 120/121-character titles, bad emails).
* **Database:** `V1` and `V2` were executed against a real MariaDB 10.11 (MySQL-compatible) server: schema, 4 indexes, 27 seed rows with all statuses/priorities, and the `LIKE ... ESCAPE '!'` syntax used by the search all work.
* **Backend:** `mvn test` passes on my machine (all tests green, `BUILD SUCCESS`), and the
  application starts against MySQL with the Flyway migrations applied automatically.
## Time spent and AI usage

* **Time spent:** approximately 6 hours in total:
  * 1.5 h: reading the assignment, writing the prompt, and generating and reviewing the backend and tests
  * 1 h: running and debugging the local setup (Maven/JDK/MySQL)
  * 2 h: frontend testing and UI polish (theme, full-width layout, mobile responsiveness, custom cursor)
  * 1.5 h: manual testing, README, screenshots and pushing to GitHub
***How I used AI tools:** I used Claude to generate the initial code base (Spring Boot
  API, Flyway migrations, tests and the React frontend) from a prompt based on
  the assignment. I chose Spring Boot, JPA and MySQL because they are my main tech stack.
  I focused my review on the backend (controller, service, specifications, validation,
  error handling and the database schema), ran and tested the whole project locally,
  fixed the environment issues, and customized the UI (theme, full-width layout,
  responsiveness, custom cursor).
