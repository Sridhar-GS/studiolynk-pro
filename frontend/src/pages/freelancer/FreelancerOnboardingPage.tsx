import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Camera,
  MapPin,
  Save,
  ArrowRight,
  CheckCircle2,
  Sparkles,
  Phone,
  User,
  Plus,
  Wrench,
  Layers,
  Award,
  DollarSign,
  AlertCircle,
  X
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { catalogueService } from '../../services/catalogueService';
import { freelancerService } from '../../services/freelancerService';
import { EquipmentCategory, EquipmentItem, FreelancerOnboardingPayload, ServiceItem, Skill } from '../../types';

const CITY_PRESETS = [
  { name: 'Chennai', lat: 13.0827, lng: 80.2707 },
  { name: 'Bangalore', lat: 12.9716, lng: 77.5946 },
  { name: 'Mumbai', lat: 19.0760, lng: 72.8777 },
  { name: 'Hyderabad', lat: 17.3850, lng: 78.4867 },
  { name: 'Delhi NCR', lat: 28.6139, lng: 77.2090 },
  { name: 'Kochi', lat: 9.9312, lng: 76.2673 },
];

export const FreelancerOnboardingPage: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();

  // Basic Information
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState<string>('');
  const [longitude, setLongitude] = useState<string>('');
  const [experienceYears, setExperienceYears] = useState<number>(1);
  const [bio, setBio] = useState('');
  const [profilePhotoUrl, setProfilePhotoUrl] = useState('');

  // Pricing
  const [fullDayRate, setFullDayRate] = useState<string>('12000');
  const [halfDayRate, setHalfDayRate] = useState<string>('7000');

  // Catalogue Master Data
  const [allSkills, setAllSkills] = useState<Skill[]>([]);
  const [allServices, setAllServices] = useState<ServiceItem[]>([]);
  const [allCategories, setAllCategories] = useState<EquipmentCategory[]>([]);
  const [allEquipment, setAllEquipment] = useState<EquipmentItem[]>([]);

  // Selected IDs
  const [selectedSkillIds, setSelectedSkillIds] = useState<number[]>([]);
  const [selectedServiceIds, setSelectedServiceIds] = useState<number[]>([]);
  const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);

  // Modals for Custom Items
  const [customSkillModal, setCustomSkillModal] = useState(false);
  const [customSkillName, setCustomSkillName] = useState('');

  const [customServiceModal, setCustomServiceModal] = useState(false);
  const [customServiceName, setCustomServiceName] = useState('');

  const [customEquipModal, setCustomEquipModal] = useState(false);
  const [customEquipName, setCustomEquipName] = useState('');
  const [customEquipCategoryId, setCustomEquipCategoryId] = useState<number | ''>('');

  // UI state
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Load Catalogue data and existing draft if any
  useEffect(() => {
    const loadData = async () => {
      try {
        setInitialLoading(true);
        const [skills, services, categories, equipment] = await Promise.all([
          catalogueService.getSkills(),
          catalogueService.getServices(),
          catalogueService.getEquipmentCategories(),
          catalogueService.getAllEquipment(),
        ]);

        setAllSkills(skills);
        setAllServices(services);
        setAllCategories(categories);
        setAllEquipment(equipment);
        if (categories.length > 0) {
          setCustomEquipCategoryId(categories[0].id);
        }

        // Try prefilling from draft if user has existing profile
        try {
          const profile = await freelancerService.getProfile();
          if (profile) {
            setFullName(profile.fullName || '');
            setPhone(profile.phone || '');
            setAddress(profile.address || '');
            if (profile.latitude) setLatitude(profile.latitude.toString());
            if (profile.longitude) setLongitude(profile.longitude.toString());
            if (profile.experienceYears !== undefined) setExperienceYears(profile.experienceYears);
            setBio(profile.bio || '');
            setProfilePhotoUrl(profile.profilePhotoUrl || '');
            if (profile.fullDayRate) setFullDayRate(profile.fullDayRate.toString());
            if (profile.halfDayRate) setHalfDayRate(profile.halfDayRate.toString());
            if (profile.skills) setSelectedSkillIds(profile.skills.map((s) => s.id));
            if (profile.services) setSelectedServiceIds(profile.services.map((s) => s.id));
            if (profile.equipment) setSelectedEquipmentIds(profile.equipment.map((e) => e.id));
          }
        } catch {
          // No prior draft found, fresh onboarding
        }
      } catch (err) {
        console.error('Failed to load catalogue data:', err);
      } finally {
        setInitialLoading(false);
      }
    };

    loadData();
  }, []);

  // Completion calculation matching ONB-005 formula
  const completionPercentage = useMemo(() => {
    let score = 0;
    if (fullName.trim()) score += 15;
    if (phone.trim()) score += 15;
    if (address.trim()) score += 10;
    if (latitude && longitude) score += 10;
    if (experienceYears >= 0) score += 10;
    if (Number(fullDayRate) > 0 && Number(halfDayRate) > 0) score += 10;
    if (selectedSkillIds.length > 0) score += 10;
    if (selectedServiceIds.length > 0) score += 10;
    if (selectedEquipmentIds.length > 0) score += 5;
    if (bio.trim()) score += 5;
    return Math.min(100, score);
  }, [
    fullName,
    phone,
    address,
    latitude,
    longitude,
    experienceYears,
    fullDayRate,
    halfDayRate,
    selectedSkillIds,
    selectedServiceIds,
    selectedEquipmentIds,
    bio,
  ]);

  const toggleSkill = (id: number) => {
    setSelectedSkillIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const toggleService = (id: number) => {
    setSelectedServiceIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const toggleEquipment = (id: number) => {
    setSelectedEquipmentIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  // Add custom items
  const handleAddCustomSkill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customSkillName.trim()) return;
    try {
      const created = await catalogueService.createCustomSkill(customSkillName.trim());
      setAllSkills((prev) => [...prev, created]);
      setSelectedSkillIds((prev) => [...prev, created.id]);
      setCustomSkillName('');
      setCustomSkillModal(false);
      setStatusMessage({ type: 'success', text: `Custom skill "${created.name}" added!` });
    } catch {
      setStatusMessage({ type: 'error', text: 'Failed to add custom skill.' });
    }
  };

  const handleAddCustomService = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customServiceName.trim()) return;
    try {
      const created = await catalogueService.createCustomService(customServiceName.trim());
      setAllServices((prev) => [...prev, created]);
      setSelectedServiceIds((prev) => [...prev, created.id]);
      setCustomServiceName('');
      setCustomServiceModal(false);
      setStatusMessage({ type: 'success', text: `Custom service "${created.name}" added!` });
    } catch {
      setStatusMessage({ type: 'error', text: 'Failed to add custom service.' });
    }
  };

  const handleAddCustomEquipment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customEquipName.trim() || !customEquipCategoryId) return;
    try {
      const created = await catalogueService.createCustomEquipment(
        Number(customEquipCategoryId),
        customEquipName.trim()
      );
      setAllEquipment((prev) => [...prev, created]);
      setSelectedEquipmentIds((prev) => [...prev, created.id]);
      setCustomEquipName('');
      setCustomEquipModal(false);
      setStatusMessage({ type: 'success', text: `Custom equipment "${created.name}" added!` });
    } catch {
      setStatusMessage({ type: 'error', text: 'Failed to add custom equipment gear.' });
    }
  };

  const buildPayload = (): FreelancerOnboardingPayload => {
    return {
      fullName: fullName.trim(),
      phone: phone.trim(),
      address: address.trim(),
      latitude: latitude ? parseFloat(latitude) : undefined,
      longitude: longitude ? parseFloat(longitude) : undefined,
      experienceYears: Number(experienceYears) || 0,
      bio: bio.trim(),
      profilePhotoUrl: profilePhotoUrl.trim() || undefined,
      fullDayRate: fullDayRate ? parseFloat(fullDayRate) : 0,
      halfDayRate: halfDayRate ? parseFloat(halfDayRate) : 0,
      skillIds: selectedSkillIds,
      serviceIds: selectedServiceIds,
      equipmentIds: selectedEquipmentIds,
    };
  };

  // Draft Save (ONB-004)
  const handleSaveDraft = async () => {
    setLoading(true);
    setStatusMessage(null);
    try {
      await freelancerService.saveDraft(buildPayload());
      setStatusMessage({
        type: 'success',
        text: 'Onboarding draft saved successfully! You can resume anytime without losing progress.',
      });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to save onboarding draft.';
      setStatusMessage({ type: 'error', text: msg });
    } finally {
      setLoading(false);
    }
  };

  // Complete Onboarding (ONB-002, ONB-003, FRL-007)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatusMessage(null);

    if (!fullName.trim() || !phone.trim() || !address.trim()) {
      setStatusMessage({
        type: 'error',
        text: 'Please provide full name, phone number, and location address.',
      });
      setLoading(false);
      return;
    }

    try {
      await freelancerService.submitOnboarding(buildPayload());
      await refreshUser();
      navigate('/freelancer/dashboard', { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to complete freelancer onboarding.';
      setStatusMessage({ type: 'error', text: msg });
      setLoading(false);
    }
  };

  // Group equipment by category
  const equipmentByCategory = useMemo(() => {
    const map = new Map<string, EquipmentItem[]>();
    allEquipment.forEach((item) => {
      const catName = item.categoryName || 'General Gear';
      if (!map.has(catName)) {
        map.set(catName, []);
      }
      map.get(catName)!.push(item);
    });
    return map;
  }, [allEquipment]);

  if (initialLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-9 h-9 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading Freelancer Onboarding &amp; Catalogue...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        {/* Header Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="absolute -right-20 -top-20 w-60 h-60 bg-teal-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="px-2.5 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 text-xs font-semibold">
                  Phase 5: Freelancer Onboarding
                </span>
                <span className="text-xs text-slate-400">Account: {user?.email}</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
                Build Your Creator Profile
              </h1>
              <p className="text-sm text-slate-400 mt-1 max-w-xl">
                Showcase your specialized skills, camera arsenal, services, and daily rates to photography studios.
              </p>
            </div>

            {/* Profile Completion Meter (ONB-005) */}
            <div className="bg-slate-950/80 border border-slate-800 p-4 rounded-2xl min-w-[210px] shrink-0">
              <div className="flex items-center justify-between text-xs mb-2">
                <span className="text-slate-400 font-medium flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-teal-400" /> Completion Meter
                </span>
                <span className="text-sm font-bold text-teal-400">{completionPercentage}%</span>
              </div>
              <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-teal-500 to-emerald-400 rounded-full transition-all duration-500"
                  style={{ width: `${completionPercentage}%` }}
                />
              </div>
              <p className="text-[11px] text-slate-500 mt-2">
                {completionPercentage === 100
                  ? 'Ready for launch! Instant activation.'
                  : 'Complete sections below to maximize studio inquiries.'}
              </p>
            </div>
          </div>
        </div>

        {/* Status Alerts */}
        {statusMessage && (
          <div
            className={`p-4 rounded-2xl border flex items-center gap-3 text-sm ${
              statusMessage.type === 'success'
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
            }`}
          >
            {statusMessage.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 shrink-0" />
            ) : (
              <AlertCircle className="w-5 h-5 shrink-0" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* SECTION 1: Personal Details & Location (FRL-001) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
              <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                <User className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white">1. Creator Details &amp; Location</h2>
                <p className="text-xs text-slate-400">Your professional identity and operating base</p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Full Name <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="e.g. Aravind Swaminathan"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Phone Number <span className="text-rose-400">*</span>
                </label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                  <input
                    type="text"
                    required
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="+91 94444 12345"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                  />
                </div>
              </div>

              <div className="sm:col-span-2">
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Full Operating Address <span className="text-rose-400">*</span>
                </label>
                <div className="relative">
                  <MapPin className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                  <input
                    type="text"
                    required
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    placeholder="e.g. 14, Nageswara Rao Park, Mylapore, Chennai, Tamil Nadu 600004"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                  />
                </div>
              </div>

              {/* City Presets */}
              <div className="sm:col-span-2 bg-slate-950/60 border border-slate-800/80 p-3.5 rounded-2xl">
                <span className="text-xs text-slate-400 block mb-2 font-medium">
                  Quick GPS Preset (Click city to autofill coordinates):
                </span>
                <div className="flex flex-wrap gap-2">
                  {CITY_PRESETS.map((city) => (
                    <button
                      key={city.name}
                      type="button"
                      onClick={() => {
                        setLatitude(city.lat.toFixed(6));
                        setLongitude(city.lng.toFixed(6));
                      }}
                      className="px-3 py-1 rounded-lg text-xs bg-slate-900 border border-slate-700/80 hover:border-teal-500/50 hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
                    >
                      {city.name}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Latitude</label>
                <input
                  type="number"
                  step="any"
                  value={latitude}
                  onChange={(e) => setLatitude(e.target.value)}
                  placeholder="13.033400"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Longitude</label>
                <input
                  type="number"
                  step="any"
                  value={longitude}
                  onChange={(e) => setLongitude(e.target.value)}
                  placeholder="80.267600"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Years of Experience
                </label>
                <input
                  type="number"
                  min="0"
                  max="50"
                  value={experienceYears}
                  onChange={(e) => setExperienceYears(parseInt(e.target.value) || 0)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Profile Photo URL (Optional)
                </label>
                <input
                  type="url"
                  value={profilePhotoUrl}
                  onChange={(e) => setProfilePhotoUrl(e.target.value)}
                  placeholder="https://example.com/avatar.jpg"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>

              <div className="sm:col-span-2">
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Professional Bio
                </label>
                <textarea
                  rows={3}
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  placeholder="Describe your shooting style, specialty, past events, and creative philosophy..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                />
              </div>
            </div>
          </div>

          {/* SECTION 2: Photography Skills (FRL-002) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-5">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                  <Award className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">2. Skills &amp; Specialties</h2>
                  <p className="text-xs text-slate-400">Select all areas you excel at (FRL-002)</p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setCustomSkillModal(true)}
                className="flex items-center gap-1.5 text-xs text-teal-400 hover:text-teal-300 bg-teal-500/10 border border-teal-500/30 px-3 py-1.5 rounded-xl transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Custom Skill</span>
              </button>
            </div>

            <div className="flex flex-wrap gap-2.5">
              {allSkills.map((skill) => {
                const isSelected = selectedSkillIds.includes(skill.id);
                return (
                  <button
                    key={skill.id}
                    type="button"
                    onClick={() => toggleSkill(skill.id)}
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-medium border transition-all ${
                      isSelected
                        ? 'bg-teal-500/20 border-teal-500 text-teal-300 shadow-sm'
                        : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700'
                    }`}
                  >
                    {skill.name}
                    {skill.custom && (
                      <span className="ml-1.5 text-[10px] text-teal-400/80">&bull; custom</span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* SECTION 3: Services Offered (FRL-003) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-5">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">3. Services Offered</h2>
                  <p className="text-xs text-slate-400">Types of assignments you deliver (FRL-003)</p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setCustomServiceModal(true)}
                className="flex items-center gap-1.5 text-xs text-teal-400 hover:text-teal-300 bg-teal-500/10 border border-teal-500/30 px-3 py-1.5 rounded-xl transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Custom Service</span>
              </button>
            </div>

            <div className="flex flex-wrap gap-2.5">
              {allServices.map((service) => {
                const isSelected = selectedServiceIds.includes(service.id);
                return (
                  <button
                    key={service.id}
                    type="button"
                    onClick={() => toggleService(service.id)}
                    className={`px-3.5 py-1.5 rounded-xl text-xs font-medium border transition-all ${
                      isSelected
                        ? 'bg-teal-500/20 border-teal-500 text-teal-300 shadow-sm'
                        : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700'
                    }`}
                  >
                    {service.name}
                    {service.custom && (
                      <span className="ml-1.5 text-[10px] text-teal-400/80">&bull; custom</span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* SECTION 4: Equipment & Gear Arsenal (FRL-004, FRL-005) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                  <Wrench className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">4. Camera Gear &amp; Equipment</h2>
                  <p className="text-xs text-slate-400">
                    Grouped by category (FRL-004). Studios check gear compatibility.
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => setCustomEquipModal(true)}
                className="flex items-center gap-1.5 text-xs text-teal-400 hover:text-teal-300 bg-teal-500/10 border border-teal-500/30 px-3 py-1.5 rounded-xl transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Custom Gear</span>
              </button>
            </div>

            <div className="space-y-5">
              {Array.from(equipmentByCategory.entries()).map(([categoryName, items]) => (
                <div key={categoryName} className="bg-slate-950/60 border border-slate-800/80 p-4 rounded-2xl">
                  <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3 flex items-center gap-1.5">
                    <Camera className="w-3.5 h-3.5 text-teal-400" />
                    {categoryName}
                  </h3>
                  <div className="flex flex-wrap gap-2">
                    {items.map((gear) => {
                      const isSelected = selectedEquipmentIds.includes(gear.id);
                      return (
                        <button
                          key={gear.id}
                          type="button"
                          onClick={() => toggleEquipment(gear.id)}
                          className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition-all ${
                            isSelected
                              ? 'bg-teal-500/20 border-teal-500 text-teal-300 shadow-sm'
                              : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700'
                          }`}
                        >
                          {gear.name}
                          {gear.custom && (
                            <span className="ml-1 text-[10px] text-teal-400/80">&bull; custom</span>
                          )}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* SECTION 5: Day Rates (FRL-001) */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
            <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
              <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                <DollarSign className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-white">5. Pricing &amp; Day Rates</h2>
                <p className="text-xs text-slate-400">
                  Standard baseline rates in INR (₹) for 8-hour and 4-hour shoots
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Full-Day Shoot Rate (₹) <span className="text-rose-400">*</span>
                </label>
                <div className="relative">
                  <span className="absolute left-3.5 top-2.5 text-slate-500 text-sm font-semibold">₹</span>
                  <input
                    type="number"
                    min="0"
                    step="500"
                    required
                    value={fullDayRate}
                    onChange={(e) => setFullDayRate(e.target.value)}
                    placeholder="12000"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-8 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                  />
                </div>
                <span className="text-[11px] text-slate-500 mt-1 block">Full-day standard shoot (approx 8 hours)</span>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Half-Day Shoot Rate (₹) <span className="text-rose-400">*</span>
                </label>
                <div className="relative">
                  <span className="absolute left-3.5 top-2.5 text-slate-500 text-sm font-semibold">₹</span>
                  <input
                    type="number"
                    min="0"
                    step="500"
                    required
                    value={halfDayRate}
                    onChange={(e) => setHalfDayRate(e.target.value)}
                    placeholder="7000"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-8 pr-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition-colors"
                  />
                </div>
                <span className="text-[11px] text-slate-500 mt-1 block">Half-day standard shoot (approx 4 hours)</span>
              </div>
            </div>
          </div>

          {/* Action Bar (Draft Save & Complete Onboarding) */}
          <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 flex flex-col sm:flex-row items-center justify-between gap-4 sticky bottom-4 shadow-2xl backdrop-blur-md">
            <div>
              <span className="text-xs font-semibold text-white block">Ready to start receiving inquiries?</span>
              <p className="text-[11px] text-slate-400">
                Freelancers gain instant platform access without waiting for admin approval (FRL-007).
              </p>
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto">
              <button
                type="button"
                onClick={handleSaveDraft}
                disabled={loading}
                className="flex-1 sm:flex-initial flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-semibold text-slate-200 transition-colors disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                <span>Save Draft (ONB-004)</span>
              </button>

              <button
                type="submit"
                disabled={loading}
                className="flex-1 sm:flex-initial flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold shadow-lg shadow-teal-500/20 transition-all disabled:opacity-50"
              >
                <span>Complete Profile &amp; Launch</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Modal: Custom Skill (FRL-002) */}
      {customSkillModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Award className="w-4 h-4 text-teal-400" /> Add Custom Skill (FRL-002)
              </h3>
              <button
                onClick={() => setCustomSkillModal(false)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <p className="text-xs text-slate-400">
              Can't find your specialization? Add a custom skill to your profile.
            </p>
            <form onSubmit={handleAddCustomSkill} className="space-y-4">
              <input
                type="text"
                required
                value={customSkillName}
                onChange={(e) => setCustomSkillName(e.target.value)}
                placeholder="e.g. Astro-Landscape Photography"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomSkillModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950 hover:bg-teal-400"
                >
                  Add Skill
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Custom Service (FRL-003) */}
      {customServiceModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Layers className="w-4 h-4 text-teal-400" /> Add Custom Service (FRL-003)
              </h3>
              <button
                onClick={() => setCustomServiceModal(false)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <p className="text-xs text-slate-400">
              Add a specialized service or package you provide to studios.
            </p>
            <form onSubmit={handleAddCustomService} className="space-y-4">
              <input
                type="text"
                required
                value={customServiceName}
                onChange={(e) => setCustomServiceName(e.target.value)}
                placeholder="e.g. 360-Degree VR Tour Production"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomServiceModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950 hover:bg-teal-400"
                >
                  Add Service
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Custom Equipment Gear (FRL-005) */}
      {customEquipModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Wrench className="w-4 h-4 text-teal-400" /> Add Custom Equipment (FRL-005)
              </h3>
              <button
                onClick={() => setCustomEquipModal(false)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <p className="text-xs text-slate-400">
              Specify the equipment name and select the appropriate gear category.
            </p>
            <form onSubmit={handleAddCustomEquipment} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Equipment Category</label>
                <select
                  value={customEquipCategoryId}
                  onChange={(e) => setCustomEquipCategoryId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-teal-500"
                >
                  {allCategories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Equipment Name / Model</label>
                <input
                  type="text"
                  required
                  value={customEquipName}
                  onChange={(e) => setCustomEquipName(e.target.value)}
                  placeholder="e.g. Sony FX3 Cinema Line"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomEquipModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950 hover:bg-teal-400"
                >
                  Add Gear
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
