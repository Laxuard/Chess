import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import { useAuth } from '../context/AuthContext';
import { getUsers, getMfaSetup, confirmMfaSetup, unlinkOAuth2Provider, redirectToGoogleOAuth, redirectToFortyTwoOAuth, setLocalPassword } from '../services/api';
import { QRCodeSVG } from 'qrcode.react';

export default function DashboardScreen() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [usersResponse, setUsersResponse] = useState('');
  const [profile, setProfile] = useState(null);
  
  // 2FA Setup state
  const [mfaSecret, setMfaSecret] = useState('');
  const [mfaQrUrl, setMfaQrUrl] = useState('');
  const [confirmCode, setConfirmCode] = useState('');
  const [mfaSuccess, setMfaSuccess] = useState(false);
  const [mfaError, setMfaError] = useState('');

  // Link status notifications
  const [linkMessage, setLinkMessage] = useState('');
  const [linkError, setLinkError] = useState('');

  // Password-binding state
  const [newPassword, setNewPassword] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');

  // Validate session on dashboard load and populate profile context
  useEffect(() => {
    async function verifyAccess() {
      const { status, data } = await getUsers();
      if (status === 401) {
        logout();
      } else if (status === 403) {
        navigate('/mfa-challenge');
      } else if (status === 200) {
        setProfile(data);
      }
    }
    verifyAccess();
  }, [logout, navigate]);

  // Handle OAuth linking query feedback on redirection back to dashboard
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('link') === 'success') {
      setLinkMessage('Social identity provider linked successfully!');
      window.history.replaceState({}, document.title, window.location.pathname);
    } else if (params.get('link') === 'error') {
      setLinkError('Failed to link social identity. Provider might already be linked to another profile.');
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  const isLinked = (provider) => {
    if (!profile || !profile.identities) return false;
    return profile.identities.some(id => id.provider === provider);
  };

  const getProviderId = (provider) => {
    if (!profile || !profile.identities) return '';
    const found = profile.identities.find(id => id.provider === provider);
    return found ? found.providerId : '';
  };

  const handleUnlink = async (provider) => {
    if (!window.confirm(`Are you sure you want to unlink your ${provider} account?`)) {
      return;
    }
    setLinkError('');
    setLinkMessage('');
    try {
      const { status, data } = await unlinkOAuth2Provider(provider);
      if (status === 200) {
        setLinkMessage(`${provider} disconnected successfully.`);
        const response = await getUsers();
        if (response.status === 200) {
          setProfile(response.data);
        }
      } else {
        setLinkError(data.message || `Failed to unlink ${provider}.`);
      }
    } catch (err) {
      setLinkError('Error contacting Gateway service.');
    }
  };

  const handleSetPassword = async (e) => {
    e.preventDefault();
    if (!newPassword) {
      setPasswordError('Please enter a password.');
      return;
    }
    if (profile && profile.hasPassword && !currentPassword) {
      setPasswordError('Please enter your current password to verify identity.');
      return;
    }
    setPasswordError('');
    setPasswordSuccess('');
    setLoading(true);

    try {
      const { status, data } = await setLocalPassword(newPassword, currentPassword);
      if (status === 200) {
        setPasswordSuccess('Local password updated successfully!');
        setNewPassword('');
        setCurrentPassword('');
        const response = await getUsers();
        if (response.status === 200) {
          setProfile(response.data);
        }
      } else {
        setPasswordError(data.message || 'Failed to update local password.');
      }
    } catch (err) {
      setPasswordError('Error connecting to auth service.');
    } finally {
      setLoading(false);
    }
  };

  const handleTestApi = async () => {
    setLoading(true);
    setUsersResponse('');
    try {
      const { status, data } = await getUsers();
      if (status === 200) {
        if (typeof data === 'string') {
          setUsersResponse(data);
        } else {
          setUsersResponse(JSON.stringify(data, null, 2));
        }
      } else {
        setUsersResponse(`[Error] Gateway rejected request. Status: ${status}`);
      }
    } catch (err) {
      setUsersResponse('[Error] Cannot contact BFF Gateway proxy.');
    } finally {
      setLoading(false);
    }
  };

  const handleInitiateMfa = async () => {
    setMfaError('');
    setMfaSuccess(false);
    try {
      const { status, data } = await getMfaSetup();
      if (status === 200) {
        setMfaSecret(data.setupDetails.secretKey);
        setMfaQrUrl(data.setupDetails.qrCodeUrl);
      } else {
        setMfaError('Failed to initiate Multi-Factor setup.');
      }
    } catch (err) {
      setMfaError('Error communicating with Auth-Service.');
    }
  };

  const handleConfirmMfa = async (e) => {
    e.preventDefault();
    if (confirmCode.length !== 6) {
      setMfaError('Enter a valid 6-digit confirmation code.');
      return;
    }

    setMfaError('');
    try {
      const { status, data } = await confirmMfaSetup(confirmCode);
      if (status === 200) {
        setMfaSuccess(true);
        setMfaSecret('');
        setMfaQrUrl('');
        setConfirmCode('');
      } else {
        setMfaError(data.message || 'MFA validation failed. Check your secret.');
      }
    } catch (err) {
      setMfaError('Network error confirming setup.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-void)', color: 'var(--text-primary)', display: 'flex', flexDirection: 'column' }}>
      
      {/* ─── Global Navbar ──────────────────────── */}
      <header style={{ background: 'var(--bg-base)', borderBottom: '1px solid var(--border-subtle)', padding: '16px 32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'relative' }}>
        <Logo size="sm" />
        <div style={{ display: 'flex', gap: '16px' }}>
          <button className="btn btn-ghost btn-sm" onClick={logout}>
            Sign Out
          </button>
        </div>
      </header>

      {/* ─── Main Content ────────────────────────── */}
      <main style={{ flex: 1, padding: '40px 32px', maxWidth: '1200px', width: '100%', margin: '0 auto', display: 'grid', gridTemplateColumns: '240px 1fr', gap: '40px' }}>
        
        {/* Sidebar Nav */}
        <aside className="stack stack-3">
          <button 
            className={`btn btn-full ${activeTab === 'profile' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('profile')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            ◉ Operator Profile
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'mfa' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('mfa')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            🔒 Multi-Factor Setup
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'linking' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('linking')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            🔗 Linked Accounts
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'api' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('api')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            ⚡ Test Transit JWTs
          </button>
        </aside>

        {/* Tab content panel */}
        <section className="card" style={{ padding: '32px', minHeight: '400px' }}>
            {/* Profile Tab */}
          {activeTab === 'profile' && (
            <div className="stack stack-6">
              <div>
                <span className="badge badge--online" style={{ marginBottom: '8px' }}>
                  <span className="badge__dot" />Edge Active
                </span>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Identity Topology Map
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Real-time database visualization of your security context, credential roots, and active MFA gates.
                </p>
              </div>

              {!profile ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                  <span className="text-mono">Synchronizing Identity Graph...</span>
                </div>
              ) : (
                <div className="stack stack-6">
                  {/* Visual Topology Diagram */}
                  <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '24px',
                    background: 'rgba(255,255,255,0.01)',
                    padding: '32px 16px',
                    borderRadius: '12px',
                    border: '1px solid var(--border-subtle)',
                    position: 'relative',
                    overflow: 'hidden'
                  }}>
                    {/* Glowing background decor */}
                    <div style={{
                      position: 'absolute',
                      top: '50%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      width: '300px',
                      height: '300px',
                      background: 'radial-gradient(circle, rgba(0,240,200,0.03) 0%, transparent 70%)',
                      pointerEvents: 'none',
                      zIndex: 0
                    }} />

                    {/* Layer 1: Identity Providers (Linked Sources) */}
                    <div style={{ display: 'flex', gap: '20px', zIndex: 1, width: '100%', justifyContent: 'center' }}>
                      {profile.identities && profile.identities.length > 0 ? (
                        profile.identities.map((ident, idx) => (
                          <div key={idx} style={{
                            background: 'var(--bg-overlay)',
                            border: '1px solid var(--teal-glow)',
                            borderRadius: '8px',
                            padding: '16px 20px',
                            textAlign: 'center',
                            minWidth: '180px',
                            boxShadow: '0 0 15px rgba(0, 240, 200, 0.05)'
                          }}>
                            <span className="text-label" style={{ color: 'var(--teal-glow)', display: 'block', fontSize: '0.75rem', marginBottom: '8px' }}>
                              AUTHENTICATION SOURCE
                            </span>
                            <span className="text-display" style={{ fontSize: '1.2rem', display: 'block', marginBottom: '4px' }}>
                              {ident.provider}
                            </span>
                            <span className="text-mono" style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                              ID: {ident.providerId.length > 20 ? ident.providerId.slice(0, 20) + '...' : ident.providerId}
                            </span>
                          </div>
                        ))
                      ) : (
                        <div style={{
                          background: 'var(--bg-overlay)',
                          border: '1px solid var(--border-subtle)',
                          borderRadius: '8px',
                          padding: '16px 20px',
                          textAlign: 'center',
                          minWidth: '180px'
                        }}>
                          <span className="text-label" style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.75rem', marginBottom: '8px' }}>
                            IDENTITY SOURCE
                          </span>
                          <span className="text-display" style={{ fontSize: '1.2rem', display: 'block', color: 'var(--text-secondary)' }}>
                            LOCAL CREDENTIALS
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Connector Arrow */}
                    <div style={{ color: 'var(--teal-glow)', fontSize: '1.5rem', fontWeight: 'bold', zIndex: 1, userSelect: 'none' }}>
                      ↓
                    </div>

                    {/* Layer 2: Central Identity Core */}
                    <div style={{
                      background: 'var(--bg-raised)',
                      border: '1.5px solid var(--teal-glow)',
                      borderRadius: '10px',
                      padding: '24px',
                      width: '100%',
                      maxWidth: '480px',
                      zIndex: 1,
                      position: 'relative',
                      boxShadow: '0 0 25px rgba(0, 240, 200, 0.08)'
                    }}>
                      <div style={{ position: 'absolute', top: '12px', right: '12px' }}>
                        <span className="badge badge--online"><span className="badge__dot" />CORE</span>
                      </div>
                      <span className="text-label" style={{ color: 'var(--text-secondary)', fontSize: '0.7rem', display: 'block', marginBottom: '4px' }}>
                        CENTRAL USER IDENTITY
                      </span>
                      <h3 className="text-display" style={{ fontSize: '1.5rem', marginBottom: '4px', color: '#fff' }}>
                        {profile.username}
                      </h3>
                      <span className="text-mono" style={{ fontSize: '0.85rem', color: 'var(--teal-glow)', display: 'block', marginBottom: '16px' }}>
                        {profile.email}
                      </span>
                      
                      <div className="divider" style={{ margin: '12px 0' }}><span className="divider__line" /></div>
                      
                      <div style={{ display: 'grid', gridTemplateColumns: '80px 1fr', gap: '8px 16px', fontSize: '0.8rem' }}>
                        <span className="text-label">USER UUID</span>
                        <span className="text-mono" style={{ color: 'var(--text-secondary)', overflowWrap: 'anywhere' }}>{profile.userId}</span>
                        
                        <span className="text-label">ROLES</span>
                        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                          {profile.roles && profile.roles.map((role, idx) => (
                            <span key={idx} className="badge badge--online" style={{ background: 'rgba(0, 240, 200, 0.1)', border: '1px solid var(--teal-glow)', padding: '2px 8px', fontSize: '0.7rem' }}>
                              {role}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Connector Arrow */}
                    <div style={{ color: 'var(--teal-glow)', fontSize: '1.5rem', fontWeight: 'bold', zIndex: 1, userSelect: 'none' }}>
                      ↓
                    </div>

                    {/* Layer 3: Multi-Factor Authentication Shield */}
                    <div style={{
                      background: 'var(--bg-overlay)',
                      border: profile.is2faEnabled ? '1.5px solid var(--teal-glow)' : '1px solid var(--border-subtle)',
                      borderRadius: '8px',
                      padding: '16px 24px',
                      width: '100%',
                      maxWidth: '360px',
                      zIndex: 1,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '16px',
                      boxShadow: profile.is2faEnabled ? '0 0 20px rgba(0, 240, 200, 0.06)' : 'none'
                    }}>
                      <div style={{
                        width: '44px',
                        height: '44px',
                        borderRadius: '50%',
                        background: profile.is2faEnabled ? 'rgba(0, 240, 200, 0.1)' : 'rgba(255, 255, 255, 0.02)',
                        border: profile.is2faEnabled ? '1.5px solid var(--teal-glow)' : '1px solid var(--border-subtle)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '1.3rem'
                      }}>
                        {profile.is2faEnabled ? '🛡️' : '🔓'}
                      </div>
                      <div style={{ flex: 1 }}>
                        <span className="text-label" style={{ color: 'var(--text-secondary)', fontSize: '0.7rem', display: 'block', marginBottom: '2px' }}>
                          STEP-UP SECURITY SHIELD
                        </span>
                        <h4 className="text-display" style={{ fontSize: '1rem', color: profile.is2faEnabled ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                          {profile.is2faEnabled ? 'MFA FULLY ENFORCED' : 'NO MFA SHIELD BIND'}
                        </h4>
                        {profile.twoFactorMethods && profile.twoFactorMethods.length > 0 && (
                          <span className="text-mono" style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>
                            Active Methods: {profile.twoFactorMethods.map(m => m.methodType).join(', ')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Network Infrastructure Meta */}
                  <div className="divider"><span className="divider__line" /></div>

                  <div className="stack stack-4">
                    <h3 className="text-display" style={{ fontSize: '1rem', color: 'var(--text-secondary)' }}>
                      EDGE NETWORK METADATA
                    </h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '180px 1fr', gap: '12px 16px', background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label">Active Cookie Domain</span>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontSize: '0.85rem' }}>https://localhost:8080</span>

                      <span className="text-label">Encryption Level</span>
                      <span className="text-mono" style={{ fontSize: '0.85rem' }}>Lightweight Transit State Representation (JWT)</span>

                      <span className="text-label">SameSite Cookie Policy</span>
                      <span className="text-mono" style={{ fontSize: '0.85rem' }}>Lax (Secure HttpOnly Session Propagation)</span>

                      <span className="text-label">Downstream Sync</span>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontSize: '0.85rem' }}>mTLS Signed RSA-256 Downstream Handshake</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* MFA Tab */}
          {activeTab === 'mfa' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Multi-Factor Authentication (TOTP)
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Add a second verification layer to secure access logs and block unauthorized proxy sessions.
                </p>
              </div>

              {mfaSuccess && (
                <div className="alert alert--success">
                  Multi-Factor Authentication enabled successfully!
                </div>
              )}

              {mfaError && (
                <div className="alert alert--error">
                  {mfaError}
                </div>
              )}

              {!mfaSecret ? (
                <button className="btn btn-primary" onClick={handleInitiateMfa}>
                  Initialize MFA Setup
                </button>
              ) : (
                <div className="stack stack-6" style={{ background: 'rgba(255,255,255,0.01)', padding: '24px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                  <div>
                    <h3 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '8px', color: 'var(--teal-glow)' }}>
                      Step 1: Scan Authenticator Token
                    </h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '16px' }}>
                      Scan the QR parameter link using Google Authenticator, or manually key in the secure secret.
                    </p>
                    <div style={{ display: 'flex', justifyContent: 'center', margin: '20px 0' }}>
                      <div style={{ padding: '16px', background: 'var(--bg-overlay)', borderRadius: '8px', border: '1px solid var(--border-subtle)', display: 'inline-block' }}>
                        <QRCodeSVG value={mfaQrUrl} size={160} bgColor="transparent" fgColor="#00f0c8" level="M" includeMargin={false} />
                      </div>
                    </div>
                    <div style={{ background: 'var(--bg-overlay)', padding: '12px', borderRadius: '6px', overflowX: 'auto', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label" style={{ display: 'block', marginBottom: '4px' }}>MANUAL KEY</span>
                      <span className="text-mono" style={{ fontSize: '1.2rem', color: 'var(--teal-glow)' }}>{mfaSecret}</span>
                    </div>
                    <div style={{ marginTop: '12px', background: 'var(--bg-overlay)', padding: '12px', borderRadius: '6px', overflowX: 'auto', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label" style={{ display: 'block', marginBottom: '4px' }}>QR URL</span>
                      <span className="text-mono" style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>{mfaQrUrl}</span>
                    </div>
                  </div>

                  <form onSubmit={handleConfirmMfa} className="stack stack-4">
                    <h3 className="text-display" style={{ fontSize: '1.1rem', color: 'var(--teal-glow)' }}>
                      Step 2: Confirm Verification Code
                    </h3>
                    <div className="form-group">
                      <label className="form-label" htmlFor="confirmCode">6-Digit Code</label>
                      <input
                        id="confirmCode"
                        type="text"
                        className="form-input"
                        placeholder="000000"
                        value={confirmCode}
                        onChange={(e) => setConfirmCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      />
                    </div>
                    <div className="row gap-3">
                      <button type="submit" className="btn btn-primary">
                        Lock 2FA To Profile
                      </button>
                      <button type="button" className="btn btn-ghost" onClick={() => setMfaSecret('')}>
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          )}

          {/* Test Transit JWT Tab */}
          {activeTab === 'api' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Transit JWT & Security Context Verification
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Test how the Gateway extracts your stateful session cookie, constructs a signed transit JWT, and passes it securely to downstream services.
                </p>
              </div>

              <div className="row gap-4">
                <button className="btn btn-primary" onClick={handleTestApi} disabled={loading}>
                  {loading ? 'Interrogating Gateway...' : 'Interrogate /api/auth/users'}
                </button>
              </div>

              {usersResponse && (
                <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', borderRadius: '8px', padding: '20px', position: 'relative' }}>
                  <span className="text-label" style={{ position: 'absolute', top: '12px', right: '12px', color: 'var(--teal-glow)' }}>DECISION RESPONSE</span>
                  <pre style={{ margin: 0, fontFamily: 'var(--font-mono)', fontSize: '0.82rem', overflowX: 'auto', color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
                    {usersResponse}
                  </pre>
                </div>
              )}
            </div>
          )}

          {/* Linked Accounts Tab */}
          {activeTab === 'linking' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Linked Identity Providers
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Connect or disconnect social single-sign-on (SSO) options to simplify access to your profile.
                </p>
              </div>

              {linkMessage && (
                <div className="alert alert--success">
                  {linkMessage}
                </div>
              )}

              {linkError && (
                <div className="alert alert--error">
                  {linkError}
                </div>
              )}

              <div className="stack stack-4">
                {/* Google Provider Card */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: 'rgba(255,255,255,0.01)',
                  padding: '20px 24px',
                  borderRadius: '8px',
                  border: '1px solid var(--border-subtle)',
                  transition: 'border-color 0.2s'
                }}>
                  <div>
                    <h4 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '4px' }}>Google Access Node</h4>
                    <span className="text-mono" style={{ fontSize: '0.78rem', color: isLinked('GOOGLE') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                      {isLinked('GOOGLE') ? `Connected (Provider ID: ${getProviderId('GOOGLE')})` : 'Disconnected'}
                    </span>
                  </div>
                  <button
                    className={`btn ${isLinked('GOOGLE') ? 'btn-ghost' : 'btn-primary'}`}
                    style={{ minWidth: '120px' }}
                    onClick={() => isLinked('GOOGLE') ? handleUnlink('GOOGLE') : redirectToGoogleOAuth(true)}
                  >
                    {isLinked('GOOGLE') ? 'Disconnect' : 'Connect'}
                  </button>
                </div>

                {/* 42 Network Node Card */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: 'rgba(255,255,255,0.01)',
                  padding: '20px 24px',
                  borderRadius: '8px',
                  border: '1px solid var(--border-subtle)',
                  transition: 'border-color 0.2s'
                }}>
                  <div>
                    <h4 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '4px' }}>42 Network Node</h4>
                    <span className="text-mono" style={{ fontSize: '0.78rem', color: isLinked('FORTYTWO') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                      {isLinked('FORTYTWO') ? `Connected (Provider ID: ${getProviderId('FORTYTWO')})` : 'Disconnected'}
                    </span>
                  </div>
                  <button
                    className={`btn ${isLinked('FORTYTWO') ? 'btn-ghost' : 'btn-primary'}`}
                    style={{ minWidth: '120px' }}
                    onClick={() => isLinked('FORTYTWO') ? handleUnlink('FORTYTWO') : redirectToFortyTwoOAuth(true)}
                  >
                    {isLinked('FORTYTWO') ? 'Disconnect' : 'Connect'}
                  </button>
                </div>

                <div className="divider" style={{ margin: '24px 0' }}><span className="divider__line" /></div>

                {/* Local Account Password Binding */}
                <div>
                  <h3 className="text-display" style={{ fontSize: '1.25rem', marginBottom: '8px', color: 'var(--teal-glow)' }}>
                    🗝️ Local Security Credentials
                  </h3>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '16px' }}>
                    Setup a security password to enable standard logging or safely sever social authentication links.
                  </p>
                </div>

                {profile && profile.hasPassword ? (
                  <div style={{ background: 'rgba(0, 240, 200, 0.02)', padding: '20px 24px', borderRadius: '8px', border: '1px solid var(--teal-glow)' }}>
                    <span className="text-mono" style={{ color: 'var(--teal-glow)', display: 'block', fontSize: '0.9rem', marginBottom: '8px', fontWeight: 'bold' }}>
                      ✓ Local Password Active
                    </span>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>
                      You can log in directly using your email/username and password, or safely manage linked accounts.
                    </p>
                  </div>
                ) : (
                  <div style={{ background: 'rgba(245, 158, 11, 0.04)', padding: '20px 24px', borderRadius: '8px', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
                    <span className="text-mono" style={{ color: '#f59e0b', display: 'block', fontSize: '0.9rem', marginBottom: '8px', fontWeight: 'bold' }}>
                      ⚠️ No Password Configured
                    </span>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>
                      Your profile currently relies exclusively on OAuth2 authentication. Bind a local password below to enable password login and allow disconnects.
                    </p>
                  </div>
                )}

                {passwordSuccess && (
                  <div className="alert alert--success">
                    {passwordSuccess}
                  </div>
                )}

                {passwordError && (
                  <div className="alert alert--error">
                    {passwordError}
                  </div>
                )}

                <form onSubmit={handleSetPassword} className="stack stack-4" style={{ background: 'rgba(255,255,255,0.01)', padding: '24px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                  {profile && profile.hasPassword && (
                    <div className="form-group">
                      <label className="form-label" htmlFor="currentPassword">
                        Current Password
                      </label>
                      <input
                        id="currentPassword"
                        type="password"
                        className="form-input"
                        placeholder="••••••••••••"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                      />
                    </div>
                  )}
                  <div className="form-group">
                    <label className="form-label" htmlFor="newPassword">
                      {profile && profile.hasPassword ? 'New password' : 'Create new password'}
                    </label>
                    <input
                      id="newPassword"
                      type="password"
                      className="form-input"
                      placeholder="••••••••••••"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                    />
                  </div>
                  <button type="submit" className="btn btn-primary" disabled={loading} style={{ alignSelf: 'flex-start' }}>
                    {loading ? 'Updating Password...' : profile && profile.hasPassword ? 'Change Password' : 'Bind Password'}
                  </button>
                </form>
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
