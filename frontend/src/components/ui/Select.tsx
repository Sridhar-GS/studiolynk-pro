import React, { forwardRef } from 'react';
import { ChevronDown } from 'lucide-react';

export interface SelectOption {
  value: string | number;
  label: string;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  options?: SelectOption[];
  icon?: React.ReactNode;
  error?: string;
  helperText?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(({
  label,
  options,
  icon,
  error,
  helperText,
  id,
  children,
  className = '',
  required,
  disabled,
  ...props
}, ref) => {
  const selectId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label htmlFor={selectId} className="block text-xs font-semibold text-slate-300">
          {label} {required && <span className="text-rose-400">*</span>}
        </label>
      )}

      <div className="relative rounded-xl shadow-sm">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            {icon}
          </div>
        )}

        <select
          id={selectId}
          ref={ref}
          disabled={disabled}
          className={`w-full text-xs text-white bg-slate-900/90 border rounded-xl appearance-none transition-colors duration-150 py-2.5 
            ${icon ? 'pl-10' : 'pl-3.5'} pr-10
            ${error 
              ? 'border-rose-500/80 focus:border-rose-500 focus:ring-1 focus:ring-rose-500/40' 
              : 'border-slate-800 focus:border-teal-500 focus:ring-1 focus:ring-teal-500/30 hover:border-slate-700'
            }
            ${disabled ? 'opacity-50 cursor-not-allowed bg-slate-900/50' : ''}
            focus:outline-none ${className}`}
          {...props}
        >
          {options ? options.map((opt) => (
            <option key={opt.value} value={opt.value} className="bg-slate-900 text-white">
              {opt.label}
            </option>
          )) : children}
        </select>

        <div className="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-slate-400">
          <ChevronDown className="w-4 h-4" />
        </div>
      </div>

      {error ? (
        <p className="text-[11px] text-rose-400 font-medium flex items-center gap-1 mt-1">
          {error}
        </p>
      ) : helperText ? (
        <p className="text-[11px] text-slate-400 mt-1">
          {helperText}
        </p>
      ) : null}
    </div>
  );
});

Select.displayName = 'Select';
