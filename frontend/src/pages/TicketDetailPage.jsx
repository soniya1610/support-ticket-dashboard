import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getTicket, updateTicket } from '../api/client.js';
import { ErrorState, LoadingState } from '../components/StateViews.jsx';
import { PriorityBadge, StatusBadge } from '../components/StatusBadge.jsx';
import { formatDate } from '../utils/format.js';

export default function TicketDetailPage() {
  const { id } = useParams();
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  const [draft, setDraft] = useState({ status: '', priority: '' });
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    setSaved(false);
    setSaveError(null);

    getTicket(id, controller.signal)
      .then((t) => {
        setTicket(t);
        setDraft({ status: t.status, priority: t.priority });
        setLoading(false);
      })
      .catch((err) => {
        if (err.name === 'AbortError') return;
        setError(err);
        setLoading(false);
      });

    return () => controller.abort();
  }, [id, reloadKey]);

  if (loading) return <LoadingState label="Loading ticket…" />;

  if (error) {
    return (
      <div className="panel">
        <p><Link to="/">← Back to tickets</Link></p>
        {error.status === 404 ? (
          <div className="state"><p><strong>Ticket not found</strong></p><p>No ticket with id {id} exists.</p></div>
        ) : (
          <ErrorState message={error.message} onRetry={() => setReloadKey((k) => k + 1)} />
        )}
      </div>
    );
  }

  const changes = {};
  if (draft.status !== ticket.status) changes.status = draft.status;
  if (draft.priority !== ticket.priority) changes.priority = draft.priority;
  const dirty = Object.keys(changes).length > 0;

  async function handleSave() {
    setSaving(true);
    setSaveError(null);
    setSaved(false);
    try {
      const updated = await updateTicket(id, changes);
      setTicket(updated);
      setDraft({ status: updated.status, priority: updated.priority });
      setSaved(true);
    } catch (err) {
      setSaveError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="panel">
      <p><Link to="/">← Back to tickets</Link></p>
      <h1 className="detail-title">{ticket.title}</h1>
      <p className="muted">Ticket #{ticket.id}</p>

      <dl className="details">
        <div><dt>Customer</dt><dd><a href={`mailto:${ticket.customerEmail}`}>{ticket.customerEmail}</a></dd></div>
        <div><dt>Status</dt><dd><StatusBadge status={ticket.status} /></dd></div>
        <div><dt>Priority</dt><dd><PriorityBadge priority={ticket.priority} /></dd></div>
        <div><dt>Created</dt><dd>{formatDate(ticket.createdAt)}</dd></div>
        <div><dt>Last updated</dt><dd>{formatDate(ticket.updatedAt)}</dd></div>
      </dl>

      <h2>Description</h2>
      <p className="description">{ticket.description}</p>

      <h2>Update ticket</h2>
      {saveError && <div className="alert alert-error" role="alert">{saveError}</div>}
      {saved && <div className="alert alert-success" role="status">Changes saved.</div>}
      <div className="update-row">
        <label className="field">
          <span>Status</span>
          <select value={draft.status} disabled={saving}
                  onChange={(e) => { setSaved(false); setDraft((d) => ({ ...d, status: e.target.value })); }}>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </label>
        <label className="field">
          <span>Priority</span>
          <select value={draft.priority} disabled={saving}
                  onChange={(e) => { setSaved(false); setDraft((d) => ({ ...d, priority: e.target.value })); }}>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </label>
        <button type="button" className="btn btn-primary" disabled={!dirty || saving} onClick={handleSave}>
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </div>
    </div>
  );
}
