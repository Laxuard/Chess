import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { loginWithCredentials, redirectToGoogleOAuth, redirectToFortyTwoOAuth } from '../services/api';
import { useAuth } from '../context/AuthContext';

const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844a4.14 4.14 0 0 1-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615Z" fill="#4285F4"/>
    <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18Z" fill="#34A853"/>
    <path d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332Z" fill="#FBBC05"/>
    <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58Z" fill="#EA4335"/>
  </svg>
);

const FortyTwoIcon = () => (
  <svg width="18" height="18" viewBox="0 0 100 100" fill="none">
    <rect width="100" height="100" rx="12" fill="#1a1a2e"/>
    <text x="50" y="70" textAnchor="middle" fontSize="58" fontWeight="900"
          fontFamily="Arial Black, sans-serif" fill="#00babc">42</text>
  </svg>
);

const EyeIcon = ({ open }) => open ? (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
) : (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

export default function LoginScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser, loadCurrentUser } = useAuth();

  const [form, setForm] = useState({ username: '', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  React.useEffect(() => {
    const params = new URLSearchParams(location.search);
    const errParam = params.get('error');
    if (errParam === 'email_taken') {
      setError('This email address is already bound to a username and password account. Please log in with your credentials.');
    } else if (errParam === 'auth_error') {
      setError('Authentication failed. Please try again.');
    }
  }, [location]);

  const handleChange = (e) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.password) {
      setError('Please enter your credentials.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await loginWithCredentials(form.username, form.password);

      if (status === 200 || status === 201) {
        if (data.status === 'AUTHENTICATED') {
          await loadCurrentUser();
          navigate('/dashboard');
        } else if (data.status === 'AWAITING_MFA') {
          navigate('/mfa-challenge');
        } else {
          setError('Unexpected response from server.');
        }
      } else if (status === 202) {
        if (data.status === 'AWAITING_MFA') {
          navigate('/mfa-challenge');
        }
      } else if (status === 401) {
        setError('Invalid username or password.');
      } else if (status === 403) {
        navigate('/mfa-challenge');
      } else {
        setError(data?.message || 'Login failed. Please try again.');
      }
    } catch (err) {
      setError('Cannot reach gateway. Check your connection.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card card">
        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div className="stack stack-6">
          {/* Header */}
          <div>
            <div style={{ marginBottom: '20px' }}>
              <Logo size="sm" />
            </div>
            <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '6px' }}>
              Welcome back
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              Sign in to your secure workspace
            </p>
          </div>

          {/* OAuth Buttons */}
          <div className="stack stack-3">
            <button className="btn btn-google" onClick={redirectToGoogleOAuth} type="button">
              <GoogleIcon />
              <span>Continue with Google</span>
              <span style={{ marginLeft: 'auto', opacity: 0.4, fontSize: '0.7rem', fontFamily: 'var(--font-mono)' }}>OAUTH2</span>
            </button>

            <button
              className="btn"
              onClick={redirectToFortyTwoOAuth}
              type="button"
              style={{
                background: 'rgba(0, 186, 188, 0.06)',
                border: '1px solid rgba(0, 186, 188, 0.35)',
                color: 'var(--text-primary)',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                width: '100%',
                padding: '11px 16px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '0.9rem',
                fontWeight: 500,
                transition: 'border-color 0.2s, background 0.2s',
              }}
              onMouseEnter={e => { e.currentTarget.style.borderColor = 'rgba(0,186,188,0.7)'; e.currentTarget.style.background = 'rgba(0,186,188,0.12)'; }}
              onMouseLeave={e => { e.currentTarget.style.borderColor = 'rgba(0,186,188,0.35)'; e.currentTarget.style.background = 'rgba(0,186,188,0.06)'; }}
            >
              <FortyTwoIcon />
              <span>Continue with 42</span>
              <span style={{ marginLeft: 'auto', opacity: 0.4, fontSize: '0.7rem', fontFamily: 'var(--font-mono)' }}>OAUTH2</span>
            </button>
          </div>

          {/* Divider */}
          <div className="divider">
            <span className="divider__line" />
            <span className="divider__text">or use credentials</span>
            <span className="divider__line" />
          </div>

          {/* Error */}
          {error && (
            <div className="alert alert--error">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{flexShrink:0,marginTop:'1px'}}>
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="stack stack-4" noValidate>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username</label>
              <input
                id="username"
                name="username"
                type="text"
                className={`form-input ${error ? 'error' : ''}`}
                placeholder="your_username"
                value={form.username}
                onChange={handleChange}
                autoComplete="username"
                autoFocus
              />
            </div>

            <div className="form-group">
              <div className="row-between" style={{ marginBottom: '6px' }}>
                <label className="form-label" htmlFor="password">Password</label>
                <button
                  type="button"
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.72rem', fontFamily: 'var(--font-mono)' }}
                  onClick={() => setShowPw(s => !s)}
                >
                  <EyeIcon open={showPw} />
                  {showPw ? 'HIDE' : 'SHOW'}
                </button>
              </div>
              <input
                id="password"
                name="password"
                type={showPw ? 'text' : 'password'}
                className={`form-input ${error ? 'error' : ''}`}
                placeholder="••••••••••••"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full btn-lg"
              disabled={loading}
              style={{ marginTop: '4px' }}
            >
              {loading ? (
                <>
                  <span className="spinner" />
                  Authenticating...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
                  </svg>
                  Sign In
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            No account?{' '}
            <Link to="/register" className="link">
              Create one free
            </Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}
