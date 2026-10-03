export default function TicketFilters({
  searchInput, onSearchChange, status, priority, sort, onFilterChange, onClear, hasActiveFilters,
}) {
  return (
    <form className="filters" onSubmit={(e) => e.preventDefault()} role="search">
      <label className="field field-grow">
        <span>Search</span>
        <input
          type="search"
          value={searchInput}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Title or customer email"
        />
      </label>

      <label className="field">
        <span>Status</span>
        <select value={status} onChange={(e) => onFilterChange({ status: e.target.value })}>
          <option value="">All</option>
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </label>

      <label className="field">
        <span>Priority</span>
        <select value={priority} onChange={(e) => onFilterChange({ priority: e.target.value })}>
          <option value="">All</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
      </label>

      <label className="field">
        <span>Sort by created</span>
        <select value={sort} onChange={(e) => onFilterChange({ sort: e.target.value })}>
          <option value="newest">Newest first</option>
          <option value="oldest">Oldest first</option>
        </select>
      </label>

      {hasActiveFilters && (
        <button type="button" className="btn btn-link" onClick={onClear}>Clear filters</button>
      )}
    </form>
  );
}
