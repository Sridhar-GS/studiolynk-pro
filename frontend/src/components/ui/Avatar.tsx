import React from 'react';

export interface AvatarProps {
  src?: string | null;
  alt?: string;
  name?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl';
  online?: boolean;
  className?: string;
}

export const Avatar: React.FC<AvatarProps> = ({
  src,
  alt = 'Avatar',
  name = '',
  size = 'md',
  online,
  className = '',
}) => {
  const sizeStyles: Record<string, string> = {
    xs: 'w-6 h-6 text-[10px]',
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-16 h-16 text-lg',
    '2xl': 'w-24 h-24 text-2xl',
  };

  const dotSizeStyles: Record<string, string> = {
    xs: 'w-1.5 h-1.5',
    sm: 'w-2 h-2',
    md: 'w-2.5 h-2.5',
    lg: 'w-3 h-3',
    xl: 'w-4 h-4',
    '2xl': 'w-5 h-5',
  };

  const getInitials = (str: string) => {
    if (!str) return 'U';
    const parts = str.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  };

  return (
    <div className={`relative inline-block shrink-0 ${className}`}>
      {src ? (
        <img
          src={src}
          alt={alt || name}
          className={`${sizeStyles[size] || sizeStyles.md} rounded-2xl object-cover border border-slate-700/80 shadow-sm`}
        />
      ) : (
        <div
          className={`${sizeStyles[size] || sizeStyles.md} rounded-2xl bg-teal-500/15 border border-teal-500/30 text-teal-300 font-bold flex items-center justify-center select-none shadow-sm`}
        >
          {getInitials(name)}
        </div>
      )}

      {typeof online === 'boolean' && (
        <span
          className={`absolute bottom-0 right-0 ${dotSizeStyles[size] || dotSizeStyles.md} rounded-full ring-2 ring-slate-950 ${
            online ? 'bg-emerald-400' : 'bg-slate-500'
          }`}
        />
      )}
    </div>
  );
};
