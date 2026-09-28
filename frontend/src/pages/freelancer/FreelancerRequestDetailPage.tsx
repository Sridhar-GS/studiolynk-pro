import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Calendar,
  Clock,
  MapPin,
  IndianRupee,
  Building2,
  CheckCircle2,
  XCircle,
  Clock3,
  ArrowLeft,
  ShieldCheck,
  Lock,
  Phone,
  User,
  Camera,
  Layers,
  Wrench,
  FileText,
  AlertCircle,
  MessageSquare
} from 'lucide-react';
import { requestService } from '../../services/requestService';
import { WorkRequest, RequestStatus } from '../../types';

export const FreelancerRequestDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  const [request, setRequest] = useState<WorkRequest | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  // Accept modal state
  const [showAcceptModal, setShowAcceptModal] = useState(false);
  const [agreedPriceInput, setAgreedPriceInput] = useState<number | ''>('');

  // Cancel modal state
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReasonInput, setCancelReasonInput] = useState('');

  const fetchRequest = async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const data = await requestService.getRequestById(Number(id));
      setRequest(data);
      setAgreedPriceInput(data.agreedPrice || data.budget);
    } catch (err: any) {
      console.error('Failed to load request details:', err);
      setError('Unable to load work request details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleAcceptSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id) return;
    setActionLoading(true);
    try {
      const price = agreedPriceInput === '' ? undefined : Number(agreedPriceInput);
      const updated = await requestService.acceptRequest(Number(id), price);
      setRequest(updated);
      setShowAcceptModal(false);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to accept request.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!id) return;
    const reason = prompt('Please enter a brief reason for declining this request (optional):');
    if (reason === null) return;

    setActionLoading(true);
    try {
      const updated = await requestService.rejectRequest(Number(id), reason || undefined);
      setRequest(updated);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to decline request.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !cancelReasonInput.trim()) return;
    setActionLoading(true);
    try {
      const updated = await requestService.cancelRequest(Number(id), cancelReasonInput.trim());
      setRequest(updated);
      setShowCancelModal(false);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel request.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 py-16 flex flex-col items-center justify-center">
        <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-4" />
        <p className="text-sm text-slate-400">Loading work request specifications...</p>
      </div>
    );
  }

  if (error || !request) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 py-16 px-4">
        <div className="max-w-xl mx-auto bg-slate-900 border border-slate-800 rounded-3xl p-8 text-center space-y-4">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
          <h2 className="text-lg font-bold text-white">Work Request Not Found</h2>
          <p className="text-xs text-slate-400">{error || 'This work request could not be located.'}</p>
          <Link
            to="/freelancer/requests"
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-slate-700 transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Work Requests</span>
          </Link>
        </div>
      </div>
    );
  }

  const renderStatusBadge = (status: RequestStatus) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs font-bold">
            <Clock3 className="w-3.5 h-3.5" />
            <span>Pending Action</span>
          </span>
        );
      case 'ACCEPTED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Accepted (Awaiting Studio Confirmation)</span>
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Confirmed Booking</span>
          </span>
        );
      case 'REJECTED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 text-xs font-bold">
            <XCircle className="w-3.5 h-3.5" />
            <span>Declined</span>
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 text-slate-400 border border-slate-700 text-xs font-bold">
            <XCircle className="w-3.5 h-3.5" />
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
        {/* Navigation Bar */}
        <div className="flex items-center justify-between">
          <Link
            to="/freelancer/requests"
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-teal-400 transition"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Incoming Requests</span>
          </Link>

          <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-400 border border-teal-500/30">
            Phase 10: REQ-001 - REQ-009
          </span>
        </div>

        {/* Accept Modal */}
        {showAcceptModal && (
          <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl">
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-white">Accept Booking Request</h3>
                <p className="text-xs text-slate-400 mt-1">
                  Confirm your availability for &ldquo;{request.eventName}&rdquo;. You may specify your agreed compensation rate below.
                </p>
              </div>
              <form onSubmit={handleAcceptSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Agreed Rate / Compensation (₹ INR)
                  </label>
                  <div className="relative">
                    <IndianRupee className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="number"
                      min="0"
                      step="500"
                      value={agreedPriceInput}
                      onChange={(e) => setAgreedPriceInput(e.target.value === '' ? '' : Number(e.target.value))}
                      className="w-full pl-8 pr-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                    />
                  </div>
                </div>
                <div className="flex items-center gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowAcceptModal(false)}
                    className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={actionLoading}
                    className="flex-1 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 rounded-xl text-xs font-bold transition disabled:opacity-50"
                  >
                    {actionLoading ? 'Accepting...' : 'Confirm Acceptance'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Cancel Modal */}
        {showCancelModal && (
          <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl">
              <div className="w-12 h-12 rounded-2xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400">
                <XCircle className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-white">Cancel Booking Assignment</h3>
                <p className="text-xs text-slate-400 mt-1">
                  Per REQ-007, both parties may cancel confirmed work by providing an explicit reason.
                </p>
              </div>
              <form onSubmit={handleCancelSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Cancellation Reason <span className="text-rose-400">*</span>
                  </label>
                  <textarea
                    rows={3}
                    required
                    value={cancelReasonInput}
                    onChange={(e) => setCancelReasonInput(e.target.value)}
                    placeholder="e.g. Schedule emergency or equipment maintenance..."
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500 resize-none"
                  />
                </div>
                <div className="flex items-center gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowCancelModal(false)}
                    className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition"
                  >
                    Close
                  </button>
                  <button
                    type="submit"
                    disabled={actionLoading || !cancelReasonInput.trim()}
                    className="flex-1 py-2 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold transition disabled:opacity-50"
                  >
                    {actionLoading ? 'Cancelling...' : 'Confirm Cancellation'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Header Hero Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl space-y-6">
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-bold text-teal-400 uppercase tracking-wider">
                  {request.eventType}
                </span>
                <span>•</span>
                {renderStatusBadge(request.status)}
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                {request.eventName}
              </h1>
              <p className="text-xs sm:text-sm text-slate-400">
                Offered by <strong>{request.studioName}</strong> ({request.studioPhone || 'Studio Partner'})
              </p>
            </div>

            {/* Price Card */}
            <div className="bg-slate-800/60 p-4 rounded-2xl border border-slate-800 text-right shrink-0">
              <span className="text-[10px] text-slate-400 uppercase font-semibold block">
                Agreed / Offered Rate
              </span>
              <span className="text-2xl font-extrabold text-teal-300">
                ₹{(request.agreedPrice || request.budget)?.toLocaleString()}
              </span>
              <span className="text-[10px] text-slate-400 block mt-0.5">
                {request.dayType === 'FULL_DAY' ? 'Full Day Coverage' : 'Half Day Coverage'}
              </span>
            </div>
          </div>

          {/* Key Metric Strips */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-6 border-t border-slate-800/80">
            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Shoot Date
              </span>
              <div className="flex items-center gap-1.5 mt-1 text-white font-bold text-sm">
                <Calendar className="w-4 h-4 text-teal-400" />
                <span>{request.eventDate}</span>
              </div>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Call Schedule
              </span>
              <div className="flex items-center gap-1.5 mt-1 text-white font-bold text-sm">
                <Clock className="w-4 h-4 text-teal-400" />
                <span>{request.formattedTime || `${request.startTime} - ${request.endTime}`}</span>
              </div>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Coverage Type
              </span>
              <span className="text-white font-bold text-sm mt-1 block">
                {request.dayType === 'FULL_DAY' ? 'Full Day (8-12h)' : 'Half Day (4-5h)'}
              </span>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Studio Budget
              </span>
              <div className="flex items-center gap-1 mt-1 text-slate-200 font-extrabold text-base">
                <span>₹{request.budget?.toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* Action Controls */}
          <div className="pt-4 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
            <span className="text-slate-400">Action Controls:</span>
            <div className="flex items-center gap-2">
              {request.status === 'PENDING' && (
                <>
                  <button
                    type="button"
                    disabled={actionLoading}
                    onClick={handleReject}
                    className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-rose-300 rounded-xl font-semibold border border-slate-700 transition disabled:opacity-50"
                  >
                    Decline Request
                  </button>
                  <button
                    type="button"
                    disabled={actionLoading}
                    onClick={() => setShowAcceptModal(true)}
                    className="px-5 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 rounded-xl font-bold shadow-md shadow-teal-500/20 transition disabled:opacity-50 flex items-center gap-1.5"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Accept Assignment</span>
                  </button>
                </>
              )}

              {request.status === 'CONFIRMED' && (
                <button
                  type="button"
                  onClick={() => setShowCancelModal(true)}
                  className="px-4 py-2 bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 rounded-xl font-semibold border border-rose-800/40 transition"
                >
                  Cancel Assignment (REQ-007)
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Content Columns */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 space-y-6">
            {/* Studio Note / Proposal Message */}
            {request.message && (
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-2">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <MessageSquare className="w-4 h-4 text-teal-400" />
                  <span>Studio Proposal Message</span>
                </h3>
                <p className="text-xs sm:text-sm text-slate-300 leading-relaxed italic">
                  &ldquo;{request.message}&rdquo;
                </p>
              </div>
            )}

            {/* Creative Brief */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <FileText className="w-4 h-4 text-teal-400" />
                <span>Shoot Overview & Creative Brief</span>
              </h3>
              <p className="text-xs sm:text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {request.description || 'No additional creative brief provided.'}
              </p>
            </div>

            {/* Venue & Location */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <MapPin className="w-4 h-4 text-teal-400" />
                <span>Shoot Location & Venue</span>
              </h3>
              <p className="text-xs sm:text-sm text-slate-200 font-medium">
                {request.location}
              </p>
            </div>

            {/* Technical Specifications */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-6">
              {/* Skills */}
              <div>
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                  <Camera className="w-3.5 h-3.5 text-teal-400" />
                  <span>Required Skills ({request.requiredSkills?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {request.requiredSkills && request.requiredSkills.length > 0 ? (
                    request.requiredSkills.map((skill) => (
                      <span
                        key={skill.id}
                        className="px-2.5 py-1 rounded-xl bg-slate-800 border border-slate-700 text-xs text-slate-200 font-medium"
                      >
                        {skill.name}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-slate-500 italic">No specific skills listed.</span>
                  )}
                </div>
              </div>

              {/* Services */}
              <div className="pt-4 border-t border-slate-800">
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                  <Layers className="w-3.5 h-3.5 text-teal-400" />
                  <span>Services & Deliverables ({request.requiredServices?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {request.requiredServices && request.requiredServices.length > 0 ? (
                    request.requiredServices.map((svc) => (
                      <span
                        key={svc.id}
                        className="px-2.5 py-1 rounded-xl bg-slate-800 border border-slate-700 text-xs text-slate-200 font-medium"
                      >
                        {svc.name}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-slate-500 italic">No specific services specified.</span>
                  )}
                </div>
              </div>

              {/* Equipment */}
              <div className="pt-4 border-t border-slate-800">
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                  <Wrench className="w-3.5 h-3.5 text-teal-400" />
                  <span>Gear & Equipment Checklist ({request.requiredEquipment?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {request.requiredEquipment && request.requiredEquipment.length > 0 ? (
                    request.requiredEquipment.map((eq) => (
                      <span
                        key={eq.id}
                        className="px-2.5 py-1 rounded-xl bg-teal-500/10 border border-teal-500/30 text-xs text-teal-300 font-medium"
                      >
                        {eq.name}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-slate-500 italic">Standard studio kit.</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Confidential Client Contact Card & Studio Info */}
          <div className="space-y-6">
            {/* Confidential Client Contact Details (REQ-002, REQ-006 Privacy Gate) */}
            <div className="bg-slate-900/80 border border-teal-500/30 rounded-3xl p-6 space-y-4 relative overflow-hidden">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <div className="flex items-center gap-2 text-teal-300 font-bold text-xs uppercase tracking-wider">
                  <Lock className="w-4 h-4 text-teal-400" />
                  <span>Client Contact</span>
                </div>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                  request.privateDetailsRevealed
                    ? 'bg-teal-500/20 text-teal-300 border-teal-500/40'
                    : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                }`}>
                  {request.privateDetailsRevealed ? 'UNLOCKED (REQ-006)' : 'GATED (REQ-002)'}
                </span>
              </div>

              {request.privateDetailsRevealed ? (
                // Unlocked View upon confirmation (REQ-006)
                <div className="space-y-3">
                  <div className="bg-emerald-950/30 border border-emerald-500/30 p-4 rounded-2xl space-y-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-400 block">
                      Shoot Day Direct Coordination
                    </span>
                    {request.eventContactName && (
                      <div className="flex items-center gap-2 text-xs">
                        <User className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        <span className="text-white font-semibold">{request.eventContactName}</span>
                      </div>
                    )}
                    {request.eventContactPhone && (
                      <div className="flex items-center gap-2 text-xs">
                        <Phone className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                        <a
                          href={`tel:${request.eventContactPhone}`}
                          className="text-teal-300 font-bold hover:underline"
                        >
                          {request.eventContactPhone}
                        </a>
                      </div>
                    )}
                  </div>
                  <p className="text-[10px] text-slate-300 leading-relaxed flex items-start gap-1.5">
                    <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                    <span>
                      Booking is officially confirmed. You have direct authorization to coordinate call time with this client.
                    </span>
                  </p>
                </div>
              ) : (
                // Gated View (REQ-002)
                <div className="bg-slate-800/40 p-4 rounded-2xl border border-slate-800 space-y-2 text-center">
                  <Lock className="w-6 h-6 text-slate-500 mx-auto" />
                  <p className="text-xs font-semibold text-slate-300">
                    Confidential Client Contact Masked
                  </p>
                  <p className="text-[11px] text-slate-400 leading-relaxed">
                    Per REQ-002, private event contact details are masked during request & negotiation stages. Details unlock immediately once the Studio confirms your booking (REQ-006).
                  </p>
                </div>
              )}
            </div>

            {/* Studio Info Card */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-3">
              <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider flex items-center gap-2">
                <Building2 className="w-4 h-4 text-teal-400" />
                <span>Partner Photography Studio</span>
              </h4>
              <div className="bg-slate-800/50 p-3.5 rounded-2xl border border-slate-700/60 space-y-1">
                <span className="text-sm font-bold text-white block">{request.studioName}</span>
                {request.studioPhone && (
                  <span className="text-xs text-teal-400 block font-semibold">{request.studioPhone}</span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
