import React, { forwardRef } from 'react';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  icon?: React.ReactNode;
  trailingIcon?: React.ReactNode;
  error?: string;
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(({
  label,
  icon,
  trailingIcon,
  error,
  helperText,
  id,
  className = '',
  required,
  disabled,
  ...props
}, ref) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label htmlFor={inputId} className="block text-xs font-semibold text-slate-300">
          {label} {required && <span className="text-rose-400">*</span>}
        </label>
      )}

      <div className="relative rounded-xl shadow-sm">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            {icon}
          </div>
        )}

        <input
          id={inputId}
          ref={ref}
          disabled={disabled}
          className={`w-full text-xs text-white placeholder-slate-500 bg-slate-900/90 border rounded-xl transition-colors duration-150 py-2.5 
            ${icon ? 'pl-10' : 'pl-3.5'} 
            ${trailingIcon ? 'pr-10' : 'pr-3.5'}
            ${error 
              ? 'border-rose-500/80 focus:border-rose-500 focus:ring-1 focus:ring-rose-500/40' 
              : 'border-slate-800 focus:border-teal-500 focus:ring-1 focus:ring-teal-500/30 hover:border-slate-700'
            }
            ${disabled ? 'opacity-50 cursor-not-allowed bg-slate-900/50' : ''}
            focus:outline-none ${className}`}
          {...props}
        />

        {trailingIcon && (
          <div className="absolute inset-y-0 right-0 pr-3.5 flex items-center pointer-events-none text-slate-400">
            {trailingIcon}
          </div>
        )}
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

Input.displayName = 'Input';
