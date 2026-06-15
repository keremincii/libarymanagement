import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import BooksPage from './pages/BooksPage';
import BookDetailPage from './pages/BookDetailPage';
import BorrowHistoryPage from './pages/BorrowHistoryPage';
import ReservationsPage from './pages/ReservationsPage';
import ReportsPage from './pages/ReportsPage';
import UsersPage from './pages/UsersPage';

function AppRoutes() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="loading"><div className="spinner"></div> Yükleniyor...</div>;
  }

  return (
    <>
      {user && <Navbar />}
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={user ? <Navigate to="/" /> : <LoginPage />} />
        <Route path="/register" element={user ? <Navigate to="/" /> : <RegisterPage />} />

        {/* Protected routes */}
        <Route path="/" element={
          <ProtectedRoute><BooksPage /></ProtectedRoute>
        } />
        <Route path="/books/:id" element={
          <ProtectedRoute><BookDetailPage /></ProtectedRoute>
        } />
        <Route path="/my-borrows" element={
          <ProtectedRoute><BorrowHistoryPage /></ProtectedRoute>
        } />
        <Route path="/my-reservations" element={
          <ProtectedRoute><ReservationsPage /></ProtectedRoute>
        } />
        <Route path="/reports" element={
          <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}><ReportsPage /></ProtectedRoute>
        } />
        <Route path="/users" element={
          <ProtectedRoute roles={['ADMIN']}><UsersPage /></ProtectedRoute>
        } />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
