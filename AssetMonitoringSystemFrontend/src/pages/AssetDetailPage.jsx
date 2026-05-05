import { useState, useEffect, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function AssetDetailPage() {
  const { id } = useParams();
  const [asset, setAsset] = useState(null);
  const [sensorData, setSensorData] = useState([]);
  const [maintenance, setMaintenance] = useState([]);
  const [uptimeLogs, setUptimeLogs] = useState([]);
  const [uptimeSummary, setUptimeSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('sensor');

  useEffect(() => {
    const load = async () => {
      try {
        const [assetRes, sensorRes, maintRes, uptimeRes, summaryRes] = await Promise.all([
          API.get(`/assets/${id}`),
          API.get(`/sensors/asset/${id}?size=20`),
          API.get(`/maintenance/asset/${id}?size=20`),
          API.get(`/uptime/asset/${id}?size=50`),
          API.get(`/uptime/asset/${id}/summary`).catch(() => ({ data: { data: null } })),
        ]);
        setAsset(assetRes.data.data);
        setSensorData(sensorRes.data.data.content || []);
        setMaintenance(maintRes.data.data.content || []);
        setUptimeLogs(uptimeRes.data.data.content || []);
        setUptimeSummary(summaryRes.data.data);
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
    { key: 'uptime', label: 'Uptime', count: uptimeLogs.length },
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
            {uptimeSummary && (
              <div className="mt-1 flex items-center gap-2 justify-end">
                <span className={`inline-block w-2 h-2 rounded-full ${uptimeSummary.currentStatus === 'UP' ? 'bg-green-400' : uptimeSummary.currentStatus === 'DOWN' ? 'bg-red-400' : 'bg-yellow-400'}`} />
                <span className="text-xs text-slate-400">{uptimeSummary.currentStatus}</span>
              </div>
            )}
          </div>
        </div>
        <div className="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-4">
          <StatCard label="Temp Threshold" value={`${asset.thresholdTemp}°`} />
          <StatCard label="Pressure Threshold" value={`${asset.thresholdPressure} PSI`} />
          <StatCard label="Sensor Readings" value={sensorData.length} />
          <StatCard label="Uptime" value={uptimeSummary ? `${uptimeSummary.uptimePercentage}%` : '—'} highlight={uptimeSummary?.uptimePercentage >= 95 ? 'green' : uptimeSummary?.uptimePercentage >= 80 ? 'yellow' : 'red'} />
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
        {activeTab === 'sensor' && <SensorDataTab data={sensorData} asset={asset} />}
        {activeTab === 'maintenance' && <MaintenanceTable data={maintenance} onRefresh={() => refreshMaintenance(id, setMaintenance)} />}
        {activeTab === 'uptime' && <UptimeTab data={uptimeLogs} summary={uptimeSummary} />}
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

function StatCard({ label, value, highlight }) {
  const colors = {
    green: 'text-green-400',
    yellow: 'text-yellow-400',
    red: 'text-red-400',
  };
  return (
    <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className={`text-lg font-semibold mt-1 ${highlight ? colors[highlight] || 'text-slate-200' : 'text-slate-200'}`}>{value}</p>
    </div>
  );
}

/* ─── Sensor Data Tab with Chart ─── */
function SensorDataTab({ data, asset }) {
  if (data.length === 0) {
    return <p className="px-4 py-8 text-center text-slate-500">No sensor data recorded yet</p>;
  }

  // Reverse data so oldest is first for chart display
  const chronological = [...data].reverse();

  return (
    <div>
      {/* Mini sensor chart */}
      <div className="p-4 border-b border-slate-700">
        <h3 className="text-sm font-medium text-slate-400 mb-3">Sensor Readings Over Time</h3>
        <SensorChart data={chronological} thresholdTemp={asset.thresholdTemp} thresholdPressure={asset.thresholdPressure} />
      </div>
      {/* Table */}
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
              <td className={`px-4 py-3 font-medium ${s.temperature > asset.thresholdTemp ? 'text-red-400' : 'text-slate-200'}`}>{s.temperature}°</td>
              <td className={`px-4 py-3 font-medium ${s.pressure > asset.thresholdPressure ? 'text-red-400' : 'text-slate-200'}`}>{s.pressure} PSI</td>
              <td className="px-4 py-3 text-slate-400 text-xs">{formatTimestamp(s.timestamp)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

/* ─── Canvas-based Sensor Chart ─── */
function SensorChart({ data, thresholdTemp, thresholdPressure }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || data.length === 0) return;
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);
    const w = rect.width;
    const h = rect.height;

    ctx.clearRect(0, 0, w, h);

    const pad = { top: 20, right: 20, bottom: 30, left: 45 };
    const cw = w - pad.left - pad.right;
    const ch = h - pad.top - pad.bottom;

    const temps = data.map(d => d.temperature);
    const pressures = data.map(d => d.pressure);
    const allVals = [...temps, ...pressures, thresholdTemp || 0, thresholdPressure || 0];
    const minV = Math.min(...allVals) - 5;
    const maxV = Math.max(...allVals) + 5;

    const xStep = cw / Math.max(data.length - 1, 1);
    const yScale = (v) => pad.top + ch - ((v - minV) / (maxV - minV)) * ch;

    // Grid lines
    ctx.strokeStyle = '#334155';
    ctx.lineWidth = 0.5;
    for (let i = 0; i <= 4; i++) {
      const y = pad.top + (ch / 4) * i;
      ctx.beginPath(); ctx.moveTo(pad.left, y); ctx.lineTo(w - pad.right, y); ctx.stroke();
      const val = maxV - ((maxV - minV) / 4) * i;
      ctx.fillStyle = '#64748b';
      ctx.font = '10px Inter, system-ui';
      ctx.textAlign = 'right';
      ctx.fillText(val.toFixed(0), pad.left - 5, y + 3);
    }

    // Threshold lines
    if (thresholdTemp != null) {
      ctx.setLineDash([4, 4]);
      ctx.strokeStyle = '#f97316'; ctx.lineWidth = 1;
      const ty = yScale(thresholdTemp);
      ctx.beginPath(); ctx.moveTo(pad.left, ty); ctx.lineTo(w - pad.right, ty); ctx.stroke();
      ctx.fillStyle = '#f97316'; ctx.font = '9px Inter'; ctx.textAlign = 'left';
      ctx.fillText(`Temp: ${thresholdTemp}°`, w - pad.right - 70, ty - 4);
    }
    if (thresholdPressure != null) {
      ctx.strokeStyle = '#a855f7'; ctx.lineWidth = 1;
      const py = yScale(thresholdPressure);
      ctx.beginPath(); ctx.moveTo(pad.left, py); ctx.lineTo(w - pad.right, py); ctx.stroke();
      ctx.fillStyle = '#a855f7'; ctx.font = '9px Inter'; ctx.textAlign = 'left';
      ctx.fillText(`Press: ${thresholdPressure}`, w - pad.right - 75, py - 4);
    }
    ctx.setLineDash([]);

    // Draw temperature line
    drawLine(ctx, data, 'temperature', pad, xStep, yScale, '#3b82f6', 2);
    // Draw pressure line
    drawLine(ctx, data, 'pressure', pad, xStep, yScale, '#8b5cf6', 2);

    // Legend
    ctx.fillStyle = '#3b82f6'; ctx.fillRect(pad.left, h - 12, 10, 3);
    ctx.fillStyle = '#94a3b8'; ctx.font = '10px Inter'; ctx.textAlign = 'left';
    ctx.fillText('Temperature', pad.left + 14, h - 8);
    ctx.fillStyle = '#8b5cf6'; ctx.fillRect(pad.left + 100, h - 12, 10, 3);
    ctx.fillStyle = '#94a3b8';
    ctx.fillText('Pressure', pad.left + 114, h - 8);
  }, [data, thresholdTemp, thresholdPressure]);

  return <canvas ref={canvasRef} className="w-full" style={{ height: 200 }} />;
}

function drawLine(ctx, data, key, pad, xStep, yScale, color, width) {
  ctx.strokeStyle = color;
  ctx.lineWidth = width;
  ctx.lineJoin = 'round';
  ctx.beginPath();
  data.forEach((d, i) => {
    const x = pad.left + i * xStep;
    const y = yScale(d[key]);
    if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
  });
  ctx.stroke();

  // Dots
  data.forEach((d, i) => {
    const x = pad.left + i * xStep;
    const y = yScale(d[key]);
    ctx.fillStyle = color;
    ctx.beginPath(); ctx.arc(x, y, 3, 0, Math.PI * 2); ctx.fill();
  });
}

/* ─── Uptime Tab with Chart + Table ─── */
function UptimeTab({ data, summary }) {
  return (
    <div>
      {/* Uptime Summary Chart */}
      {summary && (
        <div className="p-5 border-b border-slate-700">
          <h3 className="text-sm font-medium text-slate-400 mb-4">Uptime Overview</h3>
          <div className="flex flex-col sm:flex-row items-center gap-6">
            {/* Donut chart */}
            <UptimeDonutChart percentage={summary.uptimePercentage} />
            {/* Stats */}
            <div className="grid grid-cols-2 gap-4 flex-1">
              <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
                <p className="text-xs text-slate-500 uppercase">Current Status</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className={`inline-block w-2.5 h-2.5 rounded-full ${summary.currentStatus === 'UP' ? 'bg-green-400 animate-pulse' : 'bg-red-400 animate-pulse'}`} />
                  <span className={`text-lg font-bold ${summary.currentStatus === 'UP' ? 'text-green-400' : 'text-red-400'}`}>{summary.currentStatus}</span>
                </div>
              </div>
              <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
                <p className="text-xs text-slate-500 uppercase">Uptime %</p>
                <p className={`text-lg font-bold mt-1 ${summary.uptimePercentage >= 95 ? 'text-green-400' : summary.uptimePercentage >= 80 ? 'text-yellow-400' : 'text-red-400'}`}>{summary.uptimePercentage}%</p>
              </div>
              <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
                <p className="text-xs text-slate-500 uppercase">Total UP</p>
                <p className="text-lg font-bold text-green-400 mt-1">{formatDuration(summary.totalUpMinutes)}</p>
              </div>
              <div className="bg-slate-900/50 border border-slate-700 rounded p-3">
                <p className="text-xs text-slate-500 uppercase">Total DOWN</p>
                <p className="text-lg font-bold text-red-400 mt-1">{formatDuration(summary.totalDownMinutes)}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Timeline Bar */}
      {data.length > 0 && (
        <div className="p-5 border-b border-slate-700">
          <h3 className="text-sm font-medium text-slate-400 mb-3">Status Timeline</h3>
          <UptimeTimeline logs={data} />
        </div>
      )}

      {/* Table */}
      {data.length === 0 ? (
        <p className="px-4 py-8 text-center text-slate-500">No uptime logs yet</p>
      ) : (
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
                      : 'bg-red-900/30 text-red-300 border border-red-800/50'
                  }`}>{u.status}</span>
                </td>
                <td className="px-4 py-3 text-slate-400 text-xs">{formatTimestamp(u.startTime)}</td>
                <td className="px-4 py-3 text-slate-400 text-xs">{u.endTime ? formatTimestamp(u.endTime) : <span className="text-green-400">Ongoing</span>}</td>
                <td className="px-4 py-3 text-slate-300">{u.durationMinutes != null ? formatDuration(u.durationMinutes) : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

/* ─── Donut Chart (SVG) ─── */
function UptimeDonutChart({ percentage }) {
  const r = 52;
  const c = 2 * Math.PI * r;
  const offset = c - (percentage / 100) * c;
  const color = percentage >= 95 ? '#22c55e' : percentage >= 80 ? '#eab308' : '#ef4444';

  return (
    <div className="relative" style={{ width: 140, height: 140 }}>
      <svg width="140" height="140" viewBox="0 0 140 140">
        {/* Background circle */}
        <circle cx="70" cy="70" r={r} fill="none" stroke="#1e293b" strokeWidth="12" />
        {/* Progress arc */}
        <circle
          cx="70" cy="70" r={r} fill="none"
          stroke={color} strokeWidth="12" strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={offset}
          transform="rotate(-90 70 70)"
          style={{ transition: 'stroke-dashoffset 0.8s ease' }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-2xl font-bold" style={{ color }}>{percentage}%</span>
        <span className="text-xs text-slate-500">uptime</span>
      </div>
    </div>
  );
}

/* ─── Uptime Timeline Bar ─── */
function UptimeTimeline({ logs }) {
  // Sort chronologically (oldest first)
  const sorted = [...logs].reverse();
  if (sorted.length === 0) return null;

  const earliest = new Date(sorted[0].startTime);
  const now = new Date();
  const totalSpan = now - earliest;
  if (totalSpan <= 0) return null;

  return (
    <div className="relative">
      <div className="flex rounded overflow-hidden h-6 bg-slate-900">
        {sorted.map((log, i) => {
          const start = new Date(log.startTime);
          const end = log.endTime ? new Date(log.endTime) : now;
          const width = ((end - start) / totalSpan) * 100;
          if (width <= 0) return null;
          return (
            <div
              key={log.id}
              className={`h-full relative group ${log.status === 'UP' ? 'bg-green-600/70' : 'bg-red-600/70'}`}
              style={{ width: `${Math.max(width, 0.5)}%` }}
              title={`${log.status}: ${formatTimestamp(log.startTime)} → ${log.endTime ? formatTimestamp(log.endTime) : 'Now'}`}
            >
              {/* Tooltip */}
              <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 hidden group-hover:block z-10">
                <div className="bg-slate-700 text-xs text-slate-200 rounded px-2 py-1 whitespace-nowrap shadow-lg border border-slate-600">
                  {log.status} • {log.durationMinutes != null ? formatDuration(log.durationMinutes) : 'Ongoing'}
                </div>
              </div>
            </div>
          );
        })}
      </div>
      <div className="flex justify-between mt-1 text-xs text-slate-500">
        <span>{formatTimestamp(sorted[0].startTime)}</span>
        <span>Now</span>
      </div>
    </div>
  );
}

/* ─── Maintenance Table ─── */
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

/* ─── Utilities ─── */
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

function formatDuration(minutes) {
  if (minutes == null || minutes < 0) return '—';
  if (minutes < 1) return '<1 min';
  if (minutes < 60) return `${minutes} min`;
  const hrs = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hrs < 24) return `${hrs}h ${mins}m`;
  const days = Math.floor(hrs / 24);
  const remainHrs = hrs % 24;
  return `${days}d ${remainHrs}h`;
}
