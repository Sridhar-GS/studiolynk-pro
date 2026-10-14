import React, { useState, useEffect } from 'react';
import {
  Search,
  RefreshCw,
  Eye,
  Star,
  X,
  AlertCircle,
  ShieldAlert,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminFreelancerItem } from '../../types';

export const AdminFreelancersPage: React.FC = () => {
  const [freelancers, setFreelancers] = useState<AdminFreelancerItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedFreelancer, setSelectedFreelancer] = useState<AdminFreelancerItem | null>(null);

  const fetchFreelancers = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getFreelancers();
      setFreelancers(data);
    } catch (err) {
      console.error('Failed to load freelancers:', err);
      setError('Unable to load creators directory. Please ensure the backend service is operational.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFreelancers();
  }, []);

  const filteredFreelancers = freelancers.filter((f) => {
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      f.fullName.toLowerCase().includes(term) ||
      f.email.toLowerCase().includes(term) ||
      f.address.toLowerCase().includes(term) ||
      f.skills.some((s) => s.toLowerCase().includes(term)) ||
      f.services.some((s) => s.toLowerCase().includes(term));

    return matchesSearch;
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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl font-bold text-white tracking-tight">Creators Directory (Monitoring Only)</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/30">
              {freelancers.length} Registered Creators
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            View-only audit directory of photographers, videographers, cinematographers, and editors (ADM-003)
          </p>
        </div>

        <button
          onClick={fetchFreelancers}
          disabled={loading}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white text-xs font-medium border border-slate-700 transition-colors w-fit"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Directory</span>
        </button>
      </div>

      {/* View-Only Governance Notice */}
      <div className="p-3.5 bg-slate-900/60 border border-slate-800 rounded-xl text-xs text-slate-400 flex items-center gap-3">
        <ShieldAlert className="w-4 h-4 text-teal-400 shrink-0" />
        <span>
          In accordance with <strong>ADM-004</strong> and <strong>ADM-006</strong>, freelancers do not require verification approval to join. Profile data is view-only with no general editing or deletion capabilities.
        </span>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Search Input */}
      <div className="relative">
        <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Search creators by name, email, skill, service, or city..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
        />
      </div>

      {/* Table */}
      <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-sm text-slate-400 flex items-center justify-center gap-3">
            <RefreshCw className="w-5 h-5 animate-spin text-teal-400" />
            <span>Loading creators registry...</span>
          </div>
        ) : filteredFreelancers.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            No creators match your search query.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider bg-slate-950/40">
                  <th className="py-3 px-4">Creator</th>
                  <th className="py-3 px-4">Contact</th>
                  <th className="py-3 px-4">Experience & Rates</th>
                  <th className="py-3 px-4">Skills & Services</th>
                  <th className="py-3 px-4">Rating & Shoots</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-xs">
                {filteredFreelancers.map((f) => (
                  <tr key={f.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        {f.profilePhotoUrl ? (
                          <img
                            src={f.profilePhotoUrl}
                            alt={f.fullName}
                            className="w-9 h-9 rounded-lg object-cover border border-slate-700"
                          />
                        ) : (
                          <div className="w-9 h-9 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 font-bold shrink-0">
                            {f.fullName.charAt(0)}
                          </div>
                        )}
                        <div>
                          <div className="font-semibold text-white">{f.fullName}</div>
                          <div className="text-[11px] text-slate-400 truncate max-w-xs">{f.address}</div>
                        </div>
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-teal-400 font-mono text-[11px]">{f.email}</div>
                      <div className="text-slate-400 text-[11px]">{f.phone}</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{f.experienceYears} yrs experience</div>
                      <div className="text-[11px] text-slate-400">
                        ₹{f.fullDayRate.toLocaleString('en-IN')}/day • ₹{f.halfDayRate.toLocaleString('en-IN')}/half
                      </div>
                    </td>

                    <td className="py-3.5 px-4 max-w-xs">
                      <div className="flex flex-wrap gap-1">
                        {f.skills.slice(0, 3).map((skill, idx) => (
                          <span key={idx} className="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px]">
                            {skill}
                          </span>
                        ))}
                        {f.skills.length > 3 && (
                          <span className="text-[10px] text-slate-400">+{f.skills.length - 3}</span>
                        )}
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-1.5 text-white font-medium">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span>{f.averageRating.toFixed(1)}</span>
                        <span className="text-slate-400 text-[11px]">({f.ratingCount})</span>
                      </div>
                      <div className="text-teal-400 text-[11px]">{f.completedShoots} completed shoots</div>
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => setSelectedFreelancer(f)}
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
      {selectedFreelancer && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between sticky top-0 bg-slate-900/95 backdrop-blur-md">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 font-bold text-lg">
                  {selectedFreelancer.fullName.charAt(0)}
                </div>
                <div>
                  <h2 className="text-base font-bold text-white">{selectedFreelancer.fullName}</h2>
                  <p className="text-xs text-slate-400">Creator ID: #{selectedFreelancer.id} • Registered {formatDate(selectedFreelancer.createdAt)}</p>
                </div>
              </div>
              <button
                onClick={() => setSelectedFreelancer(null)}
                className="w-8 h-8 rounded-lg bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 space-y-6 text-xs">
              {/* Bio & Rates */}
              {selectedFreelancer.bio && (
                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Bio / Statement</span>
                  <p className="text-slate-300 leading-relaxed italic">"{selectedFreelancer.bio}"</p>
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Full Day Rate</span>
                  <p className="text-teal-400 font-bold text-base">₹{selectedFreelancer.fullDayRate.toLocaleString('en-IN')}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Half Day Rate</span>
                  <p className="text-teal-400 font-bold text-base">₹{selectedFreelancer.halfDayRate.toLocaleString('en-IN')}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Email Address</span>
                  <p className="text-indigo-400 font-mono text-sm">{selectedFreelancer.email}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Phone Number</span>
                  <p className="text-white font-medium text-sm">{selectedFreelancer.phone}</p>
                </div>
              </div>

              {/* Skills */}
              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                <span className="text-[10px] uppercase font-semibold text-slate-400">Skills ({selectedFreelancer.skills.length})</span>
                <div className="flex flex-wrap gap-1.5">
                  {selectedFreelancer.skills.map((skill, idx) => (
                    <span key={idx} className="px-2 py-0.5 rounded bg-slate-800 text-slate-200 text-xs">
                      {skill}
                    </span>
                  ))}
                </div>
              </div>

              {/* Services & Gear */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Services Provided</span>
                  <div className="flex flex-wrap gap-1.5">
                    {selectedFreelancer.services.map((svc, idx) => (
                      <span key={idx} className="px-2 py-0.5 rounded bg-indigo-500/10 text-indigo-300 text-xs">
                        {svc}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Equipment Gear</span>
                  <div className="flex flex-wrap gap-1.5">
                    {selectedFreelancer.equipment && selectedFreelancer.equipment.length > 0 ? (
                      selectedFreelancer.equipment.map((eq, idx) => (
                        <span key={idx} className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-xs">
                          {eq}
                        </span>
                      ))
                    ) : (
                      <span className="text-slate-500 text-xs">None declared</span>
                    )}
                  </div>
                </div>
              </div>

              {/* View-Only Confirmation */}
              <div className="p-3 bg-slate-950/30 rounded-lg border border-slate-800/50 text-[11px] text-slate-400 flex items-center justify-between">
                <span>View-only audit record. No edit or delete capability (ADM-004).</span>
                <button
                  onClick={() => setSelectedFreelancer(null)}
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
