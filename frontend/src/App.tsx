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
import { StudioDiscoveryPage } from './pages/studio/StudioDiscoveryPage';
import { StudioRequirementsPage } from './pages/studio/StudioRequirementsPage';
import { StudioCreateRequirementPage } from './pages/studio/StudioCreateRequirementPage';
import { StudioRequirementDetailPage } from './pages/studio/StudioRequirementDetailPage';
import { StudioRequestsPage } from './pages/studio/StudioRequestsPage';
import { FreelancerOnboardingPage } from './pages/freelancer/FreelancerOnboardingPage';
import { FreelancerDashboardPage } from './pages/freelancer/FreelancerDashboardPage';
import { FreelancerProfilePage } from './pages/freelancer/FreelancerProfilePage';
import { FreelancerPortfolioPage } from './pages/freelancer/FreelancerPortfolioPage';
import { FreelancerAvailabilityPage } from './pages/freelancer/FreelancerAvailabilityPage';
import { FreelancerRequestsPage } from './pages/freelancer/FreelancerRequestsPage';
import { FreelancerRequestDetailPage } from './pages/freelancer/FreelancerRequestDetailPage';
import { MessagesPage } from './pages/messaging/MessagesPage';

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
            {/* Phase 8: Studio Discovery Route (DIS-001 - DIS-006) */}
            <Route
              path="/studio/discovery"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioDiscoveryPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 9: Studio Work Requirements Routes (WRK-001 - WRK-008) */}
            <Route
              path="/studio/requirements"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioRequirementsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/studio/requirements/new"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioCreateRequirementPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/studio/requirements/:id"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioRequirementDetailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/studio/requirements/:id/edit"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioCreateRequirementPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 10: Studio Work Requests Route (WRK-006 - WRK-008, REQ-001 - REQ-009) */}
            <Route
              path="/studio/requests"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'ADMIN']}>
                  <StudioRequestsPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 5: Freelancer Onboarding & Profile Routes */}
            <Route
              path="/onboarding/freelancer"
              element={
                <ProtectedRoute requireOnboardingCompleted={false} allowedRoles={['FREELANCER']}>
                  <FreelancerOnboardingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/freelancer/dashboard"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/freelancer/profile"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerProfilePage />
                </ProtectedRoute>
              }
            />
            {/* Phase 6: Portfolio Showcase Route */}
            <Route
              path="/freelancer/portfolio"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerPortfolioPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 7: 10-Day Availability Route */}
            <Route
              path="/freelancer/availability"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerAvailabilityPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 10: Freelancer Work Requests Routes (REQ-001 - REQ-009) */}
            <Route
              path="/freelancer/requests"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerRequestsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/freelancer/requests/:id"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['FREELANCER', 'ADMIN']}>
                  <FreelancerRequestDetailPage />
                </ProtectedRoute>
              }
            />
            {/* Phase 11: Real-Time WebSocket Messaging (MSG-001 - MSG-006) */}
            <Route
              path="/messages"
              element={
                <ProtectedRoute requireOnboardingCompleted={true} allowedRoles={['STUDIO', 'FREELANCER', 'ADMIN']}>
                  <MessagesPage />
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
