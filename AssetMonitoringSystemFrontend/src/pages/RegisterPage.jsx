import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('OPERATOR');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register(name, email, password, role);
      toast.success('Registration successful');
      navigate('/dashboard');
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Registration failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900 px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-blue-400">EAMS</h1>
          <p className="text-slate-400 mt-1">Create your account</p>
        </div>
        <form onSubmit={handleSubmit} className="bg-slate-800 border border-slate-700 rounded-lg p-8 space-y-5">
          <h2 className="text-xl font-semibold text-slate-200">Register</h2>
          <div>
            <label htmlFor="reg-name" className="block text-sm font-medium text-slate-300 mb-1">Full Name</label>
            <input id="reg-name" type="text" required minLength={2} value={name} onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500" placeholder="John Doe" />
          </div>
          <div>
            <label htmlFor="reg-email" className="block text-sm font-medium text-slate-300 mb-1">Email</label>
            <input id="reg-email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500" placeholder="you@example.com" />
          </div>
          <div>
            <label htmlFor="reg-pass" className="block text-sm font-medium text-slate-300 mb-1">Password</label>
            <input id="reg-pass" type="password" required minLength={6} value={password} onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500" placeholder="Min 6 characters" />
          </div>
          <div>
            <label htmlFor="reg-role" className="block text-sm font-medium text-slate-300 mb-1">Role</label>
            <select id="reg-role" value={role} onChange={(e) => setRole(e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-900 border border-slate-600 text-slate-200 focus:outline-none focus:border-blue-500">
              <option value="OPERATOR">Operator</option>
              <option value="MANAGER">Manager</option>
            </select>
          </div>
          <button type="submit" disabled={loading}
            className="w-full py-2.5 rounded font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors">
            {loading ? 'Creating account...' : 'Register'}
          </button>
          <p className="text-sm text-center text-slate-400">
            Already have an account? <Link to="/login" className="text-blue-400 hover:text-blue-300">Sign In</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
