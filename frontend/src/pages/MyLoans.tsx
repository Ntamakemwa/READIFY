import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api';
import type { Loan } from '../types';
import './MyLoans.css';

const ICONS = ['📘', '📕', '📗', '📙', '📒'];
const MS_PER_DAY = 1000 * 60 * 60 * 24;

const formatDate = (d?: string) =>
    d ? new Date(d).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : '—';

const daysUntil = (d?: string) => (d ? Math.ceil((new Date(d).getTime() - Date.now()) / MS_PER_DAY) : null);

function LoanCard(props: { loan: Loan; history: boolean; renewing: boolean; onRenew: (id: number) => void }) {
    const { loan, history, renewing, onRenew } = props;
    const title = loan.book?.title ?? 'Unknown Title';
    const author = loan.book?.author?.fullName ?? 'Unknown Author';
    const category = loan.book?.category?.name ?? '';
    const icon = ICONS[title.charCodeAt(0) % ICONS.length];

    const days = daysUntil(loan.dueDate);
    const overdue = !history && days !== null && days < 0;
    const dueSoon = !history && days !== null && days >= 0 && days <= 3;

    let badge;
    if (history) badge = <span className="badge badge-neutral">Returned</span>;
    else if (overdue) badge = <span className="badge badge-rust">Overdue</span>;
    else if (dueSoon)
        badge = <span className="due-soon-pill">⏰ Due in {days} day{days === 1 ? '' : 's'}</span>;
    else badge = <span className="badge badge-sage">Active</span>;

    return (
        <article className={`loan-card ${overdue ? 'overdue' : ''} ${history ? 'returned' : ''}`}>
            <div className="loan-cover">{icon}</div>
            <div className="loan-body">
                <span className="book-card-category">{category.toUpperCase()}</span>
                <h3>{title}</h3>
                <p className="author">{author}</p>
                <div className="loan-dates">
                    <div className="loan-date">
                        <span className="date-label">Borrowed</span>
                        <span className="date-value">{formatDate(loan.borrowedDate ?? loan.loanDate)}</span>
                    </div>
                    {history ? (
                        <div className="loan-date">
                            <span className="date-label">Returned</span>
                            <span className="date-value">{formatDate(loan.returnDate ?? loan.returnedDate)}</span>
                        </div>
                    ) : (
                        <div className={`loan-date ${overdue ? 'overdue-date' : ''}`}>
                            <span className="date-label">Due date</span>
                            <span className="date-value">
                {overdue && '⚠ '}
                                {formatDate(loan.dueDate)}
                                {overdue && ' (Overdue)'}
              </span>
                        </div>
                    )}
                </div>
            </div>
            <div className="loan-actions">
                {badge}
                {history ? (
                    loan.book && <Link to={`/books/${loan.book.id}`} className="btn btn-ghost btn-sm">Borrow Again</Link>
                ) : (
                    <>
                        <button
                            className={`btn ${overdue ? 'btn-ghost' : 'btn-primary'} btn-sm`}
                            disabled={overdue || renewing}
                            title={overdue ? 'Cannot renew overdue books' : undefined}
                            onClick={() => onRenew(loan.id)}
                        >
                            {renewing ? 'Renewing…' : 'Renew'}
                        </button>
                        {loan.book && <Link to={`/books/${loan.book.id}`} className="btn btn-ghost btn-sm">View Book</Link>}
                    </>
                )}
            </div>
        </article>
    );
}

export default function MyLoans() {
    const navigate = useNavigate();
    const [loans, setLoans] = useState<Loan[]>([]);
    const [status, setStatus] = useState<'loading' | 'ok' | 'error'>('loading');
    const [tab, setTab] = useState<'active' | 'history'>('active');
    const [renewingId, setRenewingId] = useState<number | null>(null);
    const [error, setError] = useState('');

    const handleAuth = useCallback(
        (err: unknown) => {
            if (err instanceof ApiError && err.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                navigate('/login');
                return true;
            }
            return false;
        },
        [navigate]
    );

    const load = useCallback(() => {
        api<Loan[]>('/loans/my-loans')
            .then((data) => {
                setLoans(data);
                setStatus('ok');
            })
            .catch((err) => {
                if (!handleAuth(err)) setStatus('error');
            });
    }, [handleAuth]);

    useEffect(load, [load]);

    async function renew(id: number) {
        setRenewingId(id);
        setError('');
        try {
            await api(`/loans/${id}/renew`, { method: 'POST' });
            load();
        } catch (err) {
            if (!handleAuth(err)) {
                setError(err instanceof Error && err.message ? err.message : 'Could not renew this loan.');
            }
        } finally {
            setRenewingId(null);
        }
    }

    const { active, history, overdueCount, dueSoonCount } = useMemo(() => {
        const active = loans.filter((l) => l.status === 'ACTIVE' || l.status === 'OVERDUE');
        const history = loans.filter((l) => l.status === 'RETURNED');
        const days = active.map((l) => daysUntil(l.dueDate));
        return {
            active,
            history,
            overdueCount: days.filter((d) => d !== null && d < 0).length,
            dueSoonCount: days.filter((d) => d !== null && d >= 0 && d <= 7).length,
        };
    }, [loans]);

    const plural = (n: number) => (n ? `${n} book${n > 1 ? 's' : ''}` : 'None');
    const list = tab === 'active' ? active : history;

    return (
        <>
            <header className="page-header">
                <div className="container">
                    <h1>My Loans</h1>
                    <p>Track your borrowed books, due dates, and loan history.</p>
                </div>
            </header>

            <main>
                <div className="container">
                    <div className="loans-layout">
                        <section>
                            <div className="loan-tabs">
                                <button className={`loan-tab ${tab === 'active' ? 'active' : ''}`} onClick={() => setTab('active')}>
                                    Active <span className="tab-count">{active.length}</span>
                                </button>
                                <button className={`loan-tab ${tab === 'history' ? 'active' : ''}`} onClick={() => setTab('history')}>
                                    History <span className="tab-count">{history.length}</span>
                                </button>
                            </div>

                            {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

                            {status === 'loading' && (
                                <div className="loans-loading"><div className="spinner"></div>Loading your loans…</div>
                            )}
                            {status === 'error' && <div className="alert alert-error">Could not load loans. Please try again.</div>}

                            {status === 'ok' && list.length === 0 && (
                                <div className="empty-state">
                                    <div className="empty-icon">{tab === 'active' ? '📭' : '📋'}</div>
                                    <h3>{tab === 'active' ? 'No active loans' : 'No history yet'}</h3>
                                    <p>{tab === 'active' ? "You haven't borrowed any books yet." : 'Books you return will appear here.'}</p>
                                </div>
                            )}

                            {status === 'ok' &&
                                list.map((l) => (
                                    <LoanCard key={l.id} loan={l} history={tab === 'history'}
                                              renewing={renewingId === l.id} onRenew={renew} />
                                ))}
                        </section>

                        <aside>
                            <div className="loan-summary-card">
                                <div className="loan-summary-header"><h3>Your Reading Summary</h3></div>
                                <div className="loan-summary-body">
                                    <div className="summary-stat">
                                        <span className="s-label">Active loans</span>
                                        <span className="s-value">{status === 'ok' ? `${active.length} / 5` : '—'}</span>
                                    </div>
                                    <div className="summary-stat">
                                        <span className="s-label">Overdue</span>
                                        <span className="s-value overdue">{status === 'ok' ? plural(overdueCount) : '—'}</span>
                                    </div>
                                    <div className="summary-stat">
                                        <span className="s-label">Due this week</span>
                                        <span className="s-value">{status === 'ok' ? plural(dueSoonCount) : '—'}</span>
                                    </div>
                                    <div className="summary-stat">
                                        <span className="s-label">Total borrowed</span>
                                        <span className="s-value">{status === 'ok' ? `${loans.length} books` : '—'}</span>
                                    </div>
                                    <div className="renewal-note">
                                        📌 You can renew a book up to <strong>2 times</strong>. Renewals are not allowed on overdue books.
                                    </div>
                                    <div style={{ marginTop: 20 }}>
                                        <Link to="/catalog" className="btn btn-primary btn-full">Browse More Books</Link>
                                    </div>
                                </div>
                            </div>
                        </aside>
                    </div>
                </div>
            </main>
        </>
    );
}