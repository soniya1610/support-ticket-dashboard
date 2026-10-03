import { PRIORITY_LABELS, STATUS_LABELS } from '../utils/format.js';

export function StatusBadge({ status }) {
  return <span className={`badge status-${status.toLowerCase()}`}>{STATUS_LABELS[status] || status}</span>;
}

export function PriorityBadge({ priority }) {
  return <span className={`badge priority-${priority.toLowerCase()}`}>{PRIORITY_LABELS[priority] || priority}</span>;
}
