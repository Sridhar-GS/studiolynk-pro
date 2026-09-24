import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Camera, Building2, UserCheck, LogOut, CheckCircle2, Clock, Sparkles } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { ApiResponse, OnboardingStatusResponse } from '../../types';

export const OnboardingPendingPage: React.FC = () => {
  const { user, logout, refreshUser } = useAuth();
  const [statusData, setStatusData] = useState<OnboardingStatusResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const res = await api.get<ApiResponse<OnboardingStatusResponse>>('/onboarding/status');
        if (res.data?.data) {
          setStatusData(res.data.data);
          // If already completed in backend, refresh user and redirect
          if (res.data.data.onboardingCompleted) {
            await refreshUser();
            navigate('/', { replace: true });
          }
        }
      } catch (err) {
        console.warn('Could not fetch onboarding status:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchStatus();
  }, [refreshUser, navigate]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading onboarding status...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-between">
      {/* Top Bar */}
      <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
            <Camera className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-white">StudioLynk Onboarding Gate</h1>
            <p className="text-xs text-slate-400">Step 2: Profile &amp; Role Setup</p>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-rose-400 bg-slate-800/80 hover:bg-rose-500/10 border border-slate-700 hover:border-rose-500/30 px-3 py-1.5 rounded-lg transition-colors"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Save &amp; Sign Out</span>
        </button>
      </header>

      {/* Main Container */}
      <main className="max-w-3xl mx-auto px-6 py-12 flex-1 flex flex-col justify-center">
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-8 shadow-2xl relative overflow-hidden backdrop-blur-sm">
          {/* Subtle background glow */}
          <div className="absolute -right-20 -top-20 w-60 h-60 bg-teal-500/10 rounded-full blur-3xl pointer-events-none" />

          {/* Header Badge */}
          <div className="flex items-center gap-2 mb-6">
            <span className="flex items-center gap-1.5 text-xs font-semibold px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300">
              <Clock className="w-3.5 h-3.5" />
              Onboarding Pending (ONB-002)
            </span>
            <span className="text-xs text-slate-400">
              Account: <strong className="text-slate-200">{user?.email}</strong>
            </span>
          </div>

          <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight mb-3">
            Welcome to StudioLynk,{' '}
            <span className="text-teal-400">
              {user?.role === 'STUDIO' ? 'Photography Studio' : 'Creative Freelancer'}
            </span>
          </h2>

          <p className="text-slate-400 text-sm leading-relaxed mb-8">
            Your credentials have been securely verified and role assigned. To ensure the highest standard
            of quality for event organizers and photography professionals, StudioLynk requires a one-time
            onboarding setup before access to the main discovery and booking platform is unlocked.
          </p>

          {/* Role Setup Box */}
          {user?.role === 'STUDIO' ? (
            <div className="bg-slate-950/70 border border-slate-800 rounded-2xl p-6 mb-8">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400">
                  <Building2 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-semibold text-white">Studio Profile Requirements</h3>
                  <p className="text-xs text-slate-400">Configured in upcoming Phase 4</p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-300">
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Studio Business Name &amp; Bio</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Physical Location &amp; Coordinates</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Social Links &amp; Portfolio Site</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Identity / Business Verification Doc</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-slate-950/70 border border-slate-800 rounded-2xl p-6 mb-8">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400">
                  <UserCheck className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-semibold text-white">Freelancer Profile Requirements</h3>
                  <p className="text-xs text-slate-400">Configured in upcoming Phase 5</p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-300">
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Photography Skills &amp; Services</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Camera, Lens &amp; Lighting Equipment</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Full-Day &amp; Half-Day Pricing</span>
                </div>
                <div className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800">
                  <CheckCircle2 className="w-4 h-4 text-teal-400 shrink-0" />
                  <span>Location &amp; 10-Day Availability</span>
                </div>
              </div>
            </div>
          )}

          {/* Action Box */}
          <div className="p-4 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <Sparkles className="w-5 h-5 text-teal-400 shrink-0" />
              <div className="text-left text-xs">
                <span className="font-semibold text-white block">
                  Onboarding Gate Operational
                </span>
                <span className="text-slate-400">
                  {statusData ? statusData.message : 'Awaiting Phase 4/5 onboarding form deployment.'}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 shrink-0">
              <button
                type="button"
                onClick={handleLogout}
                className="text-xs text-slate-300 hover:text-white bg-slate-800 px-3.5 py-2 rounded-xl border border-slate-700 transition-colors"
              >
                Sign Out (Resume Later)
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 px-6 py-4 text-center text-xs text-slate-500">
        StudioLynk Security Gate &bull; ONB-001 &amp; ONB-002 Enforced
      </footer>
    </div>
  );
};
