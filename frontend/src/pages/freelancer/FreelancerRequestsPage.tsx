import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Inbox,
  Calendar,
  Clock,
  MapPin,
  CheckCircle2,
  XCircle,
  Clock3,
  ChevronRight,
  ShieldCheck,
  Building2,
  MessageSquare
} from 'lucide-react';
import { requestService } from '../../services/requestService';
import { RequestStatus, WorkRequestSummary } from '../../types';

export const FreelancerRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<WorkRequestSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<RequestStatus | 'ALL'>('ALL');
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);

  const fetchRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const filter = statusFilter === 'ALL' ? undefined : statusFilter;
      const data = await requestService.getFreelancerRequests(filter);
      setRequests(data || []);
    } catch (err: any) {
      console.error('Failed to load freelancer work requests:', err);
      setError('Unable to load work requests. Please refresh.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  const handleAccept = async (id: number) => {
    setActionLoadingId(id);
    try {
      await requestService.acceptRequest(id);
      fetchRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to accept work request.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleReject = async (id: number) => {
    const reason = prompt('Please enter a brief reason for declining this request (optional):');
    if (reason === null) return; // User cancelled prompt

    setActionLoadingId(id);
    try {
      await requestService.rejectRequest(id, reason || undefined);
      fetchRequests();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to decline work request.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const getStatusBadge = (status: RequestStatus) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30 text-[11px] font-semibold">
            <Clock3 className="w-3 h-3" />
            <span>Action Required</span>
          </span>
        );
      case 'ACCEPTED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/30 text-[11px] font-semibold">
            <CheckCircle2 className="w-3 h-3" />
            <span>Accepted (Awaiting Studio Confirmation)</span>
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30 text-[11px] font-semibold">
            <CheckCircle2 className="w-3 h-3" />
            <span>Confirmed Booking</span>
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 text-[11px] font-semibold">
            <XCircle className="w-3 h-3" />
            <span>Declined</span>
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700 text-[11px] font-semibold">
            <XCircle className="w-3 h-3" />
            <span>Cancelled</span>
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Header Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
              <Inbox className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                  PHASE 10: WORK REQUESTS (REQ-001 - REQ-009)
                </span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Work Requests Inbox
              </h1>
              <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
                Review shoot opportunities from photography studios, accept bookings, and manage assignments.
              </p>
            </div>
          </div>

          {/* Privacy Alert Reminder (REQ-002, REQ-006) */}
          <div className="mt-6 pt-5 border-t border-slate-800/80 flex items-start gap-3 bg-teal-950/20 border border-teal-500/20 p-3.5 rounded-2xl">
            <ShieldCheck className="w-4 h-4 text-teal-400 shrink-0 mt-0.5" />
            <p className="text-xs text-slate-300 leading-relaxed">
              <strong>Privacy Protection Gate:</strong> Event details and rates are visible immediately. Private client contact information is strictly protected and will be unlocked automatically once the Studio confirms your assignment (REQ-006).
            </p>
          </div>
        </div>

        {/* Status Tabs */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-3 flex flex-wrap items-center gap-2">
          {(
            [
              { label: 'All Requests', value: 'ALL' },
              { label: 'Pending Action', value: 'PENDING' },
              { label: 'Accepted', value: 'ACCEPTED' },
              { label: 'Confirmed', value: 'CONFIRMED' },
              { label: 'Declined / Cancelled', value: 'REJECTED' },
            ] as const
          ).map((tab) => (
            <button
              key={tab.value}
              onClick={() => setStatusFilter(tab.value)}
              className={`px-3 py-1.5 rounded-xl text-xs font-medium transition ${
                statusFilter === tab.value
                  ? 'bg-teal-500 text-slate-950 font-bold shadow-md shadow-teal-500/20'
                  : 'bg-slate-800/80 text-slate-300 hover:text-white hover:bg-slate-800'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Content Section */}
        {loading ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-16 flex flex-col items-center justify-center text-center">
            <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-3" />
            <p className="text-xs text-slate-400">Loading incoming requests...</p>
          </div>
        ) : error ? (
          <div className="bg-rose-950/20 border border-rose-500/30 rounded-3xl p-8 text-center text-rose-300 text-xs">
            {error}
          </div>
        ) : requests.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-12 text-center">
            <Inbox className="w-12 h-12 text-slate-500 mx-auto mb-3" />
            <h3 className="text-base font-bold text-white mb-1">No Work Requests Found</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              {statusFilter !== 'ALL'
                ? `You have no work requests currently in "${statusFilter}" status.`
                : 'When photography studios discover your profile and send booking invitations, they will appear here.'}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {requests.map((req) => (
              <div
                key={req.id}
                className="bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-3xl p-5 sm:p-6 backdrop-blur-md transition shadow-xl space-y-4"
              >
                {/* Header: Studio + Status */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800/80">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center text-teal-400 shrink-0">
                      <Building2 className="w-5 h-5" />
                    </div>
                    <div>
                      <span className="text-xs font-bold text-white block">
                        {req.studioName || 'Photography Studio'}
                      </span>
                      <span className="text-[11px] text-teal-400 font-semibold uppercase tracking-wider">
                        {req.eventType}
                      </span>
                    </div>
                  </div>

                  <div>{getStatusBadge(req.status)}</div>
                </div>

                {/* Event Name & Specs */}
                <div>
                  <h3 className="text-base sm:text-lg font-bold text-white mb-2">
                    {req.eventName}
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 text-xs text-slate-300 bg-slate-800/40 p-3.5 rounded-2xl border border-slate-800">
                    <div className="flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-teal-400 shrink-0" />
                      <span>{req.eventDate}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-teal-400 shrink-0" />
                      <span>{req.formattedTime || `${req.startTime} - ${req.endTime}`}</span>
                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-700/60 text-slate-300 font-medium">
                        {req.dayType === 'FULL_DAY' ? 'Full Day' : 'Half Day'}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-teal-400 shrink-0" />
                      <span className="truncate">{req.location}</span>
                    </div>
                  </div>
                </div>

                {/* Message from Studio if present */}
                {req.message && (
                  <div className="bg-slate-800/60 p-3 rounded-2xl border border-slate-700/50 flex items-start gap-2.5 text-xs text-slate-300">
                    <MessageSquare className="w-4 h-4 text-teal-400 shrink-0 mt-0.5" />
                    <div className="space-y-0.5">
                      <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                        Studio Proposal Note
                      </span>
                      <p className="italic leading-relaxed">{req.message}</p>
                    </div>
                  </div>
                )}

                {/* Footer: Price & Actions */}
                <div className="pt-2 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase block font-semibold">
                      Compensation / Rate Offer
                    </span>
                    <span className="text-base sm:text-lg font-extrabold text-teal-300 flex items-center gap-1">
                      <span>₹{(req.agreedPrice || req.budget)?.toLocaleString()}</span>
                    </span>
                  </div>

                  <div className="flex items-center gap-2">
                    {req.status === 'PENDING' && (
                      <>
                        <button
                          type="button"
                          disabled={actionLoadingId === req.id}
                          onClick={() => handleReject(req.id)}
                          className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-rose-300 text-xs font-semibold border border-slate-700 transition disabled:opacity-50"
                        >
                          Decline
                        </button>
                        <button
                          type="button"
                          disabled={actionLoadingId === req.id}
                          onClick={() => handleAccept(req.id)}
                          className="px-4 py-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold shadow-md shadow-teal-500/20 transition disabled:opacity-50 flex items-center gap-1.5"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          <span>Accept Assignment</span>
                        </button>
                      </>
                    )}

                    <Link
                      to={`/freelancer/requests/${req.id}`}
                      className="inline-flex items-center gap-1 px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition"
                    >
                      <span>View Specifications</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
