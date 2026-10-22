import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import type { Book } from '../types';
import './Home.css';

const ICONS = ['📘', '📕', '📗', '📙', '📒'];
const bookIcon = (title: string) => ICONS[title.charCodeAt(0) % ICONS.length];

const CATEGORY_ICONS: [string, string][] = [
    ['non', '📰'], ['fiction', '📖'], ['fantasy', '🧙'], ['self', '🌱'], ['history', '🏛️'],
    ['science', '🔬'], ['memoir', '✍️'], ['philosoph', '💭'], ['tech', '💻'], ['child', '🧸'],
];
const categoryIcon = (name: string) =>
    CATEGORY_ICONS.find(([key]) => name.toLowerCase().includes(key))?.[1] ?? '📚';

export default function Home() {
    const navigate = useNavigate();
    const [params] = useSearchParams();
    const [books, setBooks] = useState<Book[]>([]);

    // Google sign-in: the backend redirects here with ?token=...&email=...&role=...
    useEffect(() => {
        const token = params.get('token');
        if (!token) return;
        localStorage.setItem('token', token);
        localStorage.setItem(
            'user',
            JSON.stringify({
                email: params.get('email'),
                role: params.get('role'),
                avatarUrl: params.get('avatarUrl'),
            })
        );
        navigate('/', { replace: true });
    }, [params, navigate]);

    useEffect(() => {
        api<Book[] | { content: Book[] }>('/books')
            .then((d) => setBooks(Array.isArray(d) ? d : d.content))
            .catch(() => {});
    }, []);

    const loggedIn = !!localStorage.getItem('token');
    const featured = books.slice(0, 4);

    const categories = useMemo(
        () => [...new Map(books.map((b) => [b.category.id, b.category])).values()],
        [books]
    );
    const authorCount = useMemo(() => new Set(books.map((b) => b.author.id)).size, [books]);

    return (
        <>
            {/* Hero */}
            <section className="hero">
                <div className="container">
                    <div className="hero-content">
                        <p className="hero-eyebrow">Your Community Library, Online</p>
                        <h1>Every great story<br />is waiting for you.</h1>
                        <p>Browse thousands of books, manage your loans, and rediscover the joy of reading — all from one place.</p>
                        <div className="hero-actions">
                            <Link to="/catalog" className="btn btn-primary btn-lg">Browse Catalog</Link>
                            {!loggedIn && <Link to="/login?tab=register" className="btn btn-outline btn-lg">Join Free</Link>}
                        </div>
                    </div>

                    {featured.length > 0 && (
                        <div className="hero-visual">
                            {featured.map((b) => (
                                <div className="hero-book-card" key={b.id}>
                                    <div className="hero-book-icon">{bookIcon(b.title)}</div>
                                    <div className="hero-book-title">{b.title}</div>
                                    <div className="hero-book-author">{b.author.fullName}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </section>

            {/* Stats */}
            {books.length > 0 && (
                <div className="stats-bar">
                    <div className="container">
                        <div className="stats-inner">
                            <div className="stat-item">
                                <div className="stat-number">{books.length}</div>
                                <div className="stat-label">Books Available</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-number">{categories.length}</div>
                                <div className="stat-label">Categories</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-number">{authorCount}</div>
                                <div className="stat-label">Authors</div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Featured */}
            {featured.length > 0 && (
                <section className="section">
                    <div className="container">
                        <div className="section-header">
                            <div>
                                <p className="section-eyebrow">Hand-picked for you</p>
                                <h2>Featured Books</h2>
                            </div>
                            <Link to="/catalog" className="btn btn-ghost btn-sm">View all →</Link>
                        </div>
                        <div className="books-grid">
                            {featured.map((b) => {
                                const known = b.availableCopies !== undefined;
                                const available = (b.availableCopies ?? 0) > 0;
                                return (
                                    <div className="book-card" key={b.id} onClick={() => navigate(`/books/${b.id}`)}>
                                        <div className="book-card-cover">
                                            <div className="book-card-cover-placeholder">
                                                <span className="cover-icon">{bookIcon(b.title)}</span>
                                                <span>{b.title}</span>
                                            </div>
                                            <span className="book-card-badge">{b.category.name}</span>
                                        </div>
                                        <div className="book-card-body">
                                            <span className="book-card-category">{b.category.name}</span>
                                            <div className="book-card-title">{b.title}</div>
                                            <div className="book-card-author">{b.author.fullName}</div>
                                            <div className="book-card-year">{b.publishedYear}</div>
                                        </div>
                                        <div className="book-card-footer">
                                            {known && (
                                                <span className={`badge ${available ? 'badge-sage' : 'badge-rust'}`}>
                          {available ? 'Available' : 'Unavailable'}
                        </span>
                                            )}
                                            <Link to={`/books/${b.id}`} className="btn btn-primary btn-sm"
                                                  onClick={(e) => e.stopPropagation()}>
                                                {known && !available ? 'Details' : 'Borrow'}
                                            </Link>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </section>
            )}

            {/* Categories */}
            {categories.length > 0 && (
                <section className="section section-sm categories-section">
                    <div className="container">
                        <div className="section-header">
                            <div>
                                <p className="section-eyebrow">Explore</p>
                                <h2>Browse by Category</h2>
                            </div>
                        </div>
                        <div className="categories-grid">
                            {categories.map((c) => (
                                <Link key={c.id} to={`/catalog?category=${c.id}`} className="category-pill">
                                    <span className="category-icon">{categoryIcon(c.name)}</span>
                                    <span className="category-name">{c.name}</span>
                                </Link>
                            ))}
                            <Link to="/catalog" className="category-pill">
                                <span className="category-icon">✨</span>
                                <span className="category-name">All Books</span>
                            </Link>
                        </div>
                    </div>
                </section>
            )}

            {/* CTA (hidden for members) */}
            {!loggedIn && (
                <section className="section">
                    <div className="container">
                        <div className="cta-banner">
                            <div className="cta-banner-text">
                                <h2>Ready to start reading?</h2>
                                <p>Create a free account and borrow your first book today. No fees, no fuss.</p>
                            </div>
                            <div className="cta-banner-action">
                                <Link to="/login?tab=register" className="btn btn-primary btn-lg">Get Started Free</Link>
                            </div>
                        </div>
                    </div>
                </section>
            )}
        </>
    );
}