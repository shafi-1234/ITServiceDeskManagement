import { useState } from 'react';

const CATEGORIES = ['Hardware', 'Software', 'Network', 'Access / Account', 'Email', 'Other'];
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export default function RequestForm({ initial, onSubmit, onCancel }) {
  const [form, setForm] = useState({
    title: initial?.title || '',
    description: initial?.description || '',
    category: initial?.category || CATEGORIES[0],
    priority: initial?.priority || 'MEDIUM',
  });
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <div className="modal-bg" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>{initial ? 'Edit request' : 'New request'}</h2>
        <label>Title<input value={form.title} onChange={set('title')} maxLength={200} /></label>
        <label>Description<textarea rows={5} value={form.description} onChange={set('description')} maxLength={2000} /></label>
        <div className="row">
          <label>Category
            <select value={form.category} onChange={set('category')}>
              {CATEGORIES.map((c) => <option key={c}>{c}</option>)}
            </select>
          </label>
          <label>Priority
            <select value={form.priority} onChange={set('priority')}>
              {PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </label>
        </div>
        <div className="actions">
          <button className="btn ghost" onClick={onCancel}>Cancel</button>
          <button className="btn" disabled={!form.title.trim() || !form.description.trim()} onClick={() => onSubmit(form)}>
            {initial ? 'Save changes' : 'Submit request'}
          </button>
        </div>
      </div>
    </div>
  );
}
