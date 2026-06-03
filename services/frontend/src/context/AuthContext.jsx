import React, { createContext, useContext, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUsers, logoutUser } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadCurrentUser = useCallback(async () => {
    setLoading(true);
    try {
      const { status, data } = await getUsers();
      if (status === 200) {
        // Since we are validated, store full user profile state
        const profile = { authenticated: true, ...data };
        setUser(profile);
        return profile;
      } else if (status === 401) {
        setUser(null);
      } else if (status === 403) {
        navigate('/mfa-challenge');
      }
    } catch (_) { /* silent */ }
    finally {
      setLoading(false);
    }
    return null;
  }, [navigate]);

  const logout = useCallback(async () => {
    try {
      await logoutUser();
    } catch (_) { /* silent */ }
    setUser(null);
    navigate('/login');
  }, [navigate]);

  // Global session intercept handlers
  const handle401 = useCallback(() => {
    setUser(null);
    navigate('/login');
  }, [navigate]);

  const handle403 = useCallback(() => {
    navigate('/mfa-challenge');
  }, [navigate]);

  return (
    <AuthContext.Provider value={{
      user, setUser,
      loading,
      loadCurrentUser,
      logout,
      handle401,
      handle403,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
