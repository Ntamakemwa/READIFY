import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import './Navbar.css';

function getUser(): { email?: string; role?: string } {
    try {
        return JSON.parse(localStorage.getItem('user') || '{}');
    } catch {
        return {};
    }
}

export default function Navbar() {
    useLocation(); // re-render on every page change
    const navigate = useNavigate();
    const [menuOpen, setMenuOpen] = useState(false);
    const [dropOpen, setDropOpen] = useState(false);

    const loggedIn = !!localStorage.getItem('token');
    const user = getUser();
    const isAdmin = user.role === 'ROLE_ADMIN';
    const isStaff = isAdmin || user.role === 'ROLE_LIBRARIAN';
    const roleLabel = isAdmin ? 'Administrator' : isStaff ? 'Librarian' : 'Member';

    useEffect(() => {
        const close = () => setDropOpen(false);
        document.addEventListener('click', close);
        return () => document.removeEventListener('click', close);
    }, []);

    function signOut() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
    }

    const active = ({ isActive }: { isActive: boolean }) => (isActive ? 'active' : '');

    return (
        <nav className="navbar" role="navigation">
            <div className="container">
                <Link to="/" className="navbar-brand">
                    <span className="logo-text">Readify</span>
                    <span className="logo-dot"></span>
                </Link>

                <ul className={`navbar-nav ${menuOpen ? 'open' : ''}`} onClick={() => setMenuOpen(false)}>
                    <li><NavLink to="/" end className={active}>Home</NavLink></li>
                    <li><NavLink to="/catalog" className={active}>Catalog</NavLink></li>
                    <li><NavLink to="/my-loans" className={active}>My Loans</NavLink></li>
                    {isStaff && <li><NavLink to="/admin" className={active}>Admin</NavLink></li>}
                </ul>

                <div className="navbar-actions">
                    {loggedIn ? (
                        <div
                            className="user-avatar"
                            style={{ display: 'flex' }}
                            onClick={(e) => {
                                e.stopPropagation();
                                setDropOpen(!dropOpen);
                            }}
                        >
                            <span>{(user.email || '?').charAt(0).toUpperCase()}</span>
                            <div className={`user-dropdown ${dropOpen ? 'open' : ''}`}>
                                <div className="dropdown-header">
                                    <div className="dropdown-header-label">Signed in as</div>
                                    <div className="dropdown-header-email">{user.email}</div>
                                    <div className="dropdown-header-role">{roleLabel}</div>
                                </div>
                                <div className="dropdown-links">
                                    <Link to="/my-loans" className="dropdown-link">📚 My Loans</Link>
                                    {isStaff && <Link to="/admin" className="dropdown-link">⚙️ Admin Panel</Link>}
                                </div>
                                <div className="dropdown-divider"></div>
                                <div className="dropdown-links">
                                    <a href="#" className="dropdown-link danger"
                                       onClick={(e) => { e.preventDefault(); signOut(); }}>
                                        Sign Out
                                    </a>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-outline btn-sm">Sign In</Link>
                            <Link to="/login?tab=register" className="btn btn-primary btn-sm">Create Account</Link>
                        </>
                    )}
                    <button className="nav-toggle" aria-label="Toggle menu" onClick={() => setMenuOpen(!menuOpen)}>
                        <span></span><span></span><span></span>
                    </button>
                </div>
            </div>
        </nav>
    );
}