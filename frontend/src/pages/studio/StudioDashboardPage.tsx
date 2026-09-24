import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Building2,
  Users,
  Briefcase,
  MessageSquare,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  MapPin,
  Edit3
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { ApiResponse, StudioProfile } from '../../types';

export const StudioDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState<StudioProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStudioProfile = async () => {
      try {
        const res = await api.get<ApiResponse<StudioProfile>>('/studios/me');
        if (res.data?.data) {
          setProfile(res.data.data);
        }
      } catch (err) {
        console.warn('Could not load studio profile for dashboard:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchStudioProfile();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading studio dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Welcome Header */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
                <Building2 className="w-8 h-8" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                    {profile?.studioName || 'Photography Studio Hub'}
                  </h1>
                  <span className="text-xs px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 font-medium flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    Operational
                  </span>
                </div>
                <p className="text-xs sm:text-sm text-slate-400">
                  Welcome back, <strong className="text-slate-200">{profile?.ownerName || user?.email}</strong>.
                  Manage event bookings, search freelancers, and collaborate.
                </p>
              </div>
            </div>

            <Link
              to="/studio/profile"
              className="inline-flex items-center gap-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 px-4 py-2.5 text-xs font-semibold shadow-md shadow-teal-500/10 transition-colors shrink-0"
            >
              <Edit3 className="w-4 h-4" />
              <span>Studio Profile</span>
            </Link>
          </div>

          {/* Location & Quick Info */}
          {profile && (
            <div className="mt-6 pt-6 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-teal-400" />
                <span>{profile.address}</span>
              </div>
              <div className="flex items-center gap-2">
                <span>Profile Completion:</span>
                <span className="font-semibold text-teal-400">{profile.completionPercentage}%</span>
              </div>
            </div>
          )}
        </div>

        {/* Quick Workflow Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          {/* Card 1: Find Freelancers */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 hover:border-slate-700 transition flex flex-col justify-between">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 mb-4">
                <Users className="w-6 h-6" />
              </div>
              <h3 className="text-base font-semibold text-white mb-1">Discover Freelancers</h3>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Filter verified camera operators, photographers, and drone pilots by skills, equipment, and rolling 10-day availability.
              </p>
            </div>
            <span className="text-xs text-teal-400 font-medium flex items-center gap-1 opacity-70">
              <span>Enabled in Phase 8</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </div>

          {/* Card 2: Create Work Requirement */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 hover:border-slate-700 transition flex flex-col justify-between">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400 mb-4">
                <Briefcase className="w-6 h-6" />
              </div>
              <h3 className="text-base font-semibold text-white mb-1">Post Work Requirement</h3>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Publish wedding, commercial, or event dates with skill and equipment needs to receive AI-matched candidates.
              </p>
            </div>
            <span className="text-xs text-blue-400 font-medium flex items-center gap-1 opacity-70">
              <span>Enabled in Phase 9</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </div>

          {/* Card 3: Messaging */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 hover:border-slate-700 transition flex flex-col justify-between">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 mb-4">
                <MessageSquare className="w-6 h-6" />
              </div>
              <h3 className="text-base font-semibold text-white mb-1">Live Messages</h3>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Direct WebSocket chat with applicants to discuss event schedules, pricing negotiations, and equipment specifications.
              </p>
            </div>
            <span className="text-xs text-purple-400 font-medium flex items-center gap-1 opacity-70">
              <span>Enabled in Phase 11</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </div>
        </div>

        {/* Phase 4 Verification Details Banner */}
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex items-center justify-between text-xs text-slate-400">
          <div className="flex items-center gap-2.5">
            <Sparkles className="w-4 h-4 text-teal-400 shrink-0" />
            <span>
              Phase 4 Active: Studio Onboarding, Owner Identity Declaration (STU-002), and Profile Editing (STU-005) verified.
            </span>
          </div>
          <Link to="/studio/profile" className="text-teal-400 hover:text-teal-300 font-medium shrink-0">
            View Full Profile &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
};
