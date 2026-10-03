import { Link, useNavigate } from 'react-router-dom';
import TicketForm from '../components/TicketForm.jsx';
import { createTicket } from '../api/client.js';

export default function CreateTicketPage() {
  const navigate = useNavigate();

  async function handleSubmit(values) {
    const created = await createTicket(values);
    navigate(`/tickets/${created.id}`);
  }

  return (
    <div className="panel narrow">
      <p><Link to="/">← Back to tickets</Link></p>
      <h1>New ticket</h1>
      <TicketForm onSubmit={handleSubmit} />
    </div>
  );
}
