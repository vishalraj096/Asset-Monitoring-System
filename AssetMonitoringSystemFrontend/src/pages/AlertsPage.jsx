import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const params = statusFilter ? `?size=100&status=${statusFilter}` : '?size=100';
      const res = await API.get(`/alerts${params}`);
      setAlerts(res.data.data.content || []);
    } catch (err) {
      toast.error('Failed to load alerts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAlerts(); }, [statusFilter]);

  const resolveAlert = async (id) => {
    try {
      await API.put(`/alerts/${id}/resolve`);
      toast.success('Alert resolved');
      fetchAlerts();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to resolve'));
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-100">Alerts</h1>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
          className="px-3 py-2 rounded bg-slate-800 border border-slate-600 text-slate-200 text-sm focus:outline-none focus:border-blue-500">
          <option value="">All</option>
          <option value="ACTIVE">Active</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </div>
      {loading ? (
        <div className="text-center py-12 text-slate-400">Loading...</div>
      ) : (
        <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Asset</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Type</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Message</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Status</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Triggered At</th>
                <th className="text-left px-4 py-3 text-slate-400 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {alerts.length === 0 ? (
                <tr><td colSpan="7" className="px-4 py-8 text-center text-slate-500">No alerts found</td></tr>
              ) : alerts.map((a) => (
                <tr key={a.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
                  <td className="px-4 py-3 text-slate-300">{a.id}</td>
                  <td className="px-4 py-3">
                    <Link to={`/assets/${a.assetId}`} className="text-blue-400 hover:text-blue-300 font-medium">
                      {a.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                      a.type === 'TEMP_HIGH' ? 'bg-orange-900/30 text-orange-300 border border-orange-800/50' : 'bg-purple-900/30 text-purple-300 border border-purple-800/50'
                    }`}>{a.type}</span>
                  </td>
                  <td className="px-4 py-3 text-slate-300 max-w-xs truncate">{a.message}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                      a.status === 'ACTIVE' ? 'bg-red-900/30 text-red-300 border border-red-800/50' : 'bg-green-900/30 text-green-300 border border-green-800/50'
                    }`}>{a.status}</span>
                  </td>
                  <td className="px-4 py-3 text-slate-400 text-xs">{formatTimestamp(a.triggeredAt)}</td>
                  <td className="px-4 py-3">
                    {a.status === 'ACTIVE' && (
                      <button onClick={() => resolveAlert(a.id)}
                        className="px-2 py-1 rounded text-xs font-medium text-green-300 bg-green-900/30 hover:bg-green-900/50 border border-green-800/50 transition-colors">
                        Resolve
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function formatTimestamp(ts) {
  if (!ts) return '—';
  try {
    const d = new Date(ts);
    if (isNaN(d.getTime())) return ts;
    return d.toLocaleString();
  } catch {
    return ts;
  }
}
