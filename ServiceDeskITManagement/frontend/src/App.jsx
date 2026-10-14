import { useCallback, useEffect, useState } from 'react';
import { api, loginUrl } from './api.js';
import Stats from './components/Stats.jsx';
import RequestForm from './components/RequestForm.jsx';
import RequestList from './components/RequestList.jsx';

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.me().then(setUser).catch(() => setUser(null)).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="center">Loading…</div>;
  if (!user) return <Login onLoggedIn={() => api.me().then(setUser)} />;
  return <Portal user={user} onLogout={() => api.logout().finally(() => setUser(null))} />;
}

function Login({ onLoggedIn }) {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [f, setF] = useState({ username: '', password: '', email: '', name: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (mode === 'register') {
        await api.register({ username: f.username, email: f.email, name: f.name, password: f.password });
      }
      await api.login(f.username, f.password);
      await onLoggedIn();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="center">
      <div className="login-card">
        <div className="logo">⚙️</div>
        <h1>IT Service Desk</h1>
        <p>Raise and track IT service requests.</p>

        <div className="tabs auth-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => { setMode('login'); setError(''); }}>Sign in</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => { setMode('register'); setError(''); }}>Register</button>
        </div>

        <form className="auth-form" onSubmit={submit}>
          {mode === 'register' && (
            <>
              <input placeholder="Full name" value={f.name} onChange={set('name')} required />
              <input type="email" placeholder="Email" value={f.email} onChange={set('email')} required />
            </>
          )}
          <input placeholder={mode === 'login' ? 'Username or email' : 'Username'} value={f.username}
                 onChange={set('username')} autoComplete="username" required />
          <input type="password" placeholder="Password" value={f.password} onChange={set('password')}
                 autoComplete={mode === 'login' ? 'current-password' : 'new-password'} minLength={mode === 'register' ? 8 : undefined} required />
          {error && <div className="error">{error}</div>}
          <button className="btn" type="submit" disabled={busy}>{mode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>

        <div className="or"><span>or</span></div>
        <a className="btn google" href={loginUrl}>Continue with Google</a>
      </div>
    </div>
  );
}

function Portal({ user, onLogout }) {
  const isAdmin = user.role === 'ADMIN';
  const [view, setView] = useState(isAdmin ? 'all' : 'mine'); // 'mine' | 'all'
  const [requests, setRequests] = useState([]);
  const [stats, setStats] = useState({});
  const [filter, setFilter] = useState('');
  const [editing, setEditing] = useState(null); // null | 'new' | request
  const [error, setError] = useState('');

  const admin = isAdmin && view === 'all';

  const load = useCallback(async () => {
    try {
      setError('');
      const [list, s] = admin
        ? await Promise.all([api.allRequests(filter), api.allStats()])
        : await Promise.all([api.myRequests(), api.myStats()]);
      setRequests(filter && !admin ? list.filter((r) => r.status === filter) : list);
      setStats(s);
    } catch (e) {
      setError(e.message);
    }
  }, [admin, filter]);

  useEffect(() => { load(); }, [load]);

  const act = async (fn) => {
    try { setError(''); await fn(); await load(); } catch (e) { setError(e.message); }
  };

  return (
    <div className="app">
      <header>
        <div className="brand">⚙️ IT Service Desk</div>
        <div className="who">
          {user.picture && <img src={user.picture} alt="" referrerPolicy="no-referrer" />}
          <span>{user.name} <em className={`role ${user.role}`}>{user.role}</em></span>
          <button className="btn ghost" onClick={onLogout}>Sign out</button>
        </div>
      </header>

      <main>
        <div className="toolbar">
          {isAdmin && (
            <div className="tabs">
              <button className={view === 'all' ? 'active' : ''} onClick={() => setView('all')}>Admin dashboard</button>
              <button className={view === 'mine' ? 'active' : ''} onClick={() => setView('mine')}>My requests</button>
            </div>
          )}
          {!admin && <button className="btn" onClick={() => setEditing('new')}>+ New request</button>}
        </div>

        <Stats stats={stats} active={filter} onSelect={(s) => setFilter(filter === s ? '' : s)} />
        {error && <div className="error">{error}</div>}

        <RequestList
          requests={requests}
          admin={admin}
          onEdit={setEditing}
          onDelete={(r) => window.confirm(`Delete "${r.title}"?`) && act(() => api.remove(r.id))}
          onReopen={(r) => act(() => api.reopen(r.id))}
          onStatus={(r, status) => {
            const comment = status === 'RESOLVED' ? window.prompt('Resolution note (optional):', r.adminComment || '') : r.adminComment;
            if (comment === null) return;
            act(() => api.setStatus(r.id, status, comment));
          }}
        />
      </main>

      {editing && (
        <RequestForm
          initial={editing === 'new' ? null : editing}
          onCancel={() => setEditing(null)}
          onSubmit={(body) =>
            act(async () => {
              await (editing === 'new' ? api.create(body) : api.update(editing.id, body));
              setEditing(null);
            })
          }
        />
      )}
    </div>
  );
}
