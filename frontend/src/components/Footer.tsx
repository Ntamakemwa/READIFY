import { Link } from 'react-router-dom';

export default function Footer() {
    return (
        <footer className="footer">
            <div className="container">
                <div className="footer-inner">
                    <div className="footer-brand">
                        <div className="logo-text">Readify</div>
                        <p>Your community library, brought online.</p>
                    </div>
                    <div className="footer-col">
                        <h5>Explore</h5>
                        <ul>
                            <li><Link to="/catalog">Catalog</Link></li>
                            <li><Link to="/">Home</Link></li>
                        </ul>
                    </div>
                    <div className="footer-col">
                        <h5>Account</h5>
                        <ul>
                            <li><Link to="/my-loans">My Loans</Link></li>
                        </ul>
                    </div>
                </div>
                <div className="footer-bottom"><p>© 2026 Readify. All rights reserved.</p></div>
            </div>
        </footer>
    );
}