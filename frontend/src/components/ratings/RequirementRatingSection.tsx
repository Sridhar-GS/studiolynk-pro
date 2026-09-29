import React, { useState, useEffect } from 'react';
import { Star, CheckCircle2, MessageSquareQuote, Send } from 'lucide-react';
import { ratingService } from '../../services/ratingService';
import { RequirementRatingStatusDto } from '../../types';

interface RequirementRatingSectionProps {
  requirementId: number;
  requirementTitle: string;
  requirementStatus: string;
  counterpartyName: string;
  counterpartyRole: 'Studio' | 'Freelancer';
  onRatingSubmitted?: () => void;
}

const STAR_LABELS: Record<number, string> = {
  1: 'Poor — Needs Major Improvement',
  2: 'Fair — Below Expectations',
  3: 'Good — Met Requirements',
  4: 'Very Good — Highly Professional',
  5: 'Exceptional — Outstanding Work & Coordination',
};

export const RequirementRatingSection: React.FC<RequirementRatingSectionProps> = ({
  requirementId,
  requirementTitle,
  requirementStatus,
  counterpartyName,
  counterpartyRole,
  onRatingSubmitted,
}) => {
  const [ratingStatus, setRatingStatus] = useState<RequirementRatingStatusDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [selectedScore, setSelectedScore] = useState<number>(5);
  const [hoverScore, setHoverScore] = useState<number>(0);
  const [reviewText, setReviewText] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchStatus = async () => {
    try {
      setLoading(true);
      const data = await ratingService.getRequirementRatingStatus(requirementId);
      setRatingStatus(data);
    } catch (err) {
      console.error('Failed to load rating status:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (requirementStatus === 'COMPLETED') {
      fetchStatus();
    }
  }, [requirementId, requirementStatus]);

  if (requirementStatus !== 'COMPLETED') {
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedScore < 1 || selectedScore > 5) {
      setError('Please select a rating between 1 and 5 stars.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      await ratingService.submitRating(requirementId, {
        score: selectedScore,
        reviewText: reviewText.trim() || undefined,
      });
      setSuccessMsg('Your review and rating have been published to the profile.');
      await fetchStatus();
      if (onRatingSubmitted) {
        onRatingSubmitted();
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to submit rating. Please try again.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const activeStarCount = hoverScore || selectedScore;

  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-900/70 backdrop-blur-md p-6 space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800/80 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400">
            <Star className="w-5 h-5 fill-current" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <span>Shoot Ratings & Reviews</span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-purple-500/20 text-purple-300 border border-purple-500/30 font-medium">
                RAT-001 - RAT-006
              </span>
            </h3>
            <p className="text-xs text-slate-400">
              Two-way public feedback for completed work on <span className="text-slate-300 font-medium">{requirementTitle}</span>
            </p>
          </div>
        </div>

        {ratingStatus?.alreadyRated && (
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-xs font-semibold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Rating Submitted</span>
          </span>
        )}
      </div>

      {loading ? (
        <div className="p-8 text-center text-xs text-slate-400">Loading rating information...</div>
      ) : (
        <div className="space-y-6">
          {/* Submission Form if Not Yet Rated */}
          {ratingStatus?.canRate && (
            <form onSubmit={handleSubmit} className="space-y-5 bg-slate-800/40 p-5 rounded-2xl border border-slate-800">
              <div>
                <h4 className="text-sm font-semibold text-white mb-1">
                  Rate your experience with {counterpartyName} ({counterpartyRole})
                </h4>
                <p className="text-xs text-slate-400">
                  Select a star rating and leave an optional review. Ratings are public on the profile.
                </p>
              </div>

              {/* Star Rating Controls */}
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setSelectedScore(star)}
                      onMouseEnter={() => setHoverScore(star)}
                      onMouseLeave={() => setHoverScore(0)}
                      className="p-1 rounded-lg hover:scale-110 transition-transform focus:outline-none"
                      title={`${star} Star${star > 1 ? 's' : ''}`}
                    >
                      <Star
                        className={`w-7 h-7 transition-colors ${
                          star <= activeStarCount
                            ? 'text-amber-400 fill-amber-400 drop-shadow-[0_0_8px_rgba(251,191,36,0.5)]'
                            : 'text-slate-600 hover:text-slate-400'
                        }`}
                      />
                    </button>
                  ))}
                  <span className="text-xs font-semibold text-amber-300 ml-2">
                    {STAR_LABELS[activeStarCount] || ''}
                  </span>
                </div>
              </div>

              {/* Optional Written Review Input */}
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-300 block">
                  Written Feedback / Review <span className="text-slate-500 font-normal">(Optional)</span>
                </label>
                <textarea
                  rows={3}
                  value={reviewText}
                  onChange={(e) => setReviewText(e.target.value)}
                  maxLength={1000}
                  placeholder={`Share key highlights about communication, timeliness, and output quality with ${counterpartyName}...`}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700/80 text-white placeholder-slate-500 text-xs focus:outline-none focus:border-teal-500 transition-colors"
                />
                <div className="flex justify-between items-center text-[10px] text-slate-500">
                  <span>Reviews are public on profiles and cannot be edited once posted.</span>
                  <span>{reviewText.length}/1000</span>
                </div>
              </div>

              {error && (
                <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs">
                  {error}
                </div>
              )}

              {successMsg && (
                <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                  <span>{successMsg}</span>
                </div>
              )}

              <button
                type="submit"
                disabled={submitting}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-teal-400 hover:bg-teal-300 text-slate-950 font-bold text-xs shadow-md transition disabled:opacity-50"
              >
                {submitting ? (
                  <span>Submitting Review...</span>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5" />
                    <span>Submit Rating & Review</span>
                  </>
                )}
              </button>
            </form>
          )}

          {/* User's Submitted Rating Display */}
          {ratingStatus?.alreadyRated && ratingStatus.myRating && (
            <div className="bg-slate-800/40 p-4 rounded-2xl border border-slate-800 space-y-2.5">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-300">
                  Your Rating for {ratingStatus.myRating.toUserName}
                </span>
                <span className="text-[11px] text-slate-500">
                  {new Date(ratingStatus.myRating.createdAt).toLocaleDateString()}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1 text-amber-400">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star
                      key={s}
                      className={`w-4 h-4 ${
                        s <= (ratingStatus.myRating?.score || 0)
                          ? 'fill-amber-400 text-amber-400'
                          : 'text-slate-600'
                      }`}
                    />
                  ))}
                </div>
                <span className="text-xs font-bold text-white">
                  {ratingStatus.myRating.score} / 5 Stars
                </span>
              </div>
              {ratingStatus.myRating.reviewText && (
                <p className="text-xs text-slate-300 bg-slate-900/60 p-3 rounded-xl border border-slate-800/60 italic leading-relaxed">
                  "{ratingStatus.myRating.reviewText}"
                </p>
              )}
            </div>
          )}

          {/* Counterparty's Rating for Current User (if submitted) */}
          {ratingStatus?.counterpartyRated && ratingStatus.counterpartyRating && (
            <div className="bg-teal-950/20 p-4 rounded-2xl border border-teal-500/20 space-y-2.5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <MessageSquareQuote className="w-4 h-4 text-teal-400" />
                  <span className="text-xs font-semibold text-teal-300">
                    Feedback Received from {ratingStatus.counterpartyRating.fromUserName}
                  </span>
                </div>
                <span className="text-[11px] text-slate-500">
                  {new Date(ratingStatus.counterpartyRating.createdAt).toLocaleDateString()}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1 text-amber-400">
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star
                      key={s}
                      className={`w-4 h-4 ${
                        s <= (ratingStatus.counterpartyRating?.score || 0)
                          ? 'fill-amber-400 text-amber-400'
                          : 'text-slate-600'
                      }`}
                    />
                  ))}
                </div>
                <span className="text-xs font-bold text-white">
                  {ratingStatus.counterpartyRating.score} / 5 Stars
                </span>
              </div>
              {ratingStatus.counterpartyRating.reviewText && (
                <p className="text-xs text-slate-200 bg-slate-900/60 p-3 rounded-xl border border-slate-800/60 italic leading-relaxed">
                  "{ratingStatus.counterpartyRating.reviewText}"
                </p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
