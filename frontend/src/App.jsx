import { Link, NavLink, Route, Routes } from 'react-router-dom';
import TicketListPage from './pages/TicketListPage.jsx';
import CreateTicketPage from './pages/CreateTicketPage.jsx';
import TicketDetailPage from './pages/TicketDetailPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import CustomCursor from './components/CustomCursor.jsx';

export default function App() {
  return (
    <>
    <CustomCursor/>
      <header className="app-header">
        <div className="container header-inner">
          <Link to="/" className="brand">Support Tickets</Link>
          <nav className="nav">
            <NavLink to="/" end>Tickets</NavLink>
            <NavLink to="/tickets/new">New ticket</NavLink>
          </nav>
        </div>
      </header>
      <main className="container">
        <Routes>
          <Route path="/" element={<TicketListPage />} />
          <Route path="/tickets/new" element={<CreateTicketPage />} />
          <Route path="/tickets/:id" element={<TicketDetailPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  );
}
