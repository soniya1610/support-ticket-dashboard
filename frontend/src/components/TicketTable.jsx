import { Link } from 'react-router-dom';
import { PriorityBadge, StatusBadge } from './StatusBadge.jsx';
import { formatDate } from '../utils/format.js';

export default function TicketTable({ tickets }) {
  return (
    <table className="ticket-table">
      <thead>
        <tr>
          <th>Title</th>
          <th>Customer</th>
          <th>Priority</th>
          <th>Status</th>
          <th>Created</th>
        </tr>
      </thead>
      <tbody>
        {tickets.map((t) => (
          <tr key={t.id}>
            <td data-label="Title"><Link to={`/tickets/${t.id}`}>{t.title}</Link></td>
            <td data-label="Customer">{t.customerEmail}</td>
            <td data-label="Priority"><PriorityBadge priority={t.priority} /></td>
            <td data-label="Status"><StatusBadge status={t.status} /></td>
            <td data-label="Created">{formatDate(t.createdAt)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
