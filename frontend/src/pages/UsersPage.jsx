import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    try {
      const res = await api.get('/users');
      setUsers(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    setMessage(null);
    try {
      await api.put(`/users/${userId}/role?role=${newRole}`);
      setMessage({ type: 'success', text: 'Rol güncellendi.' });
      fetchUsers();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Güncelleme başarısız.' });
    }
  };

  const roleBadge = (role) => {
    const map = { ADMIN: 'badge-danger', LIBRARIAN: 'badge-warning', STUDENT: 'badge-info' };
    return <span className={`badge ${map[role]}`}>{role}</span>;
  };

  if (loading) return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>👥 Kullanıcı Yönetimi</h1>
        <p>Tüm kullanıcıları görüntüle ve yönet</p>
      </div>

      {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Ad Soyad</th>
              <th>Kullanıcı Adı</th>
              <th>E-posta</th>
              <th>Rol</th>
              <th>Durum</th>
              <th>Kayıt Tarihi</th>
              <th>İşlem</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.fullName}</td>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>{roleBadge(u.role)}</td>
                <td>
                  <span className={`badge ${u.active ? 'badge-success' : 'badge-danger'}`}>
                    {u.active ? 'Aktif' : 'Pasif'}
                  </span>
                </td>
                <td>{new Date(u.createdAt).toLocaleDateString('tr-TR')}</td>
                <td>
                  <select
                    value={u.role}
                    onChange={(e) => handleRoleChange(u.id, e.target.value)}
                    style={{
                      background: 'var(--bg-input)', color: 'var(--text-primary)',
                      border: '1px solid var(--border)', borderRadius: '6px',
                      padding: '.3rem .5rem', fontSize: '.8rem', fontFamily: 'var(--font)'
                    }}
                  >
                    <option value="STUDENT">STUDENT</option>
                    <option value="LIBRARIAN">LIBRARIAN</option>
                    <option value="ADMIN">ADMIN</option>
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
