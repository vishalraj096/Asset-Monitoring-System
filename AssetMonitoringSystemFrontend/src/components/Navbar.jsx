import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isManager } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <nav className="bg-slate-800 border-b border-slate-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-8">
            <Link to="/dashboard" className="text-blue-400 font-bold text-lg tracking-tight">
              EAMS
            </Link>
            <div className="flex items-center gap-1">
              <Link
                to="/dashboard"
                className="px-3 py-2 rounded text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-700 transition-colors"
              >
                Dashboard
              </Link>
              <Link
                to="/alerts"
                className="px-3 py-2 rounded text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-700 transition-colors"
              >
                Alerts
              </Link>
              <Link
                to="/sensor-simulator"
                className="px-3 py-2 rounded text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-700 transition-colors"
              >
                Sensor Simulator
              </Link>
              {isManager() && (
                <Link
                  to="/users"
                  className="px-3 py-2 rounded text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-700 transition-colors"
                >
                  Users
                </Link>
              )}
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-sm">
              <span className="text-slate-400">{user.email}</span>
              <span className="ml-2 px-2 py-0.5 rounded text-xs font-medium bg-blue-900/50 text-blue-300 border border-blue-800">
                {user.role.replace('ROLE_', '')}
              </span>
            </div>
            <button
              onClick={handleLogout}
              className="px-3 py-1.5 rounded text-sm font-medium text-slate-300 hover:text-white bg-slate-700 hover:bg-slate-600 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}
