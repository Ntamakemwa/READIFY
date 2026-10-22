import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import type { Book } from '../types';
import './Catalog.css';

const PAGE_SIZE = 12;
const ICONS = ['📘', '📕', '📗', '📙', '📒'];
const bookIcon = (title: string) => ICONS[title.charCodeAt(0) % ICONS.length];

const SORTS: Record<string, (a: Book, b: Book) => number> = {
  'title-asc': (a, b) => a.title.localeCompare(b.title),
  'title-desc': (a, b) => b.title.localeCompare(a.title),
  'year-desc': (a, b) => b.publishedYear - a.publishedYear,
  'year-asc': (a, b) => a.publishedYear - b.publishedYear,
};

function AvailabilityBadge({ book }: { book: Book }) {
  if (book.availableCopies === undefined) return null;
  const ok = book.availableCopies > 0;
  return <span className={`badge ${ok ? 'badge-sage' : 'badge-rust'}`}>{ok ? 'Available' : 'On Loan'}</span>;
}

export default function Catalog() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [books, setBooks] = useState<Book[]>([]);
  const [status, setStatus] = useState<'loading' | 'ok' | 'error'>('loading');
  const [query, setQuery] = useState('');
  const [categoryId, setCategoryId] = useState(params.get('category') ?? '');
  const [availableOnly, setAvailableOnly] = useState(false);
  const [sort, setSort] = useState('title-asc');
  const [view, setView] = useState<'grid' | 'list'>('grid');
  const [page, setPage] = useState(0);

  useEffect(() => {
    api<Book[] | { content: Book[] }>('/books')
        .then((data) => {
          setBooks(Array.isArray(data) ? data : data.content);
          setStatus('ok');
        })
        .catch(() => setStatus('error'));
  }, []);

  const categories = useMemo(() => {
    const map = new Map<number, { id: number; name: string; count: number }>();
    books.forEach((b) => {
      const c = map.get(b.category.id) ?? { id: b.category.id, name: b.category.name, count: 0 };
      c.count++;
      map.set(c.id, c);
    });
    return [...map.values()];
  }, [books]);

  const hasAvailability = books.some((b) => b.availableCopies !== undefined);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return books
        .filter(
            (b) =>
                (!categoryId || String(b.category.id) === categoryId) &&
                (!availableOnly || (b.availableCopies ?? 0) > 0) &&
                (!q || b.title.toLowerCase().includes(q) || b.author.fullName.toLowerCase().includes(q))
        )
        .sort(SORTS[sort]);
  }, [books, query, categoryId, availableOnly, sort]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const shown = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  function clearFilters() {
    setCategoryId('');
    setAvailableOnly(false);
    setQuery('');
    setPage(0);
  }

  function goToPage(p: number) {
    setPage(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  return (
      <>
        <header className="page-header">
          <div className="container">
            <h1>Book Catalog</h1>
            <p>Browse and discover our full collection of books.</p>
          </div>
        </header>

        <main>
          <div className="container">
            <div className="catalog-layout">
              <aside className="sidebar">
                <div className="sidebar-card">
                  <h3>Categories</h3>
                  <nav className="sidebar-list">
                    <a className={categoryId === '' ? 'active' : ''} onClick={() => { setCategoryId(''); setPage(0); }}>
                      All <span>{books.length}</span>
                    </a>
                    {categories.map((c) => (
                        <a key={c.id} className={categoryId === String(c.id) ? 'active' : ''}
                           onClick={() => { setCategoryId(String(c.id)); setPage(0); }}>
                          {c.name} <span>{c.count}</span>
                        </a>
                    ))}
                  </nav>
                </div>

                {hasAvailability && (
                    <div className="sidebar-card">
                      <h3>Availability</h3>
                      <div className="sidebar-list">
                        <label className="avail-option">
                          <input type="radio" checked={!availableOnly}
                                 onChange={() => { setAvailableOnly(false); setPage(0); }} /> All
                        </label>
                        <label className="avail-option">
                          <input type="radio" checked={availableOnly}
                                 onChange={() => { setAvailableOnly(true); setPage(0); }} /> Available now
                        </label>
                      </div>
                    </div>
                )}

                <button className="btn btn-ghost btn-full" onClick={clearFilters}>Clear all filters</button>
              </aside>

              <section>
                <div className="catalog-toolbar">
                  <div className="search-bar-wrapper">
                    <svg className="search-bar-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
                    </svg>
                    <input type="search" className="search-bar" placeholder="Search by title, author…"
                           value={query} onChange={(e) => { setQuery(e.target.value); setPage(0); }} />
                  </div>
                  <select className="sort-select" value={sort} onChange={(e) => { setSort(e.target.value); setPage(0); }}>
                    <option value="title-asc">Title A–Z</option>
                    <option value="title-desc">Title Z–A</option>
                    <option value="year-desc">Newest first</option>
                    <option value="year-asc">Oldest first</option>
                  </select>
                  <div className="view-toggle">
                    <button className={`view-btn ${view === 'grid' ? 'active' : ''}`} onClick={() => setView('grid')} title="Grid view">⊞</button>
                    <button className={`view-btn ${view === 'list' ? 'active' : ''}`} onClick={() => setView('list')} title="List view">☰</button>
                  </div>
                </div>

                {status === 'loading' && (
                    <div className="catalog-loading"><div className="spinner"></div>Loading books…</div>
                )}
                {status === 'error' && (
                    <div className="alert alert-error">Could not load books. Please try again.</div>
                )}

                {status === 'ok' && (
                    <>
                      <p className="results-count">
                        {filtered.length === 0
                            ? 'No books found'
                            : `Showing ${page * PAGE_SIZE + 1}–${Math.min((page + 1) * PAGE_SIZE, filtered.length)} of ${filtered.length} books`}
                      </p>

                      {filtered.length === 0 && (
                          <div className="empty-state">
                            <div className="empty-icon">📭</div>
                            <h3>No books found</h3>
                            <p>Try adjusting your search or filters.</p>
                          </div>
                      )}

                      {view === 'grid' ? (
                          <div className="books-grid">
                            {shown.map((b) => (
                                <article className="book-card" key={b.id} onClick={() => navigate(`/books/${b.id}`)}>
                                  <div className="book-card-cover">
                                    <div className="book-card-cover-placeholder">
                                      <span className="cover-icon">{bookIcon(b.title)}</span>
                                      <span>{b.title}</span>
                                    </div>
                                  </div>
                                  <div className="book-card-body">
                                    <span className="book-card-category">{b.category.name}</span>
                                    <div className="book-card-title">{b.title}</div>
                                    <div className="book-card-author">{b.author.fullName}</div>
                                    <div className="book-card-year">{b.publishedYear}</div>
                                  </div>
                                  <div className="book-card-footer">
                                    <AvailabilityBadge book={b} />
                                    <Link to={`/books/${b.id}`} className="btn btn-primary btn-sm" onClick={(e) => e.stopPropagation()}>
                                      Details
                                    </Link>
                                  </div>
                                </article>
                            ))}
                          </div>
                      ) : (
                          <div className="books-list">
                            {shown.map((b) => (
                                <article className="book-list-item" key={b.id}>
                                  <div className="book-list-cover">{bookIcon(b.title)}</div>
                                  <div className="book-list-body">
                                    <span className="book-card-category">{b.category.name}</span>
                                    <h3>{b.title}</h3>
                                    <p className="meta">{b.author.fullName} · {b.publishedYear}</p>
                                    <p className="desc">{b.description}</p>
                                  </div>
                                  <div className="book-list-actions">
                                    <AvailabilityBadge book={b} />
                                    <Link to={`/books/${b.id}`} className="btn btn-primary btn-sm">Details</Link>
                                  </div>
                                </article>
                            ))}
                          </div>
                      )}

                      {totalPages > 1 && (
                          <nav className="pagination" style={{ display: 'flex' }}>
                            <button className="pagination-btn" disabled={page === 0} onClick={() => goToPage(page - 1)}>‹</button>
                            {Array.from({ length: totalPages }, (_, i) =>
                                i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 1 ? (
                                    <button key={i} className={`pagination-btn ${i === page ? 'active' : ''}`} onClick={() => goToPage(i)}>
                                      {i + 1}
                                    </button>
                                ) : Math.abs(i - page) === 2 ? (
                                    <span key={i} className="pagination-btn" style={{ border: 'none', cursor: 'default' }}>…</span>
                                ) : null
                            )}
                            <button className="pagination-btn" disabled={page === totalPages - 1} onClick={() => goToPage(page + 1)}>›</button>
                          </nav>
                      )}
                    </>
                )}
              </section>
            </div>
          </div>
        </main>
      </>
  );
}