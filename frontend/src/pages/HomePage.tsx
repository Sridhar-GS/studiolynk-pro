import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Brain,
  Database,
  ShieldCheck,
  CheckCircle2,
  User,
  ArrowRight,
  Sparkles,
  Camera,
  Calendar,
  MessageSquare,
  Search,
  Layers
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { HealthStatus } from '../types';
import { Button, Badge, Card, CardContent } from '../components/ui';

export const HomePage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [backendHealth, setBackendHealth] = useState<HealthStatus | null>(null);
  const [mlHealth, setMlHealth] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkServices = async () => {
      try {
        const backendRes = await fetch('/api/health');
        if (backendRes.ok) {
          const data = await backendRes.json();
          setBackendHealth(data);
        }
      } catch (err) {
        console.warn('Backend not currently reachable:', err);
      }

      try {
        const mlRes = await fetch('http://127.0.0.1:8000/health');
        if (mlRes.ok) {
          const data = await mlRes.json();
          setMlHealth(data);
        }
      } catch (err) {
        console.warn('ML Service not currently reachable:', err);
      }

      setLoading(false);
    };

    checkServices();
  }, []);

  return (
    <div className="flex-1 flex flex-col justify-between">
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-10 sm:py-14 flex-1 flex flex-col justify-center w-full">
        {/* User Status Banner if Logged In */}
        {isAuthenticated && user && (
          <div className="mb-8 p-4 bg-teal-500/10 border border-teal-500/30 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4 animate-fade-in">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-teal-500/20 border border-teal-500/40 flex items-center justify-center text-teal-400 shrink-0">
                <User className="w-5 h-5" />
              </div>
              <div>
                <h4 className="text-sm font-semibold text-white">
                  Welcome back, <span className="text-teal-400">{user.email}</span>
                </h4>
                <p className="text-xs text-slate-400">
                  Role: <strong className="text-slate-200">{user.role}</strong> &bull;{' '}
                  Onboarding:{' '}
                  {user.onboardingCompleted ? (
                    <span className="text-emerald-400 font-medium">Completed</span>
                  ) : (
                    <span className="text-amber-400 font-medium">Pending Setup</span>
                  )}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              {!user.onboardingCompleted ? (
                <Link to="/onboarding">
                  <Button size="sm" icon={<ArrowRight className="w-3.5 h-3.5" />} iconPosition="right">
                    Continue Onboarding
                  </Button>
                </Link>
              ) : user.role === 'STUDIO' ? (
                <Link to="/studio/dashboard">
                  <Button size="sm" icon={<ArrowRight className="w-3.5 h-3.5" />} iconPosition="right">
                    Go to Studio Dashboard
                  </Button>
                </Link>
              ) : user.role === 'FREELANCER' ? (
                <Link to="/freelancer/dashboard">
                  <Button size="sm" icon={<ArrowRight className="w-3.5 h-3.5" />} iconPosition="right">
                    Go to Creator Dashboard
                  </Button>
                </Link>
              ) : (
                <Link to="/admin/dashboard">
                  <Button size="sm" icon={<ArrowRight className="w-3.5 h-3.5" />} iconPosition="right">
                    Go to Admin Portal
                  </Button>
                </Link>
              )}
            </div>
          </div>
        )}

        {/* Hero Section */}
        <div className="text-center max-w-3xl mx-auto mb-14">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-teal-500/10 border border-teal-500/30 text-teal-400 text-xs font-semibold mb-5 shadow-sm">
            <Sparkles className="w-3.5 h-3.5 animate-pulse" />
            <span>StudioLynk Pro &bull; Final Academic Prototype Live</span>
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight text-white mb-6 leading-tight">
            The Intelligent Network for{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-teal-400 to-emerald-400">
              Photography Studios
            </span>{' '}
            &amp;{' '}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-400">
              Freelance Creators
            </span>
          </h1>

          <p className="text-slate-400 text-sm sm:text-base leading-relaxed max-w-2xl mx-auto mb-8">
            Empowering professional studios to publish shoot requirements, discover verified photographers 
            via Decision Tree machine learning, check rolling 10-day availability, and negotiate in real time.
          </p>

          {!isAuthenticated ? (
            <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
              <Link to="/register" className="w-full sm:w-auto">
                <Button size="lg" className="w-full sm:w-auto" icon={<ArrowRight className="w-4 h-4" />} iconPosition="right">
                  Get Started (Join Network)
                </Button>
              </Link>
              <Link to="/login" className="w-full sm:w-auto">
                <Button variant="secondary" size="lg" className="w-full sm:w-auto">
                  Sign In to StudioLynk
                </Button>
              </Link>
            </div>
          ) : (
            <div className="flex flex-wrap items-center justify-center gap-3">
              {user?.role === 'STUDIO' && (
                <>
                  <Link to="/studio/discovery">
                    <Button icon={<Search className="w-4 h-4" />}>
                      Explore Creators (AI Match)
                    </Button>
                  </Link>
                  <Link to="/studio/requirements/new">
                    <Button variant="secondary" icon={<Camera className="w-4 h-4" />}>
                      Create Work Requirement
                    </Button>
                  </Link>
                </>
              )}
              {user?.role === 'FREELANCER' && (
                <>
                  <Link to="/freelancer/availability">
                    <Button icon={<Calendar className="w-4 h-4" />}>
                      Manage 10-Day Availability
                    </Button>
                  </Link>
                  <Link to="/freelancer/portfolio">
                    <Button variant="secondary" icon={<Layers className="w-4 h-4" />}>
                      Manage Portfolio Gallery
                    </Button>
                  </Link>
                </>
              )}
            </div>
          )}
        </div>

        {/* Feature Highlights Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-12">
          {/* Feature 1: AI Decision Tree Matching */}
          <Card hoverEffect className="relative overflow-hidden">
            <CardContent className="space-y-3">
              <div className="w-12 h-12 rounded-2xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 shadow-sm">
                <Brain className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white tracking-tight">AI Match Scoring</h3>
                <Badge variant="purple" size="sm">Phase 14 &amp; 15</Badge>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                FastAPI microservice trained on 5,000 synthetic records using Decision Tree regression across 6 normalized features, computing 0–100 match percentages.
              </p>
              <div className="pt-1 flex items-center text-xs text-purple-300 font-medium gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-purple-400" />
                <span>Hard availability filter &bull; No rating bias</span>
              </div>
            </CardContent>
          </Card>

          {/* Feature 2: Rolling 10-Day Availability */}
          <Card hoverEffect className="relative overflow-hidden">
            <CardContent className="space-y-3">
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 shadow-sm">
                <Calendar className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white tracking-tight">10-Day Shoot Window</h3>
                <Badge variant="teal" size="sm">Phase 7 &amp; 8</Badge>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Dynamic rolling scheduling window with explicit daily statuses: Available (with call/wrap hours), Busy, and Not Set. Conflict prevention built in.
              </p>
              <div className="pt-1 flex items-center text-xs text-teal-300 font-medium gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-teal-400" />
                <span>Auto-exclusion of busy creators</span>
              </div>
            </CardContent>
          </Card>

          {/* Feature 3: Real-Time Chat & Requests */}
          <Card hoverEffect className="relative overflow-hidden">
            <CardContent className="space-y-3">
              <div className="w-12 h-12 rounded-2xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400 shadow-sm">
                <MessageSquare className="w-6 h-6" />
              </div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white tracking-tight">Real-Time Negotiation</h3>
                <Badge variant="blue" size="sm">Phase 10 &amp; 11</Badge>
              </div>
              <p className="text-xs text-slate-400 leading-relaxed">
                Bidirectional WebSocket / STOMP messaging scoped per requirement. Negotiate rates, confirm one creator, and unlock confidential client contacts.
              </p>
              <div className="pt-1 flex items-center text-xs text-blue-300 font-medium gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-blue-400" />
                <span>Privacy masked until confirmation</span>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Architectural Probes & Security Banner */}
        <div className="bg-slate-900/60 border border-slate-800/80 rounded-2xl p-5 sm:p-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-xs sm:text-sm font-semibold text-white">System Architecture &amp; Live Probes</h4>
              <p className="text-[11px] text-slate-400">
                MySQL bound to 127.0.0.1:3306 &bull; Spring Boot REST API &bull; Python FastAPI &bull; S3 Asset Pipeline
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Badge variant={backendHealth ? 'emerald' : 'slate'} size="sm" dot>
              Backend: {loading ? 'Probing...' : backendHealth ? 'UP' : 'MockMvc Ready'}
            </Badge>
            <Badge variant={mlHealth ? 'purple' : 'slate'} size="sm" dot>
              ML Service: {loading ? 'Probing...' : mlHealth ? 'UP' : 'FastAPI Ready'}
            </Badge>
            <Badge variant="teal" size="sm">
              <Database className="w-3 h-3 text-teal-400" />
              MySQL 8.0 (Flyway V2)
            </Badge>
          </div>
        </div>
      </main>

      <footer className="border-t border-slate-800/80 bg-slate-950 px-4 sm:px-6 py-4 text-center text-xs text-slate-500">
        StudioLynk Academic Prototype &copy; 2026 &bull; Phase 17 UI/UX Refinement Active
      </footer>
    </div>
  );
};
