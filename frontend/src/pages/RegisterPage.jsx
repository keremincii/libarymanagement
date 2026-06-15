import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form.username, form.email, form.password, form.fullName);
      navigate('/');
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) {
        setError(Object.values(data.fieldErrors).join(', '));
      } else {
        setError(data?.message || 'Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>📚 Kayıt Ol</h1>
        <p className="subtitle">Yeni bir hesap oluşturun</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="fullName">Ad Soyad</label>
            <input id="fullName" name="fullName" type="text" className="form-control"
              placeholder="Ad Soyad" value={form.fullName} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="username">Kullanıcı Adı</label>
            <input id="username" name="username" type="text" className="form-control"
              placeholder="Kullanıcı adı" value={form.username} onChange={handleChange} required minLength={3} />
          </div>
          <div className="form-group">
            <label htmlFor="email">E-posta</label>
            <input id="email" name="email" type="email" className="form-control"
              placeholder="ornek@mail.com" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="password">Şifre</label>
            <input id="password" name="password" type="password" className="form-control"
              placeholder="En az 6 karakter" value={form.password} onChange={handleChange} required minLength={6} />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Kayıt yapılıyor...' : 'Kayıt Ol'}
          </button>
        </form>

        <p className="auth-link">
          Zaten hesabınız var mı? <Link to="/login">Giriş Yap</Link>
        </p>
      </div>
    </div>
  );
}
