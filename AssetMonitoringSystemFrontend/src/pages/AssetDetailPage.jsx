import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function AssetDetailPage() {
  const { id } = useParams();
  const [asset, setAsset] = useState(null);
  const [sensorData, setSensorData] = useState([]);
  const [maintenance, setMaintenance] = useState([]);
  const [uptimeLogs, setUptimeLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('sensor');

  useEffect(() => {
    const load = async () => {
      try {
        const [assetRes, sensorRes, maintRes, uptimeRes] = await Promise.all([
          API.get(`/assets/${id}`),
          API.get(`/sensors/asset/${id}?size=20`),
          API.get(`/maintenance/asset/${id}?size=20`),
          API.get(`/uptime/asset/${id}?size=20`),
        ]);
        setAsset(assetRes.data.data);
        setSensorData(sensorRes.data.data.content || []);
        setMaintenance(maintRes.data.data.content || []);
        setUptimeLogs(uptimeRes.data.data.content || []);
      } catch (err) {
        toast.error(extractErrorMessage(err, 'Failed to load asset details'));
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="text-slate-400">Loading...</div>
      </div>
    );
  }

  if (!asset) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <p className="text-slate-400">Asset not found.</p>
        <Link to="/dashboard" className="text-blue-400 hover:text-blue-300 text-sm mt-2 inline-block">← Back to Dashboard</Link>
      </div>
    );
  }

  const tabs = [
    { key: 'sensor', label: 'Sensor Data', count: sensorData.length },
    { key: 'maintenance', label: 'Maintenance', count: maintenance.length },
    { key: 'uptime', label: 'Uptime Logs', count: uptimeLogs.length },
  ];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <Link to="/dashboard" className="text-blue-400 hover:text-blue-300 text-sm mb-4 inline-block">← Back to Dashboard</Link>

      {/* Asset Header */}
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 mb-6">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-100">{asset.name}</h1>
            <p className="text-slate-400 mt-1">{asset.type} • {asset.location || 'No location'}</p>
          </div>
          <div className="text-right">
            {asset.assignedToName && (
              <p className="text-sm text-slate-400">Assigned to: <span className="text-slate-200">{asset.assignedToName}</span></p>
            )}
          </div>
        </div>
        <div className="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-4">
          <StatCard label="Temp Threshold" value={`${asset.thresholdTemp}°`} />
          <StatCard label="Pressure Threshold" value={`${asset.thresholdPressure} PSI`} />
          <StatCard label="Sensor Readings" value={sensorData.length} />
          <StatCard label="Maintenance Logs" value={maintenance.length} />
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-4 border-b border-slate-700 pb-px">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-4 py-2.5 text-sm font-medium rounded-t transition-colors ${
              activeTab === tab.key
                ? 'bg-slate-800 text-blue-400 border border-slate-700 border-b-slate-800 -mb-px'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {tab.label} <span className="ml-1 text-xs opacity-60">({tab.count})</span>
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
        {activeTab === 'sensor' && <SensorDataTable data={sensorData} />}
        {activeTab === 'maintenance' && <MaintenanceTable data={maintenance} onRefresh={() => refreshMaintenance(id, setMaintenance)} />}
        {activeTab === 'uptime' && <UptimeTable data={uptimeLogs} />}
      </div>
    </div>
  );
}

async function refreshMaintenance(assetId, setMaintenance) {
  try {
    const res = await API.get(`/maintenance/asset/${assetId}?size=20`);
    setMaintenance(res.data.data.content || []);
  } catch {
    // silent
  }
}

function StatCard({ label, value }) {
  return (
    <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className="text-lg font-semibold text-slate-200 mt-1">{value}</p>
    </div>
  );
}

function SensorDataTable({ data }) {
  if (data.length === 0) {
    return <p className="px-4 py-8 text-center text-slate-500">No sensor data recorded yet</p>;
  }
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-700">
          <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Temperature</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Pressure</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Timestamp</th>
        </tr>
      </thead>
      <tbody>
        {data.map((s) => (
          <tr key={s.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
            <td className="px-4 py-3 text-slate-300">{s.id}</td>
            <td className="px-4 py-3 text-slate-200 font-medium">{s.temperature}°</td>
            <td className="px-4 py-3 text-slate-200 font-medium">{s.pressure} PSI</td>
            <td className="px-4 py-3 text-slate-400 text-xs">{formatTimestamp(s.timestamp)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function MaintenanceTable({ data, onRefresh }) {
  const completeMaintenance = async (maintId) => {
    try {
      await API.put(`/maintenance/${maintId}/complete`, { remarks: 'Completed' });
      toast.success('Maintenance marked as complete');
      onRefresh();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to complete maintenance'));
    }
  };

  if (data.length === 0) {
    return <p className="px-4 py-8 text-center text-slate-500">No maintenance logs yet</p>;
  }
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-700">
          <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Scheduled</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Completed</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Remarks</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Actions</th>
        </tr>
      </thead>
      <tbody>
        {data.map((m) => (
          <tr key={m.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
            <td className="px-4 py-3 text-slate-300">{m.id}</td>
            <td className="px-4 py-3 text-slate-200">{m.scheduledDate}</td>
            <td className="px-4 py-3">
              {m.completedDate ? (
                <span className="px-2 py-0.5 rounded text-xs font-medium bg-green-900/30 text-green-300 border border-green-800/50">{m.completedDate}</span>
              ) : (
                <span className="px-2 py-0.5 rounded text-xs font-medium bg-yellow-900/30 text-yellow-300 border border-yellow-800/50">Pending</span>
              )}
            </td>
            <td className="px-4 py-3 text-slate-400 text-sm max-w-xs truncate">{m.remarks || '—'}</td>
            <td className="px-4 py-3">
              {!m.completedDate && (
                <button onClick={() => completeMaintenance(m.id)}
                  className="px-2 py-1 rounded text-xs font-medium text-green-300 bg-green-900/30 hover:bg-green-900/50 border border-green-800/50 transition-colors">
                  Complete
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function UptimeTable({ data }) {
  if (data.length === 0) {
    return <p className="px-4 py-8 text-center text-slate-500">No uptime logs yet</p>;
  }
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b border-slate-700">
          <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Status</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Start Time</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">End Time</th>
          <th className="text-left px-4 py-3 text-slate-400 font-medium">Duration</th>
        </tr>
      </thead>
      <tbody>
        {data.map((u) => (
          <tr key={u.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
            <td className="px-4 py-3 text-slate-300">{u.id}</td>
            <td className="px-4 py-3">
              <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                u.status === 'UP' ? 'bg-green-900/30 text-green-300 border border-green-800/50'
                  : u.status === 'DOWN' ? 'bg-red-900/30 text-red-300 border border-red-800/50'
                  : 'bg-yellow-900/30 text-yellow-300 border border-yellow-800/50'
              }`}>{u.status}</span>
            </td>
            <td className="px-4 py-3 text-slate-400 text-xs">{formatTimestamp(u.startTime)}</td>
            <td className="px-4 py-3 text-slate-400 text-xs">{u.endTime ? formatTimestamp(u.endTime) : 'Ongoing'}</td>
            <td className="px-4 py-3 text-slate-300">{u.durationMinutes != null ? `${u.durationMinutes} min` : '—'}</td>
          </tr>
        ))}
      </tbody>
    </table>
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
