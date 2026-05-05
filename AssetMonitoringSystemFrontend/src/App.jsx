import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AlertsPage from './pages/AlertsPage';
import SensorSimulatorPage from './pages/SensorSimulatorPage';
import UsersPage from './pages/UsersPage';
import AssetDetailPage from './pages/AssetDetailPage';

export default function App() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-900">
      <Navbar />
      <Routes>
        <Route path="/login" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
        <Route path="/register" element={user ? <Navigate to="/dashboard" replace /> : <RegisterPage />} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/assets/:id" element={<ProtectedRoute><AssetDetailPage /></ProtectedRoute>} />
        <Route path="/alerts" element={<ProtectedRoute><AlertsPage /></ProtectedRoute>} />
        <Route path="/sensor-simulator" element={<ProtectedRoute><SensorSimulatorPage /></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute roles={['ROLE_MANAGER']}><UsersPage /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to={user ? '/dashboard' : '/login'} replace />} />
      </Routes>
    </div>
  );
}
