export const STATUS_LABELS = { OPEN: 'Open', IN_PROGRESS: 'In Progress', RESOLVED: 'Resolved' };
export const PRIORITY_LABELS = { LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' };

export function formatDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}
