import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function ReportsPage() {
  const [summary, setSummary] = useState(null);
  const [mostBorrowed, setMostBorrowed] = useState([]);
  const [activeUsers, setActiveUsers] = useState([]);
  const [monthlyStats, setMonthlyStats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchReports(); }, []);

  const fetchReports = async () => {
    try {
      const [summaryRes, borrowedRes, usersRes, monthlyRes] = await Promise.all([
        api.get('/reports/summary'),
        api.get('/reports/most-borrowed?limit=10'),
        api.get('/reports/active-users?limit=10'),
        api.get('/reports/monthly-stats?months=6')
      ]);
      setSummary(summaryRes.data);
      setMostBorrowed(borrowedRes.data);
      setActiveUsers(usersRes.data);
      setMonthlyStats(monthlyRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>📊 Raporlar ve Analitik</h1>
        <p>Kütüphane istatistikleri ve raporları</p>
      </div>

      {summary && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-value">{summary.totalBooks}</div>
            <div className="stat-label">Toplam Kitap</div>
          </div>
          <div className="stat-card success">
            <div className="stat-value">{summary.availableBooks}</div>
            <div className="stat-label">Mevcut Kitap</div>
          </div>
          <div className="stat-card info">
            <div className="stat-value">{summary.totalUsers}</div>
            <div className="stat-label">Toplam Kullanıcı</div>
          </div>
          <div className="stat-card warning">
            <div className="stat-value">{summary.activeBorrows}</div>
            <div className="stat-label">Aktif Ödünç</div>
          </div>
          <div className="stat-card danger">
            <div className="stat-value">{summary.overdueBooks}</div>
            <div className="stat-label">Gecikmiş</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{summary.pendingReservations}</div>
            <div className="stat-label">Bekleyen Rezervasyon</div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '2rem' }}>
        <div className="card">
          <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>📚 En Çok Ödünç Alınan Kitaplar</h2>
          {mostBorrowed.length === 0 ? <p style={{ color: 'var(--text-muted)' }}>Henüz veri yok</p> : (
            <table>
              <thead><tr><th>#</th><th>Kitap</th><th>Yazar</th><th>Sayı</th></tr></thead>
              <tbody>
                {mostBorrowed.map((b, i) => (
                  <tr key={b.bookId}>
                    <td>{i + 1}</td><td>{b.title}</td><td>{b.author}</td>
                    <td><span className="badge badge-primary">{b.borrowCount}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>👥 En Aktif Kullanıcılar</h2>
          {activeUsers.length === 0 ? <p style={{ color: 'var(--text-muted)' }}>Henüz veri yok</p> : (
            <table>
              <thead><tr><th>#</th><th>Kullanıcı</th><th>Ad Soyad</th><th>Ödünç</th></tr></thead>
              <tbody>
                {activeUsers.map((u, i) => (
                  <tr key={u.userId}>
                    <td>{i + 1}</td><td>{u.username}</td><td>{u.fullName}</td>
                    <td><span className="badge badge-info">{u.borrowCount}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <div className="card">
        <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>📈 Aylık Ödünç Alma İstatistikleri</h2>
        {monthlyStats.length === 0 ? <p style={{ color: 'var(--text-muted)' }}>Henüz veri yok</p> : (
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: '1rem', height: '200px', padding: '1rem 0' }}>
            {monthlyStats.map((s) => {
              const maxCount = Math.max(...monthlyStats.map(m => m.borrowCount), 1);
              const height = (s.borrowCount / maxCount) * 160;
              return (
                <div key={`${s.year}-${s.month}`} style={{ flex: 1, textAlign: 'center' }}>
                  <div style={{ fontSize: '.75rem', color: 'var(--text-primary)', marginBottom: '.25rem' }}>
                    {s.borrowCount}
                  </div>
                  <div style={{
                    height: `${Math.max(height, 4)}px`,
                    background: 'linear-gradient(180deg, var(--primary), var(--secondary))',
                    borderRadius: '4px 4px 0 0',
                    transition: 'height .3s'
                  }}></div>
                  <div style={{ fontSize: '.7rem', color: 'var(--text-muted)', marginTop: '.35rem' }}>
                    {s.monthName.slice(0, 3)}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
