export function LoadingState({ label = 'Loading…' }) {
  return (
    <div className="state" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  );
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state state-error" role="alert">
      <p><strong>Something went wrong.</strong></p>
      <p>{message}</p>
      {onRetry && <button type="button" className="btn" onClick={onRetry}>Try again</button>}
    </div>
  );
}

export function EmptyState({ title, children }) {
  return (
    <div className="state">
      <p><strong>{title}</strong></p>
      {children}
    </div>
  );
}
