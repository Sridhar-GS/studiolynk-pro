import React, { useState, useEffect, useMemo } from 'react';
import {
  User,
  MapPin,
  Phone,
  Mail,
  Award,
  Layers,
  Wrench,
  Edit3,
  CheckCircle2,
  AlertCircle,
  Plus,
  Sparkles,
  ArrowLeft,
  Calendar
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { catalogueService } from '../../services/catalogueService';
import { freelancerService } from '../../services/freelancerService';
import { EquipmentCategory, EquipmentItem, FreelancerProfile, FreelancerUpdatePayload, ServiceItem, Skill } from '../../types';

export const FreelancerProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<FreelancerProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Edit form state
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [latitude, setLatitude] = useState<string>('');
  const [longitude, setLongitude] = useState<string>('');
  const [experienceYears, setExperienceYears] = useState<number>(0);
  const [bio, setBio] = useState('');
  const [profilePhotoUrl, setProfilePhotoUrl] = useState('');
  const [fullDayRate, setFullDayRate] = useState<string>('');
  const [halfDayRate, setHalfDayRate] = useState<string>('');

  // Catalogue data for editing
  const [allSkills, setAllSkills] = useState<Skill[]>([]);
  const [allServices, setAllServices] = useState<ServiceItem[]>([]);
  const [allCategories, setAllCategories] = useState<EquipmentCategory[]>([]);
  const [allEquipment, setAllEquipment] = useState<EquipmentItem[]>([]);

  const [selectedSkillIds, setSelectedSkillIds] = useState<number[]>([]);
  const [selectedServiceIds, setSelectedServiceIds] = useState<number[]>([]);
  const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);

  // Modals for adding custom items in edit mode
  const [customSkillModal, setCustomSkillModal] = useState(false);
  const [customSkillName, setCustomSkillName] = useState('');

  const [customServiceModal, setCustomServiceModal] = useState(false);
  const [customServiceName, setCustomServiceName] = useState('');

  const [customEquipModal, setCustomEquipModal] = useState(false);
  const [customEquipName, setCustomEquipName] = useState('');
  const [customEquipCategoryId, setCustomEquipCategoryId] = useState<number | ''>('');

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const data = await freelancerService.getProfile();
      setProfile(data);

      // Populate edit form
      setFullName(data.fullName || '');
      setPhone(data.phone || '');
      setAddress(data.address || '');
      setLatitude(data.latitude ? data.latitude.toString() : '');
      setLongitude(data.longitude ? data.longitude.toString() : '');
      setExperienceYears(data.experienceYears || 0);
      setBio(data.bio || '');
      setProfilePhotoUrl(data.profilePhotoUrl || '');
      setFullDayRate(data.fullDayRate ? data.fullDayRate.toString() : '0');
      setHalfDayRate(data.halfDayRate ? data.halfDayRate.toString() : '0');

      setSelectedSkillIds(data.skills ? data.skills.map((s) => s.id) : []);
      setSelectedServiceIds(data.services ? data.services.map((s) => s.id) : []);
      setSelectedEquipmentIds(data.equipment ? data.equipment.map((e) => e.id) : []);
    } catch (err: any) {
      console.error('Failed to load profile:', err);
      setStatusMessage({ type: 'error', text: 'Failed to load freelancer profile.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
    // Load catalogue for edit mode
    Promise.all([
      catalogueService.getSkills(),
      catalogueService.getServices(),
      catalogueService.getEquipmentCategories(),
      catalogueService.getAllEquipment(),
    ]).then(([skills, services, categories, equipment]) => {
      setAllSkills(skills);
      setAllServices(services);
      setAllCategories(categories);
      setAllEquipment(equipment);
      if (categories.length > 0) {
        setCustomEquipCategoryId(categories[0].id);
      }
    });
  }, []);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setStatusMessage(null);

    const payload: FreelancerUpdatePayload = {
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

    try {
      const updated = await freelancerService.updateProfile(payload);
      setProfile(updated);
      setIsEditing(false);
      setStatusMessage({ type: 'success', text: 'Profile updated successfully!' });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to update profile.';
      setStatusMessage({ type: 'error', text: msg });
    } finally {
      setSaving(false);
    }
  };

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

  const handleAddCustomSkill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customSkillName.trim()) return;
    try {
      const created = await catalogueService.createCustomSkill(customSkillName.trim());
      setAllSkills((prev) => [...prev, created]);
      setSelectedSkillIds((prev) => [...prev, created.id]);
      setCustomSkillName('');
      setCustomSkillModal(false);
    } catch (err) {
      console.error(err);
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
    } catch (err) {
      console.error(err);
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
    } catch (err) {
      console.error(err);
    }
  };

  // Group gear by category
  const equipmentByCategory = useMemo(() => {
    const map = new Map<string, EquipmentItem[]>();
    if (!profile || !profile.equipment) return map;
    profile.equipment.forEach((item) => {
      const cat = item.categoryName || 'General Gear';
      if (!map.has(cat)) map.set(cat, []);
      map.get(cat)!.push(item);
    });
    return map;
  }, [profile]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading Freelancer Profile...</p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6">
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 max-w-md text-center space-y-4">
          <AlertCircle className="w-12 h-12 text-amber-400 mx-auto" />
          <h2 className="text-lg font-bold text-white">Profile Not Found</h2>
          <p className="text-xs text-slate-400">
            You have not completed your onboarding profile yet.
          </p>
          <Link
            to="/onboarding/freelancer"
            className="inline-block px-5 py-2.5 rounded-xl bg-teal-500 text-slate-950 font-bold text-xs"
          >
            Start Freelancer Onboarding
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Navigation Breadcrumb */}
        <div className="flex items-center justify-between">
          <Link
            to="/freelancer/dashboard"
            className="flex items-center gap-2 text-xs text-slate-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Dashboard</span>
          </Link>

          <button
            type="button"
            onClick={() => setIsEditing(!isEditing)}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold border transition-all ${
              isEditing
                ? 'bg-slate-800 border-slate-700 text-slate-300'
                : 'bg-teal-500/10 border-teal-500/30 text-teal-300 hover:bg-teal-500/20'
            }`}
          >
            <Edit3 className="w-3.5 h-3.5" />
            <span>{isEditing ? 'Cancel Editing' : 'Edit Profile (FRL-006)'}</span>
          </button>
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

        {/* Profile Card Header */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 relative overflow-hidden backdrop-blur-md shadow-2xl">
          <div className="absolute -right-20 -top-20 w-60 h-60 bg-teal-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="flex flex-col md:flex-row items-start md:items-center gap-6 justify-between">
            <div className="flex items-center gap-5">
              <div className="w-20 h-20 rounded-2xl bg-slate-800 border-2 border-teal-500/40 flex items-center justify-center overflow-hidden shrink-0">
                {profile.profilePhotoUrl ? (
                  <img
                    src={profile.profilePhotoUrl}
                    alt={profile.fullName}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-10 h-10 text-teal-400" />
                )}
              </div>

              <div>
                <div className="flex flex-wrap items-center gap-2 mb-1.5">
                  <h1 className="text-2xl font-black text-white tracking-tight">{profile.fullName}</h1>
                  <span className="px-2.5 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 text-[11px] font-semibold flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3" /> Verified Creator
                  </span>
                </div>

                <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
                  <span className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-teal-400" />
                    {profile.address || 'Location set'}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 text-teal-400" />
                    {profile.phone}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <Mail className="w-3.5 h-3.5 text-teal-400" />
                    {profile.email}
                  </span>
                </div>
              </div>
            </div>

            {/* Rates & Score */}
            <div className="flex flex-wrap sm:flex-nowrap items-center gap-3 w-full md:w-auto">
              <div className="bg-slate-950/80 border border-slate-800 px-4 py-3 rounded-2xl text-center flex-1 sm:flex-initial">
                <span className="text-[10px] uppercase font-semibold text-slate-500 block">Full-Day Rate</span>
                <span className="text-base font-extrabold text-teal-400">₹{profile.fullDayRate || 0}</span>
              </div>
              <div className="bg-slate-950/80 border border-slate-800 px-4 py-3 rounded-2xl text-center flex-1 sm:flex-initial">
                <span className="text-[10px] uppercase font-semibold text-slate-500 block">Half-Day Rate</span>
                <span className="text-base font-extrabold text-teal-400">₹{profile.halfDayRate || 0}</span>
              </div>
              <div className="bg-slate-950/80 border border-slate-800 px-4 py-3 rounded-2xl text-center flex-1 sm:flex-initial">
                <span className="text-[10px] uppercase font-semibold text-slate-500 block">Completion</span>
                <span className="text-base font-extrabold text-emerald-400">{profile.completionPercentage}%</span>
              </div>
            </div>
          </div>
        </div>

        {/* View Mode vs Edit Mode */}
        {!isEditing ? (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left 2 Cols: Bio, Skills, Services, Gear */}
            <div className="lg:col-span-2 space-y-8">
              {/* Bio Card */}
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-3">
                <h2 className="text-base font-bold text-white flex items-center gap-2">
                  <User className="w-4 h-4 text-teal-400" /> About &amp; Artistic Style
                </h2>
                <p className="text-sm text-slate-300 leading-relaxed">
                  {profile.bio || 'No bio description provided yet. Click "Edit Profile" to add details.'}
                </p>
                <div className="pt-2 flex items-center gap-4 text-xs text-slate-400">
                  <span><strong>{profile.experienceYears}</strong> Years Professional Experience</span>
                  {profile.latitude && profile.longitude && (
                    <span>Coordinates: {profile.latitude}, {profile.longitude}</span>
                  )}
                </div>
              </div>

              {/* Skills Card (FRL-002) */}
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
                <h2 className="text-base font-bold text-white flex items-center gap-2">
                  <Award className="w-4 h-4 text-teal-400" /> Photography &amp; Cinematography Skills
                </h2>
                {profile.skills && profile.skills.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {profile.skills.map((skill) => (
                      <span
                        key={skill.id}
                        className="px-3.5 py-1.5 rounded-xl text-xs font-medium bg-teal-500/10 border border-teal-500/30 text-teal-300"
                      >
                        {skill.name}
                        {skill.custom && <span className="ml-1 text-[10px] text-teal-400/80">&bull; custom</span>}
                      </span>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500">No skills selected yet.</p>
                )}
              </div>

              {/* Services Card (FRL-003) */}
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
                <h2 className="text-base font-bold text-white flex items-center gap-2">
                  <Layers className="w-4 h-4 text-teal-400" /> Services &amp; Coverage Types
                </h2>
                {profile.services && profile.services.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {profile.services.map((service) => (
                      <span
                        key={service.id}
                        className="px-3.5 py-1.5 rounded-xl text-xs font-medium bg-indigo-500/10 border border-indigo-500/30 text-indigo-300"
                      >
                        {service.name}
                        {service.custom && <span className="ml-1 text-[10px] text-indigo-400/80">&bull; custom</span>}
                      </span>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500">No services selected yet.</p>
                )}
              </div>

              {/* Equipment Arsenal (FRL-004, FRL-005) */}
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-5">
                <h2 className="text-base font-bold text-white flex items-center gap-2">
                  <Wrench className="w-4 h-4 text-teal-400" /> Equipment &amp; Gear Arsenal
                </h2>
                {equipmentByCategory.size > 0 ? (
                  <div className="space-y-4">
                    {Array.from(equipmentByCategory.entries()).map(([catName, gearList]) => (
                      <div key={catName} className="bg-slate-950/60 border border-slate-800/80 p-4 rounded-2xl">
                        <span className="text-xs font-semibold text-slate-400 block mb-2 uppercase tracking-wider">
                          {catName}
                        </span>
                        <div className="flex flex-wrap gap-2">
                          {gearList.map((g) => (
                            <span
                              key={g.id}
                              className="px-3 py-1 rounded-xl text-xs bg-slate-900 border border-slate-700/80 text-slate-200"
                            >
                              {g.name}
                              {g.custom && <span className="ml-1 text-[10px] text-teal-400">&bull; custom</span>}
                            </span>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500">No equipment added yet.</p>
                )}
              </div>
            </div>

            {/* Right 1 Col: Quick Actions & Availability Preview */}
            <div className="space-y-6">
              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Calendar className="w-4 h-4 text-teal-400" /> Availability &amp; Booking
                </h3>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Manage your rolling 10-day availability calendar so studios can instantly check your open shoot dates.
                </p>
                <div className="bg-slate-950/70 border border-slate-800 p-3.5 rounded-2xl text-xs text-teal-300">
                  Availability module configured in upcoming Phase 8.
                </div>
              </div>

              <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-teal-400" /> Portfolio Showcase (POR-001)
                </h3>
                <p className="text-xs text-slate-400 leading-relaxed">
                  High-resolution photo galleries and category-based projects hosted securely on AWS S3.
                </p>
                <Link
                  to="/freelancer/portfolio"
                  className="block text-center px-4 py-2.5 rounded-xl bg-teal-500/10 hover:bg-teal-500/20 text-teal-300 border border-teal-500/30 text-xs font-semibold transition-colors"
                >
                  Manage Portfolio &amp; Upload Images
                </Link>
              </div>
            </div>
          </div>
        ) : (
          /* EDIT PROFILE FORM (FRL-006) */
          <form onSubmit={handleUpdate} className="space-y-8">
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6">
              <h2 className="text-lg font-bold text-white border-b border-slate-800 pb-4">
                Edit Freelancer Profile Details
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Full Name</label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Phone Number</label>
                  <input
                    type="text"
                    required
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Address</label>
                  <input
                    type="text"
                    required
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Latitude</label>
                  <input
                    type="number"
                    step="any"
                    value={latitude}
                    onChange={(e) => setLatitude(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Longitude</label>
                  <input
                    type="number"
                    step="any"
                    value={longitude}
                    onChange={(e) => setLongitude(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Years of Experience</label>
                  <input
                    type="number"
                    min="0"
                    value={experienceYears}
                    onChange={(e) => setExperienceYears(parseInt(e.target.value) || 0)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Profile Photo URL</label>
                  <input
                    type="url"
                    value={profilePhotoUrl}
                    onChange={(e) => setProfilePhotoUrl(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Full-Day Rate (₹)</label>
                  <input
                    type="number"
                    min="0"
                    step="500"
                    value={fullDayRate}
                    onChange={(e) => setFullDayRate(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Half-Day Rate (₹)</label>
                  <input
                    type="number"
                    min="0"
                    step="500"
                    value={halfDayRate}
                    onChange={(e) => setHalfDayRate(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>

                <div className="sm:col-span-2">
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">Bio</label>
                  <textarea
                    rows={3}
                    value={bio}
                    onChange={(e) => setBio(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>
            </div>

            {/* Skills selection in Edit Mode */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white">Edit Skills (FRL-002)</h3>
                <button
                  type="button"
                  onClick={() => setCustomSkillModal(true)}
                  className="text-xs text-teal-400 hover:text-teal-300 flex items-center gap-1"
                >
                  <Plus className="w-3.5 h-3.5" /> Add Custom
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                {allSkills.map((s) => {
                  const active = selectedSkillIds.includes(s.id);
                  return (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => toggleSkill(s.id)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition-colors ${
                        active
                          ? 'bg-teal-500/20 border-teal-500 text-teal-300'
                          : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      {s.name}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Services selection in Edit Mode */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white">Edit Services (FRL-003)</h3>
                <button
                  type="button"
                  onClick={() => setCustomServiceModal(true)}
                  className="text-xs text-teal-400 hover:text-teal-300 flex items-center gap-1"
                >
                  <Plus className="w-3.5 h-3.5" /> Add Custom
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                {allServices.map((srv) => {
                  const active = selectedServiceIds.includes(srv.id);
                  return (
                    <button
                      key={srv.id}
                      type="button"
                      onClick={() => toggleService(srv.id)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition-colors ${
                        active
                          ? 'bg-indigo-500/20 border-indigo-500 text-indigo-300'
                          : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      {srv.name}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Gear selection in Edit Mode */}
            <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <h3 className="text-sm font-bold text-white">Edit Equipment Gear (FRL-004, FRL-005)</h3>
                <button
                  type="button"
                  onClick={() => setCustomEquipModal(true)}
                  className="text-xs text-teal-400 hover:text-teal-300 flex items-center gap-1"
                >
                  <Plus className="w-3.5 h-3.5" /> Add Custom Gear
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                {allEquipment.map((eq) => {
                  const active = selectedEquipmentIds.includes(eq.id);
                  return (
                    <button
                      key={eq.id}
                      type="button"
                      onClick={() => toggleEquipment(eq.id)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition-colors ${
                        active
                          ? 'bg-teal-500/20 border-teal-500 text-teal-300'
                          : 'bg-slate-950 border-slate-800 text-slate-400 hover:text-slate-200'
                      }`}
                    >
                      {eq.name}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="px-5 py-2.5 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-6 py-2.5 rounded-xl text-xs font-bold bg-teal-500 text-slate-950 hover:bg-teal-400 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Profile Changes'}
              </button>
            </div>
          </form>
        )}
      </div>

      {/* Modal: Custom Skill */}
      {customSkillModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <h3 className="text-base font-bold text-white">Add Custom Skill</h3>
            <form onSubmit={handleAddCustomSkill} className="space-y-4">
              <input
                type="text"
                required
                value={customSkillName}
                onChange={(e) => setCustomSkillName(e.target.value)}
                placeholder="Skill name"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomSkillModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950"
                >
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Custom Service */}
      {customServiceModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <h3 className="text-base font-bold text-white">Add Custom Service</h3>
            <form onSubmit={handleAddCustomService} className="space-y-4">
              <input
                type="text"
                required
                value={customServiceName}
                onChange={(e) => setCustomServiceName(e.target.value)}
                placeholder="Service name"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomServiceModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950"
                >
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Custom Equipment */}
      {customEquipModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <h3 className="text-base font-bold text-white">Add Custom Equipment</h3>
            <form onSubmit={handleAddCustomEquipment} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">Category</label>
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
                <label className="block text-xs font-medium text-slate-400 mb-1">Gear Name</label>
                <input
                  type="text"
                  required
                  value={customEquipName}
                  onChange={(e) => setCustomEquipName(e.target.value)}
                  placeholder="e.g. Sony A7S III"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCustomEquipModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950"
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
