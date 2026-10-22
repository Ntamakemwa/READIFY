import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import type { Book } from '../types';
import './BookDetail.css';

const ICONS = ['📘', '📕', '📗', '📙', '📒'];
const bookIcon = (title: string) => ICONS[title.charCodeAt(0) % ICONS.length];
const initials = (name: string) =>
    name.split(' ').filter(Boolean).map((w) => w[0]).slice(0, 2).join('').toUpperCase();

function getRole(): string {
    try {
        return JSON.parse(localStorage.getItem('user') || '{}').role ?? '';
    } catch {
        return '';
    }
}

export default function BookDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [book, setBook] = useState<Book | null>(null);
    const [related, setRelated] = useState<Book[]>([]);
    const [status, setStatus] = useState<'loading' | 'ok' | 'error'>('loading');
    const [notice, setNotice] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
    const [busy, setBusy] = useState(false);

    const loggedIn = !!localStorage.getItem('token');
    const role = getRole();
    const isAdmin = role === 'ROLE_ADMIN' || role === 'ROLE_SUPER_ADMIN';

    useEffect(() => {
        setStatus('loading');
        setNotice(null);
        api<Book>(`/books/${id}`)
            .then((b) => {
                setBook(b);
                setStatus('ok');
                return api<Book[] | { content: Book[] }>('/books');
            })
            .then((data) => {
                const all = Array.isArray(data) ? data : data.content;
                setRelated(all.filter((x) => x.category.id === book?.category.id && x.id !== Number(id)).slice(0, 3));
            })
            .catch(() => setStatus((s) => (s === 'loading' ? 'error' : s)));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    // recompute related once the book is known
    useEffect(() => {
        if (!book) return;
        api<Book[] | { content: Book[] }>('/books')
            .then((data) => {
                const all = Array.isArray(data) ? data : data.content;
                setRelated(all.filter((x) => x.category.id === book.category.id && x.id !== book.id).slice(0, 3));
            })
            .catch(() => {});
    }, [book]);

    async function borrow() {
        if (!book) return;
        setBusy(true);
        setNotice(null);
        try {
            await api('/loans', { method: 'POST', body: JSON.stringify({ bookId: book.id }) });
            setNotice({ type: 'success', text: 'Book borrowed! Find it under My Loans.' });
        } catch (err) {
            setNotice({ type: 'error', text: err instanceof Error && err.message ? err.message : 'Could not borrow this book.' });
        } finally {
            setBusy(false);
        }
    }

    async function remove() {
        if (!book || !window.confirm(`Delete "${book.title}"? This cannot be undone.`)) return;
        try {
            await api(`/books/${book.id}`, { method: 'DELETE' });
            navigate('/catalog');
        } catch (err) {
            setNotice({ type: 'error', text: err instanceof Error && err.message ? err.message : 'Could not delete this book.' });
        }
    }

    if (status === 'loading') {
        return <div className="container"><div className="detail-loading">Loading book…</div></div>;
    }
    if (status === 'error' || !book) {
        return (
            <div className="container">
                <div className="detail-loading">
                    <h3>Book not found</h3>
                    <Link to="/catalog" className="btn btn-primary">Back to catalog</Link>
                </div>
            </div>
        );
    }

    const known = book.availableCopies !== undefined;
    const available = !known || (book.availableCopies ?? 0) > 0;

    return (
        <main>
            <div className="container">
                <nav className="breadcrumbs" aria-label="Breadcrumb" style={{ paddingTop: 28 }}>
                    <Link to="/">Home</Link>
                    <span aria-hidden="true">›</span>
                    <Link to="/catalog">Catalog</Link>
                    <span aria-hidden="true">›</span>
                    <span aria-current="page">{book.title}</span>
                </nav>

                {isAdmin && (
                    <div className="admin-bar">
                        <p>🔒 Admin view — you can edit or delete this book.</p>
                        <div className="admin-bar-actions">
                            <Link to={`/admin?id=${book.id}`} className="btn btn-ghost btn-sm">✏ Edit</Link>
                            <button className="btn btn-danger btn-sm" onClick={remove}>🗑 Delete</button>
                        </div>
                    </div>
                )}

                <div className="detail-layout">
                    <aside className="detail-cover-col" aria-label="Book cover">
                        <div className="detail-cover" role="img" aria-label={`Cover of ${book.title}`}>
                            <span className="cover-big-icon" aria-hidden="true">{bookIcon(book.title)}</span>
                            <span className="cover-title">{book.title}</span>
                        </div>

                        <div className="detail-meta-card">
                            {[
                                ['Published', book.publishedYear],
                                ['Language', book.language],
                                ['Publisher', book.publisher],
                                ['ISBN', book.isbn],
                                ['Category', book.category.name],
                            ].map(([label, value]) => (
                                <div className="detail-meta-row" key={label}>
                                    <span className="label">{label}</span>
                                    <span className="value">{value}</span>
                                </div>
                            ))}
                        </div>
                    </aside>

                    <article className="detail-info-col">
                        <div className="detail-eyebrow">
                            <span className="badge badge-amber">{book.category.name}</span>
                            {known && (
                                <span className={`badge ${available ? 'badge-sage' : 'badge-rust'}`}>
                  {available ? 'Available' : 'On Loan'}
                </span>
                            )}
                        </div>

                        <h1 className="detail-title">{book.title}</h1>
                        <p className="detail-author">by <strong>{book.author.fullName}</strong></p>
                        <p className="detail-isbn">ISBN: {book.isbn}</p>

                        <div className="borrow-box">
                            <div className="borrow-box-status">
                                {known && (
                                    <span className={`badge ${available ? 'badge-sage' : 'badge-rust'}`}>
                    ● {available ? 'Available' : 'On Loan'}
                  </span>
                                )}
                                <h4>{available ? 'Ready to borrow' : 'Currently on loan'}</h4>
                            </div>

                            {notice && (
                                <div className={`detail-notice ${notice.type}`}>{notice.text}</div>
                            )}

                            <div className="borrow-box-actions">
                                {loggedIn ? (
                                    <button className="btn btn-primary" onClick={borrow} disabled={busy || !available}>
                                        {busy ? 'Borrowing…' : 'Borrow This Book'}
                                    </button>
                                ) : (
                                    <Link to="/login" className="btn btn-outline">Sign in to Borrow</Link>
                                )}
                            </div>
                        </div>

                        <hr className="detail-divider" />

                        <p className="detail-section-label">About this book</p>
                        <div className="detail-description">
                            <p>{book.description}</p>
                        </div>

                        <hr className="detail-divider" />

                        <p className="detail-section-label">About the Author</p>
                        <div className="author-card">
                            <div className="author-avatar" aria-hidden="true">{initials(book.author.fullName)}</div>
                            <div>
                                <h4>{book.author.fullName}</h4>
                                <p>{book.author.biography || 'No biography available yet.'}</p>
                            </div>
                        </div>
                    </article>
                </div>
            </div>

            {related.length > 0 && (
                <section className="related" aria-labelledby="related-heading">
                    <div className="container">
                        <div className="related-header">
                            <h2 id="related-heading">More in {book.category.name}</h2>
                            <Link to={`/catalog?category=${book.category.id}`}>View all →</Link>
                        </div>
                        <div className="books-grid">
                            {related.map((b) => (
                                <article className="book-card" key={b.id} onClick={() => navigate(`/books/${b.id}`)}>
                                    <div className="book-card-cover">
                                        <div className="book-card-cover-placeholder">
                                            <span className="cover-icon" aria-hidden="true">{bookIcon(b.title)}</span>
                                            <span>{b.title}</span>
                                        </div>
                                    </div>
                                    <div className="book-card-body">
                                        <span className="book-card-category">{b.category.name}</span>
                                        <h3 className="book-card-title">{b.title}</h3>
                                        <p className="book-card-author">{b.author.fullName}</p>
                                    </div>
                                    <div className="book-card-footer">
                                        <Link to={`/books/${b.id}`} className="btn btn-ghost btn-sm" onClick={(e) => e.stopPropagation()}>
                                            Details
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    </div>
                </section>
            )}
        </main>
    );
}