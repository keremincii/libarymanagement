import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function BorrowHistoryPage() {
  const [active, setActive] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [activeRes, historyRes] = await Promise.all([
        api.get('/borrow/active'),
        api.get('/borrow/history?size=50')
      ]);
      setActive(activeRes.data);
      setHistory(historyRes.data.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleReturn = async (transactionId) => {
    setMessage(null);
    try {
      const res = await api.post(`/borrow/return/${transactionId}`);
      setMessage({ type: 'success', text: res.data.message });
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'İade başarısız.' });
    }
  };

  const statusBadge = (status) => {
    const map = {
      BORROWED: 'badge-info',
      RETURNED: 'badge-success',
      OVERDUE: 'badge-danger'
    };
    const labels = { BORROWED: 'Ödünç', RETURNED: 'İade Edildi', OVERDUE: 'Gecikmiş' };
    return <span className={`badge ${map[status] || ''}`}>{labels[status] || status}</span>;
  };

  if (loading) return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>📖 Ödünç Aldıklarım</h1>
        <p>Aktif ve geçmiş ödünç alma işlemleriniz</p>
      </div>

      {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      {active.length > 0 && (
        <>
          <h2 style={{ fontSize: '1.15rem', marginBottom: '1rem' }}>Aktif Ödünç Alımlar ({active.length})</h2>
          <div className="table-container" style={{ marginBottom: '2rem' }}>
            <table>
              <thead>
                <tr>
                  <th>Kitap</th>
                  <th>Yazar</th>
                  <th>Ödünç Tarihi</th>
                  <th>Son Teslim</th>
                  <th>Durum</th>
                  <th>İşlem</th>
                </tr>
              </thead>
              <tbody>
                {active.map((t) => (
                  <tr key={t.transactionId}>
                    <td>{t.bookTitle}</td>
                    <td>{t.bookAuthor}</td>
                    <td>{new Date(t.borrowDate).toLocaleDateString('tr-TR')}</td>
                    <td>{new Date(t.dueDate).toLocaleDateString('tr-TR')}</td>
                    <td>{statusBadge(t.status)}</td>
                    <td>
                      <button className="btn btn-success btn-sm" onClick={() => handleReturn(t.transactionId)}>
                        İade Et
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <h2 style={{ fontSize: '1.15rem', marginBottom: '1rem' }}>İşlem Geçmişi</h2>
      {history.length === 0 ? (
        <div className="empty-state"><p>Henüz işlem geçmişiniz yok.</p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Kitap</th>
                <th>Yazar</th>
                <th>Ödünç Tarihi</th>
                <th>Son Teslim</th>
                <th>İade Tarihi</th>
                <th>Durum</th>
              </tr>
            </thead>
            <tbody>
              {history.map((t) => (
                <tr key={t.transactionId}>
                  <td>{t.bookTitle}</td>
                  <td>{t.bookAuthor}</td>
                  <td>{new Date(t.borrowDate).toLocaleDateString('tr-TR')}</td>
                  <td>{new Date(t.dueDate).toLocaleDateString('tr-TR')}</td>
                  <td>{t.returnDate ? new Date(t.returnDate).toLocaleDateString('tr-TR') : '—'}</td>
                  <td>{statusBadge(t.status)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
