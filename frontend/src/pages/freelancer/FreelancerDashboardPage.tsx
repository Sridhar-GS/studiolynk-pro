import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  User,
  Camera,
  MapPin,
  Calendar,
  Briefcase,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  Edit3,
  Award,
  Wrench,
  DollarSign
} from 'lucide-react';
import { freelancerService } from '../../services/freelancerService';
import { FreelancerProfile } from '../../types';

export const FreelancerDashboardPage: React.FC = () => {
  const [profile, setProfile] = useState<FreelancerProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await freelancerService.getProfile();
        setProfile(data);
      } catch (err) {
        console.warn('Could not load freelancer profile for dashboard:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading creator dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Welcome Header */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="absolute -right-20 -top-20 w-60 h-60 bg-teal-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
                <Camera className="w-8 h-8" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h1 className="text-2xl font-black text-white tracking-tight">
                    {profile?.fullName || 'Freelancer Creator'}
                  </h1>
                  <span className="px-2.5 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 text-[10px] font-semibold flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3" /> Active Creator
                  </span>
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-400">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5 text-teal-400" />
                    {profile?.address || 'Operating Location'}
                  </span>
                  <span>&bull;</span>
                  <span>{profile?.experienceYears || 0} Years Exp</span>
                </div>
              </div>
            </div>

            <Link
              to="/freelancer/profile"
              className="flex items-center gap-2 text-xs font-bold text-slate-950 bg-teal-400 hover:bg-teal-300 px-5 py-2.5 rounded-xl shadow-lg shadow-teal-500/20 transition-all self-start sm:self-auto"
            >
              <Edit3 className="w-3.5 h-3.5" />
              <span>Manage Profile</span>
            </Link>
          </div>
        </div>

        {/* Quick Stats Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-1">
            <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
              <span>Full-Day Rate</span>
              <DollarSign className="w-4 h-4 text-teal-400" />
            </div>
            <div className="text-xl font-extrabold text-white">₹{profile?.fullDayRate || 0}</div>
            <p className="text-[11px] text-slate-500">Standard 8h shoot</p>
          </div>

          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-1">
            <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
              <span>Half-Day Rate</span>
              <DollarSign className="w-4 h-4 text-teal-400" />
            </div>
            <div className="text-xl font-extrabold text-white">₹{profile?.halfDayRate || 0}</div>
            <p className="text-[11px] text-slate-500">Standard 4h shoot</p>
          </div>

          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-1">
            <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
              <span>Skills Active</span>
              <Award className="w-4 h-4 text-teal-400" />
            </div>
            <div className="text-xl font-extrabold text-white">{profile?.skills?.length || 0}</div>
            <p className="text-[11px] text-slate-500">Specializations</p>
          </div>

          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 space-y-1">
            <div className="flex items-center justify-between text-slate-400 text-xs font-semibold uppercase">
              <span>Gear Arsenal</span>
              <Wrench className="w-4 h-4 text-teal-400" />
            </div>
            <div className="text-xl font-extrabold text-white">{profile?.equipment?.length || 0}</div>
            <p className="text-[11px] text-slate-500">Listed equipment</p>
          </div>
        </div>

        {/* Feature Modules Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Card 1: Profile & Gear */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4 flex flex-col justify-between">
            <div className="space-y-3">
              <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                <User className="w-5 h-5" />
              </div>
              <h2 className="text-base font-bold text-white">Creator Profile &amp; Gear Arsenal</h2>
              <p className="text-xs text-slate-400 leading-relaxed">
                Update your contact details, day rates, photographic skills, and camera equipment list so studios can evaluate your capabilities.
              </p>
            </div>
            <Link
              to="/freelancer/profile"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-teal-400 hover:text-teal-300 pt-2"
            >
              <span>Edit Profile &amp; Rates</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {/* Card 2: AWS S3 Portfolio (Phase 6) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4 flex flex-col justify-between">
            <div className="space-y-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
                <Sparkles className="w-5 h-5" />
              </div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white">Portfolio Showcase</h2>
                <span className="px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 text-[10px] font-semibold">
                  Phase 6
                </span>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                High-resolution image galleries and YouTube/Vimeo reel embedding powered by secure AWS S3 storage.
              </p>
            </div>
            <span className="text-xs text-slate-500 pt-2">Unlocks in Phase 6</span>
          </div>

          {/* Card 3: 10-Day Availability (Phase 8) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4 flex flex-col justify-between">
            <div className="space-y-3">
              <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
                <Calendar className="w-5 h-5" />
              </div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white">10-Day Availability Calendar</h2>
                <span className="px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 text-[10px] font-semibold">
                  Phase 8
                </span>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Keep your shoot calendar updated. Studios filter available talent across the next 10 consecutive days.
              </p>
            </div>
            <span className="text-xs text-slate-500 pt-2">Unlocks in Phase 8</span>
          </div>

          {/* Card 4: Studio Discovery & Bookings (Phase 7 & 9) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4 flex flex-col justify-between">
            <div className="space-y-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                <Briefcase className="w-5 h-5" />
              </div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-white">Studio Inquiries &amp; Bookings</h2>
                <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 text-[10px] font-semibold">
                  Phase 9
                </span>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Receive direct booking requests and chat with studio owners in real-time over WebSocket/STOMP.
              </p>
            </div>
            <span className="text-xs text-slate-500 pt-2">Unlocks in Phase 9</span>
          </div>
        </div>
      </div>
    </div>
  );
};
