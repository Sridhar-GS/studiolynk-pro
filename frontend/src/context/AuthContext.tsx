import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { UserSummary, AuthResponse, ApiResponse, UserRole } from '../types';

interface AuthContextType {
  user: UserSummary | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<UserSummary>;
  register: (email: string, password: string, role: UserRole) => Promise<UserSummary>;
  logout: () => void;
  refreshUser: () => Promise<UserSummary | null>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('studiolynk_token'));
  const [user, setUser] = useState<UserSummary | null>(() => {
    const saved = localStorage.getItem('studiolynk_user');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        return null;
      }
    }
    return null;
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const refreshUser = useCallback(async (): Promise<UserSummary | null> => {
    const currentToken = localStorage.getItem('studiolynk_token');
    if (!currentToken) {
      setUser(null);
      setToken(null);
      setIsLoading(false);
      return null;
    }

    try {
      const response = await api.get<ApiResponse<UserSummary>>('/auth/me');
      if (response.data && response.data.success && response.data.data) {
        const freshUser = response.data.data;
        setUser(freshUser);
        localStorage.setItem('studiolynk_user', JSON.stringify(freshUser));
        return freshUser;
      }
      return null;
    } catch (err) {
      console.warn('Failed to hydrate user session:', err);
      localStorage.removeItem('studiolynk_token');
      localStorage.removeItem('studiolynk_user');
      setUser(null);
      setToken(null);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = async (email: string, password: string): Promise<UserSummary> => {
    setIsLoading(true);
    try {
      const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });
      const authData = res.data.data;
      setToken(authData.token);
      setUser(authData.user);
      localStorage.setItem('studiolynk_token', authData.token);
      localStorage.setItem('studiolynk_user', JSON.stringify(authData.user));
      return authData.user;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (email: string, password: string, role: UserRole): Promise<UserSummary> => {
    setIsLoading(true);
    try {
      const res = await api.post<ApiResponse<AuthResponse>>('/auth/register', { email, password, role });
      const authData = res.data.data;
      setToken(authData.token);
      setUser(authData.user);
      localStorage.setItem('studiolynk_token', authData.token);
      localStorage.setItem('studiolynk_user', JSON.stringify(authData.user));
      return authData.user;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('studiolynk_token');
    localStorage.removeItem('studiolynk_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
