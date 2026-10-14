const LABELS = { OPEN: 'Open', IN_PROGRESS: 'In progress', RESOLVED: 'Resolved', REOPENED: 'Reopened' };

export default function Stats({ stats, active, onSelect }) {
  const total = Object.values(stats).reduce((a, b) => a + b, 0);
  return (
    <div className="stats">
      <div className="stat total"><b>{total}</b><span>Total</span></div>
      {Object.keys(LABELS).map((k) => (
        <button key={k} className={`stat ${k} ${active === k ? 'on' : ''}`} onClick={() => onSelect(k)}>
          <b>{stats[k] ?? 0}</b><span>{LABELS[k]}</span>
        </button>
      ))}
    </div>
  );
}
