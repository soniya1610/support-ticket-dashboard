export default function Pagination({ page, pageSize, total, totalPages, onPageChange }) {
  if (total === 0) return null;
  const from = (page - 1) * pageSize + 1;
  const to = Math.min(page * pageSize, total);

  return (
    <nav className="pagination" aria-label="Pagination">
      <span className="pagination-info">Showing {from}–{to} of {total}</span>
      <div className="pagination-controls">
        <button type="button" className="btn" disabled={page <= 1} onClick={() => onPageChange(page - 1)}>
          Previous
        </button>
        <span>Page {page} of {totalPages}</span>
        <button type="button" className="btn" disabled={page >= totalPages} onClick={() => onPageChange(page + 1)}>
          Next
        </button>
      </div>
    </nav>
  );
}
