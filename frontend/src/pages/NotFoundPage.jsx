import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="state">
      <p><strong>Page not found</strong></p>
      <Link to="/">Go to the ticket list</Link>
    </div>
  );
}
