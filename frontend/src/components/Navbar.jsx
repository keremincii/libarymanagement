import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isStaff, isAdmin } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        📚 <span>Kütüphane</span> Yönetim
      </Link>

      <div className="navbar-links">
        <Link to="/" className={isActive('/')}>Kitaplar</Link>
        <Link to="/my-borrows" className={isActive('/my-borrows')}>Ödünç Aldıklarım</Link>
        <Link to="/my-reservations" className={isActive('/my-reservations')}>Rezervasyonlarım</Link>
        {isStaff() && (
          <Link to="/reports" className={isActive('/reports')}>Raporlar</Link>
        )}
        {isAdmin() && (
          <Link to="/users" className={isActive('/users')}>Kullanıcılar</Link>
        )}
      </div>

      <div className="navbar-user">
        <span className="user-badge">
          {user?.fullName} ({user?.role})
        </span>
        <button className="btn-logout" onClick={logout}>Çıkış</button>
      </div>
    </nav>
  );
}
