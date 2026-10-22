import { Routes, Route, useLocation } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Catalog from './pages/Catalog';
import BookDetail from './pages/BookDetail';
import MyLoans from './pages/MyLoans';
import AdminBook from './pages/AdminBook';

export default function App() {
    const { pathname } = useLocation();
    const hideFooter = pathname === '/login' || pathname.startsWith('/admin');
    return (
        <>
            <Navbar />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/catalog" element={<Catalog />} />
                <Route path="/books/:id" element={<BookDetail />} />
                <Route element={<ProtectedRoute />}>
                    <Route path="/my-loans" element={<MyLoans />} />
                </Route>
                <Route element={<AdminRoute />}>
                    <Route path="/admin" element={<AdminBook />} />
                </Route>
                <Route path="/" element={<Home />} />
                <Route path="/index.html" element={<Home />} />
            </Routes>
            {!hideFooter && <Footer />}
        </>
    );
}