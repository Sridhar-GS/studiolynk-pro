import React, { useState, useEffect } from 'react';
import { Star, MessageSquareQuote } from 'lucide-react';
import { ratingService } from '../../services/ratingService';
import { RatingSummaryDto, RatingDto } from '../../types';

interface PublicReviewsListProps {
  targetType: 'FREELANCER' | 'STUDIO';
  targetId: number;
  title?: string;
  compact?: boolean;
}

export const PublicReviewsList: React.FC<PublicReviewsListProps> = ({
  targetType,
  targetId,
  title = 'Verified Client Reviews & Ratings',
  compact = false,
}) => {
  const [summary, setSummary] = useState<RatingSummaryDto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRatings = async () => {
      try {
        setLoading(true);
        const data =
          targetType === 'FREELANCER'
            ? await ratingService.getFreelancerRatings(targetId)
            : await ratingService.getStudioRatings(targetId);
        setSummary(data);
      } catch (err) {
        console.error('Failed to load public reviews:', err);
      } finally {
        setLoading(false);
      }
    };

    if (targetId) {
      fetchRatings();
    }
  }, [targetType, targetId]);

  if (loading) {
    return (
      <div className="p-6 text-center text-xs text-slate-400 bg-slate-900/40 rounded-2xl border border-slate-800">
        Loading verified ratings and reviews...
      </div>
    );
  }

  const avgRating = summary?.averageRating || 0;
  const totalCount = summary?.totalRatings || 0;
  const reviews = summary?.ratings || [];

  return (
    <div className="space-y-4">
      {/* Summary Header Strip */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-4 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
            <Star className="w-5 h-5 fill-current" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-white flex items-center gap-2">
              <span>{title}</span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 font-medium">
                RAT-003
              </span>
            </h4>
            <p className="text-xs text-slate-400">
              {totalCount > 0
                ? `${totalCount} verified review${totalCount > 1 ? 's' : ''} from completed shoots`
                : 'Ratings and reviews are public on profiles (RAT-003)'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {totalCount > 0 ? (
            <div className="flex items-center gap-2 bg-slate-800/80 px-3 py-1.5 rounded-xl border border-slate-700">
              <span className="text-lg font-extrabold text-amber-400">{avgRating.toFixed(1)}</span>
              <div className="flex items-center gap-0.5 text-amber-400">
                {[1, 2, 3, 4, 5].map((s) => (
                  <Star
                    key={s}
                    className={`w-3.5 h-3.5 ${
                      s <= Math.round(avgRating) ? 'fill-current' : 'text-slate-600'
                    }`}
                  />
                ))}
              </div>
            </div>
          ) : (
            <span className="text-xs text-slate-500 italic">No ratings yet</span>
          )}
        </div>
      </div>

      {/* Review Cards Feed */}
      {reviews.length === 0 ? (
        <div className="p-8 text-center rounded-2xl bg-slate-900/40 border border-slate-800/80 space-y-2">
          <MessageSquareQuote className="w-8 h-8 text-slate-600 mx-auto" />
          <h5 className="text-xs font-semibold text-slate-300">No public reviews yet</h5>
          <p className="text-[11px] text-slate-500 max-w-sm mx-auto">
            Reviews from verified completed shoots will be displayed here once submitted by {targetType === 'FREELANCER' ? 'studios' : 'creators'}.
          </p>
        </div>
      ) : (
        <div className={`space-y-3 ${compact ? 'max-h-80 overflow-y-auto pr-1' : ''}`}>
          {reviews.map((r: RatingDto) => (
            <div
              key={r.id}
              className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 hover:border-slate-700 transition space-y-2.5"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-white">{r.fromUserName}</span>
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700 font-medium">
                    {r.fromUserRole}
                  </span>
                </div>
                <span className="text-[10px] text-slate-500">
                  {new Date(r.createdAt).toLocaleDateString(undefined, {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                  })}
                </span>
              </div>

              {/* Stars & Event Tag */}
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-1.5">
                  <div className="flex items-center gap-0.5 text-amber-400">
                    {[1, 2, 3, 4, 5].map((s) => (
                      <Star
                        key={s}
                        className={`w-3.5 h-3.5 ${
                          s <= r.score ? 'fill-current' : 'text-slate-600'
                        }`}
                      />
                    ))}
                  </div>
                  <span className="text-xs font-bold text-slate-200">{r.score}.0</span>
                </div>

                {r.requirementTitle && (
                  <span className="text-[10px] text-teal-400 bg-teal-500/10 px-2 py-0.5 rounded-full border border-teal-500/20 font-medium">
                    Shoot: {r.requirementTitle}
                  </span>
                )}
              </div>

              {/* Review Text */}
              {r.reviewText && (
                <p className="text-xs text-slate-300 leading-relaxed bg-slate-950/40 p-3 rounded-xl border border-slate-800/60 italic">
                  "{r.reviewText}"
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
