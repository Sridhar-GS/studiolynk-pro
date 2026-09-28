import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Camera, LogOut, User as UserIcon, ShieldAlert, Building2, LayoutDashboard, Image as ImageIcon, Calendar, Search, Briefcase, Inbox, MessageSquare, Bell, CheckCheck, ExternalLink } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { notificationService } from '../../services/notificationService';
import { AppNotification } from '../../types';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [dropdownOpen, setDropdownOpen] = useState<boolean>(false);
  const [recentNotifications, setRecentNotifications] = useState<AppNotification[]>([]);
  const [loadingDropdown, setLoadingDropdown] = useState<boolean>(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const fetchUnreadCount = async () => {
    if (!isAuthenticated) return;
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
    } catch {
      // silently ignore unauthenticated/network error
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 20000);
    return () => clearInterval(interval);
  }, [isAuthenticated, location.pathname]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleOpenDropdown = async () => {
    const nextState = !dropdownOpen;
    setDropdownOpen(nextState);
    if (nextState) {
      setLoadingDropdown(true);
      try {
        const notifs = await notificationService.getNotifications(false);
        setRecentNotifications(notifs.slice(0, 5));
      } catch {
        // ignore
      } finally {
        setLoadingDropdown(false);
      }
    }
  };

  const handleMarkAllRead = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await notificationService.markAllAsRead();
      setUnreadCount(0);
      setRecentNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    } catch {
      // ignore
    }
  };

  const handleNotificationClick = async (notification: AppNotification) => {
    if (!notification.isRead) {
      try {
        await notificationService.markAsRead(notification.id);
        setUnreadCount(prev => Math.max(0, prev - 1));
        setRecentNotifications(prev =>
          prev.map(n => n.id === notification.id ? { ...n, isRead: true } : n)
        );
      } catch {
        // ignore
      }
    }
    setDropdownOpen(false);
    if (notification.linkUrl) {
      navigate(notification.linkUrl);
    } else {
      navigate('/notifications');
    }
  };

  const formatTimeAgo = (dateStr?: string) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffSec = Math.floor((now.getTime() - date.getTime()) / 1000);
    if (diffSec < 60) return 'just now';
    if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m ago`;
    if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h ago`;
    return `${Math.floor(diffSec / 86400)}d ago`;
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-md px-6 py-3.5 flex items-center justify-between sticky top-0 z-50">
      <div className="flex items-center gap-6">
        <Link to="/" className="flex items-center space-x-3 group">
          <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 group-hover:border-teal-500/60 transition-colors">
            <Camera className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-lg font-bold tracking-tight text-white group-hover:text-teal-400 transition-colors">
                StudioLynk
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 font-medium">
                Phase 12 Active
              </span>
            </div>
            <p className="text-[11px] text-slate-400">Photography Studio & Freelancer Network</p>
          </div>
        </Link>

        {/* Studio Navigation Links when Onboarding is Completed */}
        {isAuthenticated && user && user.role === 'STUDIO' && user.onboardingCompleted && (
          <nav className="hidden md:flex items-center gap-1.5 ml-4">
            <Link
              to="/studio/dashboard"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/studio/dashboard'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <LayoutDashboard className="w-3.5 h-3.5" />
              <span>Dashboard</span>
            </Link>

            <Link
              to="/studio/requirements"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname.startsWith('/studio/requirements')
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Briefcase className="w-3.5 h-3.5" />
              <span>Requirements</span>
            </Link>

            <Link
              to="/studio/requests"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/studio/requests'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Inbox className="w-3.5 h-3.5" />
              <span>Requests</span>
            </Link>

            <Link
              to="/messages"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname.startsWith('/messages')
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5" />
              <span>Messages</span>
            </Link>

            <Link
              to="/studio/discovery"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/studio/discovery'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Search className="w-3.5 h-3.5" />
              <span>Find Creators</span>
            </Link>

            <Link
              to="/studio/profile"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/studio/profile'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Building2 className="w-3.5 h-3.5" />
              <span>Studio Profile</span>
            </Link>
          </nav>
        )}

        {/* Freelancer Navigation Links when Onboarding is Completed */}
        {isAuthenticated && user && user.role === 'FREELANCER' && user.onboardingCompleted && (
          <nav className="hidden md:flex items-center gap-1.5 ml-4">
            <Link
              to="/freelancer/dashboard"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/freelancer/dashboard'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <LayoutDashboard className="w-3.5 h-3.5" />
              <span>Dashboard</span>
            </Link>

            <Link
              to="/freelancer/requests"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname.startsWith('/freelancer/requests')
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Inbox className="w-3.5 h-3.5" />
              <span>Requests</span>
            </Link>

            <Link
              to="/messages"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname.startsWith('/messages')
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5" />
              <span>Messages</span>
            </Link>

            <Link
              to="/freelancer/availability"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/freelancer/availability'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Calendar className="w-3.5 h-3.5" />
              <span>Availability</span>
            </Link>

            <Link
              to="/freelancer/portfolio"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/freelancer/portfolio'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <ImageIcon className="w-3.5 h-3.5" />
              <span>Portfolio (S3)</span>
            </Link>

            <Link
              to="/freelancer/profile"
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                location.pathname === '/freelancer/profile'
                  ? 'bg-slate-800 text-teal-400'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <UserIcon className="w-3.5 h-3.5" />
              <span>Creator Profile</span>
            </Link>
          </nav>
        )}
      </div>

      <div className="flex items-center gap-4">
        {isAuthenticated && user ? (
          <div className="flex items-center gap-3">
            {/* Notification Bell Dropdown */}
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={handleOpenDropdown}
                className="relative p-2 rounded-xl bg-slate-800/80 hover:bg-slate-800 border border-slate-700/80 text-slate-300 hover:text-white transition-all flex items-center justify-center"
                title="Notifications"
                aria-label="Notifications"
              >
                <Bell className="w-4 h-4 text-slate-300" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 flex h-4 min-w-[16px] px-1 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white shadow-sm ring-2 ring-slate-900 animate-pulse">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {dropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-80 sm:w-96 rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl shadow-black/80 z-50 overflow-hidden backdrop-blur-xl animate-in fade-in zoom-in-95 duration-100">
                  <div className="p-3.5 border-b border-slate-800 flex items-center justify-between bg-slate-900/90">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-white text-sm">Notifications</span>
                      {unreadCount > 0 && (
                        <span className="text-[10px] font-medium bg-rose-500/20 text-rose-400 border border-rose-500/30 px-2 py-0.5 rounded-full">
                          {unreadCount} new
                        </span>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button
                        onClick={handleMarkAllRead}
                        className="flex items-center gap-1 text-[11px] text-teal-400 hover:text-teal-300 transition-colors"
                      >
                        <CheckCheck className="w-3.5 h-3.5" />
                        <span>Mark all read</span>
                      </button>
                    )}
                  </div>

                  <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/60">
                    {loadingDropdown ? (
                      <div className="p-6 text-center text-xs text-slate-400">Loading notifications...</div>
                    ) : recentNotifications.length === 0 ? (
                      <div className="p-6 text-center text-xs text-slate-400">No notifications yet.</div>
                    ) : (
                      recentNotifications.map(n => (
                        <div
                          key={n.id}
                          onClick={() => handleNotificationClick(n)}
                          className={`p-3 text-left transition-colors cursor-pointer hover:bg-slate-800/70 flex gap-3 ${
                            !n.isRead ? 'bg-slate-800/40' : 'bg-transparent opacity-80 hover:opacity-100'
                          }`}
                        >
                          <div className="mt-1 shrink-0">
                            <span className={`inline-block w-2 h-2 rounded-full ${!n.isRead ? 'bg-teal-400 ring-2 ring-teal-500/30' : 'bg-transparent'}`} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between gap-1 mb-0.5">
                              <h4 className="text-xs font-semibold text-white truncate">{n.title}</h4>
                              <span className="text-[10px] text-slate-400 whitespace-nowrap">{formatTimeAgo(n.createdAt)}</span>
                            </div>
                            <p className="text-[11px] text-slate-300 line-clamp-2 leading-relaxed">{n.message}</p>
                          </div>
                        </div>
                      ))
                    )}
                  </div>

                  <div className="p-2.5 border-t border-slate-800 bg-slate-900/90 text-center">
                    <Link
                      to="/notifications"
                      onClick={() => setDropdownOpen(false)}
                      className="inline-flex items-center justify-center gap-1.5 text-xs text-teal-400 hover:text-teal-300 font-medium py-1 w-full"
                    >
                      <span>View all notifications</span>
                      <ExternalLink className="w-3 h-3" />
                    </Link>
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center gap-2 bg-slate-800/80 border border-slate-700/80 px-3 py-1.5 rounded-xl">
              <UserIcon className="w-4 h-4 text-teal-400" />
              <div className="text-left text-xs">
                <span className="text-slate-200 font-medium block truncate max-w-[160px]">{user.email}</span>
                <span className="text-[10px] font-semibold text-teal-400 tracking-wider">
                  {user.role}
                </span>
              </div>
            </div>

            {!user.onboardingCompleted && (
              <Link
                to={user.role === 'STUDIO' ? '/onboarding/studio' : '/onboarding/freelancer'}
                className="hidden sm:flex items-center gap-1.5 text-xs text-amber-300 bg-amber-500/10 border border-amber-500/30 px-2.5 py-1.5 rounded-lg hover:bg-amber-500/20 transition-colors"
              >
                <ShieldAlert className="w-3.5 h-3.5" />
                <span>Complete Onboarding</span>
              </Link>
            )}

            <button
              onClick={handleLogout}
              className="flex items-center gap-1.5 text-xs text-slate-300 hover:text-rose-400 bg-slate-800/60 hover:bg-rose-500/10 border border-slate-700 hover:border-rose-500/30 px-3 py-1.5 rounded-lg transition-colors"
              title="Sign Out"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Sign Out</span>
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-2.5">
            <Link
              to="/login"
              className="text-xs font-medium text-slate-200 hover:text-white bg-slate-800/80 hover:bg-slate-800 border border-slate-700 px-3.5 py-2 rounded-lg transition-colors"
            >
              Sign In
            </Link>
            <Link
              to="/register"
              className="text-xs font-semibold text-slate-950 bg-teal-400 hover:bg-teal-300 px-4 py-2 rounded-lg shadow-sm transition-colors"
            >
              Join Platform
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};
