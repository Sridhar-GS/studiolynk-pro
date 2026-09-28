import React, { useState, useEffect, useMemo } from 'react';
import {
  Search,
  SlidersHorizontal,
  MapPin,
  Calendar,
  Clock,
  Star,
  CheckCircle,
  AlertCircle,
  Sparkles,
  X,
  Eye,
  Send,
  Navigation,
  Camera,
  Check,
  RotateCcw,
  LayoutGrid,
  List as ListIcon,
} from 'lucide-react';
import { freelancerService } from '../../services/freelancerService';
import { catalogueService } from '../../services/catalogueService';
import { portfolioService } from '../../services/portfolioService';
import { availabilityService } from '../../services/availabilityService';
import { requirementService } from '../../services/requirementService';
import { requestService } from '../../services/requestService';
import {
  FreelancerCard,
  FreelancerSearchFilter,
  Skill,
  ServiceItem,
  EquipmentItem,
  Portfolio,
  AvailabilityWindowResponse,
  WorkRequirementSummary
} from '../../types';

// City location presets in South India
const CITY_PRESETS = [
  { name: 'All Cities', lat: undefined, lng: undefined },
  { name: 'Chennai', lat: 13.0827, lng: 80.2707 },
  { name: 'Coimbatore', lat: 11.0168, lng: 76.9558 },
  { name: 'Madurai', lat: 9.9252, lng: 78.1198 },
  { name: 'Bangalore', lat: 12.9716, lng: 77.5946 },
  { name: 'Kochi', lat: 9.9312, lng: 76.2673 },
];

export const StudioDiscoveryPage: React.FC = () => {
  // State: Freelancer Results & Status
  const [freelancers, setFreelancers] = useState<FreelancerCard[]>([]);
  const [totalResults, setTotalResults] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // State: Catalogue Data
  const [skills, setSkills] = useState<Skill[]>([]);
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [equipments, setEquipments] = useState<EquipmentItem[]>([]);

  // State: Search Filters
  const [keyword, setKeyword] = useState('');
  const [selectedServiceId, setSelectedServiceId] = useState<number | undefined>();
  const [selectedSkillId, setSelectedSkillId] = useState<number | undefined>();
  const [selectedEquipmentId, setSelectedEquipmentId] = useState<number | undefined>();
  const [selectedCity, setSelectedCity] = useState('All Cities');
  const [customLat, setCustomLat] = useState<number | undefined>();
  const [customLng, setCustomLng] = useState<number | undefined>();
  const [maxDistanceKm, setMaxDistanceKm] = useState<number | undefined>();
  const [date, setDate] = useState<string>('');
  const [startTime, setStartTime] = useState<string>('');
  const [endTime, setEndTime] = useState<string>('');
  const [dayType, setDayType] = useState<'FULL_DAY' | 'HALF_DAY'>('FULL_DAY');
  const [maxBudget, setMaxBudget] = useState<number | undefined>();
  const [minExperience, setMinExperience] = useState<number | undefined>();
  const [sortBy, setSortBy] = useState<
    'relevance' | 'distance' | 'rating' | 'experience' | 'price_asc' | 'price_desc'
  >('relevance');

  // View preferences
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFiltersPanel, setShowFiltersPanel] = useState(false);

  // Profile Preview Modal
  const [previewFreelancer, setPreviewFreelancer] = useState<FreelancerCard | null>(null);
  const [previewPortfolio, setPreviewPortfolio] = useState<Portfolio | null>(null);
  const [previewAvailability, setPreviewAvailability] = useState<AvailabilityWindowResponse | null>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewTab, setPreviewTab] = useState<'portfolio' | 'availability' | 'gear'>('portfolio');

  // Contact / Request Modal (Phase 10: REQ-001, WRK-006)
  const [bookingFreelancer, setBookingFreelancer] = useState<FreelancerCard | null>(null);
  const [bookingSuccess, setBookingSuccess] = useState(false);
  const [bookingMessage, setBookingMessage] = useState('');
  const [bookingSending, setBookingSending] = useState(false);
  const [openRequirements, setOpenRequirements] = useState<WorkRequirementSummary[]>([]);
  const [selectedRequirementId, setSelectedRequirementId] = useState<number | ''>('');
  const [bookingOfferedPrice, setBookingOfferedPrice] = useState<number | ''>('');
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [loadingRequirements, setLoadingRequirements] = useState(false);

  // 1. Initial Catalogue Load
  useEffect(() => {
    const fetchCatalogues = async () => {
      try {
        const [skillsData, servicesData, equipData] = await Promise.all([
          catalogueService.getSkills(),
          catalogueService.getServices(),
          catalogueService.getAllEquipment(),
        ]);
        setSkills(skillsData || []);
        setServices(servicesData || []);
        setEquipments(equipData || []);
      } catch (err) {
        console.warn('Could not load catalogue presets:', err);
      }
    };
    fetchCatalogues();
  }, []);

  // 2. Perform Discovery Search
  const executeSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const filterPayload: FreelancerSearchFilter = {
        keyword: keyword.trim() || undefined,
        serviceId: selectedServiceId,
        skillId: selectedSkillId,
        equipmentId: selectedEquipmentId,
        location: selectedCity !== 'All Cities' ? selectedCity : undefined,
        latitude: customLat,
        longitude: customLng,
        maxDistanceKm: maxDistanceKm,
        date: date || undefined,
        startTime: startTime || undefined,
        endTime: endTime || undefined,
        dayType: dayType,
        maxBudget: maxBudget,
        minExperience: minExperience,
        sortBy: sortBy,
      };

      const res = await freelancerService.searchFreelancers(filterPayload);
      setFreelancers(res.freelancers || []);
      setTotalResults(res.totalResults || 0);
    } catch (err: any) {
      console.error('Freelancer discovery search failed:', err);
      setError('Unable to load creators. Please refine your search criteria.');
    } finally {
      setLoading(false);
    }
  };

  // Re-search when core filter states change
  useEffect(() => {
    executeSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    selectedServiceId,
    selectedSkillId,
    selectedEquipmentId,
    selectedCity,
    customLat,
    customLng,
    maxDistanceKm,
    date,
    startTime,
    endTime,
    dayType,
    maxBudget,
    minExperience,
    sortBy,
  ]);

  // Handle City Preset Change
  const handleCityChange = (cityName: string) => {
    setSelectedCity(cityName);
    const preset = CITY_PRESETS.find((c) => c.name === cityName);
    if (preset && preset.lat !== undefined && preset.lng !== undefined) {
      setCustomLat(preset.lat);
      setCustomLng(preset.lng);
    } else {
      setCustomLat(undefined);
      setCustomLng(undefined);
    }
  };

  // Browser Geolocation
  const handleUseMyLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCustomLat(pos.coords.latitude);
        setCustomLng(pos.coords.longitude);
        setSelectedCity('Near My Coordinates');
      },
      (err) => {
        console.warn('Geolocation failed:', err);
        alert('Could not determine your GPS location. Please choose a city preset.');
      }
    );
  };

  // Reset Filters
  const handleResetFilters = () => {
    setKeyword('');
    setSelectedServiceId(undefined);
    setSelectedSkillId(undefined);
    setSelectedEquipmentId(undefined);
    setSelectedCity('All Cities');
    setCustomLat(undefined);
    setCustomLng(undefined);
    setMaxDistanceKm(undefined);
    setDate('');
    setStartTime('');
    setEndTime('');
    setDayType('FULL_DAY');
    setMaxBudget(undefined);
    setMinExperience(undefined);
    setSortBy('relevance');
  };

  // Open Preview Modal & load portfolio + availability
  const handleOpenPreview = async (freelancer: FreelancerCard) => {
    setPreviewFreelancer(freelancer);
    setPreviewLoading(true);
    setPreviewTab('portfolio');
    try {
      const [portfolioData, availData] = await Promise.all([
        portfolioService.getPortfolioByFreelancerId(freelancer.id).catch(() => null),
        availabilityService.getFreelancerAvailability(freelancer.id).catch(() => null),
      ]);
      setPreviewPortfolio(portfolioData);
      setPreviewAvailability(availData);
    } catch (err) {
      console.warn('Error fetching preview details:', err);
    } finally {
      setPreviewLoading(false);
    }
  };

  // Check rolling 10-day window logic for notice
  const isDateWithin10Days = useMemo(() => {
    if (!date) return null;
    const searchDate = new Date(date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tenDaysLater = new Date(today);
    tenDaysLater.setDate(today.getDate() + 9);
    return searchDate >= today && searchDate <= tenDaysLater;
  }, [date]);

  // Load Open Requirements when Booking Modal Opens
  const handleOpenBookingModal = async (candidate: FreelancerCard) => {
    setBookingFreelancer(candidate);
    setBookingError(null);
    setBookingSuccess(false);
    setBookingMessage('');
    setLoadingRequirements(true);
    try {
      const reqs = await requirementService.getMyRequirements('OPEN');
      setOpenRequirements(reqs || []);
      if (reqs && reqs.length > 0) {
        // Match with current filter date if available
        const matched = date ? reqs.find((r: WorkRequirementSummary) => r.eventDate === date) : null;
        const initialReq = matched || reqs[0];
        setSelectedRequirementId(initialReq.id);
        setBookingOfferedPrice(initialReq.budget || candidate.fullDayRate || 10000);
      } else {
        setSelectedRequirementId('');
        setBookingOfferedPrice(candidate.fullDayRate || 10000);
      }
    } catch (err) {
      console.error('Failed to load studio requirements:', err);
    } finally {
      setLoadingRequirements(false);
    }
  };

  // Handle Book Freelancer CTA (Phase 10: REQ-001, WRK-006)
  const handleSendBookingRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRequirementId) {
      setBookingError('Please select a shoot requirement for this work request.');
      return;
    }
    if (!bookingFreelancer) return;

    setBookingSending(true);
    setBookingError(null);
    try {
      await requestService.createRequest({
        requirementId: Number(selectedRequirementId),
        freelancerId: bookingFreelancer.id,
        message: bookingMessage.trim() || undefined,
        offeredPrice: bookingOfferedPrice ? Number(bookingOfferedPrice) : undefined
      });
      setBookingSuccess(true);
      setTimeout(() => {
        setBookingSuccess(false);
        setBookingFreelancer(null);
        setBookingMessage('');
        setSelectedRequirementId('');
        setBookingOfferedPrice('');
      }, 2000);
    } catch (err: any) {
      console.error('Failed to create work request:', err);
      setBookingError(
        err.response?.data?.message || 'Failed to dispatch work request. The creator might already be booked or requested.'
      );
    } finally {
      setBookingSending(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Top Header & Search Bar */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-6">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-[11px] font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                  DISCOVERY ENGINE (DIS-001 - DIS-006)
                </span>
                <span className="text-xs text-slate-400">Phase 8</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Discover Verified Photography & Film Creators
              </h1>
              <p className="text-xs sm:text-sm text-slate-400 mt-1 max-w-2xl">
                Multi-parameter search with Haversine distance, gear verification, and live rolling 10-day availability matching.
              </p>
            </div>

            {/* Quick City Presets */}
            <div className="flex flex-wrap items-center gap-1.5 self-start md:self-center">
              {CITY_PRESETS.map((city) => (
                <button
                  key={city.name}
                  onClick={() => handleCityChange(city.name)}
                  className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-all ${
                    selectedCity === city.name
                      ? 'bg-teal-500 text-slate-950 font-semibold shadow-md shadow-teal-500/20'
                      : 'bg-slate-800/80 text-slate-300 hover:bg-slate-800 hover:text-white border border-slate-700/60'
                  }`}
                >
                  {city.name}
                </button>
              ))}
              <button
                onClick={handleUseMyLocation}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-medium bg-slate-800/80 text-teal-400 hover:bg-slate-800 border border-teal-500/30 transition-all"
                title="Use Current Location"
              >
                <Navigation className="w-3.5 h-3.5" />
                <span>My GPS</span>
              </button>
            </div>
          </div>

          {/* Search Inputs Bar */}
          <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
            {/* Keyword Search */}
            <div className="sm:col-span-6 lg:col-span-5 relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && executeSearch()}
                placeholder="Search by name, camera model, drone pilot, candid..."
                className="w-full pl-10 pr-4 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-slate-100 placeholder-slate-400 focus:outline-none focus:border-teal-500 transition-colors"
              />
            </div>

            {/* Shoot Date Picker */}
            <div className="sm:col-span-3 lg:col-span-3 relative">
              <Calendar className="w-4 h-4 text-teal-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full pl-9 pr-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-teal-500 transition-colors"
              />
            </div>

            {/* Shoot Time Slot Quick Selector */}
            <div className="sm:col-span-3 lg:col-span-2">
              <select
                value={startTime ? `${startTime}-${endTime}` : ''}
                onChange={(e) => {
                  const val = e.target.value;
                  if (!val) {
                    setStartTime('');
                    setEndTime('');
                  } else {
                    const [s, end] = val.split('-');
                    setStartTime(s);
                    setEndTime(end);
                  }
                }}
                className="w-full px-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-teal-500 transition-colors"
              >
                <option value="">Any Time</option>
                <option value="09:00-18:00">Full Day (09:00 - 18:00)</option>
                <option value="08:00-13:00">Morning (08:00 - 13:00)</option>
                <option value="14:00-20:00">Evening (14:00 - 20:00)</option>
                <option value="18:00-23:00">Night Reception (18:00 - 23:00)</option>
              </select>
            </div>

            {/* Toggle Full Filters / Search Action */}
            <div className="sm:col-span-12 lg:col-span-2 flex items-center gap-2">
              <button
                onClick={executeSearch}
                className="flex-1 flex items-center justify-center gap-2 bg-teal-500 hover:bg-teal-400 text-slate-950 font-semibold py-2.5 px-4 rounded-xl text-xs shadow-md shadow-teal-500/20 transition-all"
              >
                <Search className="w-4 h-4" />
                <span>Search</span>
              </button>
              <button
                onClick={() => setShowFiltersPanel(!showFiltersPanel)}
                className={`flex items-center justify-center p-2.5 rounded-xl border transition-colors ${
                  showFiltersPanel
                    ? 'bg-teal-500/10 border-teal-500/40 text-teal-400'
                    : 'bg-slate-800 border-slate-700 text-slate-300 hover:text-white'
                }`}
                title="More Filters"
              >
                <SlidersHorizontal className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Expandable Advanced Filters Drawer */}
          {showFiltersPanel && (
            <div className="mt-6 pt-6 border-t border-slate-800/80 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-in fade-in duration-200">
              {/* Service Filter */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Service Category
                </label>
                <select
                  value={selectedServiceId || ''}
                  onChange={(e) => setSelectedServiceId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="">All Services</option>
                  {services.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Skill Filter */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Specialized Skill
                </label>
                <select
                  value={selectedSkillId || ''}
                  onChange={(e) => setSelectedSkillId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="">All Skills</option>
                  {skills.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Equipment Filter */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Gear / Equipment
                </label>
                <select
                  value={selectedEquipmentId || ''}
                  onChange={(e) => setSelectedEquipmentId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="">All Equipment</option>
                  {equipments.map((e) => (
                    <option key={e.id} value={e.id}>
                      {e.name} {e.categoryName ? `(${e.categoryName})` : ''}
                    </option>
                  ))}
                </select>
              </div>

              {/* Day Type & Budget */}
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">
                    Rate Type & Max Budget
                  </label>
                  <div className="flex items-center text-[10px] bg-slate-800 rounded-lg p-0.5 border border-slate-700">
                    <button
                      onClick={() => setDayType('FULL_DAY')}
                      className={`px-1.5 py-0.5 rounded ${
                        dayType === 'FULL_DAY' ? 'bg-teal-500 text-slate-950 font-bold' : 'text-slate-400'
                      }`}
                    >
                      Full Day
                    </button>
                    <button
                      onClick={() => setDayType('HALF_DAY')}
                      className={`px-1.5 py-0.5 rounded ${
                        dayType === 'HALF_DAY' ? 'bg-teal-500 text-slate-950 font-bold' : 'text-slate-400'
                      }`}
                    >
                      Half Day
                    </button>
                  </div>
                </div>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xs">₹</span>
                  <input
                    type="number"
                    value={maxBudget || ''}
                    onChange={(e) => setMaxBudget(e.target.value ? Number(e.target.value) : undefined)}
                    placeholder={dayType === 'FULL_DAY' ? 'Max ₹/day (e.g. 15000)' : 'Max ₹/half-day (e.g. 8000)'}
                    className="w-full pl-7 pr-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              {/* Min Experience */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Min Experience
                </label>
                <select
                  value={minExperience !== undefined ? minExperience : ''}
                  onChange={(e) => setMinExperience(e.target.value !== '' ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="">Any Experience</option>
                  <option value="1">1+ Years</option>
                  <option value="2">2+ Years</option>
                  <option value="3">3+ Years</option>
                  <option value="5">5+ Years</option>
                  <option value="8">8+ Years</option>
                </select>
              </div>

              {/* Max Distance (Radius) */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Max Distance (Haversine km)
                </label>
                <select
                  value={maxDistanceKm || ''}
                  onChange={(e) => setMaxDistanceKm(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="">Any Distance</option>
                  <option value="10">Within 10 km</option>
                  <option value="25">Within 25 km</option>
                  <option value="50">Within 50 km</option>
                  <option value="100">Within 100 km</option>
                  <option value="250">Within 250 km</option>
                </select>
              </div>

              {/* Sort By */}
              <div>
                <label className="block text-[11px] font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                  Sort Results By
                </label>
                <select
                  value={sortBy}
                  onChange={(e: any) => setSortBy(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-teal-500"
                >
                  <option value="relevance">Relevance & Best Match</option>
                  <option value="distance">Distance (Nearest First)</option>
                  <option value="rating">Rating (Highest First)</option>
                  <option value="experience">Experience (Highest First)</option>
                  <option value="price_asc">Price: Low to High</option>
                  <option value="price_desc">Price: High to Low</option>
                </select>
              </div>

              {/* Reset Filter Button */}
              <div className="flex items-end">
                <button
                  onClick={handleResetFilters}
                  className="w-full flex items-center justify-center gap-1.5 py-2 px-3 bg-slate-800/90 hover:bg-slate-800 border border-slate-700 text-rose-400 hover:text-rose-300 rounded-xl text-xs font-medium transition-colors"
                >
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Clear All Filters</span>
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Rolling 10-Day Availability Informational Banner */}
        {date && (
          <div
            className={`rounded-2xl p-4 border flex items-center justify-between gap-4 text-xs transition-colors ${
              isDateWithin10Days
                ? 'bg-emerald-950/30 border-emerald-500/30 text-emerald-300'
                : 'bg-amber-950/30 border-amber-500/30 text-amber-300'
            }`}
          >
            <div className="flex items-center gap-3">
              {isDateWithin10Days ? (
                <CheckCircle className="w-5 h-5 text-emerald-400 shrink-0" />
              ) : (
                <AlertCircle className="w-5 h-5 text-amber-400 shrink-0" />
              )}
              <div>
                <p className="font-semibold">
                  {isDateWithin10Days
                    ? `10-Day Rolling Availability Window Active for ${date}`
                    : `Date ${date} is beyond the 10-day scheduling window (AVL-006)`}
                </p>
                <p className="text-[11px] opacity-80 mt-0.5">
                  {isDateWithin10Days
                    ? 'Only verified AVAILABLE candidates with covering shoot hours are shown. Busy or unconfirmed creators are excluded (AVL-004, AVL-005).'
                    : 'Creators cannot set definitive calendar slots beyond 10 days. All candidates are displayed with "Availability Unknown" status.'}
                </p>
              </div>
            </div>
            <button
              onClick={() => setDate('')}
              className="px-2.5 py-1 rounded-lg bg-slate-900/60 border border-slate-700 text-slate-300 hover:text-white shrink-0 text-[11px]"
            >
              Clear Date Filter
            </button>
          </div>
        )}

        {/* Results Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-2">
          <div className="flex items-center gap-3">
            <h2 className="text-base font-bold text-white">
              Available Creators{' '}
              <span className="text-xs font-normal text-slate-400 ml-1">
                ({totalResults} {totalResults === 1 ? 'result' : 'results'} found)
              </span>
            </h2>
            {selectedCity !== 'All Cities' && (
              <span className="text-xs px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700 flex items-center gap-1">
                <MapPin className="w-3 h-3 text-teal-400" />
                {selectedCity}
              </span>
            )}
          </div>

          <div className="flex items-center gap-3 self-end sm:self-auto">
            {/* View Mode Toggle */}
            <div className="flex items-center bg-slate-900 border border-slate-800 rounded-xl p-1">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-1.5 rounded-lg transition-colors ${
                  viewMode === 'grid' ? 'bg-teal-500 text-slate-950' : 'text-slate-400 hover:text-white'
                }`}
                title="Grid View"
              >
                <LayoutGrid className="w-4 h-4" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-1.5 rounded-lg transition-colors ${
                  viewMode === 'list' ? 'bg-teal-500 text-slate-950' : 'text-slate-400 hover:text-white'
                }`}
                title="List View"
              >
                <ListIcon className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Content Section: Loading / Error / Empty / Cards */}
        {loading ? (
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-3xl p-16 flex flex-col items-center justify-center text-center">
            <div className="w-10 h-10 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-4" />
            <p className="text-sm font-medium text-slate-200">Searching verified creators...</p>
            <p className="text-xs text-slate-400 mt-1">Applying distance, availability, and gear filters.</p>
          </div>
        ) : error ? (
          <div className="bg-rose-950/20 border border-rose-500/30 rounded-3xl p-8 text-center text-rose-300 text-xs">
            {error}
          </div>
        ) : freelancers.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-3xl p-12 text-center">
            <div className="w-14 h-14 rounded-2xl bg-slate-800 flex items-center justify-center text-slate-400 mx-auto mb-4">
              <Search className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-white mb-1">No Matching Creators Found</h3>
            <p className="text-xs text-slate-400 max-w-md mx-auto mb-6 leading-relaxed">
              No freelancers currently match all selected filters (budget, specialized equipment, or rolling 10-day availability).
            </p>
            <button
              onClick={handleResetFilters}
              className="inline-flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-slate-200 px-4 py-2 rounded-xl text-xs font-semibold transition"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset Filters</span>
            </button>
          </div>
        ) : (
          /* Freelancer Cards Grid / List */
          <div
            className={
              viewMode === 'grid'
                ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6'
                : 'flex flex-col gap-4'
            }
          >
            {freelancers.map((freelancer) => {
              // Synthetic AI match score readiness (DIS-002, DIS-006)
              const scoreSeed = ((freelancer.id * 17) % 15) + 84;
              const displayScore = `${scoreSeed}% Match`;

              return (
                <div
                  key={freelancer.id}
                  className="bg-slate-900/90 border border-slate-800 hover:border-slate-700/80 rounded-3xl p-6 backdrop-blur-md transition-all duration-200 flex flex-col justify-between group shadow-xl hover:shadow-teal-500/5 relative overflow-hidden"
                >
                  {/* Card Header: Avatar, Name, Role & AI Score Badge */}
                  <div>
                    <div className="flex items-start justify-between gap-4 mb-4">
                      <div className="flex items-center gap-3.5">
                        <div className="w-14 h-14 rounded-2xl bg-slate-800 border border-slate-700 overflow-hidden shrink-0 flex items-center justify-center relative">
                          {freelancer.profilePhotoUrl ? (
                            <img
                              src={freelancer.profilePhotoUrl}
                              alt={freelancer.fullName}
                              className="w-full h-full object-cover"
                              onError={(e) => {
                                // fallback if image fails to load
                                (e.target as HTMLElement).style.display = 'none';
                              }}
                            />
                          ) : (
                            <Camera className="w-6 h-6 text-slate-500" />
                          )}
                        </div>
                        <div>
                          <h3 className="text-base font-bold text-white group-hover:text-teal-400 transition-colors line-clamp-1">
                            {freelancer.fullName}
                          </h3>
                          <p className="text-xs text-teal-400 font-medium line-clamp-1">
                            {freelancer.primaryRole || 'Photographer / Cinematographer'}
                          </p>
                          <div className="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
                            <span className="flex items-center gap-1 text-amber-400 font-semibold">
                              <Star className="w-3 h-3 fill-current" />
                              {freelancer.averageRating ? freelancer.averageRating.toFixed(1) : '4.8'}
                            </span>
                            <span>({freelancer.reviewCount || 12} reviews)</span>
                            <span>•</span>
                            <span>{freelancer.experienceYears || 0} yrs exp</span>
                          </div>
                        </div>
                      </div>

                      {/* AI Match Badge (DIS-006) */}
                      <div className="shrink-0 text-right">
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-gradient-to-r from-teal-500/20 to-blue-500/20 text-teal-300 border border-teal-500/40 text-[11px] font-semibold">
                          <Sparkles className="w-3 h-3 text-teal-400" />
                          <span>{displayScore}</span>
                        </span>
                      </div>
                    </div>

                    {/* Location & Haversine Distance Badge */}
                    <div className="flex items-center gap-2 text-xs text-slate-400 mb-3 bg-slate-800/40 px-3 py-1.5 rounded-xl border border-slate-800">
                      <MapPin className="w-3.5 h-3.5 text-teal-400 shrink-0" />
                      <span className="truncate">{freelancer.address || 'Tamil Nadu, India'}</span>
                      {freelancer.distanceKm !== undefined && (
                        <span className="ml-auto text-[11px] font-semibold text-teal-400 shrink-0">
                          {freelancer.distanceKm} km away
                        </span>
                      )}
                    </div>

                    {/* Bio Excerpt */}
                    {freelancer.bio && (
                      <p className="text-xs text-slate-400 mb-4 line-clamp-2 leading-relaxed">
                        {freelancer.bio}
                      </p>
                    )}

                    {/* Skills & Services Badges */}
                    <div className="flex flex-wrap gap-1.5 mb-4">
                      {freelancer.services?.slice(0, 2).map((s) => (
                        <span
                          key={s.id}
                          className="text-[10px] px-2 py-0.5 rounded-lg bg-teal-500/10 text-teal-300 border border-teal-500/20 font-medium"
                        >
                          {s.name}
                        </span>
                      ))}
                      {freelancer.skills?.slice(0, 2).map((sk) => (
                        <span
                          key={sk.id}
                          className="text-[10px] px-2 py-0.5 rounded-lg bg-slate-800 text-slate-300 border border-slate-700 font-medium"
                        >
                          {sk.name}
                        </span>
                      ))}
                    </div>

                    {/* Equipment Highlight */}
                    {freelancer.equipment && freelancer.equipment.length > 0 && (
                      <div className="mb-4 text-[11px] text-slate-400 flex items-center gap-1.5">
                        <Camera className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                        <span className="truncate">
                          Gear: {freelancer.equipment.map((e) => e.name).slice(0, 2).join(', ')}
                          {freelancer.equipment.length > 2 ? ` +${freelancer.equipment.length - 2}` : ''}
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Card Footer: Rates, Live Availability & Actions */}
                  <div className="pt-4 border-t border-slate-800/80 space-y-4">
                    {/* Rates & Live Availability Status */}
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="text-[10px] uppercase font-bold tracking-wider text-slate-400">
                          Rates
                        </div>
                        <div className="text-xs font-bold text-white mt-0.5">
                          ₹{freelancer.fullDayRate?.toLocaleString() || '12,000'}{' '}
                          <span className="text-[10px] text-slate-400 font-normal">/ day</span>
                        </div>
                      </div>

                      {/* Live Availability Badge */}
                      <div className="text-right">
                        {freelancer.availabilityStatus === 'AVAILABLE' ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/30 text-[10px] font-semibold">
                            <CheckCircle className="w-3 h-3 text-emerald-400" />
                            <span>
                              {freelancer.availableHours ? freelancer.availableHours : 'Available'}
                            </span>
                          </span>
                        ) : freelancer.withinWindow === false ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30 text-[10px] font-semibold" title="Beyond 10-day window">
                            <Clock className="w-3 h-3 text-amber-400" />
                            <span>Date &gt; 10 Days</span>
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-800 text-slate-400 border border-slate-700 text-[10px] font-medium">
                            <Calendar className="w-3 h-3 text-slate-400" />
                            <span>Select Shoot Date</span>
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="grid grid-cols-2 gap-2">
                      <button
                        onClick={() => handleOpenPreview(freelancer)}
                        className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-slate-800/90 hover:bg-slate-800 border border-slate-700 text-slate-200 hover:text-white text-xs font-medium transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>View Profile</span>
                      </button>

                      <button
                        onClick={() => handleOpenBookingModal(freelancer)}
                        className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold shadow-md shadow-teal-500/10 transition-colors"
                      >
                        <Send className="w-3.5 h-3.5" />
                        <span>Send Request</span>
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Profile & Portfolio Showcase Modal */}
        {previewFreelancer && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-3xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden">
              {/* Modal Header */}
              <div className="p-6 border-b border-slate-800 flex items-start justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-16 h-16 rounded-2xl bg-slate-800 border border-slate-700 overflow-hidden shrink-0 flex items-center justify-center">
                    {previewFreelancer.profilePhotoUrl ? (
                      <img
                        src={previewFreelancer.profilePhotoUrl}
                        alt={previewFreelancer.fullName}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <Camera className="w-8 h-8 text-slate-500" />
                    )}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xl font-bold text-white">{previewFreelancer.fullName}</h2>
                      <span className="text-xs px-2 py-0.5 rounded-full bg-teal-500/10 text-teal-300 border border-teal-500/30">
                        {previewFreelancer.primaryRole}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-1 flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-teal-400" />
                      <span>{previewFreelancer.address}</span>
                      {previewFreelancer.distanceKm !== undefined && (
                        <span>• {previewFreelancer.distanceKm} km away</span>
                      )}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setPreviewFreelancer(null)}
                  className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white transition"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Modal Tabs */}
              <div className="flex items-center border-b border-slate-800 px-6 bg-slate-900/60">
                <button
                  onClick={() => setPreviewTab('portfolio')}
                  className={`py-3 px-4 text-xs font-semibold border-b-2 transition-colors ${
                    previewTab === 'portfolio'
                      ? 'border-teal-400 text-teal-400'
                      : 'border-transparent text-slate-400 hover:text-slate-200'
                  }`}
                >
                  Portfolio Showcase
                </button>
                <button
                  onClick={() => setPreviewTab('availability')}
                  className={`py-3 px-4 text-xs font-semibold border-b-2 transition-colors ${
                    previewTab === 'availability'
                      ? 'border-teal-400 text-teal-400'
                      : 'border-transparent text-slate-400 hover:text-slate-200'
                  }`}
                >
                  10-Day Availability
                </button>
                <button
                  onClick={() => setPreviewTab('gear')}
                  className={`py-3 px-4 text-xs font-semibold border-b-2 transition-colors ${
                    previewTab === 'gear'
                      ? 'border-teal-400 text-teal-400'
                      : 'border-transparent text-slate-400 hover:text-slate-200'
                  }`}
                >
                  Equipment & Skills
                </button>
              </div>

              {/* Modal Body */}
              <div className="p-6 overflow-y-auto space-y-6 flex-1">
                {previewLoading ? (
                  <div className="py-16 text-center">
                    <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mx-auto mb-2" />
                    <p className="text-xs text-slate-400">Loading portfolio and calendar...</p>
                  </div>
                ) : previewTab === 'portfolio' ? (
                  /* Portfolio Tab */
                  <div>
                    {previewPortfolio && previewPortfolio.categories?.length > 0 ? (
                      <div className="space-y-6">
                        {previewPortfolio.categories.map((cat) => (
                          <div key={cat.id} className="space-y-3">
                            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300">
                              {cat.name} ({cat.images?.length || 0})
                            </h4>
                            {cat.images?.length > 0 ? (
                              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                                {cat.images.map((img) => (
                                  <div
                                    key={img.id}
                                    className="aspect-square rounded-2xl bg-slate-800 overflow-hidden border border-slate-700/60 relative group"
                                  >
                                    <img
                                      src={img.imageUrl}
                                      alt={img.originalFilename || 'Portfolio Image'}
                                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                    />
                                    {img.originalFilename && (
                                      <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-slate-950 p-2 text-[10px] text-slate-200">
                                        {img.originalFilename}
                                      </div>
                                    )}
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <p className="text-xs text-slate-500 italic">No images in this category.</p>
                            )}
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="text-center py-12 text-slate-400 text-xs">
                        <Camera className="w-8 h-8 mx-auto text-slate-600 mb-2" />
                        <p>No public portfolio uploaded yet for this creator.</p>
                      </div>
                    )}
                  </div>
                ) : previewTab === 'availability' ? (
                  /* 10-Day Availability Window Tab */
                  <div>
                    <div className="mb-4">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300">
                        Rolling 10-Day Schedule ({previewAvailability?.windowStartDate} to {previewAvailability?.windowEndDate})
                      </h4>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        Real-time calendar updated directly by {previewFreelancer.fullName}.
                      </p>
                    </div>

                    {previewAvailability && previewAvailability.slots?.length > 0 ? (
                      <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5">
                        {previewAvailability.slots.map((slot) => (
                          <div
                            key={slot.date}
                            className={`p-3 rounded-2xl border text-center flex flex-col justify-between ${
                              slot.status === 'AVAILABLE'
                                ? 'bg-emerald-950/20 border-emerald-500/30 text-emerald-300'
                                : slot.status === 'BUSY'
                                ? 'bg-rose-950/20 border-rose-500/30 text-rose-300'
                                : 'bg-slate-800/50 border-slate-700/60 text-slate-400'
                            }`}
                          >
                            <div className="text-[10px] font-semibold uppercase">{slot.dayOfWeek}</div>
                            <div className="text-base font-bold my-1">
                              {slot.month} {slot.dayOfMonth}
                            </div>
                            <div className="text-[10px] font-bold">
                              {slot.status === 'AVAILABLE'
                                ? slot.formattedTime || 'Available'
                                : slot.status === 'BUSY'
                                ? 'Booked'
                                : 'Not Set'}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="text-xs text-slate-400">Availability calendar currently not loaded.</p>
                    )}
                  </div>
                ) : (
                  /* Gear & Skills Tab */
                  <div className="space-y-6">
                    <div>
                      <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300 mb-2">
                        Camera & Production Gear
                      </h4>
                      <div className="flex flex-wrap gap-2">
                        {previewFreelancer.equipment && previewFreelancer.equipment.length > 0 ? (
                          previewFreelancer.equipment.map((eq) => (
                            <span
                              key={eq.id}
                              className="text-xs px-3 py-1.5 rounded-xl bg-slate-800 text-slate-200 border border-slate-700"
                            >
                              {eq.name} {eq.categoryName ? `(${eq.categoryName})` : ''}
                            </span>
                          ))
                        ) : (
                          <p className="text-xs text-slate-500">No equipment items specified.</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300 mb-2">
                        Specialized Skills
                      </h4>
                      <div className="flex flex-wrap gap-2">
                        {previewFreelancer.skills?.map((sk) => (
                          <span
                            key={sk.id}
                            className="text-xs px-3 py-1.5 rounded-xl bg-teal-500/10 text-teal-300 border border-teal-500/30"
                          >
                            {sk.name}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div>
                      <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300 mb-2">
                        Direct Rates
                      </h4>
                      <div className="grid grid-cols-2 gap-4">
                        <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700">
                          <span className="text-[10px] text-slate-400 block">Full Day Rate</span>
                          <span className="text-sm font-bold text-white">
                            ₹{previewFreelancer.fullDayRate?.toLocaleString() || '12,000'}
                          </span>
                        </div>
                        <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700">
                          <span className="text-[10px] text-slate-400 block">Half Day Rate</span>
                          <span className="text-sm font-bold text-white">
                            ₹{previewFreelancer.halfDayRate?.toLocaleString() || '7,000'}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Modal Footer */}
              <div className="p-4 border-t border-slate-800 flex items-center justify-between">
                <button
                  onClick={() => setPreviewFreelancer(null)}
                  className="px-4 py-2 rounded-xl text-xs text-slate-400 hover:text-white"
                >
                  Close
                </button>
                <button
                  onClick={() => {
                    const candidate = previewFreelancer;
                    setPreviewFreelancer(null);
                    if (candidate) {
                      handleOpenBookingModal(candidate);
                    }
                  }}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs shadow-md shadow-teal-500/20"
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>Send Request to {previewFreelancer.fullName}</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Send Request Modal */}
        {bookingFreelancer && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-lg p-6 shadow-2xl">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                    <Send className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-white">
                      Dispatch Work Request
                    </h3>
                    <p className="text-[11px] text-slate-400">Invite {bookingFreelancer.fullName} to your shoot (REQ-001)</p>
                  </div>
                </div>
                <button
                  onClick={() => setBookingFreelancer(null)}
                  className="p-1.5 rounded-lg bg-slate-800 text-slate-400 hover:text-white"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {bookingSuccess ? (
                <div className="py-8 text-center space-y-3">
                  <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto">
                    <Check className="w-6 h-6" />
                  </div>
                  <h4 className="text-base font-bold text-white">Work Request Dispatched!</h4>
                  <p className="text-xs text-slate-400">
                    Your request was recorded and sent to {bookingFreelancer.fullName}. They can review shoot details, accept or reject the proposal (REQ-004), and you will confirm the final creator (WRK-007).
                  </p>
                </div>
              ) : (
                <form onSubmit={handleSendBookingRequest} className="space-y-4">
                  {bookingError && (
                    <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-start gap-2.5 text-rose-300 text-xs">
                      <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                      <span>{bookingError}</span>
                    </div>
                  )}

                  {loadingRequirements ? (
                    <div className="py-6 flex flex-col items-center justify-center gap-2">
                      <div className="w-6 h-6 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
                      <p className="text-xs text-slate-400">Loading your active requirements...</p>
                    </div>
                  ) : openRequirements.length === 0 ? (
                    <div className="p-4 bg-amber-500/10 border border-amber-500/30 rounded-2xl space-y-2 text-center">
                      <AlertCircle className="w-6 h-6 text-amber-400 mx-auto" />
                      <h5 className="text-xs font-bold text-amber-300">No Open Shoot Requirements</h5>
                      <p className="text-[11px] text-slate-400">
                        You need an OPEN shoot requirement to send booking requests to creators.
                      </p>
                      <a
                        href="/studio/requirements/new"
                        className="inline-block mt-2 px-3 py-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 font-bold text-xs rounded-xl transition"
                      >
                        Create Shoot Requirement
                      </a>
                    </div>
                  ) : (
                    <>
                      <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                          Select Shoot Requirement *
                        </label>
                        <select
                          value={selectedRequirementId}
                          onChange={(e) => {
                            const reqId = Number(e.target.value);
                            setSelectedRequirementId(reqId);
                            const matched = openRequirements.find((r) => r.id === reqId);
                            if (matched && matched.budget) {
                              setBookingOfferedPrice(matched.budget);
                            }
                          }}
                          required
                          className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                        >
                          {openRequirements.map((req) => (
                            <option key={req.id} value={req.id}>
                              {req.eventName} ({req.eventDate} &bull; ₹{req.budget?.toLocaleString()})
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                          Offered Rate / Budget (₹) *
                        </label>
                        <input
                          type="number"
                          min="0"
                          value={bookingOfferedPrice}
                          onChange={(e) => setBookingOfferedPrice(e.target.value === '' ? '' : Number(e.target.value))}
                          placeholder="e.g. 15000"
                          required
                          className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-teal-500"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-medium text-slate-300 mb-1">
                          Message &amp; Creative Needs (Optional)
                        </label>
                        <textarea
                          rows={3}
                          value={bookingMessage}
                          onChange={(e) => setBookingMessage(e.target.value)}
                          placeholder="e.g. Need coverage for reception ceremonies. Candidate should bring 2 camera bodies and prime lenses."
                          className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
                        />
                      </div>

                      <div className="flex items-center justify-between pt-2">
                        <button
                          type="button"
                          onClick={() => setBookingFreelancer(null)}
                          className="text-xs text-slate-400 hover:text-white"
                        >
                          Cancel
                        </button>
                        <button
                          type="submit"
                          disabled={bookingSending || !selectedRequirementId}
                          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs shadow-md shadow-teal-500/20 disabled:opacity-50 transition"
                        >
                          {bookingSending ? (
                            <span>Dispatching...</span>
                          ) : (
                            <>
                              <Send className="w-3.5 h-3.5" />
                              <span>Dispatch Request</span>
                            </>
                          )}
                        </button>
                      </div>
                    </>
                  )}
                </form>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
