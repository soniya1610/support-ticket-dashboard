import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { listTickets } from '../api/client.js';
import { useDebounce } from '../hooks/useDebounce.js';
import SummaryCards from '../components/SummaryCards.jsx';
import TicketFilters from '../components/TicketFilters.jsx';
import TicketTable from '../components/TicketTable.jsx';
import Pagination from '../components/Pagination.jsx';
import { EmptyState, ErrorState, LoadingState } from '../components/StateViews.jsx';

const PAGE_SIZE = 10;

export default function TicketListPage() {
  const [params, setParams] = useSearchParams();

  // The URL is the single source of truth for search / filters / sort / page.
  const search = params.get('search') ?? '';
  const status = params.get('status') ?? '';
  const priority = params.get('priority') ?? '';
  const sort = params.get('sort') === 'oldest' ? 'oldest' : 'newest';
  const page = Math.max(1, parseInt(params.get('page') ?? '1', 10) || 1);

  const [searchInput, setSearchInput] = useState(search);
  const debouncedSearch = useDebounce(searchInput, 400);

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  const updateParams = useCallback((changes, replace = false) => {
    setParams((prev) => {
      const next = new URLSearchParams(prev);
      Object.entries(changes).forEach(([key, value]) => {
        const isDefault = (key === 'page' && Number(value) === 1) || (key === 'sort' && value === 'newest');
        if (value === '' || value == null || isDefault) next.delete(key);
        else next.set(key, String(value));
      });
      return next;
    }, { replace });
  }, [setParams]);

  // typed text -> URL (debounced); resets to page 1
  useEffect(() => {
    if (debouncedSearch.trim() !== search.trim()) {
      updateParams({ search: debouncedSearch.trim(), page: 1 }, true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  // URL -> input (browser back/forward, "Clear filters")
  useEffect(() => {
    setSearchInput((current) => (current.trim() === search.trim() ? current : search));
  }, [search]);

  // fetch whenever the query in the URL changes
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    listTickets({ search, status, priority, sort, page, pageSize: PAGE_SIZE }, controller.signal)
      .then((res) => {
        if (res.total > 0 && res.data.length === 0 && page > 1) {
          // e.g. a stale ?page=9 from a bookmark: jump to the last real page
          updateParams({ page: res.totalPages }, true);
          return;
        }
        setResult(res);
        setLoading(false);
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setError(err);
        setLoading(false);
      });

    return () => controller.abort();
  }, [search, status, priority, sort, page, reloadKey, updateParams]);

  const hasActiveFilters = Boolean(search || status || priority || sort !== 'newest');

  function clearFilters() {
    setSearchInput('');
    setParams({}, { replace: false });
  }

  let content;
  if (loading) {
    content = <LoadingState label="Loading tickets…" />;
  } else if (error) {
    content = <ErrorState message={error.message} onRetry={() => setReloadKey((k) => k + 1)} />;
  } else if (!result || result.data.length === 0) {
    content = hasActiveFilters ? (
      <EmptyState title="No tickets match your search or filters.">
        <button type="button" className="btn" onClick={clearFilters}>Clear filters</button>
      </EmptyState>
    ) : (
      <EmptyState title="No tickets yet.">
        <Link to="/tickets/new" className="btn btn-primary">Create the first ticket</Link>
      </EmptyState>
    );
  } else {
    content = (
      <>
        <TicketTable tickets={result.data} />
        <Pagination
          page={result.page}
          pageSize={result.pageSize}
          total={result.total}
          totalPages={result.totalPages}
          onPageChange={(p) => updateParams({ page: p })}
        />
      </>
    );
  }

  return (
    <>
      <div className="page-title">
        <h1>Tickets</h1>
        <Link to="/tickets/new" className="btn btn-primary">New ticket</Link>
      </div>

      <SummaryCards />

      <TicketFilters
        searchInput={searchInput}
        onSearchChange={setSearchInput}
        status={status}
        priority={priority}
        sort={sort}
        onFilterChange={(changes) => updateParams({ ...changes, page: 1 })}
        onClear={clearFilters}
        hasActiveFilters={hasActiveFilters || searchInput !== ''}
      />

      <div className="panel">{content}</div>
    </>
  );
}
