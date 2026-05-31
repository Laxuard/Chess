import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { register } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function RegisterScreen() {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.email || !form.password) {
      setError('Please fill in all registration fields.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await register(form.username, form.email, form.password);

      if (status === 201) {
        setSuccess(true);
        // Autologin successfully occurred on registration success!
        setUser({ authenticated: true });
        setTimeout(() => {
          navigate('/dashboard');
        }, 1500);
      } else {
        setError(data?.detail || data?.message || 'Registration failed. Please try again.');
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
              Create Account
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              Deploy your secure client profile identity
            </p>
          </div>

          {/* Success */}
          {success && (
            <div className="alert alert--success">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" style={{flexShrink:0,marginTop:'1px'}}>
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              Registration successful! Initializing session...
            </div>
          )}

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
          {!success && (
            <form onSubmit={handleSubmit} className="stack stack-4" noValidate>
              <div className="form-group">
                <label className="form-label" htmlFor="username">Username</label>
                <input
                  id="username"
                  name="username"
                  type="text"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="cyber_operator"
                  value={form.username}
                  onChange={handleChange}
                  autoComplete="username"
                  autoFocus
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">Email address</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="operator@nexus.net"
                  value={form.email}
                  onChange={handleChange}
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="••••••••••••"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
                style={{ marginTop: '8px' }}
              >
                {loading ? (
                  <>
                    <span className="spinner" />
                    Registering...
                  </>
                ) : (
                  <>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/>
                    </svg>
                    Register Operator
                  </>
                )}
              </button>
            </form>
          )}

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            Already registered?{' '}
            <Link to="/login" className="link">
              Sign in operator
            </Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}
