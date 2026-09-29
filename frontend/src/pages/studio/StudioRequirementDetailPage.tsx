import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  Calendar,
  Clock,
  MapPin,
  ShieldCheck,
  Sparkles,
  ArrowLeft,
  Edit,
  Trash2,
  CheckCircle2,
  AlertCircle,
  FileText,
  Lock,
  Phone,
  User,
  Wrench,
  Camera,
  Layers,
  ChevronRight,
  Send,
  XCircle,
  MessageSquare
} from 'lucide-react';
import { requirementService } from '../../services/requirementService';
import { requestService } from '../../services/requestService';
import { RequirementRatingSection } from '../../components/ratings/RequirementRatingSection';
import { WorkRequirement, RequirementStatus, WorkRequestSummary, FreelancerCard } from '../../types';

export const StudioRequirementDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [requirement, setRequirement] = useState<WorkRequirement | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  // Phase 10: Work Requests / Candidate Proposals State
  const [requests, setRequests] = useState<WorkRequestSummary[]>([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [confirmingRequestId, setConfirmingRequestId] = useState<number | null>(null);
  const [cancellingRequestId, setCancellingRequestId] = useState<number | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelSubmitting, setCancelSubmitting] = useState(false);

  // Phase 15: AI Top Matched Creators State (DIS-002, DIS-006, ML-012, ML-013)
  const [matchingFreelancers, setMatchingFreelancers] = useState<FreelancerCard[]>([]);

  const fetchRequirement = async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const data = await requirementService.getRequirementById(Number(id));
      setRequirement(data);
    } catch (err: any) {
      console.error('Failed to load requirement details:', err);
      setError('Unable to load requirement details. It may have been deleted or moved.');
    } finally {
      setLoading(false);
    }
  };

  const fetchRequests = async () => {
    if (!id) return;
    setLoadingRequests(true);
    try {
      const data = await requestService.getStudioRequests(Number(id));
      setRequests(data || []);
    } catch (err) {
      console.error('Failed to load candidate requests for requirement:', err);
    } finally {
      setLoadingRequests(false);
    }
  };

  const fetchMatching = async () => {
    if (!id) return;
    try {
      const data = await requirementService.getMatchingFreelancers(Number(id));
      setMatchingFreelancers(data || []);
    } catch (err) {
      console.warn('Failed to load matching freelancers:', err);
    }
  };

  useEffect(() => {
    fetchRequirement();
    fetchRequests();
    fetchMatching();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleConfirmRequest = async (requestId: number) => {
    if (!window.confirm('Confirm this creator for the assignment? All other candidate requests will be automatically closed per WRK-008, and confidential client contact will be revealed.')) {
      return;
    }
    setConfirmingRequestId(requestId);
    try {
      await requestService.confirmRequest(requestId);
      await fetchRequirement();
      await fetchRequests();
    } catch (err: any) {
      console.error('Failed to confirm request:', err);
      alert(err.response?.data?.message || 'Failed to confirm creator booking.');
    } finally {
      setConfirmingRequestId(null);
    }
  };

  const handleCancelRequestSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cancellingRequestId || !cancelReason.trim()) return;
    setCancelSubmitting(true);
    try {
      await requestService.cancelRequest(cancellingRequestId, cancelReason.trim());
      setCancellingRequestId(null);
      setCancelReason('');
      await fetchRequirement();
      await fetchRequests();
    } catch (err: any) {
      console.error('Failed to cancel request:', err);
      alert(err.response?.data?.message || 'Failed to cancel request.');
    } finally {
      setCancelSubmitting(false);
    }
  };

  const handleStatusChange = async (newStatus: RequirementStatus) => {
    if (!id) return;
    setActionLoading(true);
    try {
      const updated = await requirementService.updateRequirementStatus(Number(id), newStatus);
      setRequirement(updated);
    } catch (err: any) {
      console.error('Failed to update requirement status:', err);
      alert(err.response?.data?.message || 'Failed to update requirement status.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    setActionLoading(true);
    try {
      await requirementService.deleteRequirement(Number(id));
      navigate('/studio/requirements');
    } catch (err: any) {
      console.error('Failed to delete requirement:', err);
      alert(err.response?.data?.message || 'Failed to delete requirement.');
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 py-16 flex flex-col items-center justify-center">
        <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-4" />
        <p className="text-sm text-slate-400">Loading shoot requirement details...</p>
      </div>
    );
  }

  if (error || !requirement) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 py-16 px-4">
        <div className="max-w-xl mx-auto bg-slate-900 border border-slate-800 rounded-3xl p-8 text-center space-y-4">
          <AlertCircle className="w-10 h-10 text-rose-400 mx-auto" />
          <h2 className="text-lg font-bold text-white">Requirement Not Found</h2>
          <p className="text-xs text-slate-400">{error || 'This work requirement could not be located.'}</p>
          <Link
            to="/studio/requirements"
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-slate-700 transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Return to Requirements</span>
          </Link>
        </div>
      </div>
    );
  }

  // Pre-populated discovery URL per WRK-005 & Phase 15 AI integration
  const discoveryUrl = `/studio/discovery?requirementId=${requirement.id}&date=${requirement.eventDate}&startTime=${requirement.startTime}&endTime=${requirement.endTime}&dayType=${requirement.dayType}&maxBudget=${requirement.budget}`;

  const renderStatusBadge = (status: RequirementStatus) => {
    switch (status) {
      case 'OPEN':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Open for Applications</span>
          </span>
        );
      case 'DRAFT':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs font-bold">
            <FileText className="w-3.5 h-3.5" />
            <span>Draft Mode</span>
          </span>
        );
      case 'REQUESTED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 text-xs font-bold">
            <Send className="w-3.5 h-3.5" />
            <span>Work Request Pending</span>
          </span>
        );
      case 'ACCEPTED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Offer Accepted</span>
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Assignment Confirmed</span>
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/30 text-xs font-bold">
            <Clock className="w-3.5 h-3.5" />
            <span>Shoot In Progress</span>
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/30 text-xs font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Shoot Completed</span>
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 text-xs font-bold">
            <XCircle className="w-3.5 h-3.5" />
            <span>Cancelled</span>
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 text-slate-300 border border-slate-700 text-xs font-semibold">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Navigation Bar */}
        <div className="flex items-center justify-between">
          <Link
            to="/studio/requirements"
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-teal-400 transition"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to All Requirements</span>
          </Link>

          <div className="flex items-center gap-2">
            <Link
              to={`/studio/requirements/${requirement.id}/edit`}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-semibold border border-slate-700 transition"
            >
              <Edit className="w-3.5 h-3.5" />
              <span>Edit</span>
            </Link>

            <button
              onClick={() => setConfirmDelete(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-950/30 hover:bg-rose-900/50 text-rose-300 text-xs font-semibold border border-rose-800/40 transition"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Delete</span>
            </button>
          </div>
        </div>

        {/* Delete Confirmation Modal */}
        {confirmDelete && (
          <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 max-w-sm w-full space-y-4 shadow-2xl">
              <div className="w-12 h-12 rounded-2xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400">
                <Trash2 className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-white">Delete Work Requirement?</h3>
                <p className="text-xs text-slate-400 mt-1">
                  Are you sure you want to delete &ldquo;{requirement.eventName}&rdquo;? This action cannot be undone.
                </p>
              </div>
              <div className="flex items-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setConfirmDelete(false)}
                  className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={actionLoading}
                  onClick={handleDelete}
                  className="flex-1 py-2 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold transition disabled:opacity-50"
                >
                  {actionLoading ? 'Deleting...' : 'Delete'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Main Header Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl space-y-6">
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-6">
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <span className="text-[11px] font-bold text-teal-400 uppercase tracking-wider">
                  {requirement.eventType}
                </span>
                <span>•</span>
                {renderStatusBadge(requirement.status)}
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                {requirement.eventName}
              </h1>
              <p className="text-xs sm:text-sm text-slate-400">
                Hosted by <strong>{requirement.studioName}</strong>
                {requirement.studioPhone ? ` • Contact: ${requirement.studioPhone}` : ''}
              </p>
            </div>

            {/* Quick Matching Action Button (WRK-005) */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 shrink-0">
              <button
                onClick={() => navigate(discoveryUrl)}
                className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-2xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs shadow-lg shadow-teal-500/20 transition-all group"
              >
                <Sparkles className="w-4 h-4 text-slate-950 group-hover:rotate-12 transition-transform" />
                <span>Find Matching Creators (AI Discovery)</span>
              </button>
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
                <span>{requirement.eventDate}</span>
              </div>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Schedule & Coverage
              </span>
              <div className="flex items-center gap-1.5 mt-1 text-white font-bold text-sm">
                <Clock className="w-4 h-4 text-teal-400" />
                <span>
                  {requirement.formattedTime || `${requirement.startTime} - ${requirement.endTime}`}
                </span>
              </div>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Coverage Type
              </span>
              <span className="text-white font-bold text-sm mt-1 block">
                {requirement.dayType === 'FULL_DAY' ? 'Full Day (8-12h)' : 'Half Day (4-5h)'}
              </span>
            </div>

            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Budget
              </span>
              <div className="flex items-center gap-1 mt-1 text-teal-300 font-extrabold text-base">
                <span>₹{requirement.budget?.toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* Lifecycle State Controls */}
          <div className="pt-4 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
            <span className="text-slate-400">
              Lifecycle status controls:
            </span>
            <div className="flex items-center gap-2">
              {requirement.status === 'DRAFT' && (
                <button
                  type="button"
                  disabled={actionLoading}
                  onClick={() => handleStatusChange('OPEN')}
                  className="px-3 py-1.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl font-bold transition flex items-center gap-1.5"
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Publish Requirement (Open)</span>
                </button>
              )}

              {requirement.status === 'OPEN' && (
                <button
                  type="button"
                  disabled={actionLoading}
                  onClick={() => handleStatusChange('DRAFT')}
                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-amber-400 rounded-xl font-semibold border border-slate-700 transition"
                >
                  Revert to Draft
                </button>
              )}

              {(requirement.status === 'CONFIRMED' || requirement.status === 'IN_PROGRESS') && (
                <button
                  type="button"
                  disabled={actionLoading}
                  onClick={() => handleStatusChange('COMPLETED')}
                  className="px-3 py-1.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl font-bold transition flex items-center gap-1.5"
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Mark Shoot as Completed</span>
                </button>
              )}

              {requirement.status !== 'CANCELLED' && requirement.status !== 'COMPLETED' && (
                <button
                  type="button"
                  disabled={actionLoading}
                  onClick={() => handleStatusChange('CANCELLED')}
                  className="px-3 py-1.5 bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 rounded-xl font-semibold border border-rose-800/40 transition"
                >
                  Cancel Requirement
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Shoot Location & Creative Brief */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 space-y-6">
            {/* Description & Creative Brief */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <FileText className="w-4 h-4 text-teal-400" />
                <span>Shoot Overview & Creative Brief</span>
              </h3>
              <p className="text-xs sm:text-sm text-slate-300 whitespace-pre-line leading-relaxed">
                {requirement.description || 'No additional creative brief provided.'}
              </p>
            </div>

            {/* Venue & Location */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <MapPin className="w-4 h-4 text-teal-400" />
                <span>Shoot Location & Venue</span>
              </h3>
              <p className="text-xs sm:text-sm text-slate-200 font-medium">
                {requirement.location}
              </p>
            </div>

            {/* Technical Specifications */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-6">
              {/* Skills */}
              <div>
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                  <Camera className="w-3.5 h-3.5 text-teal-400" />
                  <span>Required Skills ({requirement.requiredSkills?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {requirement.requiredSkills && requirement.requiredSkills.length > 0 ? (
                    requirement.requiredSkills.map((skill) => (
                      <span
                        key={skill.id}
                        className="px-2.5 py-1 rounded-xl bg-slate-800 border border-slate-700 text-xs text-slate-200 font-medium"
                      >
                        {skill.name}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-slate-500 italic">No specific skills tagged.</span>
                  )}
                </div>
              </div>

              {/* Services */}
              <div className="pt-4 border-t border-slate-800">
                <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2 flex items-center gap-2">
                  <Layers className="w-3.5 h-3.5 text-teal-400" />
                  <span>Services & Deliverables ({requirement.requiredServices?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {requirement.requiredServices && requirement.requiredServices.length > 0 ? (
                    requirement.requiredServices.map((svc) => (
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
                  <span>Gear & Equipment Checklist ({requirement.requiredEquipment?.length || 0})</span>
                </h4>
                <div className="flex flex-wrap gap-2">
                  {requirement.requiredEquipment && requirement.requiredEquipment.length > 0 ? (
                    requirement.requiredEquipment.map((eq) => (
                      <span
                        key={eq.id}
                        className="px-2.5 py-1 rounded-xl bg-teal-500/10 border border-teal-500/30 text-xs text-teal-300 font-medium"
                      >
                        {eq.name}
                      </span>
                    ))
                  ) : (
                    <span className="text-xs text-slate-500 italic">Standard studio equipment kit.</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Right Column: Confidential Client Contact Card & Matcher Box */}
          <div className="space-y-6">
            {/* Confidential Client Contact Details (REQ-002, REQ-006 Privacy Gate) */}
            <div className="bg-slate-900/80 border border-teal-500/30 rounded-3xl p-6 space-y-4 relative overflow-hidden">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <div className="flex items-center gap-2 text-teal-300 font-bold text-xs uppercase tracking-wider">
                  <Lock className="w-4 h-4 text-teal-400" />
                  <span>Client Contact</span>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                  Gated (REQ-002)
                </span>
              </div>

              {requirement.eventContactName || requirement.eventContactPhone ? (
                <div className="space-y-3">
                  <div className="bg-slate-800/60 p-3 rounded-2xl border border-slate-800 space-y-2">
                    {requirement.eventContactName && (
                      <div className="flex items-center gap-2 text-xs">
                        <User className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        <span className="text-slate-200 font-semibold">{requirement.eventContactName}</span>
                      </div>
                    )}
                    {requirement.eventContactPhone && (
                      <div className="flex items-center gap-2 text-xs">
                        <Phone className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                        <a
                          href={`tel:${requirement.eventContactPhone}`}
                          className="text-teal-300 font-bold hover:underline"
                        >
                          {requirement.eventContactPhone}
                        </a>
                      </div>
                    )}
                  </div>
                  <p className="text-[10px] text-slate-400 leading-normal flex items-start gap-1.5">
                    <ShieldCheck className="w-3.5 h-3.5 text-teal-400 shrink-0 mt-0.5" />
                    <span>
                      Private info visible only to your studio and the confirmed creator. Masked from public discovery.
                    </span>
                  </p>
                </div>
              ) : (
                <div className="bg-slate-800/40 p-4 rounded-2xl border border-slate-800 text-center space-y-2">
                  <Lock className="w-6 h-6 text-slate-500 mx-auto" />
                  <p className="text-xs text-slate-400">
                    No confidential client contact details filed on this requirement.
                  </p>
                  <Link
                    to={`/studio/requirements/${requirement.id}/edit`}
                    className="inline-block text-[11px] text-teal-400 hover:underline"
                  >
                    Add client contact details
                  </Link>
                </div>
              )}
            </div>

            {/* Quick Matcher Box */}
            <div className="bg-gradient-to-br from-teal-950/40 to-slate-900 border border-teal-500/30 rounded-3xl p-6 space-y-4 shadow-xl">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                  <Sparkles className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-white uppercase tracking-wider">
                    WRK-005 Match Link
                  </h4>
                  <p className="text-xs text-slate-400">Find creators for this date & budget</p>
                </div>
              </div>

              <p className="text-xs text-slate-300 leading-relaxed">
                Seamlessly launch Freelancer Discovery with date (
                <span className="text-teal-300 font-semibold">{requirement.eventDate}</span>
                ) and maximum budget (
                <span className="text-teal-300 font-semibold">₹{requirement.budget?.toLocaleString()}</span>
                ) pre-filtered.
              </p>

              <button
                onClick={() => navigate(discoveryUrl)}
                className="w-full py-2.5 px-4 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs shadow-md transition flex items-center justify-center gap-2"
              >
                <span>Discover Matching Creators</span>
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Phase 13: Ratings & Reviews for Completed Shoot (RAT-001 - RAT-006) */}
        {requirement.status === 'COMPLETED' && (
          <RequirementRatingSection
            requirementId={requirement.id}
            requirementTitle={requirement.eventName}
            requirementStatus={requirement.status}
            counterpartyName={
              requests.find((r) => r.status === 'CONFIRMED')?.freelancerName || 'Confirmed Creator'
            }
            counterpartyRole="Freelancer"
            onRatingSubmitted={() => fetchRequirement()}
          />
        )}

        {/* Phase 15: AI Top Matched Creators (ML-012, ML-013, DIS-002, DIS-006, WRK-005) */}
        {requirement.status === 'OPEN' && matchingFreelancers.length > 0 && (
          <div className="bg-slate-900/90 border border-teal-500/30 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl relative overflow-hidden">
            <div className="absolute top-0 right-0 w-96 h-96 bg-teal-500/5 rounded-full blur-3xl pointer-events-none" />
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800 relative z-10">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-gradient-to-r from-teal-500/20 to-blue-500/20 text-teal-300 border border-teal-500/40">
                    Phase 15 AI Ranked
                  </span>
                  <h3 className="text-lg font-bold text-white flex items-center gap-2">
                    <Sparkles className="w-5 h-5 text-teal-400" />
                    Top AI Recommended Creators
                  </h3>
                </div>
                <p className="text-xs text-slate-400">
                  AI-ranked candidates verified available for this date with compatible skills, portfolio relevance, and budget.
                </p>
              </div>

              <button
                onClick={() => navigate(discoveryUrl)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-teal-300 border border-teal-500/30 rounded-xl text-xs font-semibold transition"
              >
                <span>View All AI Matches</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 relative z-10">
              {matchingFreelancers.slice(0, 3).map((candidate) => (
                <div
                  key={candidate.id}
                  className="bg-slate-950/60 border border-slate-800/80 hover:border-teal-500/40 rounded-2xl p-4 flex flex-col justify-between transition-all group"
                >
                  <div>
                    <div className="flex items-start justify-between gap-3 mb-3">
                      <div className="flex items-center gap-3">
                        <div className="w-11 h-11 rounded-xl bg-slate-800 border border-slate-700 overflow-hidden flex items-center justify-center shrink-0">
                          {candidate.profilePhotoUrl ? (
                            <img
                              src={candidate.profilePhotoUrl}
                              alt={candidate.fullName}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <Camera className="w-5 h-5 text-slate-500" />
                          )}
                        </div>
                        <div>
                          <h4 className="text-sm font-bold text-white group-hover:text-teal-400 transition-colors line-clamp-1">
                            {candidate.fullName}
                          </h4>
                          <p className="text-[11px] text-teal-400 font-medium line-clamp-1">
                            {candidate.primaryRole || 'Creator'}
                          </p>
                        </div>
                      </div>

                      {candidate.aiMatchScore != null && (
                        <span className="shrink-0 px-2 py-0.5 rounded-full bg-teal-500/10 border border-teal-500/30 text-teal-300 text-[10px] font-bold">
                          {candidate.aiMatchScore.toFixed(0)}% Match
                        </span>
                      )}
                    </div>

                    <div className="text-xs text-slate-400 space-y-1 mb-4">
                      {candidate.address && (
                        <div className="flex items-center gap-1.5 truncate">
                          <MapPin className="w-3 h-3 text-slate-500 shrink-0" />
                          <span>{candidate.address}</span>
                        </div>
                      )}
                      <div className="flex items-center justify-between text-[11px] pt-1 border-t border-slate-800/60">
                        <span className="text-slate-500">Day Rate:</span>
                        <span className="text-white font-semibold">
                          ₹{candidate.fullDayRate?.toLocaleString() || '12,000'}
                        </span>
                      </div>
                    </div>
                  </div>

                  <button
                    onClick={() =>
                      navigate(
                        `/studio/discovery?requirementId=${requirement.id}&date=${requirement.eventDate}&startTime=${requirement.startTime}&endTime=${requirement.endTime}&dayType=${requirement.dayType}&maxBudget=${requirement.budget}`
                      )
                    }
                    className="w-full py-2 rounded-xl bg-slate-800 hover:bg-teal-500 hover:text-slate-950 text-slate-200 text-xs font-semibold transition flex items-center justify-center gap-1.5"
                  >
                    <span>View in AI Discovery</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Phase 10: Candidate Creators & Bidding Proposals (WRK-006, WRK-007, REQ-005, REQ-008) */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                  WRK-006 &bull; WRK-007 &bull; REQ-005
                </span>
                <h3 className="text-lg font-bold text-white">Candidate Creators &amp; Work Requests</h3>
              </div>
              <p className="text-xs text-slate-400">
                You can invite multiple creators to review this shoot. Confirm one creator to lock the booking.
              </p>
            </div>

            <button
              onClick={() => navigate(discoveryUrl)}
              className="inline-flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 rounded-xl text-xs font-bold transition shadow-md self-start sm:self-auto"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>Invite More Creators</span>
            </button>
          </div>

          {loadingRequests ? (
            <div className="py-12 flex flex-col items-center justify-center space-y-2">
              <div className="w-6 h-6 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
              <p className="text-xs text-slate-400">Loading candidate requests...</p>
            </div>
          ) : requests.length === 0 ? (
            <div className="text-center py-10 px-4 bg-slate-800/20 rounded-2xl border border-dashed border-slate-800 space-y-3">
              <Camera className="w-10 h-10 text-slate-600 mx-auto" />
              <h4 className="text-sm font-semibold text-white">No Creators Dispatched Yet</h4>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                No work requests have been sent to freelance photographers or videographers for this requirement. Use AI Discovery to find available creators matching your budget and date.
              </p>
              <button
                onClick={() => navigate(discoveryUrl)}
                className="inline-flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-400 text-slate-950 rounded-xl text-xs font-bold transition shadow-md"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>Find Matching Creators</span>
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4">
              {requests.map((req) => (
                <div
                  key={req.id}
                  className={`p-5 rounded-2xl border transition-all ${
                    req.status === 'CONFIRMED'
                      ? 'bg-teal-950/20 border-teal-500/40 ring-1 ring-teal-500/20'
                      : req.status === 'ACCEPTED'
                      ? 'bg-indigo-950/20 border-indigo-500/40'
                      : req.status === 'REJECTED' || req.status === 'CANCELLED'
                      ? 'bg-slate-900/40 border-slate-800/60 opacity-75'
                      : 'bg-slate-800/40 border-slate-800'
                  }`}
                >
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white">
                          {req.freelancerName || `Creator #${req.freelancerId}`}
                        </span>
                        {req.freelancerCity && (
                          <span className="text-xs text-slate-400 flex items-center gap-1">
                            <MapPin className="w-3 h-3 text-teal-400" />
                            {req.freelancerCity}
                          </span>
                        )}
                        {req.freelancerPrimarySkill && (
                          <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 font-medium">
                            {req.freelancerPrimarySkill}
                          </span>
                        )}
                      </div>

                      <div className="flex items-center gap-4 text-xs text-slate-400 pt-1">
                        <span>
                          Rate / Price:{' '}
                          <strong className="text-teal-300">
                            ₹{(req.agreedPrice || req.budget || 0).toLocaleString()}
                          </strong>
                        </span>
                        <span>•</span>
                        <span>Date: {req.eventDate}</span>
                      </div>

                      {req.cancellationReason && (
                        <p className="text-xs text-rose-300 italic pt-1">
                          Cancellation note: &ldquo;{req.cancellationReason}&rdquo;
                        </p>
                      )}
                    </div>

                    <div className="flex items-center gap-3 self-end md:self-auto">
                      {/* Status Tag */}
                      <span
                        className={`text-xs font-bold px-3 py-1 rounded-full border ${
                          req.status === 'CONFIRMED'
                            ? 'bg-teal-500/20 text-teal-300 border-teal-500/40'
                            : req.status === 'ACCEPTED'
                            ? 'bg-indigo-500/20 text-indigo-300 border-indigo-500/40'
                            : req.status === 'PENDING'
                            ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                            : 'bg-slate-800 text-slate-400 border-slate-700'
                        }`}
                      >
                        {req.status}
                      </span>

                      {/* Studio Action: Message Creator (MSG-001, MSG-005) */}
                      <Link
                        to={`/messages?requirementId=${id}&freelancerId=${req.freelancerId}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-teal-300 border border-teal-500/30 text-xs font-semibold transition"
                        title="Chat & Negotiate with Creator"
                      >
                        <MessageSquare className="w-3.5 h-3.5" />
                        <span>Chat</span>
                      </Link>

                      {/* Studio Action: Confirm Creator (WRK-007, REQ-005) */}
                      {req.status === 'ACCEPTED' && (
                        <button
                          type="button"
                          disabled={confirmingRequestId === req.id}
                          onClick={() => handleConfirmRequest(req.id)}
                          className="px-3.5 py-1.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs shadow-md transition disabled:opacity-50"
                        >
                          {confirmingRequestId === req.id ? 'Confirming...' : 'Confirm Creator'}
                        </button>
                      )}

                      {/* Studio Action: Cancel Work (REQ-007) */}
                      {req.status === 'CONFIRMED' && (
                        <button
                          type="button"
                          onClick={() => setCancellingRequestId(req.id)}
                          className="px-3 py-1.5 rounded-xl bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 border border-rose-800/40 font-semibold text-xs transition"
                        >
                          Cancel Work
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Cancellation Reason Modal (REQ-007 Mandatory Reason) */}
        {cancellingRequestId && (
          <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 max-w-md w-full space-y-4 shadow-2xl">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400">
                  <XCircle className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Cancel Confirmed Assignment</h3>
                  <p className="text-xs text-slate-400">A mandatory cancellation reason is required (REQ-007).</p>
                </div>
              </div>

              <form onSubmit={handleCancelRequestSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">
                    Cancellation Reason *
                  </label>
                  <textarea
                    rows={3}
                    required
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                    placeholder="e.g. Client rescheduled wedding shoot date or event cancelled."
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div className="flex items-center justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => {
                      setCancellingRequestId(null);
                      setCancelReason('');
                    }}
                    className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold transition"
                  >
                    Keep Booking
                  </button>
                  <button
                    type="submit"
                    disabled={cancelSubmitting || !cancelReason.trim()}
                    className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold transition disabled:opacity-50"
                  >
                    {cancelSubmitting ? 'Cancelling...' : 'Confirm Cancellation'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
