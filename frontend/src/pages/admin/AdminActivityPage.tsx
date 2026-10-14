import React, { useState, useEffect } from 'react';
import {
  Activity,
  Search,
  RefreshCw,
  AlertCircle,
  ShieldAlert,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminActivityItem } from '../../types';

export const AdminActivityPage: React.FC = () => {
  const [activities, setActivities] = useState<AdminActivityItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('ALL');

  const fetchActivity = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getActivity(50);
      setActivities(data);
    } catch (err) {
      console.error('Failed to load activity stream:', err);
      setError('Unable to load activity audit log. Please ensure the backend service is operational.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivity();
  }, []);

  const filtered = activities.filter((a) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      a.title.toLowerCase().includes(term) ||
      a.description.toLowerCase().includes(term) ||
      a.actorEmail.toLowerCase().includes(term) ||
      a.type.toLowerCase().includes(term);

    const matchesType =
      typeFilter === 'ALL' ||
      (typeFilter === 'IDENTITY' && a.type.includes('IDENTITY')) ||
      (typeFilter === 'REQUEST' && a.type.includes('REQUEST')) ||
      (typeFilter === 'WORK' && a.type.includes('WORK')) ||
      (typeFilter === 'NOTIFICATION' && a.targetEntity === 'NOTIFICATION');

    return matchesSearch && matchesType;
  });

  const formatTimestamp = (ts: string) => {
    try {
      const date = new Date(ts);
      return date.toLocaleString('en-IN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return ts;
    }
  };

  const getEventBadgeColor = (type: string) => {
    if (type.includes('IDENTITY')) {
      return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
    } else if (type.includes('REQUEST') || type.includes('CONFIRMATION')) {
      return 'bg-teal-500/10 text-teal-400 border-teal-500/30';
    } else if (type.includes('WORK') || type.includes('COMPLETED')) {
      return 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30';
    } else if (type.includes('CANCELLED') || type.includes('REJECTED')) {
      return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
    }
    return 'bg-slate-800 text-slate-300 border-slate-700';
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl font-bold text-white tracking-tight">Platform Event Audit Log</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-500/10 text-teal-400 border border-teal-500/30">
              {activities.length} Recorded Events
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            Real-time chronological telemetry capturing bookings, identity compliance declarations, and shoot events (ADM-003)
          </p>
        </div>

        <button
          onClick={fetchActivity}
          disabled={loading}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors w-fit"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Stream</span>
        </button>
      </div>

      {/* Governance Notice */}
      <div className="p-3.5 bg-slate-900/60 border border-slate-800 rounded-xl text-xs text-slate-400 flex items-center gap-3">
        <ShieldAlert className="w-4 h-4 text-teal-400 shrink-0" />
        <span>
          Audit logs are generated automatically by platform state machines. Administrators cannot purge, delete, or fabricate audit events (ADM-003, ADM-004).
        </span>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search events by actor email, title, or keywords..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400 shrink-0">Event Category:</span>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="bg-slate-900/80 border border-slate-800 rounded-lg text-xs text-white px-3 py-2 focus:outline-none focus:border-teal-500 transition-colors"
          >
            <option value="ALL">All Categories</option>
            <option value="REQUEST">Booking & Proposals</option>
            <option value="IDENTITY">Identity Compliance</option>
            <option value="WORK">Shoot Milestones</option>
            <option value="NOTIFICATION">In-App Notifications</option>
          </select>
        </div>
      </div>

      {/* Activity Timeline */}
      <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden divide-y divide-slate-800/60">
        {loading ? (
          <div className="p-12 text-center text-sm text-slate-400 flex items-center justify-center gap-3">
            <RefreshCw className="w-5 h-5 animate-spin text-teal-400" />
            <span>Loading telemetry stream...</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            No audit records match your search criteria.
          </div>
        ) : (
          filtered.map((act) => (
            <div key={act.id} className="p-4 hover:bg-slate-800/30 transition-colors flex items-start gap-4">
              <div className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700/80 flex items-center justify-center text-teal-400 shrink-0 mt-0.5">
                <Activity className="w-4 h-4" />
              </div>

              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-white text-xs">{act.title}</span>
                    <span className={`px-2 py-0.2 rounded text-[10px] font-semibold border ${getEventBadgeColor(act.type)}`}>
                      {act.type}
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-400 font-mono shrink-0">
                    {formatTimestamp(act.timestamp)}
                  </span>
                </div>

                <p className="text-xs text-slate-300 leading-relaxed">{act.description}</p>

                <div className="flex items-center gap-3 text-[11px] text-slate-400 pt-0.5">
                  <span className="font-mono text-teal-400">{act.actorEmail}</span>
                  <span>•</span>
                  <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300 text-[10px] uppercase font-semibold">
                    {act.actorRole}
                  </span>
                  {act.targetEntity && (
                    <>
                      <span>•</span>
                      <span className="text-slate-400">Target: {act.targetEntity} {act.targetId ? `#${act.targetId}` : ''}</span>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
