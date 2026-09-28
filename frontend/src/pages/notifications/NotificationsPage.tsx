import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  CheckCheck,
  CheckCircle2,
  XCircle,
  Clock,
  ShieldCheck,
  MessageSquare,
  Sparkles,
  AlertCircle,
  Star,
  Inbox,
  ArrowRight
} from 'lucide-react';
import { notificationService } from '../../services/notificationService';
import { AppNotification, NotificationType } from '../../types';

export const NotificationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'ALL' | 'UNREAD'>('ALL');
  const [markingAll, setMarkingAll] = useState(false);
  const [markingId, setMarkingId] = useState<number | null>(null);

  const fetchNotifications = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await notificationService.getNotifications(activeTab === 'UNREAD');
      setNotifications(data || []);
    } catch (err: any) {
      console.error('Failed to load notifications:', err);
      setError('Unable to load notifications. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [activeTab]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const handleMarkOne = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    setMarkingId(id);
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    } finally {
      setMarkingId(null);
    }
  };

  const handleMarkAll = async () => {
    setMarkingAll(true);
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    } finally {
      setMarkingAll(false);
    }
  };

  const handleCardClick = async (notif: AppNotification) => {
    if (!notif.isRead) {
      try {
        await notificationService.markAsRead(notif.id);
      } catch (err) {
        console.error('Failed to mark read on click:', err);
      }
    }
    if (notif.linkUrl) {
      navigate(notif.linkUrl);
    }
  };

  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case 'REQUEST_RECEIVED':
        return <Inbox className="w-5 h-5 text-teal-400" />;
      case 'REQUEST_ACCEPTED':
        return <CheckCircle2 className="w-5 h-5 text-indigo-400" />;
      case 'REQUEST_REJECTED':
        return <XCircle className="w-5 h-5 text-rose-400" />;
      case 'STUDIO_CONFIRMATION':
        return <ShieldCheck className="w-5 h-5 text-emerald-400" />;
      case 'NEW_MESSAGE':
        return <MessageSquare className="w-5 h-5 text-sky-400" />;
      case 'WORK_STARTED':
        return <Clock className="w-5 h-5 text-amber-400" />;
      case 'WORK_COMPLETED':
        return <Sparkles className="w-5 h-5 text-emerald-400" />;
      case 'WORK_CANCELLED':
        return <AlertCircle className="w-5 h-5 text-rose-400" />;
      case 'RATING_REMINDER':
        return <Star className="w-5 h-5 text-amber-400" />;
      default:
        return <Bell className="w-5 h-5 text-teal-400" />;
    }
  };

  const getNotificationBadgeColor = (type: NotificationType) => {
    switch (type) {
      case 'REQUEST_RECEIVED':
        return 'bg-teal-500/10 text-teal-300 border-teal-500/30';
      case 'REQUEST_ACCEPTED':
        return 'bg-indigo-500/10 text-indigo-300 border-indigo-500/30';
      case 'REQUEST_REJECTED':
        return 'bg-rose-500/10 text-rose-300 border-rose-500/30';
      case 'STUDIO_CONFIRMATION':
        return 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30';
      case 'NEW_MESSAGE':
        return 'bg-sky-500/10 text-sky-300 border-sky-500/30';
      case 'WORK_STARTED':
        return 'bg-amber-500/10 text-amber-300 border-amber-500/30';
      case 'WORK_COMPLETED':
        return 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30';
      case 'WORK_CANCELLED':
        return 'bg-rose-500/10 text-rose-300 border-rose-500/30';
      case 'RATING_REMINDER':
        return 'bg-yellow-500/10 text-yellow-300 border-yellow-500/30';
      default:
        return 'bg-slate-800 text-slate-300 border-slate-700';
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8 px-4 sm:px-6 lg:px-8 selection:bg-teal-500 selection:text-slate-950">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 shrink-0">
                <Bell className="w-7 h-7" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                    Phase 12: In-App Notifications (NOT-001 - NOT-004)
                  </span>
                </div>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Notification Center
                </h1>
                <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
                  Real-time activity on requests, confirmations, messages, and shoot lifecycle milestones.
                </p>
              </div>
            </div>

            {unreadCount > 0 && (
              <button
                type="button"
                onClick={handleMarkAll}
                disabled={markingAll}
                className="inline-flex items-center gap-2 px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-teal-300 hover:text-white border border-teal-500/30 rounded-xl text-xs font-semibold shadow-md transition disabled:opacity-50 self-start sm:self-auto shrink-0"
              >
                <CheckCheck className="w-4 h-4" />
                <span>{markingAll ? 'Marking All...' : 'Mark All as Read'}</span>
              </button>
            )}
          </div>
        </div>

        {/* Tab Controls */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-2.5 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setActiveTab('ALL')}
              className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition ${
                activeTab === 'ALL'
                  ? 'bg-teal-500 text-slate-950 font-bold shadow-md shadow-teal-500/20'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              All Notifications
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('UNREAD')}
              className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition flex items-center gap-1.5 ${
                activeTab === 'UNREAD'
                  ? 'bg-teal-500 text-slate-950 font-bold shadow-md shadow-teal-500/20'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              <span>Unread</span>
              {unreadCount > 0 && (
                <span className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                  activeTab === 'UNREAD' ? 'bg-slate-950 text-teal-300' : 'bg-teal-500/20 text-teal-300'
                }`}>
                  {unreadCount}
                </span>
              )}
            </button>
          </div>

          <span className="text-xs text-slate-500 pr-2">
            {notifications.length} {notifications.length === 1 ? 'item' : 'items'}
          </span>
        </div>

        {/* Notification Stream */}
        {loading ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-16 flex flex-col items-center justify-center text-center">
            <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin mb-3" />
            <p className="text-xs text-slate-400">Loading notifications...</p>
          </div>
        ) : error ? (
          <div className="bg-rose-950/20 border border-rose-500/30 rounded-3xl p-8 text-center text-rose-300 text-xs">
            {error}
          </div>
        ) : notifications.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-16 text-center space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-slate-800/80 border border-slate-700 flex items-center justify-center text-slate-500 mx-auto">
              <Bell className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-white">No Notifications</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              {activeTab === 'UNREAD'
                ? "You're all caught up! There are no unread notifications."
                : "You don't have any activity notifications yet."}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {notifications.map((notif) => (
              <div
                key={notif.id}
                onClick={() => handleCardClick(notif)}
                className={`p-4 sm:p-5 rounded-2xl border transition-all duration-150 cursor-pointer flex items-start gap-4 ${
                  !notif.isRead
                    ? 'bg-slate-900/90 border-teal-500/40 shadow-lg shadow-teal-500/5 hover:border-teal-500/60'
                    : 'bg-slate-900/40 border-slate-800 hover:border-slate-700 opacity-80 hover:opacity-100'
                }`}
              >
                {/* Icon Container */}
                <div className="w-10 h-10 rounded-xl bg-slate-800 border border-slate-700/80 flex items-center justify-center shrink-0 mt-0.5">
                  {getNotificationIcon(notif.type)}
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0 space-y-1">
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full border ${getNotificationBadgeColor(notif.type)}`}>
                        {notif.type.replace('_', ' ')}
                      </span>
                      {!notif.isRead && (
                        <span className="w-2 h-2 rounded-full bg-teal-400 shrink-0 animate-pulse" />
                      )}
                    </div>
                    <span className="text-[11px] text-slate-500 shrink-0">
                      {new Date(notif.createdAt).toLocaleDateString()} {new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>

                  <h3 className={`text-sm font-bold ${!notif.isRead ? 'text-white' : 'text-slate-300'}`}>
                    {notif.title}
                  </h3>

                  <p className="text-xs text-slate-400 leading-relaxed">
                    {notif.message}
                  </p>

                  <div className="pt-2 flex items-center justify-between">
                    <span className="text-xs font-semibold text-teal-400 flex items-center gap-1 hover:underline">
                      <span>View Details</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </span>

                    {!notif.isRead && (
                      <button
                        type="button"
                        onClick={(e) => handleMarkOne(e, notif.id)}
                        disabled={markingId === notif.id}
                        className="text-[11px] text-slate-400 hover:text-teal-300 bg-slate-800/80 hover:bg-slate-800 px-2.5 py-1 rounded-lg border border-slate-700 transition"
                      >
                        {markingId === notif.id ? 'Marking...' : 'Mark as read'}
                      </button>
                    )}
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
