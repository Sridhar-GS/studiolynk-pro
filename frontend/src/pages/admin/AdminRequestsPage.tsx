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
import { AdminRequestItem } from '../../types';

export const AdminRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<AdminRequestItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedRequest, setSelectedRequest] = useState<AdminRequestItem | null>(null);

  const fetchRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getRequests();
      setRequests(data);
    } catch (err) {
      console.error('Failed to load requests:', err);
      setError('Unable to load proposals log. Please ensure the backend service is operational.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const filtered = requests.filter((r) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      r.eventName.toLowerCase().includes(term) ||
      r.studioName.toLowerCase().includes(term) ||
      r.freelancerName.toLowerCase().includes(term) ||
      r.freelancerEmail.toLowerCase().includes(term);

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
      case 'PENDING':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'ACCEPTED':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'CONFIRMED':
        return 'bg-teal-500/10 text-teal-400 border-teal-500/30';
      case 'REJECTED':
        return 'bg-slate-800 text-slate-400 border-slate-700';
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
            <h1 className="text-2xl font-bold text-white tracking-tight">Work Proposals & Requests (Monitoring Only)</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-violet-500/10 text-violet-400 border border-violet-500/30">
              {requests.length} Proposals Logged
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            View-only audit log of booking proposals, rate negotiations, and confirmation states (ADM-003)
          </p>
        </div>

        <button
          onClick={fetchRequests}
          disabled={loading}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors w-fit"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Requests</span>
        </button>
      </div>

      {/* View-Only Governance Notice */}
      <div className="p-3.5 bg-slate-900/60 border border-slate-800 rounded-xl text-xs text-slate-400 flex items-center gap-3">
        <ShieldAlert className="w-4 h-4 text-teal-400 shrink-0" />
        <span>
          Proposal negotiations and bookings are governed solely by the counterparties. Administrators maintain <strong>monitoring-only visibility</strong> without intervention capabilities (ADM-003, ADM-004).
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
            placeholder="Search proposals by event, studio, creator, or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400 shrink-0">Request Status:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900/80 border border-slate-800 rounded-lg text-xs text-white px-3 py-2 focus:outline-none focus:border-teal-500 transition-colors"
          >
            <option value="ALL">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="REJECTED">Rejected</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-sm text-slate-400 flex items-center justify-center gap-3">
            <RefreshCw className="w-5 h-5 animate-spin text-teal-400" />
            <span>Loading proposals registry...</span>
          </div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            No work requests match your filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider bg-slate-950/40">
                  <th className="py-3 px-4">Event Shoot</th>
                  <th className="py-3 px-4">Studio</th>
                  <th className="py-3 px-4">Creator Candidate</th>
                  <th className="py-3 px-4">Agreed Price</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Updated</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-xs">
                {filtered.map((r) => (
                  <tr key={r.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">{r.eventName}</div>
                      <div className="text-[11px] text-slate-400">Date: {formatDate(r.eventDate)}</div>
                    </td>

                    <td className="py-3.5 px-4 text-slate-200 font-medium">
                      {r.studioName}
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{r.freelancerName}</div>
                      <div className="text-teal-400 text-[11px] font-mono">{r.freelancerEmail}</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <span className="text-teal-400 font-bold">
                        {r.agreedPrice ? `₹${r.agreedPrice.toLocaleString('en-IN')}` : 'Pending Quote'}
                      </span>
                    </td>

                    <td className="py-3.5 px-4">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${getStatusBadge(r.status)}`}>
                        {r.status}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-slate-400 text-[11px]">
                      {formatDate(r.updatedAt || r.createdAt)}
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => setSelectedRequest(r)}
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
      {selectedRequest && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between sticky top-0 bg-slate-900/95 backdrop-blur-md">
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-base font-bold text-white">Proposal for {selectedRequest.eventName}</h2>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${getStatusBadge(selectedRequest.status)}`}>
                    {selectedRequest.status}
                  </span>
                </div>
                <p className="text-xs text-slate-400">Request ID: #{selectedRequest.id} • Submitted {formatDate(selectedRequest.createdAt)}</p>
              </div>
              <button
                onClick={() => setSelectedRequest(null)}
                className="w-8 h-8 rounded-lg bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 space-y-6 text-xs">
              {/* Proposal Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Originating Studio</span>
                  <p className="text-white font-medium text-sm">{selectedRequest.studioName}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Creator Candidate</span>
                  <p className="text-white font-medium text-sm">{selectedRequest.freelancerName}</p>
                  <p className="text-[11px] font-mono text-teal-400">{selectedRequest.freelancerEmail}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Agreed Price</span>
                  <p className="text-teal-400 font-bold text-base">
                    {selectedRequest.agreedPrice ? `₹${selectedRequest.agreedPrice.toLocaleString('en-IN')}` : 'Not finalized'}
                  </p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Event Date</span>
                  <p className="text-white font-medium text-sm">{formatDate(selectedRequest.eventDate)}</p>
                </div>
              </div>

              {/* Message */}
              {selectedRequest.message && (
                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Studio Proposal Message</span>
                  <p className="text-slate-300 leading-relaxed italic">"{selectedRequest.message}"</p>
                </div>
              )}

              {/* Cancellation Reason if cancelled */}
              {selectedRequest.cancellationReason && (
                <div className="p-4 bg-rose-500/10 rounded-xl border border-rose-500/30 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-rose-400">Cancellation Reason</span>
                  <p className="text-rose-300 leading-relaxed italic">"{selectedRequest.cancellationReason}"</p>
                </div>
              )}

              {/* View-Only Confirmation */}
              <div className="p-3 bg-slate-950/30 rounded-lg border border-slate-800/50 text-[11px] text-slate-400 flex items-center justify-between">
                <span>View-only audit record. No edit or delete capability (ADM-004).</span>
                <button
                  onClick={() => setSelectedRequest(null)}
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
