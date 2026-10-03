import { useEffect, useState } from 'react';
import { getSummary } from '../api/client.js';

const CARDS = [
  { key: 'total', label: 'Total', className: 'card-total' },
  { key: 'open', label: 'Open', className: 'card-open' },
  { key: 'inProgress', label: 'In Progress', className: 'card-progress' },
  { key: 'resolved', label: 'Resolved', className: 'card-resolved' },
];

/** Counts always cover the whole dataset: they ignore the list filters on purpose. */
export default function SummaryCards() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setError(null);
    getSummary(controller.signal)
      .then(setSummary)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err);
      });
    return () => controller.abort();
  }, [reloadKey]);

  if (error) {
    return (
      <div className="state state-error summary-error" role="alert">
        Could not load summary counts: {error.message}{' '}
        <button type="button" className="btn btn-small" onClick={() => setReloadKey((k) => k + 1)}>Retry</button>
      </div>
    );
  }

  return (
    <section className="summary" aria-label="Ticket summary">
      {CARDS.map((c) => (
        <div key={c.key} className={`summary-card ${c.className}`}>
          <span className="summary-value">{summary ? summary[c.key] : '–'}</span>
          <span className="summary-label">{c.label}</span>
        </div>
      ))}
    </section>
  );
}
