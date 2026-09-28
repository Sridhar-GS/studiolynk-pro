import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import {
  Briefcase,
  Calendar,
  IndianRupee,
  ShieldCheck,
  ArrowLeft,
  Save,
  CheckCircle2,
  AlertCircle,
  FileText,
  Lock,
  Wrench,
  Camera,
  Layers
} from 'lucide-react';
import { requirementService } from '../../services/requirementService';
import { catalogueService } from '../../services/catalogueService';
import { WorkRequirementPayload, Skill, ServiceItem, EquipmentItem } from '../../types';

const EVENT_TYPES = [
  'Wedding',
  'Pre-Wedding Shoot',
  'Reception',
  'Engagement',
  'Corporate Event',
  'Fashion / Editorial',
  'Product Photography',
  'Birthday / Anniversary',
  'Baby Shower / Maternity',
  'Cultural & Traditional Ceremony',
  'Commercial Advertisement',
  'Other'
];

export const StudioCreateRequirementPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();

  // Master catalogues
  const [availableSkills, setAvailableSkills] = useState<Skill[]>([]);
  const [availableServices, setAvailableServices] = useState<ServiceItem[]>([]);
  const [availableEquipment, setAvailableEquipment] = useState<EquipmentItem[]>([]);

  // Form state
  const [eventName, setEventName] = useState('');
  const [eventType, setEventType] = useState(EVENT_TYPES[0]);
  const [customEventType, setCustomEventType] = useState('');
  const [eventDate, setEventDate] = useState('');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('18:00');
  const [dayType, setDayType] = useState<'FULL_DAY' | 'HALF_DAY'>('FULL_DAY');
  const [location, setLocation] = useState('');
  const [budget, setBudget] = useState<number | ''>(15000);
  const [description, setDescription] = useState('');

  // Selected IDs
  const [selectedSkillIds, setSelectedSkillIds] = useState<number[]>([]);
  const [selectedServiceIds, setSelectedServiceIds] = useState<number[]>([]);
  const [selectedEquipmentIds, setSelectedEquipmentIds] = useState<number[]>([]);

  // Custom addition inputs
  const [customSkillInput, setCustomSkillInput] = useState('');
  const [customServiceInput, setCustomServiceInput] = useState('');
  const [customEquipmentInput, setCustomEquipmentInput] = useState('');

  // Confidential client contact info (gated)
  const [eventContactName, setEventContactName] = useState('');
  const [eventContactPhone, setEventContactPhone] = useState('');

  // UI status
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(isEditMode);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Load catalogues
  useEffect(() => {
    const loadCatalogues = async () => {
      try {
        const [skills, services, equipment] = await Promise.all([
          catalogueService.getSkills().catch(() => []),
          catalogueService.getServices().catch(() => []),
          catalogueService.getAllEquipment().catch(() => [])
        ]);
        setAvailableSkills(skills);
        setAvailableServices(services);
        setAvailableEquipment(equipment);
      } catch (err) {
        console.error('Failed to load catalogue data', err);
      }
    };
    loadCatalogues();
  }, []);

  // If edit mode, load existing requirement
  useEffect(() => {
    if (!id) return;
    const loadExisting = async () => {
      setInitialLoading(true);
      try {
        const req = await requirementService.getRequirementById(Number(id));
        setEventName(req.eventName);
        if (EVENT_TYPES.includes(req.eventType)) {
          setEventType(req.eventType);
        } else {
          setEventType('Other');
          setCustomEventType(req.eventType);
        }
        setEventDate(req.eventDate);
        setStartTime(req.startTime);
        setEndTime(req.endTime);
        setDayType(req.dayType);
        setLocation(req.location);
        setBudget(req.budget);
        setDescription(req.description || '');
        setSelectedSkillIds(req.requiredSkills?.map((s) => s.id) || []);
        setSelectedServiceIds(req.requiredServices?.map((s) => s.id) || []);
        setSelectedEquipmentIds(req.requiredEquipment?.map((e) => e.id) || []);
        setEventContactName(req.eventContactName || '');
        setEventContactPhone(req.eventContactPhone || '');
      } catch (err: any) {
        console.error('Failed to load requirement for editing', err);
        setError('Failed to fetch requirement details for editing.');
      } finally {
        setInitialLoading(false);
      }
    };
    loadExisting();
  }, [id]);

  // Toggle helper
  const toggleId = (list: number[], setList: (l: number[]) => void, targetId: number) => {
    if (list.includes(targetId)) {
      setList(list.filter((i) => i !== targetId));
    } else {
      setList([...list, targetId]);
    }
  };

  const handleAddCustomSkill = async () => {
    const trimmed = customSkillInput.trim();
    if (!trimmed) return;
    try {
      const created = await catalogueService.createCustomSkill(trimmed);
      setAvailableSkills((prev) => [...prev, created]);
      setSelectedSkillIds((prev) => [...prev, created.id]);
      setCustomSkillInput('');
    } catch (err: any) {
      console.error('Failed to add custom skill', err);
    }
  };

  const handleAddCustomService = async () => {
    const trimmed = customServiceInput.trim();
    if (!trimmed) return;
    try {
      const created = await catalogueService.createCustomService(trimmed);
      setAvailableServices((prev) => [...prev, created]);
      setSelectedServiceIds((prev) => [...prev, created.id]);
      setCustomServiceInput('');
    } catch (err: any) {
      console.error('Failed to add custom service', err);
    }
  };

  const handleAddCustomEquipment = async () => {
    const trimmed = customEquipmentInput.trim();
    if (!trimmed) return;
    try {
      const catId = availableEquipment.length > 0 ? (availableEquipment[0] as any).categoryId || 1 : 1;
      const created = await catalogueService.createCustomEquipment(catId, trimmed);
      setAvailableEquipment((prev) => [...prev, created]);
      setSelectedEquipmentIds((prev) => [...prev, created.id]);
      setCustomEquipmentInput('');
    } catch (err: any) {
      console.error('Failed to add custom equipment', err);
    }
  };

  const handleSubmit = async (targetStatus: 'OPEN' | 'DRAFT') => {
    setError(null);
    setSuccessMsg(null);

    // Basic validation
    if (!eventName.trim()) {
      setError('Event name is required.');
      return;
    }
    const finalEventType = eventType === 'Other' ? customEventType.trim() : eventType;
    if (!finalEventType) {
      setError('Please specify the event type.');
      return;
    }
    if (!eventDate) {
      setError('Please specify the shoot date.');
      return;
    }
    if (!startTime || !endTime) {
      setError('Both start time and end time are required.');
      return;
    }
    if (startTime >= endTime) {
      setError('Start time must be strictly before end time.');
      return;
    }
    if (!location.trim()) {
      setError('Shoot location / venue is required.');
      return;
    }
    if (budget === '' || budget < 0) {
      setError('A valid budget amount is required.');
      return;
    }

    const payload: WorkRequirementPayload = {
      eventName: eventName.trim(),
      eventType: finalEventType,
      eventDate,
      startTime,
      endTime,
      dayType,
      location: location.trim(),
      budget: Number(budget),
      description: description.trim(),
      requiredSkillIds: selectedSkillIds,
      requiredServiceIds: selectedServiceIds,
      requiredEquipmentIds: selectedEquipmentIds,
      eventContactName: eventContactName.trim() || undefined,
      eventContactPhone: eventContactPhone.trim() || undefined,
      status: targetStatus
    };

    setLoading(true);
    try {
      if (isEditMode && id) {
        const updated = await requirementService.updateRequirement(Number(id), payload);
        setSuccessMsg('Work requirement updated successfully!');
        setTimeout(() => {
          navigate(`/studio/requirements/${updated.id}`);
        }, 800);
      } else {
        const created = await requirementService.createRequirement(payload);
        setSuccessMsg(
          targetStatus === 'OPEN'
            ? 'Shoot requirement published! Creators can now view this opportunity.'
            : 'Requirement saved as draft.'
        );
        setTimeout(() => {
          navigate(`/studio/requirements/${created.id}`);
        }, 800);
      }
    } catch (err: any) {
      console.error('Failed to submit requirement', err);
      const msg = err.response?.data?.message || err.message || 'Failed to save requirement.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (initialLoading) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 py-16 flex flex-col items-center justify-center">
        <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-4" />
        <p className="text-sm text-slate-400">Loading requirement specifications...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Back Link */}
        <div className="flex items-center justify-between">
          <Link
            to="/studio/requirements"
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-teal-400 transition"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Requirements</span>
          </Link>
          <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-400 border border-teal-500/30">
            Phase 9: WRK-001 - WRK-008
          </span>
        </div>

        {/* Title Card */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
              <Briefcase className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-extrabold text-white tracking-tight">
                {isEditMode ? 'Edit Work Requirement' : 'Create Shoot Requirement'}
              </h1>
              <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
                Define the event criteria, equipment checklist, and target budget for candidate matching.
              </p>
            </div>
          </div>
        </div>

        {/* Alerts */}
        {error && (
          <div className="bg-rose-950/30 border border-rose-500/40 rounded-2xl p-4 flex items-center gap-3 text-rose-300 text-xs">
            <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
            <span>{error}</span>
          </div>
        )}
        {successMsg && (
          <div className="bg-emerald-950/30 border border-emerald-500/40 rounded-2xl p-4 flex items-center gap-3 text-emerald-300 text-xs">
            <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-400" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Form Container */}
        <div className="space-y-6">
          {/* SECTION 1: Event Fundamentals */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
            <div className="flex items-center gap-2 pb-3 border-b border-slate-800 text-white font-bold text-sm">
              <Calendar className="w-4 h-4 text-teal-400" />
              <span>1. Shoot & Event Fundamentals</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Event Name */}
              <div className="md:col-span-2">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Event Title / Shoot Headline <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={eventName}
                  onChange={(e) => setEventName(e.target.value)}
                  placeholder="e.g., Grand Wedding Reception — Ananya & Karthik"
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>

              {/* Event Type */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Event Category <span className="text-rose-400">*</span>
                </label>
                <select
                  value={eventType}
                  onChange={(e) => setEventType(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                >
                  {EVENT_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              {/* Custom Event Type if Other */}
              {eventType === 'Other' && (
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">
                    Specify Event Type <span className="text-rose-400">*</span>
                  </label>
                  <input
                    type="text"
                    value={customEventType}
                    onChange={(e) => setCustomEventType(e.target.value)}
                    placeholder="e.g. Drone Survey / Documentary"
                    className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              )}

              {/* Shoot Date */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Shoot Date <span className="text-rose-400">*</span>
                </label>
                <input
                  type="date"
                  value={eventDate}
                  onChange={(e) => setEventDate(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              {/* Day Type */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Shoot Coverage <span className="text-rose-400">*</span>
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setDayType('FULL_DAY')}
                    className={`py-2 px-3 rounded-xl text-xs font-semibold border transition ${
                      dayType === 'FULL_DAY'
                        ? 'bg-teal-500/20 text-teal-300 border-teal-500/50'
                        : 'bg-slate-800/60 text-slate-400 border-slate-700 hover:text-white'
                    }`}
                  >
                    Full Day (8-12 hrs)
                  </button>
                  <button
                    type="button"
                    onClick={() => setDayType('HALF_DAY')}
                    className={`py-2 px-3 rounded-xl text-xs font-semibold border transition ${
                      dayType === 'HALF_DAY'
                        ? 'bg-teal-500/20 text-teal-300 border-teal-500/50'
                        : 'bg-slate-800/60 text-slate-400 border-slate-700 hover:text-white'
                    }`}
                  >
                    Half Day (4-5 hrs)
                  </button>
                </div>
              </div>

              {/* Start & End Times */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Call Time (Start) <span className="text-rose-400">*</span>
                </label>
                <input
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Wrap Time (End) <span className="text-rose-400">*</span>
                </label>
                <input
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                />
              </div>

              {/* Shoot Location */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Location / Venue <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  placeholder="e.g. ITC Grand Chola, Guindy, Chennai"
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>

              {/* Budget */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Total Budget (₹ INR) <span className="text-rose-400">*</span>
                </label>
                <div className="relative">
                  <IndianRupee className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="number"
                    min="0"
                    step="500"
                    value={budget}
                    onChange={(e) => setBudget(e.target.value === '' ? '' : Number(e.target.value))}
                    placeholder="e.g. 20000"
                    className="w-full pl-9 pr-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              {/* Description */}
              <div className="md:col-span-2">
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Description & Creative Brief
                </label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Outline the aesthetic mood, key milestones to cover (e.g., bridal entry, stage lighting), deliverable expectations, etc."
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 resize-none"
                />
              </div>
            </div>
          </div>

          {/* SECTION 2: Skills & Specializations */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2 text-white font-bold text-sm">
                <Camera className="w-4 h-4 text-teal-400" />
                <span>2. Required Skills & Styles</span>
              </div>
              <span className="text-xs text-slate-400">
                {selectedSkillIds.length} selected
              </span>
            </div>

            <p className="text-xs text-slate-400">
              Select key proficiencies needed from candidate creators for automatic AI suitability scoring.
            </p>

            {/* Catalogue Chips */}
            <div className="flex flex-wrap gap-2">
              {availableSkills.map((skill) => {
                const isSelected = selectedSkillIds.includes(skill.id);
                return (
                  <button
                    key={skill.id}
                    type="button"
                    onClick={() => toggleId(selectedSkillIds, setSelectedSkillIds, skill.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition ${
                      isSelected
                        ? 'bg-teal-500/20 text-teal-300 border-teal-500/50'
                        : 'bg-slate-800/60 text-slate-400 border-slate-700/80 hover:text-white'
                    }`}
                  >
                    {skill.name}
                  </button>
                );
              })}
            </div>

            {/* Custom Skill Input */}
            <div className="flex items-center gap-2 pt-2">
              <input
                type="text"
                value={customSkillInput}
                onChange={(e) => setCustomSkillInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddCustomSkill();
                  }
                }}
                placeholder="Add custom skill (e.g. Low-light Photography)..."
                className="flex-1 px-3 py-1.5 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <button
                type="button"
                onClick={handleAddCustomSkill}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-slate-700 transition"
              >
                Add Skill
              </button>
            </div>
          </div>

          {/* SECTION 3: Services & Deliverables */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2 text-white font-bold text-sm">
                <Layers className="w-4 h-4 text-teal-400" />
                <span>3. Required Services & Deliverables</span>
              </div>
              <span className="text-xs text-slate-400">
                {selectedServiceIds.length} selected
              </span>
            </div>

            <p className="text-xs text-slate-400">
              Specify deliverables the freelancer is expected to provide on the assignment.
            </p>

            <div className="flex flex-wrap gap-2">
              {availableServices.map((svc) => {
                const isSelected = selectedServiceIds.includes(svc.id);
                return (
                  <button
                    key={svc.id}
                    type="button"
                    onClick={() => toggleId(selectedServiceIds, setSelectedServiceIds, svc.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition ${
                      isSelected
                        ? 'bg-teal-500/20 text-teal-300 border-teal-500/50'
                        : 'bg-slate-800/60 text-slate-400 border-slate-700/80 hover:text-white'
                    }`}
                  >
                    {svc.name}
                  </button>
                );
              })}
            </div>

            {/* Custom Service Input */}
            <div className="flex items-center gap-2 pt-2">
              <input
                type="text"
                value={customServiceInput}
                onChange={(e) => setCustomServiceInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddCustomService();
                  }
                }}
                placeholder="Add custom service (e.g. 4K Drone Reeler)..."
                className="flex-1 px-3 py-1.5 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <button
                type="button"
                onClick={handleAddCustomService}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-slate-700 transition"
              >
                Add Service
              </button>
            </div>
          </div>

          {/* SECTION 4: Gear & Equipment Checklist */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2 text-white font-bold text-sm">
                <Wrench className="w-4 h-4 text-teal-400" />
                <span>4. Gear & Equipment Checklist</span>
              </div>
              <span className="text-xs text-slate-400">
                {selectedEquipmentIds.length} selected
              </span>
            </div>

            <p className="text-xs text-slate-400">
              Identify camera bodies, prime/zoom lenses, gimbals, or lights the freelancer must bring.
            </p>

            <div className="flex flex-wrap gap-2 max-h-48 overflow-y-auto pr-1">
              {availableEquipment.map((eq) => {
                const isSelected = selectedEquipmentIds.includes(eq.id);
                return (
                  <button
                    key={eq.id}
                    type="button"
                    onClick={() => toggleId(selectedEquipmentIds, setSelectedEquipmentIds, eq.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-medium border transition ${
                      isSelected
                        ? 'bg-teal-500/20 text-teal-300 border-teal-500/50'
                        : 'bg-slate-800/60 text-slate-400 border-slate-700/80 hover:text-white'
                    }`}
                  >
                    {eq.name}
                  </button>
                );
              })}
            </div>

            {/* Custom Equipment Input */}
            <div className="flex items-center gap-2 pt-2">
              <input
                type="text"
                value={customEquipmentInput}
                onChange={(e) => setCustomEquipmentInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddCustomEquipment();
                  }
                }}
                placeholder="Add custom gear (e.g. DJI Ronin RS3 Pro)..."
                className="flex-1 px-3 py-1.5 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <button
                type="button"
                onClick={handleAddCustomEquipment}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-slate-700 transition"
              >
                Add Gear
              </button>
            </div>
          </div>

          {/* SECTION 5: Private Client Contact Details (REQ-002, REQ-006 Privacy Gate) */}
          <div className="bg-slate-900/80 border border-teal-500/30 rounded-3xl p-6 space-y-4 relative overflow-hidden">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2 text-teal-300 font-bold text-sm">
                <Lock className="w-4 h-4 text-teal-400" />
                <span>5. Confidential Client Contact Details (Privacy Gated)</span>
              </div>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/40">
                REQ-002 & REQ-006 Protected
              </span>
            </div>

            {/* Privacy Guarantee Alert Banner */}
            <div className="bg-teal-950/40 border border-teal-500/30 rounded-2xl p-4 flex items-start gap-3">
              <ShieldCheck className="w-5 h-5 text-teal-400 shrink-0 mt-0.5" />
              <div className="space-y-1">
                <p className="text-xs font-bold text-teal-300">
                  Strict Confidentiality Architecture:
                </p>
                <p className="text-[11px] text-slate-300 leading-relaxed">
                  These contact details are strictly <strong>masked from public listings and unconfirmed applicants</strong>. 
                  Only your studio and the single confirmed freelancer (post formal acceptance) will ever have access to call or coordinate with this client on shoot day.
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Client / Event Contact Person Name (Optional)
                </label>
                <input
                  type="text"
                  value={eventContactName}
                  onChange={(e) => setEventContactName(e.target.value)}
                  placeholder="e.g. Ramesh Kumar (Bride's Father)"
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Client Direct Phone Number (Optional)
                </label>
                <input
                  type="tel"
                  value={eventContactPhone}
                  onChange={(e) => setEventContactPhone(e.target.value)}
                  placeholder="e.g. +91 98401 23456"
                  className="w-full px-3.5 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                />
              </div>
            </div>
          </div>

          {/* Submission Buttons */}
          <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
            <Link
              to="/studio/requirements"
              className="text-xs text-slate-400 hover:text-white transition"
            >
              Cancel & Return
            </Link>

            <div className="flex items-center gap-3 w-full sm:w-auto">
              {!isEditMode && (
                <button
                  type="button"
                  disabled={loading}
                  onClick={() => handleSubmit('DRAFT')}
                  className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition disabled:opacity-50"
                >
                  <FileText className="w-3.5 h-3.5" />
                  <span>Save as Draft</span>
                </button>
              )}

              <button
                type="button"
                disabled={loading}
                onClick={() => handleSubmit('OPEN')}
                className="flex-1 sm:flex-initial inline-flex items-center justify-center gap-2 px-6 py-2.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold shadow-lg shadow-teal-500/20 transition disabled:opacity-50"
              >
                {loading ? (
                  <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                ) : (
                  <Save className="w-4 h-4" />
                )}
                <span>
                  {isEditMode
                    ? 'Update Requirement'
                    : 'Publish Shoot Requirement'}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
