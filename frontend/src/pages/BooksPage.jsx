import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function BooksPage() {
  const [books, setBooks] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchBooks = async (pageNum = 0, search = '') => {
    setLoading(true);
    try {
      const endpoint = search
        ? `/books/search?keyword=${encodeURIComponent(search)}&page=${pageNum}&size=12`
        : `/books?page=${pageNum}&size=12`;
      const res = await api.get(endpoint);
      setBooks(res.data.content);
      setTotalPages(res.data.totalPages);
      setPage(res.data.number);
    } catch (err) {
      console.error('Error fetching books:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchBooks(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchBooks(0, keyword);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>📚 Kitap Kataloğu</h1>
        <p>Kütüphanedeki tüm kitapları arayın ve keşfedin</p>
      </div>

      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          className="form-control"
          placeholder="Kitap adı, yazar, ISBN veya yayınevi ara..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button type="submit" className="btn btn-primary">Ara</button>
        {keyword && (
          <button type="button" className="btn btn-secondary"
            onClick={() => { setKeyword(''); fetchBooks(0, ''); }}>
            Temizle
          </button>
        )}
      </form>

      {loading ? (
        <div className="loading"><div className="spinner"></div> Yükleniyor...</div>
      ) : books.length === 0 ? (
        <div className="empty-state">
          <p>📖 Kitap bulunamadı</p>
          <p style={{ fontSize: '.85rem' }}>Farklı bir arama terimi deneyin</p>
        </div>
      ) : (
        <>
          <div className="book-grid">
            {books.map((book) => (
              <div key={book.id} className="book-card" onClick={() => navigate(`/books/${book.id}`)}>
                <h3>{book.title}</h3>
                <p className="book-author">{book.author}</p>
                <div className="book-meta">
                  <span>{book.yearOfPublication || '—'}</span>
                  <span className={`badge ${book.availableCopies > 0 ? 'badge-success' : 'badge-danger'}`}>
                    {book.availableCopies > 0 ? `${book.availableCopies} mevcut` : 'Stokta yok'}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button disabled={page === 0} onClick={() => fetchBooks(page - 1, keyword)}>
                ← Önceki
              </button>
              {[...Array(Math.min(totalPages, 5))].map((_, i) => {
                const pageNum = page < 3 ? i : page - 2 + i;
                if (pageNum >= totalPages) return null;
                return (
                  <button key={pageNum} className={pageNum === page ? 'active' : ''}
                    onClick={() => fetchBooks(pageNum, keyword)}>
                    {pageNum + 1}
                  </button>
                );
              })}
              <button disabled={page >= totalPages - 1} onClick={() => fetchBooks(page + 1, keyword)}>
                Sonraki →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
