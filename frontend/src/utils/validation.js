// Mirrors the backend Bean Validation rules in CreateTicketRequest.
export const TITLE_MAX = 120;
export const DESCRIPTION_MAX = 5000;
const EMAIL_RE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function validateTicket(values) {
  const errors = {};

  const title = values.title.trim();
  if (!title) errors.title = 'Title is required';
  else if (values.title.length > TITLE_MAX) errors.title = `Title must be at most ${TITLE_MAX} characters`;

  const description = values.description.trim();
  if (!description) errors.description = 'Description is required';
  else if (values.description.length > DESCRIPTION_MAX) {
    errors.description = `Description must be at most ${DESCRIPTION_MAX} characters`;
  }

  const email = values.customerEmail.trim();
  if (!email) errors.customerEmail = 'Customer email is required';
  else if (email.length > 254 || !EMAIL_RE.test(email)) errors.customerEmail = 'Customer email must be a valid email address';

  if (!['LOW', 'MEDIUM', 'HIGH'].includes(values.priority)) errors.priority = 'Priority is required';

  return errors;
}
