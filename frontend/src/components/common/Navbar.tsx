import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Camera, LogOut, User as UserIcon, ShieldAlert } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-md px-6 py-3.5 flex items-center justify-between sticky top-0 z-50">
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
              Phase 3 Active
            </span>
          </div>
          <p className="text-[11px] text-slate-400">Photography Studio & Freelancer Network</p>
        </div>
      </Link>

      <div className="flex items-center gap-4">
        {isAuthenticated && user ? (
          <div className="flex items-center gap-3">
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
                to="/onboarding"
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
