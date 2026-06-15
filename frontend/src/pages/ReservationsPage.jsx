import { useState, useEffect } from 'react';
import api from '../api/axios';

export default function ReservationsPage() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => { fetchReservations(); }, []);

  const fetchReservations = async () => {
    setLoading(true);
    try {
      const res = await api.get('/reservations');
      setReservations(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    setMessage(null);
    try {
      await api.delete(`/reservations/${id}`);
      setMessage({ type: 'success', text: 'Rezervasyon iptal edildi.' });
      fetchReservations();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'İptal başarısız.' });
    }
  };

  const statusBadge = (status) => {
    const map = {
      PENDING: 'badge-warning', FULFILLED: 'badge-success',
      CANCELLED: 'badge-info', EXPIRED: 'badge-danger'
    };
    const labels = {
      PENDING: 'Bekliyor', FULFILLED: 'Tamamlandı',
      CANCELLED: 'İptal Edildi', EXPIRED: 'Süresi Doldu'
    };
    return <span className={`badge ${map[status]}`}>{labels[status] || status}</span>;
  };

  if (loading) return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>🔖 Rezervasyonlarım</h1>
        <p>Kitap rezervasyonlarınızı takip edin</p>
      </div>

      {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      {reservations.length === 0 ? (
        <div className="empty-state"><p>Henüz rezervasyonunuz yok.</p></div>
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Kitap</th>
                <th>Yazar</th>
                <th>Tarih</th>
                <th>Sıra</th>
                <th>Durum</th>
                <th>İşlem</th>
              </tr>
            </thead>
            <tbody>
              {reservations.map((r) => (
                <tr key={r.reservationId}>
                  <td>{r.bookTitle}</td>
                  <td>{r.bookAuthor}</td>
                  <td>{new Date(r.reservationDate).toLocaleDateString('tr-TR')}</td>
                  <td>{r.status === 'PENDING' ? `#${r.queuePosition}` : '—'}</td>
                  <td>{statusBadge(r.status)}</td>
                  <td>
                    {r.status === 'PENDING' && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleCancel(r.reservationId)}>
                        İptal
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
