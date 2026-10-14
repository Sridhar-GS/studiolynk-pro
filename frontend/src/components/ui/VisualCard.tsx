import React from 'react';
import { Check } from 'lucide-react';

export interface VisualCardProps {
  selected: boolean;
  onClick: () => void;
  title: string;
  subtitle?: string;
  description?: string;
  icon?: React.ReactNode;
  badge?: string;
  price?: string;
  disabled?: boolean;
  className?: string;
}

export const VisualCard: React.FC<VisualCardProps> = ({
  selected,
  onClick,
  title,
  subtitle,
  description,
  icon,
  badge,
  price,
  disabled = false,
  className = '',
}) => {
  return (
    <div
      role="button"
      tabIndex={disabled ? -1 : 0}
      onClick={() => {
        if (!disabled) onClick();
      }}
      onKeyDown={(e) => {
        if (!disabled && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          onClick();
        }
      }}
      className={`relative p-4 rounded-2xl border transition-all duration-200 cursor-pointer select-none text-left flex flex-col justify-between 
        ${selected 
          ? 'bg-teal-500/10 border-teal-500/60 shadow-md shadow-teal-500/5 ring-1 ring-teal-500/40' 
          : 'bg-slate-900/70 border-slate-800 hover:border-slate-700 hover:bg-slate-850'
        }
        ${disabled ? 'opacity-40 cursor-not-allowed pointer-events-none' : ''}
        ${className}`}
    >
      <div className="flex items-start justify-between gap-3 mb-2">
        <div className="flex items-center gap-3">
          {icon && (
            <div
              className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 transition-colors ${
                selected
                  ? 'bg-teal-500/20 text-teal-300 border border-teal-500/40'
                  : 'bg-slate-800 text-slate-400 border border-slate-700'
              }`}
            >
              {icon}
            </div>
          )}
          <div>
            <h4 className={`text-xs sm:text-sm font-semibold transition-colors ${selected ? 'text-white' : 'text-slate-200'}`}>
              {title}
            </h4>
            {subtitle && (
              <p className="text-[11px] text-slate-400 mt-0.5">{subtitle}</p>
            )}
          </div>
        </div>

        {/* Selected Check Indicator */}
        <div
          className={`w-5 h-5 rounded-lg flex items-center justify-center shrink-0 border transition-colors ${
            selected
              ? 'bg-teal-400 border-teal-400 text-slate-950'
              : 'border-slate-700 bg-slate-800/80 text-transparent'
          }`}
        >
          <Check className="w-3.5 h-3.5 stroke-[3]" />
        </div>
      </div>

      {description && (
        <p className="text-[11px] text-slate-400 line-clamp-2 leading-relaxed mb-3">
          {description}
        </p>
      )}

      {(badge || price) && (
        <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 text-xs">
          {badge && (
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
              {badge}
            </span>
          )}
          {price && (
            <span className="font-semibold text-teal-400 text-xs ml-auto">
              {price}
            </span>
          )}
        </div>
      )}
    </div>
  );
};
