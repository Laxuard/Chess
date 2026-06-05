import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import { useAuth } from '../context/AuthContext';
import {
  getUsers,
  getMfaSetup,
  confirmMfaSetup,
  unlinkOAuth2Provider,
  redirectToGoogleOAuth,
  redirectToFortyTwoOAuth,
  setLocalPassword,
  getMyProfile,
  updateMyProfile,
  getProfileByUsername,
  getPublicProfiles
} from '../services/api';
import { QRCodeSVG } from 'qrcode.react';
import {
  User,
  Lock,
  Link2,
  Terminal,
  Database,
  Users,
  Shield,
  Send,
  Search,
  Check,
  AlertTriangle,
  Settings,
  Eye,
  EyeOff
} from 'lucide-react';

const DEFAULT_AVATAR = '/assets/avatars/default-placeholder.png';

function isDefaultAvatar(url) {
  return !url || url === DEFAULT_AVATAR;
}

function UserAvatar({ url, username, size = 64 }) {
  const [failed, setFailed] = useState(false);
  const showFallback = !url || isDefaultAvatar(url) || failed;

  if (showFallback) {
    const initials = (username || '?').slice(0, 2).toUpperCase();
    return (
      <div style={{
        width: size,
        height: size,
        borderRadius: '50%',
        background: 'linear-gradient(135deg, hsl(175,80%,35%), hsl(250,60%,50%))',
        border: '2px solid var(--teal-glow)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: size * 0.35,
        fontWeight: 700,
        color: '#fff',
        letterSpacing: '0.05em',
        flexShrink: 0,
        boxShadow: '0 0 15px rgba(0,240,200,0.15)',
        userSelect: 'none',
      }}>
        {initials}
      </div>
    );
  }

  return (
    <img
      src={url}
      alt={`${username} avatar`}
      onError={() => setFailed(true)}
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        objectFit: 'cover',
        border: '2px solid var(--teal-glow)',
        flexShrink: 0,
        boxShadow: '0 0 15px rgba(0,240,200,0.15)',
        transition: 'transform 0.2s ease',
      }}
    />
  );
}

export default function DashboardScreen() {
  const gatewayPort = typeof __GATEWAY_PORT__ !== 'undefined' ? __GATEWAY_PORT__ : '8080';
  const gatewayOrigin = window.location.port === '5173' ? `https://localhost:${gatewayPort}` : window.location.origin;

  const navigate = useNavigate();
  const { logout } = useAuth();

  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [usersResponse, setUsersResponse] = useState('');
  
  // Auth Profile context (Central identity)
  const [profile, setProfile] = useState(null);
  
  // Social Profile context (social-service)
  const [socialProfile, setSocialProfile] = useState(null);
  const [bioInput, setBioInput] = useState('');
  const [isHiddenInput, setIsHiddenInput] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileError, setProfileError] = useState('');

  // Operator Directory search
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [searchError, setSearchError] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);

  // Friends & Chat Simulator
  const [chatInput, setChatInput] = useState('');
  const [chatLogs, setChatLogs] = useState([
    { time: '00:54:10', service: 'GATEWAY', message: 'Reactive Netty Session trace initialized.', traceId: '8f273b-e012-42da' },
    { time: '00:54:12', service: 'AUTH-SERVICE', message: 'UserSyncEvent payload published to Kafka outbox.', traceId: '8f273b-e012-42da' },
    { time: '00:54:13', service: 'SOCIAL-SERVICE', message: 'Kafka user-sync-topic consumer replicated profile successfully.', traceId: '8f273b-e012-42da' },
    { time: '00:54:15', service: 'GATEWAY-WS', message: 'Multiplexed WebSession socket pipeline connection verified.', traceId: '33e10c-f410-9988' },
    { time: '00:54:16', service: 'MATCHMAKER', message: 'Operator matchmaking routing tables refreshed.', traceId: '33e10c-f410-9988' }
  ]);
  const chatEndRef = useRef(null);

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

  // Fetch all profile contexts
  const syncProfileContext = async () => {
    try {
      const { status, data } = await getUsers();
      if (status === 401) {
        logout();
        return;
      } else if (status === 403) {
        navigate('/mfa-challenge');
        return;
      } else if (status === 200) {
        setProfile(data);

        // Fetch social profile
        const socialRes = await getMyProfile();
        if (socialRes.status === 200) {
          setSocialProfile(socialRes.data);
          setBioInput(socialRes.data.bio || '');
          setIsHiddenInput(socialRes.data.profileHidden || false);
        }
      }
    } catch (err) {
      console.error('Failed to sync profile context:', err);
    }
  };

  useEffect(() => {
    syncProfileContext();
  }, [logout, navigate]);

  // Handle URL redirect query flags
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('link') === 'success') {
      setLinkMessage('OAuth Provider linked successfully!');
      window.history.replaceState({}, document.title, window.location.pathname);
    } else if (params.get('link') === 'error') {
      setLinkError('Failed to link provider. Connection might already belong to another user.');
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  // Auto-scroll chat log
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatLogs]);

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
    if (!window.confirm(`Disconnect authentication link to ${provider}?`)) {
      return;
    }
    setLinkError('');
    setLinkMessage('');
    try {
      const { status, data } = await unlinkOAuth2Provider(provider);
      if (status === 200) {
        setLinkMessage(`Unlinked ${provider} successfully.`);
        await syncProfileContext();
      } else {
        setLinkError(data.message || `Failed to unlink ${provider}.`);
      }
    } catch (err) {
      setLinkError('BFF Gateway communication error.');
    }
  };

  const handleSetPassword = async (e) => {
    e.preventDefault();
    if (!newPassword) {
      setPasswordError('Please enter a new password.');
      return;
    }
    if (profile && profile.hasPassword && !currentPassword) {
      setPasswordError('Current credentials required to authorize change.');
      return;
    }
    setPasswordError('');
    setPasswordSuccess('');
    setLoading(true);

    try {
      const { status, data } = await setLocalPassword(newPassword, currentPassword);
      if (status === 200) {
        setPasswordSuccess('Password bindings updated successfully!');
        setNewPassword('');
        setCurrentPassword('');
        await syncProfileContext();
      } else {
        setPasswordError(data.message || 'Failed to update credentials.');
      }
    } catch (err) {
      setPasswordError('Auth Service connection error.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setProfileSuccess('');
    setProfileError('');
    setLoading(true);

    try {
      const { status, data } = await updateMyProfile(bioInput, isHiddenInput);
      if (status === 200) {
        setProfileSuccess('Profile attributes synchronized successfully.');
        setSocialProfile(data);
      } else {
        setProfileError(data.message || 'Failed to update profile.');
      }
    } catch (err) {
      setProfileError('Failed to contact social service api.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchOperator = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setSearchError('');
    setSearchResult(null);
    setSearchLoading(true);

    try {
      const { status, data } = await getProfileByUsername(searchQuery.trim());
      if (status === 200) {
        setSearchResult(data);
      } else if (status === 403) {
        setSearchError('Access Restricted: Target Operator profile is configured as Private.');
      } else if (status === 404) {
        setSearchError('Operator not found. Double check display name.');
      } else {
        setSearchError('Unexpected response from directory.');
      }
    } catch (err) {
      setSearchError('Directory lookup error.');
    } finally {
      setSearchLoading(false);
    }
  };

  const handleSendChat = (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const fakeTraceId = Math.random().toString(16).substr(2, 6) + '-' + Math.random().toString(16).substr(2, 4) + '-trans';
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0];

    const logEntry = {
      time: timeStr,
      service: 'CLIENT-COMM',
      message: `${profile?.username || 'Operator'}: ${chatInput}`,
      traceId: fakeTraceId
    };

    setChatLogs(prev => [...prev, logEntry]);
    setChatInput('');

    // Simulate system echo replies 
    setTimeout(() => {
      const echoEntry = {
        time: new Date().toTimeString().split(' ')[0],
        service: 'WS-MULTICAST',
        message: `Echo packet received. Packet routed to active session subscribers.`,
        traceId: fakeTraceId
      };
      setChatLogs(prev => [...prev, echoEntry]);
    }, 800);
  };

  const handleTestApi = async () => {
    setLoading(true);
    setUsersResponse('');
    try {
      const { status, data } = await getUsers();
      if (status === 200) {
        setUsersResponse(typeof data === 'string' ? data : JSON.stringify(data, null, 2));
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
        await syncProfileContext();
      } else {
        setMfaError(data.message || 'MFA validation failed.');
      }
    } catch (err) {
      setMfaError('Network error confirming setup.');
    }
  };

  const mockFriends = [
    { username: 'Laxuard', status: 'ONLINE', mode: 'Matchmaking' },
    { username: 'fortytwo_spectre', status: 'IN_GAME', mode: 'Classic Pong' },
    { username: 'alpha_ponger', status: 'OFFLINE', mode: '' },
    { username: 'transcendent_bot', status: 'ONLINE', mode: 'Practice Arena' },
  ];

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-void)', color: 'var(--text-primary)', display: 'flex', flexDirection: 'column' }}>
      
      {/* ─── Global Cyber Navbar ──────────────────────── */}
      <header style={{ 
        background: 'var(--bg-base)', 
        borderBottom: '1px solid var(--border-subtle)', 
        padding: '16px 32px', 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        position: 'relative',
        boxShadow: '0 4px 20px rgba(0,0,0,0.4)'
      }}>
        <div className="scan-line" />
        <Logo size="sm" />
        
        {profile && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <UserAvatar url={socialProfile?.avatarUrl} username={profile.username} size={36} />
              <div style={{ textAlign: 'right' }}>
                <span className="text-mono" style={{ fontSize: '0.85rem', color: '#fff', display: 'block', fontWeight: 700 }}>
                  {profile.username}
                </span>
                <span className="text-label" style={{ fontSize: '0.6rem', color: 'var(--teal-glow)' }}>
                  {profile.roles?.includes('ROLE_ADMIN') ? 'ROOT OPERATOR' : 'OPERATOR'}
                </span>
              </div>
            </div>
            <button className="btn btn-ghost btn-sm" onClick={logout}>
              Disconnect
            </button>
          </div>
        )}
      </header>

      {/* ─── Main Grid Layout ────────────────────────── */}
      <main style={{ 
        flex: 1, 
        padding: '40px 32px', 
        maxWidth: '1300px', 
        width: '100%', 
        margin: '0 auto', 
        display: 'grid', 
        gridTemplateColumns: '260px 1fr', 
        gap: '40px' 
      }}>
        
        {/* Cyber Sidebar Navigation */}
        <aside className="stack stack-3">
          <div style={{ 
            background: 'var(--bg-surface)', 
            border: '1px solid var(--border-subtle)', 
            borderRadius: '8px', 
            padding: '12px 16px',
            marginBottom: '10px'
          }}>
            <span className="text-label" style={{ fontSize: '0.62rem', color: 'var(--text-secondary)' }}>SESSION BINDINGS</span>
            <div className="text-mono" style={{ fontSize: '0.72rem', marginTop: '6px', color: 'var(--teal-glow)', overflowWrap: 'anywhere' }}>
              SID: {profile?.userId ? `${profile.userId.substring(0, 18)}...` : 'DISCONNECTED'}
            </div>
          </div>

          <button 
            className={`btn btn-full ${activeTab === 'profile' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('profile')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            <User size={16} /> ◉ Profile Hub
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'social' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('social')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            <Users size={16} /> 👥 Social Center
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'settings' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('settings')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            <Settings size={16} /> 🔒 Account Settings
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'diagnostics' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('diagnostics')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            <Terminal size={16} /> ⚡ Diagnostics
          </button>
        </aside>

        {/* Dynamic Display Panel */}
        <section className="card" style={{ padding: '32px', minHeight: '520px', background: 'var(--bg-surface)' }}>
          <div className="corner-decor corner-decor--tl" />
          <div className="corner-decor corner-decor--tr" />
          <div className="corner-decor corner-decor--bl" />
          <div className="corner-decor corner-decor--br" />

          {/* ==============================================================
              PROFILE HUB TAB
              ============================================================== */}
          {activeTab === 'profile' && (
            <div className="stack stack-6">
              <div>
                <span className="badge badge--online" style={{ marginBottom: '8px' }}>
                  <span className="badge__dot" />Edge Nodes Sync Active
                </span>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Transcendence Operator Profile
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Manage personal identifiers synced across decentralized outboxes and search the global directory map.
                </p>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', alignItems: 'start' }}>
                
                {/* Left Card: My Details */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-4">
                  <span className="text-label" style={{ color: 'var(--teal-glow)' }}>MY PROFILE PROFILE</span>
                  
                  {socialProfile ? (
                    <form onSubmit={handleUpdateProfile} className="stack stack-4">
                      {profileSuccess && <div className="alert alert--success">{profileSuccess}</div>}
                      {profileError && <div className="alert alert--error">{profileError}</div>}
                      
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <UserAvatar url={socialProfile.avatarUrl} username={profile?.username} size={64} />
                        <div>
                          <h3 className="text-display" style={{ fontSize: '1.25rem', color: '#fff' }}>
                            {socialProfile.username}
                          </h3>
                          <span className="text-mono" style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                            ID: {socialProfile.userId}
                          </span>
                        </div>
                      </div>

                      <div className="form-group">
                        <label className="form-label">Operator Bio</label>
                        <textarea 
                          className="form-input" 
                          rows="3" 
                          placeholder="Introduce yourself to the gaming network..."
                          value={bioInput}
                          onChange={(e) => setBioInput(e.target.value)}
                          maxLength={500}
                          style={{ resize: 'none', fontFamily: 'var(--font-body)' }}
                        />
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', background: 'rgba(0,240,200,0.02)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                        <input 
                          type="checkbox" 
                          id="profileHidden" 
                          checked={isHiddenInput} 
                          onChange={(e) => setIsHiddenInput(e.target.checked)}
                          style={{ width: '18px', height: '18px', cursor: 'pointer', accentColor: 'var(--teal-glow)' }}
                        />
                        <label htmlFor="profileHidden" style={{ fontSize: '0.85rem', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                          {isHiddenInput ? <EyeOff size={14} className="text-danger" /> : <Eye size={14} className="text-accent" />}
                          <div>
                            <span style={{ fontWeight: 'bold', color: isHiddenInput ? 'var(--red-danger)' : 'var(--teal-glow)' }}>
                              {isHiddenInput ? 'Private Profile' : 'Public Profile'}
                            </span>
                            <span style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                              {isHiddenInput ? 'Hidden from directory searches' : 'Searchable and visible to global network players'}
                            </span>
                          </div>
                        </label>
                      </div>

                      <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                        {loading ? 'Saving Changes...' : 'Save Profile Changes'}
                      </button>
                    </form>
                  ) : (
                    <div className="text-mono" style={{ color: 'var(--text-secondary)', padding: '20px', textAlign: 'center' }}>
                      Locating social identity record...
                    </div>
                  )}
                </div>

                {/* Right Card: Directory Search */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-4">
                  <span className="text-label">OPERATOR DIRECTORY LOOKUP</span>
                  
                  <form onSubmit={handleSearchOperator} style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ flex: 1, position: 'relative' }}>
                      <input 
                        type="text" 
                        className="form-input" 
                        placeholder="Search username..." 
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ paddingLeft: '36px' }}
                      />
                      <Search size={14} style={{ position: 'absolute', left: '12px', top: '13px', color: 'var(--text-muted)' }} />
                    </div>
                    <button type="submit" className="btn btn-primary" disabled={searchLoading}>
                      {searchLoading ? 'Searching...' : 'Search'}
                    </button>
                  </form>

                  {searchError && (
                    <div className="alert alert--error" style={{ padding: '12px' }}>
                      <AlertTriangle size={16} />
                      <span>{searchError}</span>
                    </div>
                  )}

                  {searchResult && (
                    <div style={{ 
                      background: 'var(--bg-overlay)', 
                      border: '1px solid var(--teal-glow)', 
                      borderRadius: '8px', 
                      padding: '16px',
                      position: 'relative'
                    }} className="stack stack-3">
                      <div style={{ position: 'absolute', top: '12px', right: '12px' }}>
                        <span className="badge badge--online" style={{ fontSize: '0.6rem' }}>FOUND</span>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <UserAvatar url={searchResult.avatarUrl} username={searchResult.username} size={48} />
                        <div>
                          <h4 className="text-display" style={{ fontSize: '1.1rem', color: '#fff' }}>
                            {searchResult.username}
                          </h4>
                          <span className="text-mono" style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>
                            UID: {searchResult.userId?.substring(0, 18)}...
                          </span>
                        </div>
                      </div>
                      <div style={{ background: 'rgba(255,255,255,0.02)', padding: '10px', borderRadius: '6px', minHeight: '50px' }}>
                        <span className="text-label" style={{ fontSize: '0.62rem', display: 'block', marginBottom: '4px' }}>BIO</span>
                        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0 }}>
                          {searchResult.bio || 'This operator has not filled out their bio.'}
                        </p>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                        <span>Joined: {new Date(searchResult.createdAt).toLocaleDateString()}</span>
                        <span>Sync Status: Verifiable</span>
                      </div>
                    </div>
                  )}
                </div>

              </div>
            </div>
          )}

          {/* ==============================================================
              SOCIAL CENTER TAB (SIMULATION)
              ============================================================== */}
          {activeTab === 'social' && (
            <div className="stack stack-6">
              <div>
                <span className="badge badge--online" style={{ marginBottom: '8px' }}>
                  <span className="badge__dot" />Transcendence Network Lobby
                </span>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Social Center Hub
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Interact with other operators across gaming networks. Prepare matchmaking queues and audit WebSocket sync traces.
                </p>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '280px 1fr', gap: '30px', alignItems: 'start' }}>
                
                {/* Left Card: Operator list */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '20px' 
                }} className="stack stack-4">
                  <span className="text-label">ACTIVE PLAYERS</span>
                  <div className="stack stack-3">
                    {mockFriends.map((f, idx) => (
                      <div key={idx} style={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        justifyContent: 'space-between', 
                        padding: '10px', 
                        borderRadius: '6px', 
                        background: 'var(--bg-overlay)',
                        border: '1px solid var(--border-subtle)'
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <UserAvatar username={f.username} size={32} />
                          <div>
                            <span style={{ fontSize: '0.85rem', fontWeight: 'bold', display: 'block' }}>{f.username}</span>
                            {f.status === 'IN_GAME' && (
                              <span style={{ fontSize: '0.7rem', color: 'var(--amber-warn)' }}>🎮 {f.mode}</span>
                            )}
                            {f.status === 'ONLINE' && (
                              <span style={{ fontSize: '0.7rem', color: 'var(--teal-glow)' }}>🟢 {f.mode || 'Idle'}</span>
                            )}
                            {f.status === 'OFFLINE' && (
                              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>🔴 Offline</span>
                            )}
                          </div>
                        </div>
                        {f.status !== 'OFFLINE' && (
                          <button 
                            className="btn btn-ghost btn-sm" 
                            style={{ padding: '4px 8px', fontSize: '0.68rem' }}
                            onClick={() => alert(`Invite sent to ${f.username} for classic Matchmaking!`)}
                          >
                            Challenge
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                {/* Right Card: Terminal Console log */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '20px' 
                }} className="stack stack-4">
                  <span className="text-label">MULTIPLEX CHANNELS COMM LOG</span>
                  
                  <div style={{ 
                    background: '#040608', 
                    borderRadius: '8px', 
                    border: '1px solid var(--border-subtle)', 
                    padding: '16px',
                    height: '240px',
                    overflowY: 'auto',
                    fontFamily: 'var(--font-mono)',
                    fontSize: '0.78rem',
                    color: '#89a'
                  }} className="stack stack-2">
                    {chatLogs.map((log, idx) => (
                      <div key={idx} style={{ lineHeight: '1.4' }}>
                        <span style={{ color: 'var(--text-muted)' }}>[{log.time}]</span>{' '}
                        <span style={{ color: log.service === 'CLIENT-COMM' ? 'var(--teal-glow)' : 'var(--blue-info)', fontWeight: 'bold' }}>
                          [{log.service}]
                        </span>{' '}
                        <span style={{ color: '#fff' }}>{log.message}</span>
                        {log.traceId && (
                          <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem', display: 'block', marginLeft: '12px' }}>
                            ↳ trace_id: {log.traceId}
                          </span>
                        )}
                      </div>
                    ))}
                    <div ref={chatEndRef} />
                  </div>

                  <form onSubmit={handleSendChat} style={{ display: 'flex', gap: '10px' }}>
                    <input 
                      type="text" 
                      className="form-input" 
                      placeholder="Type packet message to broadcast to trace network..." 
                      value={chatInput}
                      onChange={(e) => setChatInput(e.target.value)}
                    />
                    <button type="submit" className="btn btn-primary">
                      <Send size={14} /> Send
                    </button>
                  </form>
                </div>

              </div>
            </div>
          )}

          {/* ==============================================================
              ACCOUNT SETTINGS TAB
              ============================================================== */}
          {activeTab === 'settings' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Operator Access Configuration
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Secure your authentication keys, configure Multi-Factor TOTP, and link social single-sign-on providers.
                </p>
              </div>

              {linkMessage && <div className="alert alert--success">{linkMessage}</div>}
              {linkError && <div className="alert alert--error">{linkError}</div>}

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', alignItems: 'start' }}>
                
                {/* Left Card: 2FA TOTP */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-4">
                  <span className="text-label" style={{ color: 'var(--teal-glow)' }}>🛡️ MULTI-FACTOR AUTHENTICATION (TOTP)</span>
                  
                  {mfaSuccess && (
                    <div className="alert alert--success">
                      Multi-Factor Shield activated successfully!
                    </div>
                  )}

                  {mfaError && (
                    <div className="alert alert--error">
                      {mfaError}
                    </div>
                  )}

                  {profile?.is2faEnabled ? (
                    <div style={{ background: 'rgba(0, 240, 200, 0.04)', padding: '20px', borderRadius: '8px', border: '1px solid var(--teal-glow)' }} className="stack stack-2">
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontWeight: 'bold' }}>✓ Step-Up Active</span>
                      <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', margin: 0 }}>
                        Your session requires double verification codes generated dynamically from Google Authenticator on every login attempt.
                      </p>
                    </div>
                  ) : !mfaSecret ? (
                    <div className="stack stack-3">
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                        Protect your account from unauthorized remote access by adding a second dynamic password shield.
                      </p>
                      <button className="btn btn-primary" onClick={handleInitiateMfa}>
                        Initialize MFA Setup
                      </button>
                    </div>
                  ) : (
                    <div className="stack stack-4" style={{ background: 'var(--bg-overlay)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                      <div>
                        <h4 className="text-display" style={{ fontSize: '0.95rem', marginBottom: '8px' }}>Scan Authenticator Token</h4>
                        <div style={{ display: 'flex', justifyContent: 'center', margin: '14px 0' }}>
                          <div style={{ padding: '10px', background: '#fff', borderRadius: '6px', display: 'inline-block' }}>
                            <QRCodeSVG value={mfaQrUrl} size={140} bgColor="#fff" fgColor="#000" level="M" />
                          </div>
                        </div>
                        <div style={{ background: '#050709', padding: '10px', borderRadius: '6px', border: '1px solid var(--border-subtle)', marginBottom: '14px' }}>
                          <span className="text-label" style={{ fontSize: '0.6rem', display: 'block', marginBottom: '2px' }}>SECRET KEY</span>
                          <span className="text-mono" style={{ fontSize: '1.05rem', color: 'var(--teal-glow)', wordBreak: 'break-all' }}>{mfaSecret}</span>
                        </div>
                      </div>

                      <form onSubmit={handleConfirmMfa} className="stack stack-3">
                        <div className="form-group">
                          <label className="form-label">Verify TOTP Code</label>
                          <input 
                            type="text" 
                            className="form-input" 
                            placeholder="000000" 
                            value={confirmCode}
                            onChange={(e) => setConfirmCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                          />
                        </div>
                        <div className="row gap-2">
                          <button type="submit" className="btn btn-primary">Enable 2FA</button>
                          <button type="button" className="btn btn-ghost" onClick={() => setMfaSecret('')}>Cancel</button>
                        </div>
                      </form>
                    </div>
                  )}
                </div>

                {/* Right Card: Password Binding & Social Logins */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-5">
                  <span className="text-label">SSO PROVIDER BINDS</span>
                  
                  <div className="stack stack-3">
                    {/* Google Binds */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-overlay)', padding: '12px 16px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                      <div>
                        <span style={{ fontSize: '0.9rem', fontWeight: 'bold', display: 'block' }}>Google SSO Node</span>
                        <span className="text-mono" style={{ fontSize: '0.72rem', color: isLinked('GOOGLE') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                          {isLinked('GOOGLE') ? 'Linked' : 'Disconnected'}
                        </span>
                      </div>
                      <button 
                        className={`btn btn-sm ${isLinked('GOOGLE') ? 'btn-ghost' : 'btn-primary'}`}
                        onClick={() => isLinked('GOOGLE') ? handleUnlink('GOOGLE') : redirectToGoogleOAuth(true)}
                      >
                        {isLinked('GOOGLE') ? 'Disconnect' : 'Connect'}
                      </button>
                    </div>

                    {/* 42 Binds */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-overlay)', padding: '12px 16px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                      <div>
                        <span style={{ fontSize: '0.9rem', fontWeight: 'bold', display: 'block' }}>FortyTwo SSO Node</span>
                        <span className="text-mono" style={{ fontSize: '0.72rem', color: isLinked('FORTYTWO') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                          {isLinked('FORTYTWO') ? 'Linked' : 'Disconnected'}
                        </span>
                      </div>
                      <button 
                        className={`btn btn-sm ${isLinked('FORTYTWO') ? 'btn-ghost' : 'btn-primary'}`}
                        onClick={() => isLinked('FORTYTWO') ? handleUnlink('FORTYTWO') : redirectToFortyTwoOAuth(true)}
                      >
                        {isLinked('FORTYTWO') ? 'Disconnect' : 'Connect'}
                      </button>
                    </div>
                  </div>

                  <span className="text-label">LOCAL CREDENTIAL BINDING</span>
                  
                  {profile?.hasPassword ? (
                    <div style={{ background: 'rgba(0, 240, 200, 0.01)', padding: '10px', borderRadius: '6px', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontSize: '0.78rem' }}>✓ Credentials Set</span>
                    </div>
                  ) : (
                    <div style={{ background: 'rgba(240, 165, 0, 0.03)', padding: '10px', borderRadius: '6px', border: '1px solid rgba(240, 165, 0, 0.2)' }}>
                      <span className="text-mono" style={{ color: 'var(--amber-warn)', fontSize: '0.78rem' }}>⚠️ Social Authentication Only</span>
                    </div>
                  )}

                  {passwordSuccess && <div className="alert alert--success">{passwordSuccess}</div>}
                  {passwordError && <div className="alert alert--error">{passwordError}</div>}

                  <form onSubmit={handleSetPassword} className="stack stack-3">
                    {profile?.hasPassword && (
                      <div className="form-group">
                        <label className="form-label">Current Password</label>
                        <input 
                          type="password" 
                          className="form-input" 
                          placeholder="••••••••" 
                          value={currentPassword}
                          onChange={(e) => setCurrentPassword(e.target.value)}
                        />
                      </div>
                    )}
                    <div className="form-group">
                      <label className="form-label">{profile?.hasPassword ? 'New Password' : 'Create Local Password'}</label>
                      <input 
                        type="password" 
                        className="form-input" 
                        placeholder="••••••••" 
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                      />
                    </div>
                    <button type="submit" className="btn btn-primary btn-sm" disabled={loading} style={{ alignSelf: 'flex-start' }}>
                      {profile?.hasPassword ? 'Update credentials' : 'Bind password'}
                    </button>
                  </form>
                </div>

              </div>
            </div>
          )}

          {/* ==============================================================
              SYSTEM DIAGNOSTICS TAB
              ============================================================== */}
          {activeTab === 'diagnostics' && (
            <div className="stack stack-6">
              <div>
                <span className="badge badge--online" style={{ marginBottom: '8px' }}>
                  <span className="badge__dot" />Diagnostic Console
                </span>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  BFF Gateway Tracing & Topology
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Inspect the mTLS credentials validation flow and visual security routing topology.
                </p>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.2fr', gap: '30px', alignItems: 'start' }}>
                
                {/* Left Card: JWT Transit Test */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-4">
                  <span className="text-label">TRANSIT JWT INSPECTOR</span>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    Interrogate the gateway to fetch transit security records and extract cryptographic user token definitions.
                  </p>
                  <button className="btn btn-primary" onClick={handleTestApi} disabled={loading}>
                    {loading ? 'Requesting...' : 'Interrogate BFF /api/auth/users'}
                  </button>
                  
                  {usersResponse && (
                    <div style={{ 
                      background: '#040608', 
                      border: '1px solid var(--border-subtle)', 
                      borderRadius: '8px', 
                      padding: '16px',
                      maxHeight: '260px',
                      overflowY: 'auto'
                    }}>
                      <pre style={{ margin: 0, fontFamily: 'var(--font-mono)', fontSize: '0.78rem', color: 'var(--teal-glow)' }}>
                        {usersResponse}
                      </pre>
                    </div>
                  )}
                </div>

                {/* Right Card: Identity Topology Map */}
                <div style={{ 
                  background: 'var(--bg-raised)', 
                  border: '1px solid var(--border-default)', 
                  borderRadius: '10px', 
                  padding: '24px' 
                }} className="stack stack-4">
                  <span className="text-label">IDENTITY TOPOLOGY MAP</span>
                  
                  <div style={{ 
                    display: 'flex', 
                    flexDirection: 'column', 
                    alignItems: 'center', 
                    gap: '14px', 
                    background: 'rgba(255,255,255,0.01)', 
                    padding: '20px', 
                    borderRadius: '8px', 
                    border: '1px solid var(--border-subtle)',
                    position: 'relative'
                  }}>
                    {/* Layer 1: Inputs */}
                    <div style={{ display: 'flex', gap: '10px', width: '100%', justifyContent: 'center' }}>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', padding: '6px 12px', borderRadius: '6px', fontSize: '0.72rem', textAlign: 'center' }}>
                        <span style={{ display: 'block', fontSize: '0.6rem', color: 'var(--text-muted)' }}>SOCIAL LOGINS</span>
                        <span>Google / 42</span>
                      </div>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', padding: '6px 12px', borderRadius: '6px', fontSize: '0.72rem', textAlign: 'center' }}>
                        <span style={{ display: 'block', fontSize: '0.6rem', color: 'var(--text-muted)' }}>CREDENTIALS</span>
                        <span>Local login</span>
                      </div>
                    </div>

                    <div style={{ color: 'var(--teal-glow)', fontSize: '0.9rem' }}>↓</div>

                    {/* Layer 2: Gateway */}
                    <div style={{ background: 'var(--bg-overlay)', border: '1.5px solid var(--teal-glow)', padding: '10px 20px', borderRadius: '6px', fontSize: '0.8rem', textAlign: 'center', width: '80%', boxShadow: '0 0 10px rgba(0, 240, 200, 0.05)' }}>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontWeight: 'bold' }}>BFF GATEWAY EDGE (8080)</span>
                      <span style={{ display: 'block', fontSize: '0.62rem', color: 'var(--text-secondary)', marginTop: '2px' }}>Session Verification & Trace Interception</span>
                    </div>

                    <div style={{ color: 'var(--teal-glow)', fontSize: '0.9rem' }}>↓</div>

                    {/* Layer 3: Session Cache */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', width: '90%' }}>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', padding: '8px', borderRadius: '6px', fontSize: '0.7rem', textAlign: 'center' }}>
                        <span className="text-mono" style={{ color: 'var(--blue-info)', fontWeight: 'bold' }}>REDIS SESSION</span>
                        <span style={{ display: 'block', fontSize: '0.55rem', color: 'var(--text-muted)' }}>Namespace: transcendence</span>
                      </div>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', padding: '8px', borderRadius: '6px', fontSize: '0.7rem', textAlign: 'center' }}>
                        <span className="text-mono" style={{ color: 'var(--amber-warn)', fontWeight: 'bold' }}>TRANSIT JWT</span>
                        <span style={{ display: 'block', fontSize: '0.55rem', color: 'var(--text-muted)' }}>Signed RSA-256 Envelope</span>
                      </div>
                    </div>

                    <div style={{ color: 'var(--teal-glow)', fontSize: '0.9rem' }}>↓</div>

                    {/* Layer 4: Downstream */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', width: '100%' }}>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--teal-glow)', padding: '10px', borderRadius: '6px', fontSize: '0.72rem', textAlign: 'center' }}>
                        <span style={{ fontWeight: 'bold', display: 'block' }}>AUTH SERVICE</span>
                        <span style={{ display: 'block', fontSize: '0.6rem', color: 'var(--text-muted)' }}>Outbox: Kafka Sync Publisher</span>
                      </div>
                      <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--teal-glow)', padding: '10px', borderRadius: '6px', fontSize: '0.72rem', textAlign: 'center' }}>
                        <span style={{ fontWeight: 'bold', display: 'block' }}>SOCIAL SERVICE</span>
                        <span style={{ display: 'block', fontSize: '0.6rem', color: 'var(--text-muted)' }}>Inbox: profile replica DB</span>
                      </div>
                    </div>
                  </div>

                  <span className="text-label" style={{ marginTop: '10px' }}>INFRASTRUCTURE METADATA</span>
                  <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '6px 10px', fontSize: '0.75rem', background: 'rgba(255,255,255,0.02)', padding: '12px', borderRadius: '6px', border: '1px solid var(--border-subtle)' }}>
                    <span className="text-label">BFF Gateway Origin</span>
                    <span className="text-mono" style={{ color: 'var(--teal-glow)' }}>{gatewayOrigin}</span>
                    
                    <span className="text-label">Sync Pipeline</span>
                    <span className="text-mono">Kafka JSON-serialized Outbox</span>
                  </div>
                </div>

              </div>
            </div>
          )}

        </section>
      </main>
    </div>
  );
}
