import React, { useState, useEffect } from 'react';
import {
  Search,
  RefreshCw,
  Eye,
  X,
  AlertCircle,
  ShieldAlert,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminRequirementItem } from '../../types';

export const AdminRequirementsPage: React.FC = () => {
  const [requirements, setRequirements] = useState<AdminRequirementItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedReq, setSelectedReq] = useState<AdminRequirementItem | null>(null);

  const fetchRequirements = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getRequirements();
      setRequirements(data);
    } catch (err) {
      console.error('Failed to load requirements:', err);
      setError('Unable to load requirements log. Please ensure the backend service is operational.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequirements();
  }, []);

  const filtered = requirements.filter((r) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      r.eventName.toLowerCase().includes(term) ||
      r.studioName.toLowerCase().includes(term) ||
      r.location.toLowerCase().includes(term) ||
      r.eventType.toLowerCase().includes(term);

    const matchesStatus =
      statusFilter === 'ALL' || r.status.toUpperCase() === statusFilter.toUpperCase();

    return matchesSearch && matchesStatus;
  });

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status.toUpperCase()) {
      case 'OPEN':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'CONFIRMED':
        return 'bg-teal-500/10 text-teal-400 border-teal-500/30';
      case 'COMPLETED':
        return 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30';
      case 'CANCELLED':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl font-bold text-white tracking-tight">Work Requirements (Monitoring Only)</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/30">
              {requirements.length} Total Shoots
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            View-only audit log of studio work requirements, shoot criteria, and booking status (ADM-003)
          </p>
        </div>

        <button
          onClick={fetchRequirements}
          disabled={loading}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors w-fit"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Requirements</span>
        </button>
      </div>

      {/* View-Only Governance Notice */}
      <div className="p-3.5 bg-slate-900/60 border border-slate-800 rounded-xl text-xs text-slate-400 flex items-center gap-3">
        <ShieldAlert className="w-4 h-4 text-teal-400 shrink-0" />
        <span>
          Requirements are managed directly between Studios and Creators. Administrators have <strong>monitoring-only visibility</strong> with no editing or cancellation powers (ADM-003, ADM-004).
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
            placeholder="Search requirements by event title, studio, venue, or type..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400 shrink-0">Lifecycle Status:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900/80 border border-slate-800 rounded-lg text-xs text-white px-3 py-2 focus:outline-none focus:border-teal-500 transition-colors"
          >
            <option value="ALL">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="REQUESTED">Requested</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-sm text-slate-400 flex items-center justify-center gap-3">
            <RefreshCw className="w-5 h-5 animate-spin text-teal-400" />
            <span>Loading requirements log...</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            No requirements match your filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider bg-slate-950/40">
                  <th className="py-3 px-4">Shoot Event</th>
                  <th className="py-3 px-4">Studio</th>
                  <th className="py-3 px-4">Date & Call Times</th>
                  <th className="py-3 px-4">Location</th>
                  <th className="py-3 px-4">Budget</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-xs">
                {filtered.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">{r.eventName}</div>
                      <div className="text-[11px] text-slate-400">{r.eventType} • {r.dayType}</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{r.studioName}</div>
                      <div className="text-slate-400 text-[11px] font-mono">{r.studioEmail}</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{formatDate(r.eventDate)}</div>
                      <div className="text-[11px] text-slate-400">
                        {r.startTime.substring(0, 5)} - {r.endTime.substring(0, 5)}
                      </div>
                    </td>

                    <td className="py-3.5 px-4 text-slate-300 max-w-xs truncate">
                      {r.location}
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-teal-400 font-bold">₹{r.budget.toLocaleString('en-IN')}</div>
                      <div className="text-[11px] text-slate-400">{r.requestCount} proposals</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${getStatusBadge(r.status)}`}>
                        {r.status}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => setSelectedReq(r)}
                        className="inline-flex items-center gap-1 px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Inspect</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Detail Inspection Modal */}
      {selectedReq && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between sticky top-0 bg-slate-900/95 backdrop-blur-md">
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-base font-bold text-white">{selectedReq.eventName}</h2>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${getStatusBadge(selectedReq.status)}`}>
                    {selectedReq.status}
                  </span>
                </div>
                <p className="text-xs text-slate-400">Requirement ID: #{selectedReq.id} • Created {formatDate(selectedReq.createdAt)}</p>
              </div>
              <button
                onClick={() => setSelectedReq(null)}
                className="w-8 h-8 rounded-lg bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 space-y-6 text-xs">
              {/* Event & Logistics Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Studio</span>
                  <p className="text-white font-medium text-sm">{selectedReq.studioName}</p>
                  <p className="text-[11px] font-mono text-teal-400">{selectedReq.studioEmail}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Budget & Type</span>
                  <p className="text-teal-400 font-bold text-sm">₹{selectedReq.budget.toLocaleString('en-IN')}</p>
                  <p className="text-[11px] text-slate-400">{selectedReq.eventType} • {selectedReq.dayType}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Shoot Date</span>
                  <p className="text-white font-medium text-sm">{formatDate(selectedReq.eventDate)}</p>
                  <p className="text-[11px] text-slate-400">
                    Call: {selectedReq.startTime.substring(0, 5)} - {selectedReq.endTime.substring(0, 5)}
                  </p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Confirmed Creator</span>
                  <p className="text-white font-medium text-sm">
                    {selectedReq.confirmedFreelancerName || 'None assigned yet'}
                  </p>
                  <p className="text-[11px] text-slate-400">{selectedReq.requestCount} proposals received</p>
                </div>
              </div>

              {/* Location */}
              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                <span className="text-[10px] uppercase font-semibold text-slate-400">Venue Location</span>
                <p className="text-slate-200 leading-relaxed">{selectedReq.location}</p>
              </div>

              {/* Creative Brief */}
              {selectedReq.description && (
                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Shoot Description / Brief</span>
                  <p className="text-slate-300 leading-relaxed italic">"{selectedReq.description}"</p>
                </div>
              )}

              {/* Required Skills & Services */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Required Skills</span>
                  <div className="flex flex-wrap gap-1.5">
                    {selectedReq.requiredSkills.map((s, idx) => (
                      <span key={idx} className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 text-xs">
                        {s}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Required Services</span>
                  <div className="flex flex-wrap gap-1.5">
                    {selectedReq.requiredServices.map((s, idx) => (
                      <span key={idx} className="px-2 py-0.5 rounded bg-amber-500/10 text-amber-300 text-xs">
                        {s}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              {/* View-Only Confirmation */}
              <div className="p-3 bg-slate-950/30 rounded-lg border border-slate-800/50 text-[11px] text-slate-400 flex items-center justify-between">
                <span>View-only audit record. No edit or delete capability (ADM-004).</span>
                <button
                  onClick={() => setSelectedReq(null)}
                  className="px-3 py-1.5 rounded-lg bg-slate-800 text-white font-medium hover:bg-slate-700 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
