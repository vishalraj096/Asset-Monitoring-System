import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function DashboardPage() {
  const { user, isManager } = useAuth();

  return isManager() ? <ManagerDashboard /> : <OperatorDashboard user={user} />;
}

function ManagerDashboard() {
  const [assets, setAssets] = useState([]);
  const [alertCount, setAlertCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editAsset, setEditAsset] = useState(null);

  const fetchAssets = async () => {
    try {
      const [assetRes, alertRes] = await Promise.all([
        API.get('/assets?size=100'),
        API.get('/alerts/count/active').catch(() => ({ data: { data: 0 } })),
      ]);
      setAssets(assetRes.data.data.content || []);
      setAlertCount(alertRes.data.data || 0);
    } catch (err) {
      toast.error('Failed to load assets');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAssets(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this asset?')) return;
    try {
      await API.delete(`/assets/${id}`);
      toast.success('Asset deleted');
      fetchAssets();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Delete failed'));
    }
  };

  if (loading) return <PageLoader />;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Manager Dashboard</h1>
          {alertCount > 0 && (
            <Link to="/alerts" className="text-sm text-red-400 hover:text-red-300 mt-1 inline-block">
              ⚠ {alertCount} active alert{alertCount !== 1 ? 's' : ''}
            </Link>
          )}
        </div>
        <button onClick={() => { setEditAsset(null); setShowModal(true); }}
          className="px-4 py-2 rounded font-medium text-white bg-blue-600 hover:bg-blue-700 transition-colors">
          + Create Asset
        </button>
      </div>
      <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-750">
            <tr className="border-b border-slate-700">
              <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Name</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Type</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Location</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Temp Threshold</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Pressure Threshold</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Assigned To</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {assets.length === 0 ? (
              <tr><td colSpan="8" className="px-4 py-8 text-center text-slate-500">No assets found</td></tr>
            ) : assets.map((a) => (
              <tr key={a.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
                <td className="px-4 py-3 text-slate-300">{a.id}</td>
                <td className="px-4 py-3">
                  <Link to={`/assets/${a.id}`} className="text-blue-400 hover:text-blue-300 font-medium">
                    {a.name}
                  </Link>
                </td>
                <td className="px-4 py-3 text-slate-300">{a.type}</td>
                <td className="px-4 py-3 text-slate-300">{a.location || '—'}</td>
                <td className="px-4 py-3 text-slate-300">{a.thresholdTemp}°</td>
                <td className="px-4 py-3 text-slate-300">{a.thresholdPressure} PSI</td>
                <td className="px-4 py-3 text-slate-300">{a.assignedToName || '—'}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    <button onClick={() => { setEditAsset(a); setShowModal(true); }}
                      className="px-2 py-1 rounded text-xs font-medium text-blue-300 bg-blue-900/30 hover:bg-blue-900/50 border border-blue-800/50 transition-colors">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(a.id)}
                      className="px-2 py-1 rounded text-xs font-medium text-red-300 bg-red-900/30 hover:bg-red-900/50 border border-red-800/50 transition-colors">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {showModal && (
        <AssetModal asset={editAsset} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); fetchAssets(); }} />
      )}
    </div>
  );
}

function OperatorDashboard({ user }) {
  const [assets, setAssets] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [maintModal, setMaintModal] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const [assetRes, alertRes] = await Promise.all([
          API.get('/assets?size=100'),
          API.get('/alerts?size=100&status=ACTIVE'),
        ]);
        // Filter to show only assets assigned to the current user
        const allAssets = assetRes.data.data.content || [];
        setAssets(allAssets);
        setAlerts(alertRes.data.data.content || []);
      } catch (err) {
        toast.error('Failed to load data');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const resolveAlert = async (id) => {
    try {
      await API.put(`/alerts/${id}/resolve`);
      toast.success('Alert resolved');
      setAlerts((prev) => prev.filter((a) => a.id !== id));
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to resolve'));
    }
  };

  if (loading) return <PageLoader />;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-100 mb-6">Operator Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {assets.length === 0 ? (
          <p className="text-slate-500 col-span-full text-center py-8">No assets found</p>
        ) : assets.map((a) => {
          const assetAlerts = alerts.filter((al) => al.assetId === a.id);
          return (
            <div key={a.id} className="bg-slate-800 border border-slate-700 rounded-lg p-5">
              <Link to={`/assets/${a.id}`} className="text-lg font-semibold text-blue-400 hover:text-blue-300">
                {a.name}
              </Link>
              <p className="text-sm text-slate-400 mt-1">{a.type} • {a.location || 'No location'}</p>
              <div className="mt-3 grid grid-cols-2 gap-2 text-sm">
                <div><span className="text-slate-500">Temp Threshold:</span> <span className="text-slate-300">{a.thresholdTemp}°</span></div>
                <div><span className="text-slate-500">Pressure:</span> <span className="text-slate-300">{a.thresholdPressure} PSI</span></div>
              </div>
              {assetAlerts.length > 0 && (
                <div className="mt-3 space-y-2">
                  {assetAlerts.map((al) => (
                    <div key={al.id} className="flex items-center justify-between bg-red-900/20 border border-red-800/30 rounded p-2 text-xs">
                      <span className="text-red-300">{al.message}</span>
                      <button onClick={() => resolveAlert(al.id)}
                        className="ml-2 px-2 py-0.5 rounded text-xs font-medium text-green-300 bg-green-900/30 hover:bg-green-900/50 border border-green-800/50">
                        Resolve
                      </button>
                    </div>
                  ))}
                </div>
              )}
              <button onClick={() => setMaintModal(a)}
                className="mt-4 w-full py-2 rounded text-sm font-medium text-blue-300 bg-blue-900/30 hover:bg-blue-900/50 border border-blue-800/50 transition-colors">
                Log Maintenance
              </button>
            </div>
          );
        })}
      </div>
      {maintModal && (
        <MaintenanceModal asset={maintModal} onClose={() => setMaintModal(null)} />
      )}
    </div>
  );
}

function AssetModal({ asset, onClose, onSaved }) {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState({
    name: asset?.name || '',
    type: asset?.type || '',
    location: asset?.location || '',
    thresholdTemp: asset?.thresholdTemp || '',
    thresholdPressure: asset?.thresholdPressure || '',
    assignedToId: asset?.assignedToId || '',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    API.get('/users').then((res) => setUsers(res.data.data || [])).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        thresholdTemp: parseFloat(form.thresholdTemp),
        thresholdPressure: parseFloat(form.thresholdPressure),
        assignedToId: form.assignedToId ? parseInt(form.assignedToId) : null,
      };
      if (asset) {
        await API.put(`/assets/${asset.id}`, payload);
        toast.success('Asset updated');
      } else {
        await API.post('/assets', payload);
        toast.success('Asset created');
      }
      onSaved();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Save failed'));
    } finally {
      setSaving(false);
    }
  };

  const update = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 px-4">
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 w-full max-w-lg">
        <h2 className="text-lg font-semibold text-slate-200 mb-4">{asset ? 'Edit Asset' : 'Create Asset'}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Name" id="asset-name" value={form.name} onChange={(v) => update('name', v)} required />
          <Input label="Type" id="asset-type" value={form.type} onChange={(v) => update('type', v)} required />
          <Input label="Location" id="asset-location" value={form.location} onChange={(v) => update('location', v)} />
          <div className="grid grid-cols-2 gap-4">
            <Input label="Temp Threshold" id="asset-temp" type="number" value={form.thresholdTemp} onChange={(v) => update('thresholdTemp', v)} required />
            <Input label="Pressure Threshold" id="asset-pressure" type="number" value={form.thresholdPressure} onChange={(v) => update('thresholdPressure', v)} required />
          </div>
          <div>
            <label htmlFor="asset-assigned" className="block text-sm font-medium text-slate-300 mb-1">Assign To</label>
            <select id="asset-assigned" value={form.assignedToId} onChange={(e) => update('assignedToId', e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 focus:outline-none focus:border-blue-500">
              <option value="">Unassigned</option>
              {users.map((u) => <option key={u.id} value={u.id}>{u.name} ({u.email})</option>)}
            </select>
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 rounded text-sm text-slate-300 bg-slate-700 hover:bg-slate-600 transition-colors">Cancel</button>
            <button type="submit" disabled={saving} className="px-4 py-2 rounded text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors">
              {saving ? 'Saving...' : asset ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function MaintenanceModal({ asset, onClose }) {
  const [scheduledDate, setScheduledDate] = useState('');
  const [remarks, setRemarks] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await API.post('/maintenance', { assetId: asset.id, scheduledDate, remarks });
      toast.success('Maintenance logged');
      onClose();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to log maintenance'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 px-4">
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 w-full max-w-md">
        <h2 className="text-lg font-semibold text-slate-200 mb-1">Log Maintenance</h2>
        <p className="text-sm text-slate-400 mb-4">Asset: {asset.name}</p>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="maint-date" className="block text-sm font-medium text-slate-300 mb-1">Scheduled Date</label>
            <input id="maint-date" type="date" required value={scheduledDate} onChange={(e) => setScheduledDate(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 focus:outline-none focus:border-blue-500" />
          </div>
          <div>
            <label htmlFor="maint-remarks" className="block text-sm font-medium text-slate-300 mb-1">Remarks</label>
            <textarea id="maint-remarks" rows={3} value={remarks} onChange={(e) => setRemarks(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500" placeholder="Optional notes..." />
          </div>
          <div className="flex gap-3 justify-end">
            <button type="button" onClick={onClose} className="px-4 py-2 rounded text-sm text-slate-300 bg-slate-700 hover:bg-slate-600 transition-colors">Cancel</button>
            <button type="submit" disabled={saving} className="px-4 py-2 rounded text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors">
              {saving ? 'Saving...' : 'Submit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Input({ label, id, type = 'text', value, onChange, required = false }) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-slate-300 mb-1">{label}</label>
      <input id={id} type={type} required={required} value={value} onChange={(e) => onChange(e.target.value)}
        className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 focus:outline-none focus:border-blue-500" />
    </div>
  );
}

function PageLoader() {
  return (
    <div className="flex items-center justify-center py-20">
      <div className="text-slate-400">Loading...</div>
    </div>
  );
}
