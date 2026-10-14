const fmt = (d) => (d ? new Date(d).toLocaleString() : '—');

export default function RequestList({ requests, admin, onEdit, onDelete, onReopen, onStatus }) {
  if (!requests.length) return <div className="empty">No requests to show.</div>;

  return (
    <div className="list">
      {requests.map((r) => (
        <article key={r.id} className="card">
          <div className="card-head">
            <h3>#{r.id} · {r.title}</h3>
            <div className="badges">
              <span className={`badge p-${r.priority}`}>{r.priority}</span>
              <span className={`badge s-${r.status}`}>{r.status.replace('_', ' ')}</span>
            </div>
          </div>
          <p>{r.description}</p>
          <div className="meta">
            <span>{r.category}</span>
            {admin && <span>By {r.createdByName} ({r.createdByEmail})</span>}
            <span>Raised {fmt(r.createdAt)}</span>
            {r.resolvedAt && <span>Resolved {fmt(r.resolvedAt)}</span>}
            {r.reopenCount > 0 && <span>Reopened ×{r.reopenCount}</span>}
          </div>
          {r.adminComment && <div className="note"><b>Admin note:</b> {r.adminComment}</div>}

          <div className="actions">
            {admin ? (
              <>
                {r.status !== 'IN_PROGRESS' && r.status !== 'RESOLVED' && (
                  <button className="btn ghost" onClick={() => onStatus(r, 'IN_PROGRESS')}>Start work</button>
                )}
                {r.status !== 'RESOLVED' && <button className="btn" onClick={() => onStatus(r, 'RESOLVED')}>Resolve</button>}
              </>
            ) : (
              <>
                {r.status !== 'RESOLVED' && <button className="btn ghost" onClick={() => onEdit(r)}>Edit</button>}
                {r.status === 'RESOLVED' && <button className="btn ghost" onClick={() => onReopen(r)}>Reopen</button>}
                <button className="btn danger" onClick={() => onDelete(r)}>Delete</button>
              </>
            )}
          </div>
        </article>
      ))}
    </div>
  );
}
