import React, { useState, useEffect } from 'react';
import {
  Building2,
  MapPin,
  Globe,
  ShieldCheck,
  Edit3,
  CheckCircle2,
  Save,
  X,
  Phone,
  Mail,
  AlertCircle,
  ExternalLink,
  Plus,
  Trash2,
  Sparkles
} from 'lucide-react';
import api from '../../services/api';
import { ApiResponse, StudioProfile, StudioSocialLink, StudioUpdatePayload } from '../../types';
import axios from 'axios';

export const StudioProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<StudioProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);

  // Edit Form Fields
  const [editStudioName, setEditStudioName] = useState('');
  const [editOwnerName, setEditOwnerName] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const [editAddress, setEditAddress] = useState('');
  const [editLatitude, setEditLatitude] = useState<string>('');
  const [editLongitude, setEditLongitude] = useState<string>('');
  const [editYears, setEditYears] = useState<number>(0);
  const [editLogoUrl, setEditLogoUrl] = useState('');
  const [editSocialLinks, setEditSocialLinks] = useState<StudioSocialLink[]>([]);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchProfile = async () => {
    try {
      const res = await api.get<ApiResponse<StudioProfile>>('/studios/me');
      if (res.data?.data) {
        setProfile(res.data.data);
      }
    } catch (err) {
      console.warn('Failed to load studio profile:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleStartEditing = () => {
    if (!profile) return;
    setEditStudioName(profile.studioName || '');
    setEditOwnerName(profile.ownerName || '');
    setEditPhone(profile.phone || '');
    setEditAddress(profile.address || '');
    setEditLatitude(profile.latitude ? profile.latitude.toString() : '');
    setEditLongitude(profile.longitude ? profile.longitude.toString() : '');
    setEditYears(profile.yearsOfOperation || 0);
    setEditLogoUrl(profile.logoUrl || '');
    setEditSocialLinks(profile.socialLinks ? [...profile.socialLinks] : []);
    setError(null);
    setSuccessMsg(null);
    setIsEditing(true);
  };

  const handleCancelEditing = () => {
    setIsEditing(false);
    setError(null);
  };

  const handleAddSocialLink = () => {
    setEditSocialLinks([...editSocialLinks, { platformName: 'Instagram', url: '' }]);
  };

  const handleRemoveSocialLink = (index: number) => {
    setEditSocialLinks(editSocialLinks.filter((_, i) => i !== index));
  };

  const handleSocialChange = (index: number, field: keyof StudioSocialLink, value: string) => {
    const updated = [...editSocialLinks];
    updated[index] = { ...updated[index], [field]: value };
    setEditSocialLinks(updated);
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setSaving(true);

    const payload: StudioUpdatePayload = {
      studioName: editStudioName.trim(),
      ownerName: editOwnerName.trim(),
      phone: editPhone.trim(),
      address: editAddress.trim(),
      latitude: editLatitude ? parseFloat(editLatitude) : undefined,
      longitude: editLongitude ? parseFloat(editLongitude) : undefined,
      yearsOfOperation: Number(editYears) || 0,
      logoUrl: editLogoUrl.trim() || undefined,
      socialLinks: editSocialLinks.filter((l) => l.url.trim().length > 0),
    };

    try {
      const res = await api.put<ApiResponse<StudioProfile>>('/studios/me', payload);
      if (res.data?.data) {
        setProfile(res.data.data);
      }
      setSuccessMsg('Studio profile updated successfully (STU-005).');
      setIsEditing(false);
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to update studio profile.');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading studio profile...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6 text-center">
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 max-w-md">
          <AlertCircle className="w-10 h-10 text-amber-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-white mb-2">Studio Profile Not Found</h3>
          <p className="text-xs text-slate-400 mb-6">
            Please complete the onboarding setup to initialize your studio profile.
          </p>
          <a
            href="/onboarding/studio"
            className="inline-block bg-teal-500 hover:bg-teal-400 text-slate-950 font-semibold text-xs px-5 py-2.5 rounded-xl transition-colors"
          >
            Start Studio Onboarding
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Status Alerts */}
        {error && (
          <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/30 flex items-start gap-3 text-rose-300 text-sm">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5 text-rose-400" />
            <span>{error}</span>
          </div>
        )}
        {successMsg && (
          <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-start gap-3 text-emerald-300 text-sm">
            <CheckCircle2 className="w-5 h-5 shrink-0 mt-0.5 text-emerald-400" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* HERO BANNER CARD */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="flex items-start sm:items-center gap-5">
              {profile.logoUrl ? (
                <img
                  src={profile.logoUrl}
                  alt={profile.studioName}
                  className="w-20 h-20 rounded-2xl object-cover border border-slate-700 shadow-md shrink-0 bg-slate-950"
                />
              ) : (
                <div className="w-20 h-20 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
                  <Building2 className="w-10 h-10" />
                </div>
              )}

              <div>
                <div className="flex flex-wrap items-center gap-2 mb-1.5">
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                    {profile.studioName}
                  </h1>
                  <span className="flex items-center gap-1 text-[11px] font-semibold text-emerald-400 bg-emerald-500/10 border border-emerald-500/30 px-2.5 py-0.5 rounded-full">
                    <ShieldCheck className="w-3.5 h-3.5" />
                    Verified Studio
                  </span>
                </div>
                <p className="text-xs text-slate-400 flex items-center gap-2">
                  <span>Owner: <strong className="text-slate-200">{profile.ownerName}</strong></span>
                  &bull;
                  <span>{profile.yearsOfOperation} years in operation</span>
                </p>
              </div>
            </div>

            {/* Profile Action Buttons */}
            <div className="flex items-center gap-3">
              {!isEditing ? (
                <button
                  type="button"
                  onClick={handleStartEditing}
                  className="flex items-center gap-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 px-4 py-2.5 text-xs font-semibold shadow-md shadow-teal-500/10 transition-colors"
                >
                  <Edit3 className="w-4 h-4" />
                  <span>Edit Profile (STU-005)</span>
                </button>
              ) : (
                <button
                  type="button"
                  onClick={handleCancelEditing}
                  className="flex items-center gap-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 px-3.5 py-2.5 text-xs font-medium border border-slate-700 transition-colors"
                >
                  <X className="w-4 h-4" />
                  <span>Cancel</span>
                </button>
              )}
            </div>
          </div>

          {/* Profile Completion Meter (ONB-005) */}
          <div className="mt-6 pt-6 border-t border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
            <div className="flex items-center gap-2 text-slate-300">
              <Sparkles className="w-4 h-4 text-teal-400" />
              <span>Profile Health: <strong>{profile.completionPercentage}% Complete</strong></span>
            </div>
            <div className="w-full sm:w-64 h-2 bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-teal-500 to-emerald-400 rounded-full"
                style={{ width: `${profile.completionPercentage}%` }}
              />
            </div>
          </div>
        </div>

        {/* VIEW MODE vs EDIT MODE */}
        {!isEditing ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Contact & Location Details */}
            <div className="md:col-span-2 bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
              <h3 className="text-base font-semibold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
                <MapPin className="w-4 h-4 text-teal-400" />
                <span>Contact &amp; Location Information</span>
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-slate-400 block mb-1">Direct Phone</span>
                  <div className="flex items-center gap-2 text-slate-200 font-medium">
                    <Phone className="w-3.5 h-3.5 text-teal-400" />
                    <span>{profile.phone}</span>
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-slate-400 block mb-1">Account Email</span>
                  <div className="flex items-center gap-2 text-slate-200 font-medium">
                    <Mail className="w-3.5 h-3.5 text-teal-400" />
                    <span className="truncate">{profile.email}</span>
                  </div>
                </div>

                <div className="sm:col-span-2 p-4 rounded-2xl bg-slate-950 border border-slate-800">
                  <span className="text-slate-400 block mb-1">Studio Address</span>
                  <p className="text-slate-200 leading-relaxed">{profile.address}</p>
                  {profile.latitude && profile.longitude && (
                    <div className="mt-2 pt-2 border-t border-slate-800 flex items-center gap-2 font-mono text-[11px] text-teal-400">
                      <span>Coordinates:</span>
                      <span>{profile.latitude}, {profile.longitude}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Social Channels */}
              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3">
                  Online &amp; Social Portfolios
                </h4>
                {profile.socialLinks && profile.socialLinks.length > 0 ? (
                  <div className="flex flex-wrap gap-2.5">
                    {profile.socialLinks.map((link, idx) => (
                      <a
                        key={idx}
                        href={link.url.startsWith('http') ? link.url : `https://${link.url}`}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 hover:text-teal-400 hover:border-teal-500/40 transition-colors"
                      >
                        <Globe className="w-3.5 h-3.5 text-teal-400" />
                        <span className="font-medium">{link.platformName}</span>
                        <ExternalLink className="w-3 h-3 text-slate-500" />
                      </a>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500 italic">No social links configured yet.</p>
                )}
              </div>
            </div>

            {/* Prototype Verification & Declaration Status (STU-002, STU-003) */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-5">
              <h3 className="text-base font-semibold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Verification Status</span>
              </h3>

              {profile.identitySubmission ? (
                <div className="space-y-4 text-xs">
                  <div className="p-3.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300">
                    <span className="font-semibold block mb-0.5">Status: Verified (STU-004)</span>
                    <span className="text-[11px] text-emerald-400">
                      Academic Prototype Verification Active
                    </span>
                  </div>

                  <div>
                    <span className="text-slate-400 block mb-1">Document Type</span>
                    <span className="font-medium text-slate-200">
                      {profile.identitySubmission.documentType}
                    </span>
                  </div>

                  {profile.identitySubmission.documentUrl && (
                    <div>
                      <span className="text-slate-400 block mb-1">Document Reference</span>
                      <span className="text-slate-300 font-mono text-[11px] break-all">
                        {profile.identitySubmission.documentUrl}
                      </span>
                    </div>
                  )}

                  {profile.identitySubmission.declarationText && (
                    <div>
                      <span className="text-slate-400 block mb-1">Owner Declaration</span>
                      <p className="p-3 rounded-xl bg-slate-950 border border-slate-800 text-[11px] text-slate-400 leading-relaxed font-mono">
                        "{profile.identitySubmission.declarationText}"
                      </p>
                    </div>
                  )}
                </div>
              ) : (
                <p className="text-xs text-slate-400">No identity submission recorded.</p>
              )}
            </div>
          </div>
        ) : (
          /* EDIT PROFILE FORM (STU-005) */
          <form onSubmit={handleSaveProfile} className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div>
                <h3 className="text-base font-semibold text-white">Edit Studio Profile (STU-005)</h3>
                <p className="text-xs text-slate-400">Modify studio details, contact numbers, and social links</p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Studio Name *</label>
                <input
                  type="text"
                  required
                  value={editStudioName}
                  onChange={(e) => setEditStudioName(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Owner Name *</label>
                <input
                  type="text"
                  required
                  value={editOwnerName}
                  onChange={(e) => setEditOwnerName(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Phone *</label>
                <input
                  type="tel"
                  required
                  value={editPhone}
                  onChange={(e) => setEditPhone(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Years in Operation</label>
                <input
                  type="number"
                  min={0}
                  value={editYears}
                  onChange={(e) => setEditYears(parseInt(e.target.value) || 0)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div className="sm:col-span-2">
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Logo URL</label>
                <input
                  type="url"
                  value={editLogoUrl}
                  onChange={(e) => setEditLogoUrl(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div className="sm:col-span-2">
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Address *</label>
                <textarea
                  rows={2}
                  required
                  value={editAddress}
                  onChange={(e) => setEditAddress(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 p-3 text-sm text-white focus:outline-none focus:border-teal-500 resize-none"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Latitude</label>
                <input
                  type="number"
                  step="any"
                  value={editLatitude}
                  onChange={(e) => setEditLatitude(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500 font-mono"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Longitude</label>
                <input
                  type="number"
                  step="any"
                  value={editLongitude}
                  onChange={(e) => setEditLongitude(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white focus:outline-none focus:border-teal-500 font-mono"
                />
              </div>
            </div>

            {/* Social Links Editor */}
            <div className="pt-4 border-t border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-300">Social Links</span>
                <button
                  type="button"
                  onClick={handleAddSocialLink}
                  className="flex items-center gap-1 text-xs text-teal-400 hover:text-teal-300"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Add Link</span>
                </button>
              </div>

              {editSocialLinks.map((link, idx) => (
                <div key={idx} className="flex items-center gap-3">
                  <select
                    value={link.platformName}
                    onChange={(e) => handleSocialChange(idx, 'platformName', e.target.value)}
                    className="w-32 rounded-xl bg-slate-950 border border-slate-700/80 px-3 py-2 text-xs text-white"
                  >
                    <option value="Instagram">Instagram</option>
                    <option value="Website">Website</option>
                    <option value="Facebook">Facebook</option>
                    <option value="YouTube">YouTube</option>
                    <option value="LinkedIn">LinkedIn</option>
                  </select>
                  <input
                    type="url"
                    value={link.url}
                    onChange={(e) => handleSocialChange(idx, 'url', e.target.value)}
                    placeholder="https://..."
                    className="flex-1 rounded-xl bg-slate-950 border border-slate-700/80 px-3 py-2 text-xs text-white"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveSocialLink(idx)}
                    className="p-2 text-slate-500 hover:text-rose-400"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>

            {/* Save Buttons */}
            <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
              <button
                type="button"
                onClick={handleCancelEditing}
                className="px-4 py-2 rounded-xl text-xs text-slate-400 hover:text-white bg-slate-800"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="flex items-center gap-1.5 px-6 py-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-semibold text-xs transition-colors disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                <span>{saving ? 'Saving...' : 'Save Profile Changes'}</span>
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
