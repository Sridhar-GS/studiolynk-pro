import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Search,
  Briefcase,
  Inbox,
  MessageSquare,
  Bell,
  Calendar,
  Image as ImageIcon,
  Building2,
  Users,
  Activity,
  Camera,
  User,
  Sparkles
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { notificationService } from '../../services/notificationService';

export const BottomNav: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();
  const [unreadNotifications, setUnreadNotifications] = useState<number>(0);

  useEffect(() => {
    if (!isAuthenticated) return;
    const fetchUnread = async () => {
      try {
        const count = await notificationService.getUnreadCount();
        setUnreadNotifications(count);
      } catch {
        // ignore
      }
    };
    fetchUnread();
    const timer = setInterval(fetchUnread, 30000);
    return () => clearInterval(timer);
  }, [isAuthenticated, location.pathname]);

  const isActive = (path: string) => {
    if (path === '/' && location.pathname === '/') return true;
    if (path !== '/' && location.pathname.startsWith(path)) return true;
    return false;
  };

  // Build role-specific navigation items
  let navItems: { label: string; path: string; icon: React.ReactNode; badge?: number }[] = [];

  if (isAuthenticated && user?.onboardingCompleted) {
    if (user.role === 'STUDIO') {
      navItems = [
        { label: 'Overview', path: '/studio/dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
        { label: 'Discover', path: '/studio/discovery', icon: <Search className="w-4 h-4" /> },
        { label: 'Shoots', path: '/studio/requirements', icon: <Briefcase className="w-4 h-4" /> },
        { label: 'Requests', path: '/studio/requests', icon: <Inbox className="w-4 h-4" /> },
        { label: 'Messages', path: '/messages', icon: <MessageSquare className="w-4 h-4" /> },
        { label: 'Alerts', path: '/notifications', icon: <Bell className="w-4 h-4" />, badge: unreadNotifications },
      ];
    } else if (user.role === 'FREELANCER') {
      navItems = [
        { label: 'Overview', path: '/freelancer/dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
        { label: 'Requests', path: '/freelancer/requests', icon: <Inbox className="w-4 h-4" /> },
        { label: 'Calendar', path: '/freelancer/availability', icon: <Calendar className="w-4 h-4" /> },
        { label: 'Portfolio', path: '/freelancer/portfolio', icon: <ImageIcon className="w-4 h-4" /> },
        { label: 'Messages', path: '/messages', icon: <MessageSquare className="w-4 h-4" /> },
        { label: 'Alerts', path: '/notifications', icon: <Bell className="w-4 h-4" />, badge: unreadNotifications },
      ];
    } else if (user.role === 'ADMIN') {
      navItems = [
        { label: 'Overview', path: '/admin/dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
        { label: 'Studios', path: '/admin/studios', icon: <Building2 className="w-4 h-4" /> },
        { label: 'Creators', path: '/admin/freelancers', icon: <Users className="w-4 h-4" /> },
        { label: 'Shoots', path: '/admin/requirements', icon: <Briefcase className="w-4 h-4" /> },
        { label: 'Audit', path: '/admin/activity', icon: <Activity className="w-4 h-4" /> },
      ];
    }
  } else if (isAuthenticated && !user?.onboardingCompleted) {
    navItems = [
      { label: 'Setup', path: user?.role === 'STUDIO' ? '/onboarding/studio' : '/onboarding/freelancer', icon: <User className="w-4 h-4" /> },
      { label: 'Home', path: '/', icon: <Camera className="w-4 h-4" /> },
    ];
  } else {
    navItems = [
      { label: 'Home', path: '/', icon: <Camera className="w-4 h-4" /> },
      { label: 'Sign In', path: '/login', icon: <User className="w-4 h-4" /> },
      { label: 'Join', path: '/register', icon: <Sparkles className="w-4 h-4" /> },
    ];
  }

  return (
    <nav
      aria-label="Mobile Bottom Navigation"
      className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-slate-950/95 backdrop-blur-lg border-t border-slate-800/80 px-1 py-1.5 flex items-center justify-around shadow-2xl"
    >
      {navItems.map((item) => {
        const active = isActive(item.path);
        return (
          <Link
            key={item.path}
            to={item.path}
            className={`flex flex-col items-center justify-center py-1 px-1.5 rounded-xl transition-all duration-150 relative min-w-[50px] min-h-[44px] ${
              active
                ? 'text-teal-400 font-semibold'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <div className="relative">
              {item.icon}
              {typeof item.badge === 'number' && item.badge > 0 && (
                <span className="absolute -top-1.5 -right-2 bg-teal-400 text-slate-950 text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center shadow-sm">
                  {item.badge > 9 ? '9+' : item.badge}
                </span>
              )}
            </div>
            <span className="text-[10px] mt-1 tracking-tight truncate max-w-[62px]">
              {item.label}
            </span>
            {active && (
              <span className="w-1 h-1 bg-teal-400 rounded-full mt-0.5" />
            )}
          </Link>
        );
      })}
    </nav>
  );
};
