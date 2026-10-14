import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Building2,
  Users,
  Briefcase,
  Inbox,
  Star,
  ShieldCheck,
  Activity,
  ArrowRight,
  RefreshCw,
  FileCheck,
  AlertCircle,
  ExternalLink,
  ShieldAlert,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminDashboardMetrics } from '../../types';

export const AdminDashboardPage: React.FC = () => {
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMetrics = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getDashboardMetrics();
      setMetrics(data);
    } catch (err) {
      console.error('Failed to load admin metrics:', err);
      setError('Unable to load dashboard telemetry. Please ensure the backend service is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
  }, []);

  const formatTime = (ts: string) => {
    try {
      const date = new Date(ts);
      return date.toLocaleString('en-IN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return ts;
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Platform Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl font-bold text-white tracking-tight">Admin Operations & Monitoring</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-500/10 text-teal-400 border border-teal-500/30">
              Phase 16 View-Only
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            Real-time operational metrics, identity compliance monitoring, and activity audit feed
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={fetchMetrics}
            disabled={loading}
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh Telemetry</span>
          </button>
        </div>
      </div>

      {/* Strict View-Only Architecture Banner (ADM-003, ADM-004, ADM-006) */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-4.5 flex items-start gap-3.5 text-xs text-slate-300">
        <ShieldAlert className="w-5 h-5 text-teal-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <span className="font-semibold text-white">Monitoring & Governance Policy (ADM-003, ADM-004, ADM-006)</span>
          <p className="text-slate-400 leading-relaxed">
            StudioLynk enforces decentralized autonomy: Administrators operate strictly in a <strong>read-only monitoring capacity</strong>. Normal users do not require administrative approval gates to onboard, publish requirements, or book creators. Platform entity alteration and deletion endpoints are strictly non-existent.
          </p>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading && !metrics ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-32 bg-slate-900/60 rounded-xl border border-slate-800" />
          ))}
        </div>
      ) : metrics ? (
        <>
          {/* Primary KPI Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Studios */}
            <Link
              to="/admin/studios"
              className="group p-5 bg-slate-900/60 border border-slate-800 hover:border-teal-500/40 rounded-xl transition-all duration-200"
            >
              <div className="flex items-center justify-between text-slate-400">
                <span className="text-xs font-semibold uppercase tracking-wider">Studios</span>
                <div className="w-8 h-8 rounded-lg bg-teal-500/10 flex items-center justify-center text-teal-400 group-hover:scale-105 transition-transform">
                  <Building2 className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-white">{metrics.totalStudios}</span>
                <span className="text-xs text-teal-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                  View directory <ArrowRight className="w-3 h-3" />
                </span>
              </div>
              <div className="mt-2 text-[11px] text-slate-400">
                {metrics.activeUsers} total onboarded users on network
              </div>
            </Link>

            {/* Freelancers */}
            <Link
              to="/admin/freelancers"
              className="group p-5 bg-slate-900/60 border border-slate-800 hover:border-teal-500/40 rounded-xl transition-all duration-200"
            >
              <div className="flex items-center justify-between text-slate-400">
                <span className="text-xs font-semibold uppercase tracking-wider">Creators</span>
                <div className="w-8 h-8 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 group-hover:scale-105 transition-transform">
                  <Users className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-white">{metrics.totalFreelancers}</span>
                <span className="text-xs text-indigo-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                  View directory <ArrowRight className="w-3 h-3" />
                </span>
              </div>
              <div className="mt-2 text-[11px] text-slate-400">
                Photographers, videographers, editors
              </div>
            </Link>

            {/* Requirements */}
            <Link
              to="/admin/requirements"
              className="group p-5 bg-slate-900/60 border border-slate-800 hover:border-teal-500/40 rounded-xl transition-all duration-200"
            >
              <div className="flex items-center justify-between text-slate-400">
                <span className="text-xs font-semibold uppercase tracking-wider">Shoot Requirements</span>
                <div className="w-8 h-8 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-400 group-hover:scale-105 transition-transform">
                  <Briefcase className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-white">{metrics.totalRequirements}</span>
                <span className="text-xs text-amber-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                  Inspect <ArrowRight className="w-3 h-3" />
                </span>
              </div>
              <div className="mt-2 flex items-center gap-2 text-[11px] text-slate-400">
                <span className="text-emerald-400 font-medium">{metrics.openRequirements} open</span>
                <span>•</span>
                <span className="text-teal-400 font-medium">{metrics.confirmedRequirements} confirmed</span>
              </div>
            </Link>

            {/* Work Requests */}
            <Link
              to="/admin/requests"
              className="group p-5 bg-slate-900/60 border border-slate-800 hover:border-teal-500/40 rounded-xl transition-all duration-200"
            >
              <div className="flex items-center justify-between text-slate-400">
                <span className="text-xs font-semibold uppercase tracking-wider">Proposals & Bookings</span>
                <div className="w-8 h-8 rounded-lg bg-violet-500/10 flex items-center justify-center text-violet-400 group-hover:scale-105 transition-transform">
                  <Inbox className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-4 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-white">{metrics.totalRequests}</span>
                <span className="text-xs text-violet-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                  Inspect <ArrowRight className="w-3 h-3" />
                </span>
              </div>
              <div className="mt-2 flex items-center gap-2 text-[11px] text-slate-400">
                <span className="text-amber-400 font-medium">{metrics.pendingRequests} pending</span>
                <span>•</span>
                <span className="text-teal-400 font-medium">{metrics.confirmedRequests} confirmed</span>
              </div>
            </Link>
          </div>

          {/* Secondary Metric Bar */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 bg-slate-900/40 border border-slate-800/80 rounded-xl flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center text-amber-400 shrink-0">
                <Star className="w-5 h-5 fill-amber-400" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xl font-bold text-white">{metrics.averagePlatformRating.toFixed(1)} / 5.0</span>
                  <span className="text-xs text-slate-400">({metrics.totalRatings} ratings)</span>
                </div>
                <p className="text-xs text-slate-400 mt-0.5">Average mutual review score across shoots</p>
              </div>
            </div>

            <div className="p-4 bg-slate-900/40 border border-slate-800/80 rounded-xl flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-400 shrink-0">
                <FileCheck className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xl font-bold text-white">{metrics.totalIdentitySubmissions}</span>
                  <span className="text-xs text-emerald-400 font-medium">Compliance Declared</span>
                </div>
                <p className="text-xs text-slate-400 mt-0.5">Studio owner declarations submitted (ADM-005)</p>
              </div>
            </div>

            <div className="p-4 bg-slate-900/40 border border-slate-800/80 rounded-xl flex items-center gap-4">
              <div className="w-10 h-10 rounded-lg bg-teal-500/10 flex items-center justify-center text-teal-400 shrink-0">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xl font-bold text-white">{metrics.completedRequirements}</span>
                  <span className="text-xs text-teal-400 font-medium">Completed Shoots</span>
                </div>
                <p className="text-xs text-slate-400 mt-0.5">Successfully concluded production events</p>
              </div>
            </div>
          </div>

          {/* Two-Column Telemetry Stream */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Identity Submissions Monitoring (ADM-005) */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden flex flex-col justify-between">
              <div>
                <div className="px-5 py-4 border-b border-slate-800 flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <FileCheck className="w-4 h-4 text-emerald-400" />
                    <h2 className="text-sm font-semibold text-white">Studio Identity Submissions (ADM-005)</h2>
                  </div>
                  <Link
                    to="/admin/studios"
                    className="text-xs text-teal-400 hover:text-teal-300 font-medium flex items-center gap-1 transition-colors"
                  >
                    <span>All Studios</span>
                    <ArrowRight className="w-3 h-3" />
                  </Link>
                </div>

                <div className="divide-y divide-slate-800/60">
                  {metrics.recentIdentitySubmissions && metrics.recentIdentitySubmissions.length > 0 ? (
                    metrics.recentIdentitySubmissions.map((sub) => (
                      <div key={sub.id} className="p-4 hover:bg-slate-800/30 transition-colors space-y-2">
                        <div className="flex items-center justify-between">
                          <div>
                            <span className="font-semibold text-white text-xs">{sub.studioName}</span>
                            <span className="text-slate-400 text-xs ml-2">by {sub.ownerName}</span>
                          </div>
                          <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                            {sub.status}
                          </span>
                        </div>

                        <div className="flex items-center gap-3 text-xs text-slate-400">
                          <span className="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px]">
                            {sub.documentType}
                          </span>
                          <span>{sub.studioEmail}</span>
                          <span className="ml-auto text-[11px] text-slate-400">
                            {formatTime(sub.submittedAt)}
                          </span>
                        </div>

                        {sub.declarationText && (
                          <p className="text-[11px] text-slate-400 bg-slate-950/50 p-2 rounded border border-slate-800/60 italic line-clamp-2">
                            "{sub.declarationText}"
                          </p>
                        )}

                        {sub.documentUrl && (
                          <a
                            href={sub.documentUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="inline-flex items-center gap-1 text-[11px] text-teal-400 hover:text-teal-300"
                          >
                            <span>Inspect attached document</span>
                            <ExternalLink className="w-3 h-3" />
                          </a>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="p-8 text-center text-xs text-slate-400">
                      No studio identity declarations recorded yet.
                    </div>
                  )}
                </div>
              </div>

              <div className="p-3 bg-slate-950/40 border-t border-slate-800/60 text-center">
                <span className="text-[11px] text-slate-400">
                  ADM-005 Compliance: Identity declarations recorded for regulatory and audit tracking without manual gating.
                </span>
              </div>
            </div>

            {/* Platform Activity Audit Stream (ADM-003) */}
            <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden flex flex-col justify-between">
              <div>
                <div className="px-5 py-4 border-b border-slate-800 flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <Activity className="w-4 h-4 text-teal-400" />
                    <h2 className="text-sm font-semibold text-white">Platform Event Audit Log</h2>
                  </div>
                  <Link
                    to="/admin/activity"
                    className="text-xs text-teal-400 hover:text-teal-300 font-medium flex items-center gap-1 transition-colors"
                  >
                    <span>Full Log</span>
                    <ArrowRight className="w-3 h-3" />
                  </Link>
                </div>

                <div className="divide-y divide-slate-800/60">
                  {metrics.recentActivity && metrics.recentActivity.length > 0 ? (
                    metrics.recentActivity.map((act) => (
                      <div key={act.id} className="p-3.5 hover:bg-slate-800/30 transition-colors flex items-start gap-3">
                        <div className="w-2 h-2 rounded-full bg-teal-400 mt-1.5 shrink-0" />
                        <div className="flex-1 min-w-0 space-y-0.5">
                          <div className="flex items-center justify-between gap-2">
                            <span className="text-xs font-semibold text-white truncate">{act.title}</span>
                            <span className="text-[10px] text-slate-400 shrink-0">{formatTime(act.timestamp)}</span>
                          </div>
                          <p className="text-xs text-slate-400 line-clamp-1">{act.description}</p>
                          <div className="flex items-center gap-2 text-[10px] text-slate-400">
                            <span className="text-teal-400 font-mono">{act.actorEmail}</span>
                            <span>•</span>
                            <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300 uppercase">
                              {act.actorRole}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="p-8 text-center text-xs text-slate-400">
                      No platform events recorded yet.
                    </div>
                  )}
                </div>
              </div>

              <div className="p-3 bg-slate-950/40 border-t border-slate-800/60 text-center">
                <span className="text-[11px] text-slate-400">
                  Unified event stream capturing bookings, proposals, messaging events, and reviews.
                </span>
              </div>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};
