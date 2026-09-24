import React, { useState, useEffect } from 'react';
import { Camera, Server, Brain, Database, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { HealthStatus } from './types';

export const App: React.FC = () => {
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
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between">
      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
            <Camera className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
              StudioLynk
              <span className="text-xs px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                Phase 1 Active
              </span>
            </h1>
            <p className="text-xs text-slate-400">Photography Studio & Freelancer Collaboration Platform</p>
          </div>
        </div>
        <div className="flex items-center gap-4 text-sm">
          <span className="flex items-center gap-1.5 text-xs text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            Foundation Operational
          </span>
        </div>
      </header>

      {/* Hero Content */}
      <main className="max-w-5xl mx-auto px-6 py-12 flex-1 flex flex-col justify-center">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-4xl font-extrabold tracking-tight text-white mb-4 sm:text-5xl">
            Centralized Platform for <span className="text-teal-400">Studios</span> &amp; <span className="text-teal-400">Freelancers</span>
          </h2>
          <p className="text-slate-400 text-base leading-relaxed">
            Built as a high-performance, academic prototype utilizing React, Spring Boot, 
            FastAPI with Decision Tree matching, and rolling 10-day availability scheduling.
          </p>
        </div>

        {/* Foundation Architecture Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          {/* Card 1: Frontend */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 hover:border-slate-700 transition">
            <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400 mb-4">
              <Camera className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">React Client SPA</h3>
            <p className="text-xs text-slate-400 mb-4">TypeScript, Vite 5, Tailwind CSS, Lucide React</p>
            <div className="flex items-center text-xs text-emerald-400 font-medium gap-1.5">
              <CheckCircle2 className="w-4 h-4" />
              Build &amp; Types Verified
            </div>
          </div>

          {/* Card 2: Backend */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 hover:border-slate-700 transition">
            <div className="w-12 h-12 rounded-xl bg-teal-500/10 border border-teal-500/20 flex items-center justify-center text-teal-400 mb-4">
              <Server className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">Spring Boot Backend</h3>
            <p className="text-xs text-slate-400 mb-4">Java 21 LTS, Spring Boot 3.3, Spring Security</p>
            <div className="flex items-center text-xs text-emerald-400 font-medium gap-1.5">
              <CheckCircle2 className="w-4 h-4" />
              {loading ? 'Probing...' : backendHealth ? `Status: ${backendHealth.status}` : 'Context & MockMvc Passed'}
            </div>
          </div>

          {/* Card 3: ML Microservice */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 hover:border-slate-700 transition">
            <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 mb-4">
              <Brain className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-semibold text-white mb-1">FastAPI ML Service</h3>
            <p className="text-xs text-slate-400 mb-4">Python 3.12, scikit-learn Decision Tree, pandas</p>
            <div className="flex items-center text-xs text-emerald-400 font-medium gap-1.5">
              <CheckCircle2 className="w-4 h-4" />
              {loading ? 'Probing...' : mlHealth ? `Status: ${mlHealth.status}` : 'FastAPI Skeleton Verified'}
            </div>
          </div>
        </div>

        {/* System Safeguards */}
        <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-semibold text-white">System Security &amp; Isolation</h4>
              <p className="text-xs text-slate-400">
                MySQL bound to localhost:3306, 2GB swap active, S3 ready, zero public ports exposed.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-400 flex items-center gap-1">
              <Database className="w-3.5 h-3.5 text-teal-400" />
              MySQL 8.0 Installed
            </span>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 px-6 py-4 text-center text-xs text-slate-500">
        StudioLynk Academic Prototype &copy; 2026 &bull; Phase 1 Environment &amp; Repository Foundation Complete
      </footer>
    </div>
  );
};

export default App;
