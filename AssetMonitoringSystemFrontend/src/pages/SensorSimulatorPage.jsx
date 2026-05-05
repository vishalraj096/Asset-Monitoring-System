import { useState, useEffect } from 'react';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function SensorSimulatorPage() {
  const [assets, setAssets] = useState([]);
  const [assetId, setAssetId] = useState('');
  const [temperature, setTemperature] = useState(50);
  const [pressure, setPressure] = useState(30);
  const [loading, setLoading] = useState(false);
  const [lastResult, setLastResult] = useState(null);

  useEffect(() => {
    API.get('/assets?size=100')
      .then((res) => {
        const list = res.data.data.content || [];
        setAssets(list);
        if (list.length > 0) setAssetId(String(list[0].id));
      })
      .catch(() => toast.error('Failed to load assets'));
  }, []);

  const selectedAsset = assets.find((a) => String(a.id) === assetId);

  const handleSubmit = async () => {
    if (!assetId) { toast.error('Select an asset'); return; }
    setLoading(true);
    try {
      const res = await API.post('/sensors/send-data', {
        assetId: parseInt(assetId),
        temperature,
        pressure,
        timestamp: new Date().toISOString(),
      });
      setLastResult(res.data.data);
      toast.success('Sensor data submitted');
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Submit failed'));
    } finally {
      setLoading(false);
    }
  };

  const tempExceeded = selectedAsset && temperature > selectedAsset.thresholdTemp;
  const pressureExceeded = selectedAsset && pressure > selectedAsset.thresholdPressure;

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-100 mb-6">Sensor Data Simulator</h1>
      <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 space-y-6">
        <div>
          <label htmlFor="sim-asset" className="block text-sm font-medium text-slate-300 mb-1">Select Asset</label>
          <select id="sim-asset" value={assetId} onChange={(e) => setAssetId(e.target.value)}
            className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 focus:outline-none focus:border-blue-500">
            {assets.length === 0 && <option value="">No assets available</option>}
            {assets.map((a) => <option key={a.id} value={a.id}>{a.name} (ID: {a.id})</option>)}
          </select>
        </div>

        {selectedAsset && (
          <div className="bg-slate-900/50 border border-slate-700 rounded p-3 text-sm text-slate-400">
            <span>Thresholds — Temp: <span className="text-yellow-300">{selectedAsset.thresholdTemp}°</span> | Pressure: <span className="text-yellow-300">{selectedAsset.thresholdPressure} PSI</span></span>
          </div>
        )}

        <div>
          <div className="flex justify-between mb-1">
            <label htmlFor="sim-temp" className="text-sm font-medium text-slate-300">Temperature</label>
            <span className={`text-sm font-mono ${tempExceeded ? 'text-red-400' : 'text-slate-400'}`}>{temperature}°</span>
          </div>
          <input id="sim-temp" type="range" min="0" max="150" value={temperature} onChange={(e) => setTemperature(Number(e.target.value))}
            className="w-full h-2 rounded-lg appearance-none cursor-pointer accent-blue-500 bg-slate-700" />
          <div className="flex justify-between text-xs text-slate-500 mt-1"><span>0°</span><span>150°</span></div>
          {tempExceeded && (
            <p className="text-xs text-red-400 mt-1">⚠ Exceeds threshold — will trigger TEMP_HIGH alert</p>
          )}
        </div>

        <div>
          <div className="flex justify-between mb-1">
            <label htmlFor="sim-pressure" className="text-sm font-medium text-slate-300">Pressure</label>
            <span className={`text-sm font-mono ${pressureExceeded ? 'text-red-400' : 'text-slate-400'}`}>{pressure} PSI</span>
          </div>
          <input id="sim-pressure" type="range" min="0" max="100" value={pressure} onChange={(e) => setPressure(Number(e.target.value))}
            className="w-full h-2 rounded-lg appearance-none cursor-pointer accent-blue-500 bg-slate-700" />
          <div className="flex justify-between text-xs text-slate-500 mt-1"><span>0 PSI</span><span>100 PSI</span></div>
          {pressureExceeded && (
            <p className="text-xs text-red-400 mt-1">⚠ Exceeds threshold — will trigger PRESSURE_HIGH alert</p>
          )}
        </div>

        <button onClick={handleSubmit} disabled={loading || !assetId}
          className="w-full py-2.5 rounded font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
          {loading ? 'Submitting...' : 'Submit Sensor Data'}
        </button>

        {lastResult && (
          <div className="bg-slate-900/50 border border-slate-700 rounded p-4 text-sm">
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-2">Last Submission</p>
            <div className="grid grid-cols-2 gap-2 text-slate-300">
              <div>Asset: <span className="text-slate-200">{lastResult.assetName}</span></div>
              <div>ID: <span className="text-slate-200">{lastResult.id}</span></div>
              <div>Temp: <span className="text-slate-200">{lastResult.temperature}°</span></div>
              <div>Pressure: <span className="text-slate-200">{lastResult.pressure} PSI</span></div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
