import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Building2,
  MapPin,
  Globe,
  ShieldCheck,
  CheckCircle2,
  Save,
  ArrowRight,
  Plus,
  Trash2,
  AlertCircle,
  Sparkles,
  Phone,
  User,
  Calendar,
  Image as ImageIcon
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { ApiResponse, StudioProfile, StudioSocialLink } from '../../types';
import axios from 'axios';

const CITY_PRESETS = [
  { name: 'Chennai', lat: 13.0827, lng: 80.2707 },
  { name: 'Bangalore', lat: 12.9716, lng: 77.5946 },
  { name: 'Mumbai', lat: 19.0760, lng: 72.8777 },
  { name: 'Hyderabad', lat: 17.3850, lng: 78.4867 },
  { name: 'Delhi NCR', lat: 28.6139, lng: 77.2090 },
  { name: 'Kochi', lat: 9.9312, lng: 76.2673 },
];

export const StudioOnboardingPage: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();

  const [studioName, setStudioName] = useState('');
  const [ownerName, setOwnerName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState<string>('');
  const [longitude, setLongitude] = useState<string>('');
  const [yearsOfOperation, setYearsOfOperation] = useState<number>(1);
  const [logoUrl, setLogoUrl] = useState('');

  // Social Links
  const [socialLinks, setSocialLinks] = useState<StudioSocialLink[]>([
    { platformName: 'Instagram', url: '' },
  ]);

  // Identity / Declaration (STU-002, STU-003)
  const [documentType, setDocumentType] = useState('STUDIO_REGISTRATION_CERTIFICATE');
  const [documentUrl, setDocumentUrl] = useState('');
  const [declarationText, setDeclarationText] = useState(
    'I hereby declare that this photography studio is a lawfully operating enterprise and all provided operational information is accurate and authentic.'
  );
  const [agreedToTerms, setAgreedToTerms] = useState(false);

  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Load existing profile / draft if available (ONB-004)
  useEffect(() => {
    const fetchExistingDraft = async () => {
      try {
        const res = await api.get<ApiResponse<StudioProfile>>('/studios/me');
        if (res.data?.data) {
          const s = res.data.data;
          setStudioName(s.studioName || '');
          setOwnerName(s.ownerName || '');
          setPhone(s.phone || '');
          setAddress(s.address || '');
          if (s.latitude) setLatitude(s.latitude.toString());
          if (s.longitude) setLongitude(s.longitude.toString());
          if (s.yearsOfOperation) setYearsOfOperation(s.yearsOfOperation);
          if (s.logoUrl) setLogoUrl(s.logoUrl);
          if (s.socialLinks && s.socialLinks.length > 0) {
            setSocialLinks(s.socialLinks);
          }
          if (s.identitySubmission) {
            setDocumentType(s.identitySubmission.documentType || 'STUDIO_REGISTRATION_CERTIFICATE');
            setDocumentUrl(s.identitySubmission.documentUrl || '');
            if (s.identitySubmission.declarationText) {
              setDeclarationText(s.identitySubmission.declarationText);
              setAgreedToTerms(true);
            }
          }
        }
      } catch {
        // No existing draft is expected for fresh accounts
      } finally {
        setInitialLoading(false);
      }
    };

    fetchExistingDraft();
  }, []);

  // Compute live completion percentage (ONB-005)
  const computePercentage = (): number => {
    let score = 0;
    if (studioName.trim()) score += 15;
    if (ownerName.trim()) score += 15;
    if (phone.trim()) score += 15;
    if (address.trim()) score += 15;
    if (latitude && longitude) score += 10;
    if (yearsOfOperation > 0) score += 10;
    if (logoUrl.trim()) score += 10;
    if (socialLinks.some((l) => l.url.trim())) score += 5;
    if (agreedToTerms && declarationText.trim()) score += 10;
    return Math.min(100, score);
  };

  const completionPercentage = computePercentage();

  const handleAddSocialLink = () => {
    setSocialLinks([...socialLinks, { platformName: 'Instagram', url: '' }]);
  };

  const handleRemoveSocialLink = (index: number) => {
    setSocialLinks(socialLinks.filter((_, i) => i !== index));
  };

  const handleSocialChange = (index: number, field: keyof StudioSocialLink, value: string) => {
    const updated = [...socialLinks];
    updated[index] = { ...updated[index], [field]: value };
    setSocialLinks(updated);
  };

  const handleCityPreset = (lat: number, lng: number, name: string) => {
    setLatitude(lat.toString());
    setLongitude(lng.toString());
    if (!address) {
      setAddress(`${name}, India`);
    }
  };

  const buildPayload = () => {
    return {
      studioName: studioName.trim(),
      ownerName: ownerName.trim(),
      phone: phone.trim(),
      address: address.trim(),
      latitude: latitude ? parseFloat(latitude) : undefined,
      longitude: longitude ? parseFloat(longitude) : undefined,
      yearsOfOperation: Number(yearsOfOperation) || 0,
      logoUrl: logoUrl.trim() || undefined,
      socialLinks: socialLinks.filter((l) => l.url.trim().length > 0),
      documentType,
      documentUrl: documentUrl.trim() || undefined,
      declarationText: agreedToTerms ? declarationText.trim() : undefined,
    };
  };

  // Save Draft (ONB-004)
  const handleSaveDraft = async () => {
    setError(null);
    setSuccessMsg(null);
    setLoading(true);

    try {
      await api.put('/onboarding/studio', buildPayload());
      setSuccessMsg('Onboarding draft saved. You may resume anytime.');
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to save draft. Please check your inputs.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Submit Final Onboarding (STU-001, STU-004, ONB-003)
  const handleCompleteOnboarding = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);

    if (!studioName.trim() || !ownerName.trim() || !phone.trim() || !address.trim()) {
      setError('Please provide Studio Name, Owner Name, Contact Phone, and Address.');
      return;
    }

    if (!agreedToTerms) {
      setError('Please acknowledge and sign the Owner Identity Declaration (STU-002).');
      return;
    }

    setLoading(true);
    try {
      await api.post('/onboarding/studio', buildPayload());
      await refreshUser();
      navigate('/studio/profile', { replace: true });
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to complete onboarding. Please verify all required fields.');
      }
    } finally {
      setLoading(false);
    }
  };

  if (initialLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading studio profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header with Live Progress */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-xs px-2.5 py-1 rounded-full bg-teal-500/10 text-teal-400 border border-teal-500/30 font-medium flex items-center gap-1.5">
                  <Building2 className="w-3.5 h-3.5" />
                  Phase 4 &bull; Studio Onboarding (STU-001)
                </span>
                <span className="text-xs text-slate-400">{user?.email}</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Studio Profile &amp; Verification Setup
              </h1>
              <p className="text-xs sm:text-sm text-slate-400 mt-1">
                Configure your business credentials, location coordinates, and owner identity declaration.
              </p>
            </div>

            {/* Completion Percentage Badge & Meter (ONB-005) */}
            <div className="bg-slate-950/80 border border-slate-800 p-4 rounded-2xl flex flex-col items-end min-w-[180px]">
              <div className="flex items-center justify-between w-full mb-1.5">
                <span className="text-xs text-slate-400 font-medium">Profile Score:</span>
                <span className="text-sm font-bold text-teal-400 font-mono">
                  {completionPercentage}%
                </span>
              </div>
              <div className="w-full h-2.5 bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-teal-500 to-emerald-400 transition-all duration-500 rounded-full"
                  style={{ width: `${completionPercentage}%` }}
                />
              </div>
              <span className="text-[10px] text-slate-500 mt-1">
                {completionPercentage === 100 ? 'All requirements satisfied' : 'Complete all fields for 100%'}
              </span>
            </div>
          </div>
        </div>

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

        {/* Form Container */}
        <form onSubmit={handleCompleteOnboarding} className="space-y-8">
          {/* SECTION 1: STUDIO IDENTITY */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
              <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400">
                <Building2 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-white">1. Business Identity</h3>
                <p className="text-xs text-slate-400">Core studio identity and contact details</p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Studio Business Name *
                </label>
                <div className="relative rounded-xl">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
                    <Building2 className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    required
                    value={studioName}
                    onChange={(e) => setStudioName(e.target.value)}
                    placeholder="e.g. Apex Visuals &amp; Photography"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Owner / Representative Full Name *
                </label>
                <div className="relative rounded-xl">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
                    <User className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    required
                    value={ownerName}
                    onChange={(e) => setOwnerName(e.target.value)}
                    placeholder="e.g. Sridhar Ganesan"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Business Phone / WhatsApp *
                </label>
                <div className="relative rounded-xl">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
                    <Phone className="h-4 w-4" />
                  </div>
                  <input
                    type="tel"
                    required
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="+91 98765 43210"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Years in Operation
                </label>
                <div className="relative rounded-xl">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
                    <Calendar className="h-4 w-4" />
                  </div>
                  <input
                    type="number"
                    min={0}
                    max={100}
                    value={yearsOfOperation}
                    onChange={(e) => setYearsOfOperation(parseInt(e.target.value) || 0)}
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div className="sm:col-span-2">
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Studio Logo / Photo URL (Optional)
                </label>
                <div className="relative rounded-xl">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-500">
                    <ImageIcon className="h-4 w-4" />
                  </div>
                  <input
                    type="url"
                    value={logoUrl}
                    onChange={(e) => setLogoUrl(e.target.value)}
                    placeholder="https://images.unsplash.com/... or S3 URL"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 2: PHYSICAL ADDRESS & COORDINATES */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
                <MapPin className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-white">2. Studio Location &amp; Coordinates</h3>
                <p className="text-xs text-slate-400">Required for distance-based freelancer discovery</p>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Street Address &amp; Landmarks *
                </label>
                <textarea
                  rows={2}
                  required
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="e.g. 102, 2nd Cross, Anna Nagar West, Chennai, Tamil Nadu - 600040"
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 p-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 resize-none"
                />
              </div>

              {/* City Presets */}
              <div>
                <span className="text-[11px] text-slate-400 block mb-2 font-medium">
                  Quick City Coordinates Presets:
                </span>
                <div className="flex flex-wrap gap-2">
                  {CITY_PRESETS.map((city) => (
                    <button
                      key={city.name}
                      type="button"
                      onClick={() => handleCityPreset(city.lat, city.lng, city.name)}
                      className="px-2.5 py-1 text-xs rounded-lg bg-slate-950 border border-slate-800 hover:border-teal-500/60 hover:text-teal-400 text-slate-300 transition-colors"
                    >
                      {city.name}
                    </button>
                  ))}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">
                    Latitude (Decimal)
                  </label>
                  <input
                    type="number"
                    step="any"
                    value={latitude}
                    onChange={(e) => setLatitude(e.target.value)}
                    placeholder="13.0827"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">
                    Longitude (Decimal)
                  </label>
                  <input
                    type="number"
                    step="any"
                    value={longitude}
                    onChange={(e) => setLongitude(e.target.value)}
                    placeholder="80.2707"
                    className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 font-mono"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 3: SOCIAL & ONLINE PRESENCE */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
                  <Globe className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-semibold text-white">3. Social Links &amp; Website</h3>
                  <p className="text-xs text-slate-400">Showcase your studio's public brand</p>
                </div>
              </div>

              <button
                type="button"
                onClick={handleAddSocialLink}
                className="flex items-center gap-1.5 text-xs text-teal-400 hover:text-teal-300 bg-teal-500/10 border border-teal-500/20 px-3 py-1.5 rounded-lg transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Platform</span>
              </button>
            </div>

            <div className="space-y-3">
              {socialLinks.map((link, idx) => (
                <div key={idx} className="flex items-center gap-3">
                  <select
                    value={link.platformName}
                    onChange={(e) => handleSocialChange(idx, 'platformName', e.target.value)}
                    className="w-36 rounded-xl bg-slate-950 border border-slate-700/80 px-3 py-2 text-xs text-white focus:outline-none focus:border-teal-500"
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
                    className="flex-1 rounded-xl bg-slate-950 border border-slate-700/80 px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />

                  {socialLinks.length > 1 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveSocialLink(idx)}
                      className="p-2 text-slate-500 hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition-colors"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* SECTION 4: PROTOTYPE IDENTITY DECLARATION (STU-002, STU-003) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-white">4. Owner Identity Declaration</h3>
                <p className="text-xs text-slate-400">
                  Prototype identity workflow adhering to STU-002 &amp; STU-003 (No external Aadhaar integration)
                </p>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Verification Document Type
                </label>
                <select
                  value={documentType}
                  onChange={(e) => setDocumentType(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                >
                  <option value="STUDIO_REGISTRATION_CERTIFICATE">Studio Registration Certificate / Shop Act</option>
                  <option value="OWNER_AFFIDAVIT">Owner Legal Declaration &amp; ID Affidavit</option>
                  <option value="GST_DECLARATION">GST / Tax Registration Declaration</option>
                  <option value="TRADE_LICENSE">Municipal Trade License</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Document Reference Link or Certificate URL (Optional)
                </label>
                <input
                  type="text"
                  value={documentUrl}
                  onChange={(e) => setDocumentUrl(e.target.value)}
                  placeholder="e.g. DOC-STUDIO-2026-REG or S3 storage link"
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 px-3.5 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">
                  Owner Legal Declaration Text
                </label>
                <textarea
                  rows={3}
                  value={declarationText}
                  onChange={(e) => setDeclarationText(e.target.value)}
                  className="w-full rounded-xl bg-slate-950 border border-slate-700/80 p-3 text-xs text-slate-300 focus:outline-none focus:border-teal-500 resize-none font-mono"
                />
              </div>

              <div className="pt-2">
                <label className="flex items-start gap-3 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={agreedToTerms}
                    onChange={(e) => setAgreedToTerms(e.target.checked)}
                    className="mt-1 w-4 h-4 rounded text-teal-500 bg-slate-950 border-slate-700 focus:ring-teal-500"
                  />
                  <span className="text-xs text-slate-300">
                    I solemnly affirm and declare that I am the authorized owner/representative of{' '}
                    <strong>{studioName || 'this photography studio'}</strong>, and all information submitted above
                    is true, complete, and authentic in compliance with StudioLynk operational guidelines (STU-002).
                  </span>
                </label>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4">
            <button
              type="button"
              disabled={loading}
              onClick={handleSaveDraft}
              className="w-full sm:w-auto flex items-center justify-center gap-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-700 px-6 py-3 text-sm font-medium transition-colors disabled:opacity-50"
            >
              <Save className="w-4 h-4" />
              <span>Save Draft &amp; Exit (ONB-004)</span>
            </button>

            <button
              type="submit"
              disabled={loading || !agreedToTerms}
              className="w-full sm:w-auto flex items-center justify-center gap-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 px-8 py-3 text-sm font-semibold shadow-md shadow-teal-500/10 transition-colors disabled:opacity-50"
            >
              {loading ? (
                <>
                  <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                  <span>Submitting Onboarding...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>Complete Studio Onboarding (STU-004)</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
