import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Building2,
  Users,
  Briefcase,
  MessageSquare,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  MapPin,
  Edit3,
  Plus,
  Calendar
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { requirementService } from '../../services/requirementService';
import { ApiResponse, StudioProfile, WorkRequirementSummary } from '../../types';

export const StudioDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<StudioProfile | null>(null);
  const [requirements, setRequirements] = useState<WorkRequirementSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profileRes, reqData] = await Promise.all([
          api.get<ApiResponse<StudioProfile>>('/studios/me').catch(() => null),
          requirementService.getMyRequirements().catch(() => [])
        ]);

        if (profileRes?.data?.data) {
          setProfile(profileRes.data.data);
        }
        setRequirements(reqData || []);
      } catch (err) {
        console.warn('Could not load studio dashboard data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
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

  const openCount = requirements.filter((r) => r.status === 'OPEN').length;

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
                  Manage shoot postings, discover available freelancers, and review requirements.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 shrink-0">
              <Link
                to="/studio/requirements/new"
                className="inline-flex items-center gap-1.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 px-4 py-2.5 text-xs font-bold shadow-md shadow-teal-500/10 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>Post Requirement</span>
              </Link>
              <Link
                to="/studio/profile"
                className="inline-flex items-center gap-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 px-3.5 py-2.5 text-xs font-semibold border border-slate-700 transition-colors"
              >
                <Edit3 className="w-3.5 h-3.5" />
                <span>Profile</span>
              </Link>
            </div>
          </div>

          {/* Location & Quick Info */}
          {profile && (
            <div className="mt-6 pt-6 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-teal-400" />
                <span>{profile.address}</span>
              </div>
              <div className="flex items-center gap-4">
                <div>
                  Active Requirements: <strong className="text-white">{openCount}</strong>
                </div>
                <div>
                  Profile Completion: <strong className="text-teal-400">{profile.completionPercentage}%</strong>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Quick Workflow Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          {/* Card 1: Find Freelancers */}
          <Link
            to="/studio/discovery"
            className="bg-slate-900/80 border border-slate-800 hover:border-teal-500/50 rounded-3xl p-6 transition-all duration-200 flex flex-col justify-between group shadow-lg hover:shadow-teal-500/10 cursor-pointer"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/20 group-hover:border-teal-500/40 flex items-center justify-center text-teal-400 mb-4 transition-colors">
                <Users className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2 mb-1">
                <h3 className="text-base font-semibold text-white group-hover:text-teal-400 transition-colors">
                  Discover Freelancers
                </h3>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 font-medium">
                  Active
                </span>
              </div>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Filter verified camera operators, photographers, and drone pilots by skills, equipment, and rolling 10-day availability.
              </p>
            </div>
            <span className="text-xs text-teal-400 font-semibold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>Find Available Creators</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </Link>

          {/* Card 2: Work Requirements (Active in Phase 9) */}
          <Link
            to="/studio/requirements"
            className="bg-slate-900/80 border border-slate-800 hover:border-teal-500/50 rounded-3xl p-6 transition-all duration-200 flex flex-col justify-between group shadow-lg hover:shadow-teal-500/10 cursor-pointer"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/20 group-hover:border-teal-500/40 flex items-center justify-center text-teal-400 mb-4 transition-colors">
                <Briefcase className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2 mb-1">
                <h3 className="text-base font-semibold text-white group-hover:text-teal-400 transition-colors">
                  Work Requirements
                </h3>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 font-medium">
                  Active
                </span>
              </div>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Post shoot dates, specify equipment checklists & skills, and manage event lifecycle from Draft to Completed.
              </p>
            </div>
            <span className="text-xs text-teal-400 font-semibold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>Manage Requirements ({requirements.length})</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </Link>

          {/* Card 3: Messaging (Phase 11 Active) */}
          <Link
            to="/messages"
            className="bg-slate-900/80 border border-slate-800 hover:border-teal-500/50 rounded-3xl p-6 transition-all duration-200 flex flex-col justify-between group shadow-lg hover:shadow-teal-500/10 cursor-pointer"
          >
            <div>
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/20 group-hover:border-teal-500/40 flex items-center justify-center text-teal-400 mb-4 transition-colors">
                <MessageSquare className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2 mb-1">
                <h3 className="text-base font-semibold text-white group-hover:text-teal-400 transition-colors">
                  Direct Messaging
                </h3>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 font-medium">
                  Phase 11 Active
                </span>
              </div>
              <p className="text-xs text-slate-400 mb-4 leading-relaxed">
                Real-time WebSocket chat with applicants to coordinate call times, rate negotiations, and shoot logistics.
              </p>
            </div>
            <span className="text-xs text-teal-400 font-semibold flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>Open Conversations</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </span>
          </Link>
        </div>

        {/* Recent Work Requirements Section */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Briefcase className="w-5 h-5 text-teal-400" />
              <h2 className="text-base font-bold text-white">Recent Work Requirements</h2>
            </div>
            <Link
              to="/studio/requirements"
              className="text-xs text-teal-400 hover:text-teal-300 font-medium flex items-center gap-1"
            >
              <span>View All ({requirements.length})</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {requirements.length === 0 ? (
            <div className="bg-slate-800/40 rounded-2xl p-6 text-center border border-slate-800 space-y-3">
              <p className="text-xs text-slate-400">
                You haven&apos;t posted any shoot requirements yet.
              </p>
              <Link
                to="/studio/requirements/new"
                className="inline-flex items-center gap-1.5 px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 rounded-xl text-xs font-bold transition"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Post Your First Requirement</span>
              </Link>
            </div>
          ) : (
            <div className="divide-y divide-slate-800">
              {requirements.slice(0, 3).map((req) => (
                <div
                  key={req.id}
                  className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 first:pt-0 last:pb-0"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-white hover:text-teal-300 transition">
                        <Link to={`/studio/requirements/${req.id}`}>{req.eventName}</Link>
                      </span>
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                        {req.eventType}
                      </span>
                      <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30">
                        {req.status}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slate-400">
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3 text-teal-400" />
                        {req.eventDate}
                      </span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <MapPin className="w-3 h-3 text-teal-400" />
                        {req.location}
                      </span>
                      <span>•</span>
                      <span className="font-semibold text-slate-200">
                        ₹{req.budget?.toLocaleString()}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() =>
                        navigate(
                          `/studio/discovery?date=${req.eventDate}&startTime=${req.startTime}&endTime=${req.endTime}&dayType=${req.dayType}&maxBudget=${req.budget}`
                        )
                      }
                      className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-teal-400 text-xs font-semibold rounded-xl border border-slate-700 flex items-center gap-1"
                    >
                      <Sparkles className="w-3 h-3" />
                      <span>Find Creators</span>
                    </button>
                    <Link
                      to={`/studio/requirements/${req.id}`}
                      className="px-3 py-1.5 bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold rounded-xl"
                    >
                      View
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Phase 9 Banner */}
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex items-center justify-between text-xs text-slate-400">
          <div className="flex items-center gap-2.5">
            <Sparkles className="w-4 h-4 text-teal-400 shrink-0" />
            <span>
              Phase 9 Active: Work Requirements (WRK-001 - WRK-008), Multi-Stage Shoot Lifecycle, and Privacy-Gated Client Contacts.
            </span>
          </div>
          <Link to="/studio/requirements" className="text-teal-400 hover:text-teal-300 font-medium shrink-0">
            View Requirements &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
};
