import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import type { Author, Book, Category } from '../types';
import './AdminBook.css';

const LANGUAGES = ['English', 'French', 'Spanish', 'German', 'Portuguese', 'Arabic', 'Kinyarwanda', 'Swahili', 'Other'];

const EMPTY = {
    title: '', isbn: '', publishedYear: '', language: 'English',
    publisher: '', authorId: '', categoryId: '', description: '',
};
type Form = typeof EMPTY;

const toList = <T,>(d: T[] | { content: T[] }) => (Array.isArray(d) ? d : d.content);

const fromBook = (b: Book): Form => ({
    title: b.title,
    isbn: b.isbn,
    publishedYear: b.publishedYear ? String(b.publishedYear) : '',
    language: b.language || '',
    publisher: b.publisher || '',
    authorId: String(b.author.id),
    categoryId: String(b.category.id),
    description: b.description || '',
});

export default function AdminBook() {
    const navigate = useNavigate();
    const [params] = useSearchParams();
    const bookId = params.get('id');
    const editing = !!bookId;

    const [form, setForm] = useState<Form>(EMPTY);
    const [original, setOriginal] = useState<Form>(EMPTY);
    const [books, setBooks] = useState<Book[]>([]);
    const [apiAuthors, setApiAuthors] = useState<Author[] | null>(null);
    const [apiCategories, setApiCategories] = useState<Category[] | null>(null);
    const [saving, setSaving] = useState(false);
    const [loadingBook, setLoadingBook] = useState(editing);
    const [error, setError] = useState('');
    const [savedId, setSavedId] = useState<number | null>(null);
    const [saved, setSaved] = useState(false);

    // dropdown data (falls back to what the books already contain if an endpoint is missing)
    useEffect(() => {
        api<Book[] | { content: Book[] }>('/books').then((d) => setBooks(toList(d))).catch(() => {});
        api<Author[] | { content: Author[] }>('/authors').then((d) => setApiAuthors(toList(d))).catch(() => {});
        api<Category[] | { content: Category[] }>('/categories').then((d) => setApiCategories(toList(d))).catch(() => {});
    }, []);

    const authors = useMemo(() => {
        if (apiAuthors) return apiAuthors;
        return [...new Map(books.map((b) => [b.author.id, b.author])).values()];
    }, [apiAuthors, books]);

    const categories = useMemo(() => {
        if (apiCategories) return apiCategories;
        return [...new Map(books.map((b) => [b.category.id, b.category])).values()];
    }, [apiCategories, books]);

    // edit mode: load the book
    useEffect(() => {
        setSaved(false);
        setError('');
        if (!bookId) {
            setForm(EMPTY);
            setOriginal(EMPTY);
            setLoadingBook(false);
            return;
        }
        setLoadingBook(true);
        api<Book>(`/books/${bookId}`)
            .then((b) => {
                const f = fromBook(b);
                setForm(f);
                setOriginal(f);
            })
            .catch(() => setError('Could not load this book.'))
            .finally(() => setLoadingBook(false));
    }, [bookId]);

    const set = (key: keyof Form) => (e: { target: { value: string } }) =>
        setForm({ ...form, [key]: e.target.value });

    async function submit(e: FormEvent) {
        e.preventDefault();
        setError('');
        setSaved(false);

        const year = form.publishedYear ? Number(form.publishedYear) : null;
        if (!form.title.trim()) return setError('Title is required.');
        if (!/^[\d-]{10,17}$/.test(form.isbn.trim())) return setError('Enter a valid ISBN (10–17 digits or dashes).');
        if (year !== null && (year < 1000 || year > 2099)) return setError('Published year must be between 1000 and 2099.');
        if (!form.authorId) return setError('Please select an author.');
        if (!form.categoryId) return setError('Please select a category.');

        const body = JSON.stringify({
            title: form.title.trim(),
            isbn: form.isbn.trim(),
            publishedYear: year,
            language: form.language,
            publisher: form.publisher.trim(),
            description: form.description,
            authorId: Number(form.authorId),
            categoryId: Number(form.categoryId),
        });

        setSaving(true);
        try {
            const res = await api<Book>(editing ? `/books/${bookId}` : '/books', {
                method: editing ? 'PUT' : 'POST',
                body,
            });
            setSavedId(res?.id ?? (bookId ? Number(bookId) : null));
            setSaved(true);
            if (!editing) {
                setForm(EMPTY);
                setOriginal(EMPTY);
            }
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (err) {
            setError(err instanceof Error && err.message ? err.message : 'Could not save the book. Please try again.');
        } finally {
            setSaving(false);
        }
    }

    async function remove() {
        if (!window.confirm('Are you sure you want to delete this book? This action cannot be undone.')) return;
        try {
            await api(`/books/${bookId}`, { method: 'DELETE' });
            navigate('/catalog');
        } catch (err) {
            setError(err instanceof Error && err.message ? err.message : 'Could not delete this book.');
        }
    }

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar" aria-label="Admin navigation">
                <p className="admin-sidebar-title">Admin Panel</p>
                <nav className="admin-nav">
                    <Link to="/admin" className="active"><span className="nav-icon">📚</span> Books</Link>
                    <a className="soon"><span className="nav-icon">✍</span> Authors</a>
                    <a className="soon"><span className="nav-icon">🏷</span> Categories</a>
                    <hr className="admin-nav-divider" />
                    <a className="soon"><span className="nav-icon">📋</span> All Loans</a>
                    <a className="soon"><span className="nav-icon">👤</span> Members</a>
                    <hr className="admin-nav-divider" />
                    <Link to="/catalog"><span className="nav-icon">↩</span> Back to Site</Link>
                </nav>
                <div className="admin-stats">
                    <div className="admin-stat">
                        <div className="num">{books.length}</div>
                        <div className="lbl">Total books</div>
                    </div>
                </div>
            </aside>

            <main className="admin-main">
                <div className="admin-topbar">
                    <h1>{editing ? 'Edit Book' : 'Add New Book'}</h1>
                    <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                        <Link to="/catalog" className="btn btn-ghost btn-sm">← Back to Catalog</Link>
                        {editing && <Link to={`/books/${bookId}`} className="btn btn-ghost btn-sm">View Book</Link>}
                    </div>
                </div>

                <div className="admin-body">
                    {saved && (
                        <div className="admin-status-bar">
                            ✅ Book saved successfully!{' '}
                            {savedId && <Link to={`/books/${savedId}`}>View it here →</Link>}
                        </div>
                    )}
                    {error && <div className="alert alert-error" role="alert" style={{ marginBottom: 24 }}>{error}</div>}

                    <div className="form-card">
                        <div className="form-card-header">
                            <div className="form-card-icon" aria-hidden="true">📖</div>
                            <div>
                                <h2>{editing ? 'Edit Book Information' : 'Book Information'}</h2>
                                <p className="form-card-sub">Fields marked <span className="required">*</span> are required.</p>
                            </div>
                        </div>

                        {loadingBook ? (
                            <div className="form-card-body">Loading book…</div>
                        ) : (
                            <form onSubmit={submit} noValidate>
                                <div className="form-card-body">
                                    <p className="form-section-title">Basic Information</p>

                                    <div className="form-group">
                                        <label className="form-label" htmlFor="book-title">Title <span className="required">*</span></label>
                                        <input className="form-control" id="book-title" maxLength={255}
                                               placeholder="Enter the full book title" value={form.title} onChange={set('title')} />
                                        <p className="form-hint">Use the official title as it appears on the cover.</p>
                                    </div>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-isbn">ISBN <span className="required">*</span></label>
                                            <input className="form-control" id="book-isbn" placeholder="978-0-000-00000-0"
                                                   value={form.isbn} onChange={set('isbn')} />
                                            <p className="form-hint">ISBN-10 or ISBN-13.</p>
                                        </div>
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-year">Published Year</label>
                                            <input className="form-control" id="book-year" type="number" min={1000} max={2099}
                                                   placeholder="e.g. 2020" value={form.publishedYear} onChange={set('publishedYear')} />
                                        </div>
                                    </div>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-language">Language</label>
                                            <select className="form-control" id="book-language" value={form.language} onChange={set('language')}>
                                                <option value="">— Select language —</option>
                                                {LANGUAGES.map((l) => <option key={l}>{l}</option>)}
                                            </select>
                                        </div>
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-publisher">Publisher</label>
                                            <input className="form-control" id="book-publisher" placeholder="e.g. Penguin Random House"
                                                   value={form.publisher} onChange={set('publisher')} />
                                        </div>
                                    </div>

                                    <p className="form-section-title">Classification</p>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-author">Author <span className="required">*</span></label>
                                            <select className="form-control" id="book-author" value={form.authorId} onChange={set('authorId')}>
                                                <option value="">— Select author —</option>
                                                {authors.map((a) => <option key={a.id} value={a.id}>{a.fullName}</option>)}
                                            </select>
                                        </div>
                                        <div className="form-group">
                                            <label className="form-label" htmlFor="book-category">Category <span className="required">*</span></label>
                                            <select className="form-control" id="book-category" value={form.categoryId} onChange={set('categoryId')}>
                                                <option value="">— Select category —</option>
                                                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                                            </select>
                                        </div>
                                    </div>

                                    <p className="form-section-title">Description</p>

                                    <div className="form-group">
                                        <label className="form-label" htmlFor="book-description">Book Description</label>
                                        <textarea className="form-control" id="book-description" rows={6} maxLength={2000}
                                                  placeholder="Write a brief description of the book's plot, themes, or significance…"
                                                  value={form.description} onChange={set('description')} />
                                        <p className="char-counter">{form.description.length} / 2000 characters</p>
                                    </div>
                                </div>

                                <div className="form-card-footer">
                                    <div style={{ display: 'flex', gap: 10 }}>
                                        <button type="submit" className="btn btn-primary" disabled={saving}>
                                            {saving ? 'Saving…' : editing ? 'Update Book' : 'Save Book'}
                                        </button>
                                        <button type="button" className="btn btn-ghost" onClick={() => setForm(original)}>
                                            Reset Form
                                        </button>
                                    </div>
                                    {editing && (
                                        <button type="button" className="btn btn-danger" onClick={remove}>🗑 Delete Book</button>
                                    )}
                                </div>
                            </form>
                        )}
                    </div>
                </div>
            </main>
        </div>
    );
}