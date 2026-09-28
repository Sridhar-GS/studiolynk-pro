import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Briefcase,
  Plus,
  Calendar,
  Clock,
  MapPin,
  ChevronRight,
  Sparkles,
  Search,
  CheckCircle2,
  XCircle,
  FileText
} from 'lucide-react';
import { requirementService } from '../../services/requirementService';
import { RequirementStatus, WorkRequirementSummary } from '../../types';

export const StudioRequirementsPage: React.FC = () => {
  const navigate = useNavigate();
  const [requirements, setRequirements] = useState<WorkRequirementSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<RequirementStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchRequirements = async () => {
    setLoading(true);
    setError(null);
    try {
      const filter = statusFilter === 'ALL' ? undefined : statusFilter;
      const data = await requirementService.getMyRequirements(filter);
      setRequirements(data || []);
    } catch (err: any) {
      console.error('Failed to load studio requirements:', err);
      setError('Unable to load requirements. Please refresh the page.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequirements();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  // Filtered requirements by search query
  const filteredRequirements = requirements.filter((r) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      r.eventName.toLowerCase().includes(q) ||
      r.eventType.toLowerCase().includes(q) ||
      r.location.toLowerCase().includes(q)
    );
  });

  // Calculate statistics
  const totalCount = requirements.length;
  const openCount = requirements.filter((r) => r.status === 'OPEN').length;
  const confirmedCount = requirements.filter((r) => r.status === 'CONFIRMED' || r.status === 'IN_PROGRESS').length;
  const totalBudget = requirements
    .filter((r) => r.status !== 'CANCELLED')
    .reduce((sum, r) => sum + (r.budget || 0), 0);

  const getStatusBadge = (status: RequirementStatus) => {
    switch (status) {
      case 'OPEN':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-[11px] font-semibold">
            <CheckCircle2 className="w-3 h-3" />
            <span>Open for Applications</span>
          </span>
        );
      case 'DRAFT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30 text-[11px] font-semibold">
            <FileText className="w-3 h-3" />
            <span>Draft</span>
          </span>
        );
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30 text-[11px] font-semibold">
            <CheckCircle2 className="w-3 h-3" />
            <span>Confirmed</span>
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/30 text-[11px] font-semibold">
            <Clock className="w-3 h-3" />
            <span>In Progress</span>
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/30 text-[11px] font-semibold">
            <CheckCircle2 className="w-3 h-3" />
            <span>Completed</span>
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 text-[11px] font-semibold">
            <XCircle className="w-3 h-3" />
            <span>Cancelled</span>
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 text-[11px]">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Header Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
                <Briefcase className="w-7 h-7" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                    PHASE 9: WORK REQUIREMENTS (WRK-001 - WRK-008)
                  </span>
                </div>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Studio Work Requirements
                </h1>
                <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
                  Publish shoot specifications, set skill & equipment criteria, and find matching creators.
                </p>
              </div>
            </div>

            <Link
              to="/studio/requirements/new"
              className="inline-flex items-center justify-center gap-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold px-5 py-2.5 rounded-xl text-xs shadow-lg shadow-teal-500/20 transition-all shrink-0"
            >
              <Plus className="w-4 h-4" />
              <span>Post New Requirement</span>
            </Link>
          </div>

          {/* Metrics summary cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 pt-6 border-t border-slate-800/80">
            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Total Requirements
              </span>
              <span className="text-xl font-bold text-white mt-1 block">{totalCount}</span>
            </div>
            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-emerald-400 uppercase font-bold tracking-wider block">
                Open Shoots
              </span>
              <span className="text-xl font-bold text-emerald-400 mt-1 block">{openCount}</span>
            </div>
            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-teal-400 uppercase font-bold tracking-wider block">
                Confirmed / Active
              </span>
              <span className="text-xl font-bold text-teal-300 mt-1 block">{confirmedCount}</span>
            </div>
            <div className="bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">
                Committed Budget
              </span>
              <span className="text-xl font-bold text-white mt-1 block">
                ₹{totalBudget.toLocaleString()}
              </span>
            </div>
          </div>
        </div>

        {/* Filter and Search Bar */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
          {/* Status Tabs */}
          <div className="flex flex-wrap items-center gap-1.5">
            {(
              [
                { label: 'All', value: 'ALL' },
                { label: 'Open', value: 'OPEN' },
                { label: 'Drafts', value: 'DRAFT' },
                { label: 'Confirmed', value: 'CONFIRMED' },
                { label: 'Completed', value: 'COMPLETED' },
                { label: 'Cancelled', value: 'CANCELLED' },
              ] as const
            ).map((tab) => (
              <button
                key={tab.value}
                onClick={() => setStatusFilter(tab.value)}
                className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-colors ${
                  statusFilter === tab.value
                    ? 'bg-teal-500 text-slate-950 font-bold shadow-md shadow-teal-500/20'
                    : 'bg-slate-800/80 text-slate-300 hover:text-white hover:bg-slate-800'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Search Input */}
          <div className="relative min-w-[240px]">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Filter by event name, type, venue..."
              className="w-full pl-9 pr-3 py-1.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-slate-100 placeholder-slate-400 focus:outline-none focus:border-teal-500 transition-colors"
            />
          </div>
        </div>

        {/* Requirements List */}
        {loading ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-16 flex flex-col items-center justify-center text-center">
            <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-3" />
            <p className="text-xs text-slate-400">Loading work requirements...</p>
          </div>
        ) : error ? (
          <div className="bg-rose-950/20 border border-rose-500/30 rounded-3xl p-8 text-center text-rose-300 text-xs">
            {error}
          </div>
        ) : filteredRequirements.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-12 text-center">
            <div className="w-14 h-14 rounded-2xl bg-slate-800 flex items-center justify-center text-slate-400 mx-auto mb-4">
              <Briefcase className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-white mb-1">No Requirements Found</h3>
            <p className="text-xs text-slate-400 max-w-md mx-auto mb-6">
              {searchQuery
                ? 'No requirements matched your search query.'
                : statusFilter !== 'ALL'
                ? `You have no requirements currently in "${statusFilter}" status.`
                : 'Create your first event requirement to specify shoot dates, skills, and equipment needs.'}
            </p>
            <Link
              to="/studio/requirements/new"
              className="inline-flex items-center gap-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold px-4 py-2 rounded-xl text-xs shadow-md transition"
            >
              <Plus className="w-4 h-4" />
              <span>Create Requirement</span>
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {filteredRequirements.map((req) => (
              <div
                key={req.id}
                className="bg-slate-900/90 border border-slate-800 hover:border-slate-700 rounded-3xl p-5 backdrop-blur-md transition flex flex-col justify-between group shadow-xl hover:shadow-teal-500/5 relative"
              >
                <div>
                  {/* Top Bar: Event Type & Status */}
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <span className="text-[11px] font-bold text-teal-400 uppercase tracking-wider">
                      {req.eventType}
                    </span>
                    {getStatusBadge(req.status)}
                  </div>

                  {/* Title */}
                  <h3 className="text-base font-bold text-white group-hover:text-teal-300 transition-colors line-clamp-1 mb-2">
                    {req.eventName}
                  </h3>

                  {/* Date, Time & Venue */}
                  <div className="space-y-1.5 text-xs text-slate-400 mb-4 bg-slate-800/40 p-3 rounded-2xl border border-slate-800">
                    <div className="flex items-center gap-2">
                      <Calendar className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                      <span>{req.eventDate}</span>
                      <span>•</span>
                      <Clock className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                      <span>{req.formattedTime || `${req.startTime} - ${req.endTime}`}</span>
                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-700/60 text-slate-300 font-medium">
                        {req.dayType === 'FULL_DAY' ? 'Full Day' : 'Half Day'}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                      <span className="truncate">{req.location}</span>
                    </div>
                  </div>

                  {/* Criteria Counts */}
                  <div className="flex items-center gap-3 text-xs text-slate-400 mb-4">
                    <span>
                      Skills: <strong className="text-slate-200">{req.skillsCount}</strong>
                    </span>
                    <span>•</span>
                    <span>
                      Services: <strong className="text-slate-200">{req.servicesCount}</strong>
                    </span>
                    <span>•</span>
                    <span>
                      Gear: <strong className="text-slate-200">{req.equipmentCount}</strong>
                    </span>
                  </div>
                </div>

                {/* Footer: Budget & Actions */}
                <div className="pt-3 border-t border-slate-800 flex items-center justify-between gap-3">
                  <div>
                    <span className="text-[10px] text-slate-400 uppercase block font-semibold">Budget</span>
                    <span className="text-sm font-extrabold text-white">
                      ₹{req.budget?.toLocaleString()}
                    </span>
                  </div>

                  <div className="flex items-center gap-2">
                    {/* Find Matching Freelancers (WRK-005) */}
                    <button
                      onClick={() =>
                        navigate(
                          `/studio/discovery?date=${req.eventDate}&startTime=${req.startTime}&endTime=${req.endTime}&dayType=${req.dayType}&maxBudget=${req.budget}`
                        )
                      }
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-teal-400 hover:text-teal-300 text-xs font-semibold border border-slate-700 transition"
                      title="Find AI matching creators for this requirement"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-teal-400" />
                      <span>Find Creators</span>
                    </button>

                    <Link
                      to={`/studio/requirements/${req.id}`}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold transition shadow-sm"
                    >
                      <span>Details</span>
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
