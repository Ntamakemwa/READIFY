import { Navigate, Outlet } from 'react-router-dom';

const STAFF = ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_LIBRARIAN'];

export default function AdminRoute() {
    if (!localStorage.getItem('token')) return <Navigate to="/login" replace />;
    let role = '';
    try {
        role = JSON.parse(localStorage.getItem('user') || '{}').role ?? '';
    } catch {
        /* ignore */
    }
    return STAFF.includes(role) ? <Outlet /> : <Navigate to="/" replace />;
}