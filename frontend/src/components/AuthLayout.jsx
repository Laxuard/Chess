import React from 'react';
import Logo from './Logo';

const BRAND_FEATURES = [
  { icon: '⬡', label: 'Zero-Trust Architecture', desc: 'Every request verified at the gateway layer.' },
  { icon: '◈', label: 'OAuth2 + MFA', desc: 'Multi-factor enforcement with TOTP support.' },
  { icon: '◉', label: 'Session Intercept', desc: 'Automatic 401/403 challenge handling.' },
];

export default function AuthLayout({ children }) {
  return (
    <div className="auth-layout">
      {/* ─── Brand Panel ─────────────────────────── */}
      <div className="auth-layout__brand">
        <div className="grid-bg" />
        <div className="glow-orb glow-orb--teal" style={{ top: '20%', left: '-10%' }} />
        <div className="glow-orb glow-orb--blue" style={{ bottom: '15%', right: '10%' }} />
        <div className="scan-line" />

        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div style={{ position: 'relative', zIndex: 1, maxWidth: '480px' }}>
          <Logo size="lg" />

          <div style={{ marginTop: '48px', marginBottom: '40px' }}>
            <h1 className="text-display" style={{ fontSize: 'clamp(2rem, 3.5vw, 2.8rem)', color: 'var(--text-primary)', marginBottom: '16px' }}>
              Secure by<br />
              <span style={{ color: 'var(--teal-glow)' }}>design.</span>
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', lineHeight: 1.7, maxWidth: '380px' }}>
              Enterprise-grade authentication backed by a Spring microservices BFF gateway.
            </p>
          </div>

          <div className="stack stack-4">
            {BRAND_FEATURES.map((f, i) => (
              <div key={i} style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '16px',
                padding: '16px',
                background: 'rgba(13,17,23,0.6)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '8px',
                backdropFilter: 'blur(8px)',
              }}>
                <span style={{
                  fontSize: '1.1rem',
                  color: 'var(--teal-glow)',
                  lineHeight: 1,
                  marginTop: '2px',
                  flexShrink: 0,
                  fontFamily: 'var(--font-mono)',
                }}>{f.icon}</span>
                <div>
                  <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.9rem', marginBottom: '2px' }}>{f.label}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>

          {/* Version tag */}
          <div style={{ marginTop: '40px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="text-label">BFF Gateway</span>
            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: 'var(--text-muted)' }} />
            <span className="text-label" style={{ color: 'var(--teal-deep)' }}>v2.4.1</span>
            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: 'var(--text-muted)' }} />
            <span className="badge badge--online"><span className="badge__dot" />ONLINE</span>
          </div>
        </div>
      </div>

      {/* ─── Form Panel ──────────────────────────── */}
      <div className="auth-layout__form">
        {children}
      </div>
    </div>
  );
}
