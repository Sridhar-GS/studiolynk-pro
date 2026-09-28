import React, { useEffect, useState } from 'react';
import {
  Calendar,
  Clock,
  CheckCircle2,
  XCircle,
  HelpCircle,
  Save,
  RotateCcw,
  Sparkles,
  AlertCircle,
  Info,
  ShieldCheck,
  Check,
  AlertTriangle,
} from 'lucide-react';
import { availabilityService } from '../../services/availabilityService';
import {
  AvailabilitySlot,
  AvailabilityStatus,
  AvailabilityWindowResponse,
  UpdateAvailabilityItem,
} from '../../types';

export const FreelancerAvailabilityPage: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [resetting, setResetting] = useState<boolean>(false);
  const [windowData, setWindowData] = useState<AvailabilityWindowResponse | null>(null);
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [isDirty, setIsDirty] = useState<boolean>(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [showResetConfirm, setShowResetConfirm] = useState<boolean>(false);

  const fetchAvailability = async () => {
    try {
      setLoading(true);
      const data = await availabilityService.getMyAvailability();
      setWindowData(data);
      setSlots(data.slots);
      setIsDirty(false);
    } catch (err: any) {
      setMessage({
        type: 'error',
        text: err?.response?.data?.message || 'Failed to fetch rolling availability.',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAvailability();
  }, []);

  const handleStatusChange = (index: number, newStatus: AvailabilityStatus) => {
    setSlots((prev) => {
      const updated = [...prev];
      const current = updated[index];
      let newStartTime = current.startTime;
      let newEndTime = current.endTime;

      if (newStatus === 'AVAILABLE') {
        // Default to standard 09:00 - 18:00 if not set
        if (!newStartTime) newStartTime = '09:00';
        if (!newEndTime) newEndTime = '18:00';
      } else {
        newStartTime = null;
        newEndTime = null;
      }

      updated[index] = {
        ...current,
        status: newStatus,
        startTime: newStartTime,
        endTime: newEndTime,
        formattedTime:
          newStatus === 'AVAILABLE' && newStartTime && newEndTime
            ? `${newStartTime} - ${newEndTime}`
            : null,
        available: newStatus === 'AVAILABLE',
      };
      return updated;
    });
    setIsDirty(true);
    setMessage(null);
  };

  const handleTimeChange = (index: number, field: 'startTime' | 'endTime', value: string) => {
    setSlots((prev) => {
      const updated = [...prev];
      const current = updated[index];
      const newStartTime = field === 'startTime' ? value : current.startTime || '09:00';
      const newEndTime = field === 'endTime' ? value : current.endTime || '18:00';

      updated[index] = {
        ...current,
        [field]: value,
        formattedTime: `${newStartTime} - ${newEndTime}`,
      };
      return updated;
    });
    setIsDirty(true);
    setMessage(null);
  };

  const applyPresetTime = (index: number, start: string, end: string) => {
    setSlots((prev) => {
      const updated = [...prev];
      updated[index] = {
        ...updated[index],
        status: 'AVAILABLE',
        startTime: start,
        endTime: end,
        formattedTime: `${start} - ${end}`,
        available: true,
      };
      return updated;
    });
    setIsDirty(true);
    setMessage(null);
  };

  const setAllAvailable = () => {
    setSlots((prev) =>
      prev.map((slot) => ({
        ...slot,
        status: 'AVAILABLE',
        startTime: '09:00',
        endTime: '18:00',
        formattedTime: '09:00 - 18:00',
        available: true,
      }))
    );
    setIsDirty(true);
    setMessage(null);
  };

  const setWeekdaysAvailableWeekendsBusy = () => {
    setSlots((prev) =>
      prev.map((slot) => {
        const isWeekend = slot.dayOfWeek === 'Sat' || slot.dayOfWeek === 'Sun';
        return {
          ...slot,
          status: isWeekend ? 'BUSY' : 'AVAILABLE',
          startTime: isWeekend ? null : '09:00',
          endTime: isWeekend ? null : '18:00',
          formattedTime: isWeekend ? null : '09:00 - 18:00',
          available: !isWeekend,
        };
      })
    );
    setIsDirty(true);
    setMessage(null);
  };

  const handleSave = async () => {
    // Validate that all AVAILABLE days have start time < end time
    for (const slot of slots) {
      if (slot.status === 'AVAILABLE') {
        if (!slot.startTime || !slot.endTime) {
          setMessage({
            type: 'error',
            text: `Please specify both start and end times for ${slot.date} (${slot.dayOfWeek}).`,
          });
          return;
        }
        if (slot.startTime >= slot.endTime) {
          setMessage({
            type: 'error',
            text: `Start time must be before end time on ${slot.date} (${slot.dayOfWeek}).`,
          });
          return;
        }
      }
    }

    try {
      setSaving(true);
      setMessage(null);
      const payloadItems: UpdateAvailabilityItem[] = slots.map((s) => ({
        date: s.date,
        status: s.status,
        startTime: s.status === 'AVAILABLE' ? s.startTime : null,
        endTime: s.status === 'AVAILABLE' ? s.endTime : null,
      }));

      const res = await availabilityService.updateMyAvailability({ availability: payloadItems });
      setWindowData(res);
      setSlots(res.slots);
      setIsDirty(false);
      setMessage({
        type: 'success',
        text: 'Availability schedule updated successfully! Studios can now discover your open shoot hours.',
      });
    } catch (err: any) {
      setMessage({
        type: 'error',
        text: err?.response?.data?.message || 'Failed to update availability schedule.',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleReset = async () => {
    try {
      setResetting(true);
      setShowResetConfirm(false);
      setMessage(null);
      const res = await availabilityService.resetMyAvailability();
      setWindowData(res);
      setSlots(res.slots);
      setIsDirty(false);
      setMessage({
        type: 'success',
        text: '10-day availability reset back to NOT_SET successfully.',
      });
    } catch (err: any) {
      setMessage({
        type: 'error',
        text: err?.response?.data?.message || 'Failed to reset availability.',
      });
    } finally {
      setResetting(false);
    }
  };

  const availableCount = slots.filter((s) => s.status === 'AVAILABLE').length;
  const busyCount = slots.filter((s) => s.status === 'BUSY').length;
  const notSetCount = slots.filter((s) => s.status === 'NOT_SET').length;

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center py-20">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 border-4 border-teal-500/20 border-t-teal-400 rounded-full animate-spin" />
          <p className="text-slate-400 text-sm font-medium">Loading rolling 10-day availability calendar...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 border-b border-slate-800 pb-6">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <div className="p-2 rounded-xl bg-teal-500/10 border border-teal-500/30 text-teal-400">
                <Calendar className="w-5 h-5" />
              </div>
              <h1 className="text-2xl font-bold tracking-tight text-white">10-Day Availability Calendar</h1>
              <span className="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-teal-500/20 text-teal-300 border border-teal-500/30">
                Phase 7 Active
              </span>
            </div>
            <p className="text-xs text-slate-400 max-w-2xl">
              Configure your shoot availability for the rolling 10-day window
              {windowData?.windowStartDate && windowData?.windowEndDate && (
                <span className="text-teal-400 font-medium"> ({windowData.windowStartDate} to {windowData.windowEndDate})</span>
              )}
              . Studios search and confirm bookings strictly based on your open dates and working hours.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setShowResetConfirm(true)}
              disabled={resetting || saving}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-semibold bg-slate-900 border border-slate-700/80 text-slate-300 hover:text-white hover:border-slate-600 transition-colors disabled:opacity-50"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset to Not Set</span>
            </button>

            <button
              onClick={handleSave}
              disabled={!isDirty || saving}
              className={`inline-flex items-center gap-2 px-5 py-2 rounded-xl text-xs font-semibold shadow-lg transition-all ${
                isDirty
                  ? 'bg-teal-500 hover:bg-teal-400 text-slate-950 shadow-teal-500/20 animate-pulse'
                  : 'bg-slate-800 text-slate-500 border border-slate-700/60 cursor-not-allowed'
              }`}
            >
              {saving ? (
                <>
                  <div className="w-3.5 h-3.5 border-2 border-slate-950/20 border-t-slate-950 rounded-full animate-spin" />
                  <span>Saving...</span>
                </>
              ) : (
                <>
                  <Save className="w-3.5 h-3.5" />
                  <span>Save Availability</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* Feedback Alert */}
        {message && (
          <div
            className={`p-4 rounded-2xl flex items-start gap-3 border text-xs leading-relaxed ${
              message.type === 'success'
                ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-300'
                : 'bg-rose-950/40 border-rose-500/40 text-rose-300'
            }`}
          >
            {message.type === 'success' ? (
              <Check className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            ) : (
              <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            )}
            <p className="flex-1">{message.text}</p>
          </div>
        )}

        {/* Specifications & Rules Callout */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-3xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-teal-400">
            <Info className="w-4 h-4" />
            <h2 className="text-xs font-bold uppercase tracking-wider">How the Rolling 10-Day Availability Rule Works</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs text-slate-400">
            <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <span className="font-semibold text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5" /> 1. Available (Visible)
              </span>
              <p className="text-[11px] leading-relaxed text-slate-400">
                You configure your shoot hours (e.g. 09:00 - 18:00). Studios searching for shoots within these hours
                can view and request you.
              </p>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <span className="font-semibold text-rose-400 flex items-center gap-1.5">
                <XCircle className="w-3.5 h-3.5" /> 2. Busy / Not Set (Hidden)
              </span>
              <p className="text-[11px] leading-relaxed text-slate-400">
                Days marked Busy or Not Set are strictly excluded from shoot searches within the 10-day window to
                prevent booking conflicts.
              </p>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/80 space-y-1">
              <span className="font-semibold text-amber-400 flex items-center gap-1.5">
                <HelpCircle className="w-3.5 h-3.5" /> 3. Beyond 10 Days
              </span>
              <p className="text-[11px] leading-relaxed text-slate-400">
                Dates beyond 10 days appear as "Availability Unknown". Studios may still contact you, but availability
                is not guaranteed.
              </p>
            </div>
          </div>
        </div>

        {/* Stats & Quick Actions Bar */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-xs text-slate-400 font-medium">Window Status:</span>
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-400" />
              <span>{availableCount} Available</span>
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-rose-400" />
              <span>{busyCount} Busy</span>
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-slate-400 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-slate-500" />
              <span>{notSetCount} Not Set</span>
            </div>
            {isDirty && (
              <span className="px-2.5 py-0.5 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-[10px] font-semibold animate-pulse">
                Unsaved Changes
              </span>
            )}
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={setAllAvailable}
              className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700/80 text-slate-200 text-xs font-medium border border-slate-700/80 transition-colors flex items-center gap-1.5"
            >
              <Sparkles className="w-3.5 h-3.5 text-teal-400" />
              <span>Quick: All Available</span>
            </button>
            <button
              onClick={setWeekdaysAvailableWeekendsBusy}
              className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700/80 text-slate-200 text-xs font-medium border border-slate-700/80 transition-colors flex items-center gap-1.5"
            >
              <Clock className="w-3.5 h-3.5 text-indigo-400" />
              <span>Weekdays Open, Weekends Busy</span>
            </button>
          </div>
        </div>

        {/* 10-Day Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
          {slots.map((slot, index) => {
            const isToday = index === 0;
            const isTomorrow = index === 1;

            return (
              <div
                key={slot.date}
                className={`p-5 rounded-3xl border transition-all space-y-4 ${
                  slot.status === 'AVAILABLE'
                    ? 'bg-slate-900/90 border-emerald-500/30 shadow-sm shadow-emerald-500/5'
                    : slot.status === 'BUSY'
                    ? 'bg-slate-900/90 border-rose-500/30 shadow-sm shadow-rose-500/5'
                    : 'bg-slate-900/60 border-slate-800/80'
                }`}
              >
                {/* Day Header */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-11 h-11 rounded-2xl flex flex-col items-center justify-center font-bold text-center border ${
                        slot.status === 'AVAILABLE'
                          ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                          : slot.status === 'BUSY'
                          ? 'bg-rose-500/10 border-rose-500/30 text-rose-400'
                          : 'bg-slate-800 border-slate-700 text-slate-300'
                      }`}
                    >
                      <span className="text-[10px] uppercase font-semibold leading-none">{slot.dayOfWeek}</span>
                      <span className="text-sm leading-tight">{slot.dayOfMonth}</span>
                    </div>

                    <div>
                      <div className="flex items-center gap-2">
                        <h2 className="text-sm font-bold text-white">
                          {slot.month} {slot.dayOfMonth}, {slot.date.substring(0, 4)}
                        </h2>
                        {isToday && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-teal-500/20 text-teal-300 border border-teal-500/30">
                            Today
                          </span>
                        )}
                        {isTomorrow && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                            Tomorrow
                          </span>
                        )}
                        {!isToday && !isTomorrow && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-slate-800 text-slate-400">
                            Day {index + 1}
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-slate-400">{slot.dayOfWeek}day</span>
                    </div>
                  </div>

                  {/* Status Indicator Pill */}
                  <span
                    className={`px-2.5 py-1 rounded-full text-[11px] font-bold flex items-center gap-1 ${
                      slot.status === 'AVAILABLE'
                        ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                        : slot.status === 'BUSY'
                        ? 'bg-rose-500/20 text-rose-300 border border-rose-500/40'
                        : 'bg-slate-800 text-slate-400 border border-slate-700'
                    }`}
                  >
                    {slot.status === 'AVAILABLE' && <CheckCircle2 className="w-3 h-3" />}
                    {slot.status === 'BUSY' && <XCircle className="w-3 h-3" />}
                    {slot.status === 'NOT_SET' && <HelpCircle className="w-3 h-3" />}
                    <span>{slot.status.replace('_', ' ')}</span>
                  </span>
                </div>

                {/* Status Switcher Buttons */}
                <div className="grid grid-cols-3 gap-2 bg-slate-950/60 p-1 rounded-2xl border border-slate-800/80">
                  <button
                    type="button"
                    onClick={() => handleStatusChange(index, 'AVAILABLE')}
                    className={`py-1.5 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-all ${
                      slot.status === 'AVAILABLE'
                        ? 'bg-emerald-500 text-slate-950 font-bold shadow-md shadow-emerald-500/20'
                        : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                    }`}
                  >
                    <CheckCircle2 className="w-3 h-3" />
                    <span>Available</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleStatusChange(index, 'BUSY')}
                    className={`py-1.5 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-all ${
                      slot.status === 'BUSY'
                        ? 'bg-rose-500 text-white font-bold shadow-md shadow-rose-500/20'
                        : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                    }`}
                  >
                    <XCircle className="w-3 h-3" />
                    <span>Busy</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleStatusChange(index, 'NOT_SET')}
                    className={`py-1.5 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-all ${
                      slot.status === 'NOT_SET'
                        ? 'bg-slate-700 text-white font-bold'
                        : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                    }`}
                  >
                    <HelpCircle className="w-3 h-3" />
                    <span>Not Set</span>
                  </button>
                </div>

                {/* Config section depending on status */}
                {slot.status === 'AVAILABLE' && (
                  <div className="space-y-3 pt-1">
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-[11px] font-semibold text-slate-400 mb-1">
                          Shoot Start Time
                        </label>
                        <input
                          type="time"
                          value={slot.startTime || '09:00'}
                          onChange={(e) => handleTimeChange(index, 'startTime', e.target.value)}
                          className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-teal-400 transition-colors"
                        />
                      </div>

                      <div>
                        <label className="block text-[11px] font-semibold text-slate-400 mb-1">
                          Shoot End Time
                        </label>
                        <input
                          type="time"
                          value={slot.endTime || '18:00'}
                          onChange={(e) => handleTimeChange(index, 'endTime', e.target.value)}
                          className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-teal-400 transition-colors"
                        />
                      </div>
                    </div>

                    {/* Quick Preset Chips */}
                    <div className="flex flex-wrap items-center gap-1.5 pt-1">
                      <span className="text-[10px] font-medium text-slate-500 mr-1">Presets:</span>
                      <button
                        type="button"
                        onClick={() => applyPresetTime(index, '09:00', '18:00')}
                        className="px-2 py-0.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-medium transition-colors"
                      >
                        Full Day (09:00 - 18:00)
                      </button>
                      <button
                        type="button"
                        onClick={() => applyPresetTime(index, '09:00', '13:00')}
                        className="px-2 py-0.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-medium transition-colors"
                      >
                        Morning (09:00 - 13:00)
                      </button>
                      <button
                        type="button"
                        onClick={() => applyPresetTime(index, '14:00', '19:00')}
                        className="px-2 py-0.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-medium transition-colors"
                      >
                        Afternoon (14:00 - 19:00)
                      </button>
                      <button
                        type="button"
                        onClick={() => applyPresetTime(index, '17:00', '22:00')}
                        className="px-2 py-0.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-medium transition-colors"
                      >
                        Night (17:00 - 22:00)
                      </button>
                    </div>
                  </div>
                )}

                {slot.status === 'BUSY' && (
                  <div className="p-3 rounded-2xl bg-rose-950/20 border border-rose-500/20 text-rose-300 text-xs flex items-center gap-2">
                    <ShieldCheck className="w-4 h-4 text-rose-400 shrink-0" />
                    <span>Excluded from studio searches. Confirmed work &amp; off-days are locked.</span>
                  </div>
                )}

                {slot.status === 'NOT_SET' && (
                  <div className="p-3 rounded-2xl bg-slate-950/40 border border-slate-800 text-slate-400 text-xs flex items-center gap-2">
                    <AlertCircle className="w-4 h-4 text-slate-500 shrink-0" />
                    <span>Availability not configured. Hidden from studio search results on this date.</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Floating Bottom Save Bar if dirty */}
        {isDirty && (
          <div className="sticky bottom-6 z-20 bg-slate-900/95 backdrop-blur-md border border-teal-500/40 rounded-3xl p-4 shadow-2xl flex items-center justify-between gap-4 animate-in fade-in slide-in-from-bottom-3 duration-300">
            <div className="flex items-center gap-2 text-xs">
              <span className="w-2.5 h-2.5 rounded-full bg-teal-400 animate-ping" />
              <span className="text-white font-semibold">You have unsaved availability changes.</span>
              <span className="text-slate-400 hidden sm:inline">Save them so studios can see your updated shoot schedule.</span>
            </div>

            <div className="flex items-center gap-3">
              <button
                onClick={fetchAvailability}
                disabled={saving}
                className="px-3.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 transition-colors"
              >
                Discard
              </button>
              <button
                onClick={handleSave}
                disabled={saving}
                className="px-5 py-2 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 text-xs font-bold shadow-lg shadow-teal-500/20 transition-all flex items-center gap-1.5"
              >
                {saving ? (
                  <>
                    <div className="w-3.5 h-3.5 border-2 border-slate-950/20 border-t-slate-950 rounded-full animate-spin" />
                    <span>Saving...</span>
                  </>
                ) : (
                  <>
                    <Save className="w-3.5 h-3.5" />
                    <span>Save Changes Now</span>
                  </>
                )}
              </button>
            </div>
          </div>
        )}

        {/* Confirmation Modal for Reset */}
        {showResetConfirm && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl">
              <div className="flex items-center gap-3 text-rose-400">
                <div className="p-2.5 rounded-2xl bg-rose-500/10 border border-rose-500/20">
                  <RotateCcw className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">Reset Availability?</h3>
                  <span className="text-xs text-slate-400">Revert entire 10-day window to Not Set</span>
                </div>
              </div>

              <p className="text-xs text-slate-300 leading-relaxed">
                This will reset all 10 rolling calendar days back to <strong className="text-white">NOT SET</strong> and clear all configured shoot hours. Studios will not see your profile in available shoot searches until you reconfigure your dates.
              </p>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowResetConfirm(false)}
                  disabled={resetting}
                  className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleReset}
                  disabled={resetting}
                  className="px-4 py-2 rounded-xl bg-rose-500 hover:bg-rose-400 text-xs font-bold text-white shadow-lg shadow-rose-500/20 transition-all flex items-center gap-1.5"
                >
                  {resetting ? (
                    <>
                      <div className="w-3.5 h-3.5 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                      <span>Resetting...</span>
                    </>
                  ) : (
                    <span>Confirm Reset</span>
                  )}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
