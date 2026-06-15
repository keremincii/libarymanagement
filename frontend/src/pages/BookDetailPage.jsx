import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function BookDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isStaff } = useAuth();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    fetchBook();
  }, [id]);

  const fetchBook = async () => {
    try {
      const res = await api.get(`/books/${id}`);
      setBook(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleBorrow = async () => {
    setActionLoading(true);
    setMessage(null);
    try {
      await api.post('/borrow', { bookId: book.id });
      setMessage({ type: 'success', text: 'Kitap başarıyla ödünç alındı!' });
      fetchBook();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Ödünç alma başarısız.' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleReserve = async () => {
    setActionLoading(true);
    setMessage(null);
    try {
      const res = await api.post('/reservations', { bookId: book.id });
      setMessage({ type: 'success', text: `Rezervasyon oluşturuldu! Sıra: ${res.data.queuePosition}` });
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Rezervasyon başarısız.' });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;
  if (!book) return <div className="page-container"><div className="empty-state"><p>Kitap bulunamadı</p></div></div>;

  return (
    <div className="page-container">
      <button className="btn btn-secondary" onClick={() => navigate(-1)} style={{ marginBottom: '1.5rem' }}>
        ← Geri
      </button>

      {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

      <div className="card" style={{ maxWidth: '700px' }}>
        <h1 style={{ fontSize: '1.5rem', marginBottom: '.5rem' }}>{book.title}</h1>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '1.05rem' }}>{book.author}</p>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
          <div>
            <span style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>ISBN</span>
            <p>{book.isbn}</p>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>Yayın Yılı</span>
            <p>{book.yearOfPublication || '—'}</p>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>Yayınevi</span>
            <p>{book.publisher || '—'}</p>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>Durum</span>
            <p>
              <span className={`badge ${book.availableCopies > 0 ? 'badge-success' : 'badge-danger'}`}>
                {book.availableCopies} / {book.totalCopies} mevcut
              </span>
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '.75rem', flexWrap: 'wrap' }}>
          {book.availableCopies > 0 ? (
            <button className="btn btn-primary" onClick={handleBorrow} disabled={actionLoading}>
              {actionLoading ? 'İşleniyor...' : '📖 Ödünç Al'}
            </button>
          ) : (
            <button className="btn btn-warning" style={{ background: 'var(--warning)', color: '#000' }}
              onClick={handleReserve} disabled={actionLoading}>
              {actionLoading ? 'İşleniyor...' : '🔖 Rezervasyon Yap'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
