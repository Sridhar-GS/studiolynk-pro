import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/common/Navbar';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { OnboardingPendingPage } from './pages/onboarding/OnboardingPendingPage';
import { StudioOnboardingPage } from './pages/studio/StudioOnboardingPage';
import { StudioDashboardPage } from './pages/studio/StudioDashboardPage';
import { StudioProfilePage } from './pages/studio/StudioProfilePage';

export const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between selection:bg-teal-500 selection:text-slate-950">
          <Navbar />
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route
              path="/onboarding"
              element={
                <ProtectedRoute requireOnboardingCompleted={false}>
                  <OnboardingPendingPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 4: Studio Onboarding & Profile Routes */}
            <Route
              path="/onboarding/studio"
              element={
                <ProtectedRoute requireOnboardingCompleted={false} allowedRoles={['STUDIO']}>
                  <StudioOnboardingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/studio/dashboard"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/studio/profile"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioProfilePage />
                </ProtectedRoute>
              }
            />
            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </AuthProvider>
    </Router>
  );
};

export default App;
