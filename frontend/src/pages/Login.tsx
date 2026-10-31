import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import type { LoginResponse } from '../types';
import './Login.css';

type Tab = 'signin' | 'register';
type AccountType = 'READER' | 'SCHOOL_LIBRARY' | 'PRIVATE_LIBRARY';
type Notice = { type: 'error' | 'success'; message: string } | null;

const ACCOUNT_TYPES: { value: AccountType; icon: string; title: string; text: string }[] = [
  { value: 'READER', icon: '📖', title: 'Reader', text: 'Browse and borrow books.' },
  { value: 'SCHOOL_LIBRARY', icon: '🏫', title: 'School library', text: 'Lend books to students. No selling.' },
  { value: 'PRIVATE_LIBRARY', icon: '🏛️', title: 'Private library', text: 'Lend and sell books.' },
];

function errorMessage(err: unknown, fallback: string) {
  if (err instanceof TypeError) return 'Could not connect to the server. Please try again.';
  return err instanceof Error && err.message ? err.message : fallback;
}

function PasswordInput(props: {
  id: string;
  value: string;
  onChange: (v: string) => void;
  placeholder: string;
  autoComplete: string;
}) {
  const [show, setShow] = useState(false);
  return (
      <div className="input-password-wrapper">
        <input
            type={show ? 'text' : 'password'}
            id={props.id}
            className="form-control"
            placeholder={props.placeholder}
            autoComplete={props.autoComplete}
            value={props.value}
            onChange={(e) => props.onChange(e.target.value)}
        />
        <button type="button" className="password-toggle" onClick={() => setShow(!show)}>
          {show ? '🙈' : '👁'}
        </button>
      </div>
  );
}

function GoogleButton() {
  return (
      <a href="/oauth2/authorization/google" className="btn-google">
        <svg width="18" height="18" viewBox="0 0 18 18">
          <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z" fill="#4285F4" />
          <path d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" fill="#34A853" />
          <path d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.175 0 7.548 0 9s.348 2.825.957 4.039l3.007-2.332z" fill="#FBBC05" />
          <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.293C4.672 5.166 6.656 3.58 9 3.58z" fill="#EA4335" />
        </svg>
        Continue with Google
      </a>
  );
}

export default function Login() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [tab, setTab] = useState<Tab>(params.get('tab') === 'register' ? 'register' : 'signin');
  const [notice, setNotice] = useState<Notice>(null);
  const [loading, setLoading] = useState(false);
  const [signin, setSignin] = useState({ email: '', password: '' });
  const [reg, setReg] = useState({
    fullName: '',
    email: '',
    password: '',
    confirm: '',
    accountType: 'READER' as AccountType,
  });

  function switchTab(t: Tab) {
    setTab(t);
    setNotice(null);
  }

  async function handleSignIn(e: FormEvent) {
    e.preventDefault();
    const email = signin.email.trim();
    if (!email || !signin.password) {
      return setNotice({ type: 'error', message: 'Please fill in all fields.' });
    }
    setLoading(true);
    setNotice(null);
    try {
      const data = await api<LoginResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password: signin.password }),
      });
      const user = data.user ?? {
        email: data.email ?? email,
        role: data.role,
        accountType: data.accountType,
      };
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(user));
      navigate('/');
    } catch (err) {
      setNotice({ type: 'error', message: errorMessage(err, 'Invalid email or password.') });
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(e: FormEvent) {
    e.preventDefault();
    const fullName = reg.fullName.trim();
    const email = reg.email.trim();
    if (!fullName || !email || !reg.password || !reg.confirm) {
      return setNotice({ type: 'error', message: 'Please fill in all fields.' });
    }
    if (reg.password.length < 8) {
      return setNotice({ type: 'error', message: 'Password must be at least 8 characters.' });
    }
    if (reg.password !== reg.confirm) {
      return setNotice({ type: 'error', message: 'Passwords do not match.' });
    }
    setLoading(true);
    setNotice(null);
    try {
      await api('/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          fullName,
          email,
          password: reg.password,
          accountType: reg.accountType,
        }),
      });
      setNotice({
        type: 'success',
        message: 'Account created! Please check your email to verify your account, then sign in.',
      });
      setTimeout(() => switchTab('signin'), 3000);
    } catch (err) {
      setNotice({ type: 'error', message: errorMessage(err, 'Registration failed. Please try again.') });
    } finally {
      setLoading(false);
    }
  }

  const alertBox = notice && <div className={`auth-alert ${notice.type}`}>{notice.message}</div>;
  const spinner = loading && <span className="btn-spinner" />;

  return (
      <div className="auth-wrapper">
        <div className="auth-card">
          <div className="auth-header">
            <Link to="/" className="auth-logo">Readify</Link>
            <p className="auth-tagline">Your community library, online</p>
          </div>

          <div className="auth-tabs">
            <button type="button" className={`auth-tab ${tab === 'signin' ? 'active' : ''}`} onClick={() => switchTab('signin')}>
              Sign In
            </button>
            <button type="button" className={`auth-tab ${tab === 'register' ? 'active' : ''}`} onClick={() => switchTab('register')}>
              Create Account
            </button>
          </div>

          <div className="auth-body">
            {tab === 'signin' ? (
                <form className="auth-panel" onSubmit={handleSignIn}>
                  <h2>Welcome back</h2>
                  <p className="auth-subtitle">Sign in to access your loans and reading history.</p>
                  {alertBox}

                  <div className="form-group">
                    <label className="form-label" htmlFor="signinEmail">Email address</label>
                    <input type="email" id="signinEmail" className="form-control" placeholder="you@example.com"
                           autoComplete="email" value={signin.email}
                           onChange={(e) => setSignin({ ...signin, email: e.target.value })} />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="signinPassword">Password</label>
                    <PasswordInput id="signinPassword" placeholder="••••••••" autoComplete="current-password"
                                   value={signin.password} onChange={(v) => setSignin({ ...signin, password: v })} />
                  </div>

                  <button className="btn btn-primary btn-full" disabled={loading}>{spinner}Sign In</button>

                  <div className="auth-divider"><span>or continue with</span></div>
                  <GoogleButton />

                  <div className="auth-footer-link">
                    Don't have an account?{' '}
                    <a href="#" onClick={(e) => { e.preventDefault(); switchTab('register'); }}>Create one free</a>
                  </div>
                </form>
            ) : (
                <form className="auth-panel" onSubmit={handleRegister}>
                  <h2>Join Readify</h2>
                  <p className="auth-subtitle">Free forever. Choose the account that fits you.</p>
                  {alertBox}

                  <div className="form-group">
                    <span className="form-label">I am a…</span>
                    <div className="account-types">
                      {ACCOUNT_TYPES.map((t) => (
                          <label key={t.value}
                                 className={`account-type ${reg.accountType === t.value ? 'selected' : ''}`}>
                            <input type="radio" name="accountType" value={t.value}
                                   checked={reg.accountType === t.value}
                                   onChange={() => setReg({ ...reg, accountType: t.value })} />
                            <span className="account-type-icon">{t.icon}</span>
                            <span className="account-type-title">{t.title}</span>
                            <span className="account-type-text">{t.text}</span>
                          </label>
                      ))}
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="registerName">Full name</label>
                    <input type="text" id="registerName" className="form-control" placeholder="Jane Doe"
                           autoComplete="name" value={reg.fullName}
                           onChange={(e) => setReg({ ...reg, fullName: e.target.value })} />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="registerEmail">Email address</label>
                    <input type="email" id="registerEmail" className="form-control" placeholder="you@example.com"
                           autoComplete="email" value={reg.email}
                           onChange={(e) => setReg({ ...reg, email: e.target.value })} />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="registerPassword">Password</label>
                    <PasswordInput id="registerPassword" placeholder="At least 8 characters" autoComplete="new-password"
                                   value={reg.password} onChange={(v) => setReg({ ...reg, password: v })} />
                    <div className="form-hint">Must be at least 8 characters.</div>
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="registerPasswordConfirm">Confirm password</label>
                    <PasswordInput id="registerPasswordConfirm" placeholder="Repeat your password" autoComplete="new-password"
                                   value={reg.confirm} onChange={(v) => setReg({ ...reg, confirm: v })} />
                  </div>

                  <button className="btn btn-primary btn-full" disabled={loading}>{spinner}Create Account</button>

                  <div className="auth-divider"><span>or continue with</span></div>
                  <GoogleButton />

                  <div className="auth-footer-link">
                    Already have an account?{' '}
                    <a href="#" onClick={(e) => { e.preventDefault(); switchTab('signin'); }}>Sign in</a>
                  </div>
                </form>
            )}
          </div>
        </div>
      </div>
  );
}