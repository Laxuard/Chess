import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { verifyMfa } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function MfaChallengeScreen() {
  const navigate = useNavigate();
  const { setUser, loadCurrentUser } = useAuth();

  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setCode(e.target.value.replace(/\D/g, '').slice(0, 6));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (code.length !== 6) {
      setError('Please enter a valid 6-digit code.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await verifyMfa(code);

      if (status === 200) {
        await loadCurrentUser();
        navigate('/dashboard');
      } else {
        setError(data?.message || 'Verification failed. Code may be invalid or expired.');
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
            <span className="badge badge--warn" style={{ marginBottom: '12px' }}>
              <span className="badge__dot" />Step-Up Verification
            </span>
            <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '6px' }}>
              Enter MFA Code
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              An active Multi-Factor configuration is enabled on this profile. Verify your token.
            </p>
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
          <form onSubmit={handleSubmit} className="stack stack-4">
            <div className="form-group">
              <label className="form-label" htmlFor="mfaCode">6-Digit Verification Token</label>
              <input
                id="mfaCode"
                name="mfaCode"
                type="text"
                className={`form-input ${error ? 'error' : ''}`}
                style={{ textAlign: 'center', fontSize: '1.6rem', letterSpacing: '0.4em', fontFamily: 'var(--font-mono)' }}
                placeholder="000000"
                value={code}
                onChange={handleChange}
                autoComplete="one-time-code"
                autoFocus
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full btn-lg"
              disabled={loading || code.length !== 6}
              style={{ marginTop: '8px' }}
            >
              {loading ? (
                <>
                  <span className="spinner" />
                  Verifying challenge...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                  Decrypt Gateway Session
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            Having issues?{' '}
            <button
              onClick={() => navigate('/login')}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
              className="link"
            >
              Return to Authentication Portal
            </button>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}
