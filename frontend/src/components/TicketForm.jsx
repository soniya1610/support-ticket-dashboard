import { useState } from 'react';
import { ApiError } from '../api/client.js';
import { DESCRIPTION_MAX, TITLE_MAX, validateTicket } from '../utils/validation.js';

const INITIAL = { title: '', description: '', customerEmail: '', priority: 'MEDIUM' };

/**
 * Create-ticket form. `onSubmit(values)` must return a promise; if it rejects with an ApiError
 * carrying field details, those are shown next to the matching inputs.
 */
export default function TicketForm({ onSubmit }) {
  const [values, setValues] = useState(INITIAL);
  const [touched, setTouched] = useState({});
  const [serverErrors, setServerErrors] = useState({});
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const clientErrors = validateTicket(values);
  // A server message is dropped as soon as the user edits that field (see handleChange).
  const errorFor = (field) => (touched[field] ? clientErrors[field] : undefined) || serverErrors[field];

  function handleChange(e) {
    const { name, value } = e.target;
    setValues((v) => ({ ...v, [name]: value }));
    setServerErrors((s) => {
      if (!s[name]) return s;
      const { [name]: _removed, ...rest } = s;
      return rest;
    });
  }

  function handleBlur(e) {
    setTouched((t) => ({ ...t, [e.target.name]: true }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setFormError(null);
    setTouched({ title: true, description: true, customerEmail: true, priority: true });
    if (Object.keys(clientErrors).length > 0) return;

    setSubmitting(true);
    try {
      await onSubmit({
        title: values.title.trim(),
        description: values.description.trim(),
        customerEmail: values.customerEmail.trim(),
        priority: values.priority,
      });
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setServerErrors(err.fieldErrors);
        setFormError('Please fix the highlighted fields.');
      } else {
        setFormError(err.message || 'Could not create the ticket.');
      }
      setSubmitting(false);
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit} noValidate>
      {formError && <div className="alert alert-error" role="alert">{formError}</div>}

      <div className={`field ${errorFor('title') ? 'has-error' : ''}`}>
        <label htmlFor="title">Title</label>
        <input id="title" name="title" value={values.title} onChange={handleChange} onBlur={handleBlur}
               aria-invalid={Boolean(errorFor('title'))} />
        <div className="field-meta">
          <span className="error-text">{errorFor('title')}</span>
          <span className={values.title.length > TITLE_MAX ? 'counter over' : 'counter'}>
            {values.title.length}/{TITLE_MAX}
          </span>
        </div>
      </div>

      <div className={`field ${errorFor('description') ? 'has-error' : ''}`}>
        <label htmlFor="description">Description</label>
        <textarea id="description" name="description" rows={6} value={values.description}
                  onChange={handleChange} onBlur={handleBlur} aria-invalid={Boolean(errorFor('description'))} />
        <div className="field-meta">
          <span className="error-text">{errorFor('description')}</span>
          <span className={values.description.length > DESCRIPTION_MAX ? 'counter over' : 'counter'}>
            {values.description.length}/{DESCRIPTION_MAX}
          </span>
        </div>
      </div>

      <div className={`field ${errorFor('customerEmail') ? 'has-error' : ''}`}>
        <label htmlFor="customerEmail">Customer email</label>
        <input id="customerEmail" name="customerEmail" type="email" value={values.customerEmail}
               onChange={handleChange} onBlur={handleBlur} aria-invalid={Boolean(errorFor('customerEmail'))} />
        <div className="field-meta"><span className="error-text">{errorFor('customerEmail')}</span></div>
      </div>

      <div className={`field ${errorFor('priority') ? 'has-error' : ''}`}>
        <label htmlFor="priority">Priority</label>
        <select id="priority" name="priority" value={values.priority} onChange={handleChange} onBlur={handleBlur}>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
        <div className="field-meta"><span className="error-text">{errorFor('priority')}</span></div>
      </div>

      <button type="submit" className="btn btn-primary" disabled={submitting}>
        {submitting ? 'Creating…' : 'Create ticket'}
      </button>
    </form>
  );
}
