import React, { useState, useEffect } from 'react';
import {
  Search,
  RefreshCw,
  Eye,
  FileCheck,
  AlertCircle,
  X,
  MapPin,
  ShieldAlert,
} from 'lucide-react';
import { adminService } from '../../services/adminService';
import { AdminStudioItem } from '../../types';

export const AdminStudiosPage: React.FC = () => {
  const [studios, setStudios] = useState<AdminStudioItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedStudio, setSelectedStudio] = useState<AdminStudioItem | null>(null);

  const fetchStudios = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.getStudios();
      setStudios(data);
    } catch (err) {
      console.error('Failed to load studios:', err);
      setError('Unable to load studio directory. Please ensure the backend service is operational.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudios();
  }, []);

  const filteredStudios = studios.filter((s) => {
    const matchesSearch =
      s.studioName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.ownerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      s.address.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' || s.identityStatus.toUpperCase() === statusFilter.toUpperCase();

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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-2xl font-bold text-white tracking-tight">Studios Directory (Monitoring Only)</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-teal-500/10 text-teal-400 border border-teal-500/30">
              {studios.length} Total Registered
            </span>
          </div>
          <p className="text-sm text-slate-400 mt-1">
            View-only audit directory of photography studios and regulatory identity declarations (ADM-003, ADM-005)
          </p>
        </div>

        <button
          onClick={fetchStudios}
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
          In accordance with <strong>ADM-004</strong> and <strong>ADM-006</strong>, studios operate without administrative approval gates. Records are non-editable and cannot be deleted by administrators.
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
            placeholder="Search by studio name, owner, email, or city..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-900/80 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400 shrink-0">Identity Status:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900/80 border border-slate-800 rounded-lg text-xs text-white px-3 py-2 focus:outline-none focus:border-teal-500 transition-colors"
          >
            <option value="ALL">All Statuses</option>
            <option value="VERIFIED">Verified / Declared</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="NOT_SUBMITTED">Not Submitted</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-slate-900/70 border border-slate-800 rounded-xl overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-sm text-slate-400 flex items-center justify-center gap-3">
            <RefreshCw className="w-5 h-5 animate-spin text-teal-400" />
            <span>Loading studio registry...</span>
          </div>
        ) : filteredStudios.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            No studios match your filter criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider bg-slate-950/40">
                  <th className="py-3 px-4">Studio</th>
                  <th className="py-3 px-4">Owner & Contact</th>
                  <th className="py-3 px-4">Location</th>
                  <th className="py-3 px-4">Experience</th>
                  <th className="py-3 px-4">Shoots</th>
                  <th className="py-3 px-4">Identity (ADM-005)</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-xs">
                {filteredStudios.map((s) => (
                  <tr key={s.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-lg bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 shrink-0 font-bold">
                          {s.studioName.charAt(0)}
                        </div>
                        <div>
                          <div className="font-semibold text-white">{s.studioName}</div>
                          <div className="text-[11px] text-slate-400">Registered {formatDate(s.createdAt)}</div>
                        </div>
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{s.ownerName}</div>
                      <div className="text-slate-400 text-[11px] font-mono">{s.email}</div>
                      <div className="text-slate-400 text-[11px]">{s.phone}</div>
                    </td>

                    <td className="py-3.5 px-4 text-slate-300 max-w-xs truncate">
                      {s.address}
                    </td>

                    <td className="py-3.5 px-4 text-slate-300">
                      {s.yearsOfOperation} {s.yearsOfOperation === 1 ? 'year' : 'years'}
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium">{s.totalRequirements} requirements</div>
                      <div className="text-teal-400 text-[11px]">{s.completedShoots} completed</div>
                    </td>

                    <td className="py-3.5 px-4">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-semibold border ${
                          s.identityStatus === 'VERIFIED'
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                            : s.identityStatus === 'SUBMITTED'
                            ? 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                            : 'bg-slate-800 text-slate-400 border-slate-700'
                        }`}
                      >
                        {s.identityStatus === 'VERIFIED' ? 'Verified Declaration' : s.identityStatus}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => setSelectedStudio(s)}
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

      {/* Detail Inspection Modal (Strictly Read-Only) */}
      {selectedStudio && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="p-5 border-b border-slate-800 flex items-center justify-between sticky top-0 bg-slate-900/95 backdrop-blur-md">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 font-bold text-lg">
                  {selectedStudio.studioName.charAt(0)}
                </div>
                <div>
                  <h2 className="text-base font-bold text-white">{selectedStudio.studioName}</h2>
                  <p className="text-xs text-slate-400">Studio ID: #{selectedStudio.id} • Registered {formatDate(selectedStudio.createdAt)}</p>
                </div>
              </div>
              <button
                onClick={() => setSelectedStudio(null)}
                className="w-8 h-8 rounded-lg bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-6 space-y-6 text-xs">
              {/* Profile Details */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Owner Name</span>
                  <p className="text-white font-medium text-sm">{selectedStudio.ownerName}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Years of Operation</span>
                  <p className="text-white font-medium text-sm">{selectedStudio.yearsOfOperation} years</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Email Address</span>
                  <p className="text-teal-400 font-mono text-sm">{selectedStudio.email}</p>
                </div>

                <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-1">
                  <span className="text-[10px] uppercase font-semibold text-slate-400">Phone Number</span>
                  <p className="text-white font-medium text-sm">{selectedStudio.phone}</p>
                </div>
              </div>

              {/* Physical Location */}
              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                <div className="flex items-center gap-2 text-slate-400">
                  <MapPin className="w-4 h-4 text-teal-400" />
                  <span className="font-semibold text-white">Registered Studio Address</span>
                </div>
                <p className="text-slate-300 leading-relaxed">{selectedStudio.address}</p>
                {selectedStudio.latitude && selectedStudio.longitude && (
                  <p className="text-[11px] font-mono text-slate-400">
                    Coordinates: {selectedStudio.latitude.toFixed(6)}, {selectedStudio.longitude.toFixed(6)}
                  </p>
                )}
              </div>

              {/* Identity Compliance (ADM-005) */}
              <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-slate-400">
                    <FileCheck className="w-4 h-4 text-emerald-400" />
                    <span className="font-semibold text-white">Identity & Regulatory Declaration (ADM-005)</span>
                  </div>
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${
                      selectedStudio.identityStatus === 'VERIFIED'
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    {selectedStudio.identityStatus}
                  </span>
                </div>

                <div className="text-slate-400 text-xs">
                  Submitted: {formatDate(selectedStudio.identitySubmittedAt)}
                </div>

                <div className="p-3 bg-slate-900 rounded-lg border border-slate-800 text-slate-300 italic text-[11px]">
                  Owner declared verification statement on file. In compliance with STU-002 and STU-003, no Aadhaar number persistence is retained.
                </div>
              </div>

              {/* View-Only Confirmation */}
              <div className="p-3 bg-slate-950/30 rounded-lg border border-slate-800/50 text-[11px] text-slate-400 flex items-center justify-between">
                <span>View-only audit record. No edit or delete capability (ADM-004).</span>
                <button
                  onClick={() => setSelectedStudio(null)}
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
