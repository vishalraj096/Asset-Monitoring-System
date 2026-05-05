import { useState, useEffect } from 'react';
import API, { extractErrorMessage } from '../api/api';
import toast from 'react-hot-toast';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    try {
      const res = await API.get('/users');
      setUsers(res.data.data || []);
    } catch (err) {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const changeRole = async (userId, newRole) => {
    try {
      await API.put(`/users/${userId}/role`, { role: newRole });
      toast.success('Role updated');
      fetchUsers();
    } catch (err) {
      toast.error(extractErrorMessage(err, 'Failed to update role'));
    }
  };

  if (loading) {
    return <div className="text-center py-12 text-slate-400">Loading...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-100 mb-6">User Management</h1>
      <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-700">
              <th className="text-left px-4 py-3 text-slate-400 font-medium">ID</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Name</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Email</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Role</th>
              <th className="text-left px-4 py-3 text-slate-400 font-medium">Change Role</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-slate-700/50 hover:bg-slate-750/50">
                <td className="px-4 py-3 text-slate-300">{u.id}</td>
                <td className="px-4 py-3 text-slate-200 font-medium">{u.name}</td>
                <td className="px-4 py-3 text-slate-300">{u.email}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                    u.role === 'ROLE_MANAGER' ? 'bg-blue-900/30 text-blue-300 border border-blue-800/50' : 'bg-slate-700 text-slate-300 border border-slate-600'
                  }`}>{u.role.replace('ROLE_', '')}</span>
                </td>
                <td className="px-4 py-3">
                  <select value="" onChange={(e) => { if (e.target.value) changeRole(u.id, e.target.value); }}
                    className="px-2 py-1 rounded bg-slate-900 border border-slate-600 text-slate-200 text-xs focus:outline-none focus:border-blue-500">
                    <option value="">Select...</option>
                    <option value="OPERATOR">Operator</option>
                    <option value="MANAGER">Manager</option>
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
