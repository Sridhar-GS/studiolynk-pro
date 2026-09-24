import React, { useState, useEffect, useRef } from 'react';
import {
  Plus,
  Trash2,
  Edit2,
  ArrowLeft,
  ArrowRight,
  Upload,
  Image as ImageIcon,
  CheckCircle2,
  AlertCircle,
  X,
  Maximize2,
  MoveLeft,
  MoveRight,
  ShieldCheck,
  FolderPlus
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { portfolioService } from '../../services/portfolioService';
import { Portfolio, PortfolioCategory, PortfolioImage } from '../../types';

export const FreelancerPortfolioPage: React.FC = () => {
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [activeCategoryId, setActiveCategoryId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Modals
  const [showAddCategoryModal, setShowAddCategoryModal] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  const [editingCategory, setEditingCategory] = useState<PortfolioCategory | null>(null);
  const [editCategoryName, setEditCategoryName] = useState('');

  const [lightboxImage, setLightboxImage] = useState<PortfolioImage | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchPortfolio = async (selectCategoryId?: number) => {
    try {
      setLoading(true);
      const data = await portfolioService.getMyPortfolio();
      setPortfolio(data);

      if (data.categories && data.categories.length > 0) {
        if (selectCategoryId && data.categories.some((c) => c.id === selectCategoryId)) {
          setActiveCategoryId(selectCategoryId);
        } else if (!activeCategoryId || !data.categories.some((c) => c.id === activeCategoryId)) {
          setActiveCategoryId(data.categories[0].id);
        }
      } else {
        setActiveCategoryId(null);
      }
    } catch (err: any) {
      console.error('Failed to load portfolio:', err);
      setStatusMessage({ type: 'error', text: 'Failed to load portfolio.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPortfolio();
  }, []);

  const activeCategory = portfolio?.categories.find((c) => c.id === activeCategoryId);

  // Category Actions
  const handleCreateCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    try {
      const created = await portfolioService.createCategory({ name: newCategoryName.trim() });
      setNewCategoryName('');
      setShowAddCategoryModal(false);
      setStatusMessage({ type: 'success', text: `Category "${created.name}" created!` });
      await fetchPortfolio(created.id);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to create category.';
      setStatusMessage({ type: 'error', text: msg });
    }
  };

  const handleUpdateCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCategory || !editCategoryName.trim()) return;
    try {
      await portfolioService.updateCategory(editingCategory.id, { name: editCategoryName.trim() });
      setEditingCategory(null);
      setStatusMessage({ type: 'success', text: 'Category renamed successfully.' });
      await fetchPortfolio(editingCategory.id);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to update category.';
      setStatusMessage({ type: 'error', text: msg });
    }
  };

  const handleDeleteCategory = async (categoryId: number) => {
    if (!window.confirm('Are you sure you want to delete this category and all its uploaded images?')) {
      return;
    }
    try {
      await portfolioService.deleteCategory(categoryId);
      setStatusMessage({ type: 'success', text: 'Category deleted successfully.' });
      await fetchPortfolio();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to delete category.';
      setStatusMessage({ type: 'error', text: msg });
    }
  };

  // Image Upload (POR-004, POR-008)
  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0 || !activeCategoryId) return;
    const files = Array.from(e.target.files);

    // Validation
    const validFiles = files.filter((f) => {
      const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
      return validTypes.includes(f.type) && f.size <= 10 * 1024 * 1024;
    });

    if (validFiles.length === 0) {
      setStatusMessage({
        type: 'error',
        text: 'Please select valid JPEG, PNG, or WEBP images under 10MB.',
      });
      return;
    }

    try {
      setUploading(true);
      setStatusMessage(null);
      await portfolioService.uploadImages(activeCategoryId, validFiles);
      setStatusMessage({
        type: 'success',
        text: `Successfully uploaded ${validFiles.length} image(s) to AWS S3 storage!`,
      });
      await fetchPortfolio(activeCategoryId);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to upload images.';
      setStatusMessage({ type: 'error', text: msg });
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleDeleteImage = async (imageId: number) => {
    if (!window.confirm('Are you sure you want to delete this image?')) return;
    try {
      await portfolioService.deleteImage(imageId);
      setStatusMessage({ type: 'success', text: 'Image removed from portfolio.' });
      if (activeCategoryId) {
        await fetchPortfolio(activeCategoryId);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to delete image.';
      setStatusMessage({ type: 'error', text: msg });
    }
  };

  // Image Reordering (POR-006)
  const handleMoveImage = async (currentIndex: number, direction: 'left' | 'right') => {
    if (!activeCategory || !activeCategory.images) return;
    const targetIndex = direction === 'left' ? currentIndex - 1 : currentIndex + 1;
    if (targetIndex < 0 || targetIndex >= activeCategory.images.length) return;

    const reordered = [...activeCategory.images];
    const temp = reordered[currentIndex];
    reordered[currentIndex] = reordered[targetIndex];
    reordered[targetIndex] = temp;

    const itemIds = reordered.map((img) => img.id);

    try {
      await portfolioService.reorderImages(activeCategory.id, itemIds);
      await fetchPortfolio(activeCategory.id);
    } catch (err: any) {
      console.error('Failed to reorder images:', err);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading Portfolio &amp; S3 Assets...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-8">
        {/* Navigation & Header */}
        <div className="flex items-center justify-between">
          <Link
            to="/freelancer/dashboard"
            className="flex items-center gap-2 text-xs text-slate-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Dashboard</span>
          </Link>

          <Link
            to="/freelancer/profile"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
          >
            <span>View Creator Profile</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {/* Portfolio Overview Banner */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 sm:p-8 backdrop-blur-md shadow-2xl relative overflow-hidden">
          <div className="absolute -right-20 -top-20 w-60 h-60 bg-teal-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30 text-xs font-semibold flex items-center gap-1">
                  <ShieldCheck className="w-3 h-3" /> AWS S3 Storage Active
                </span>
                <span className="text-xs text-slate-400">Phase 6: Portfolio Showcase</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
                Creator Portfolio Workspace
              </h1>
              <p className="text-sm text-slate-400 max-w-2xl">
                Organize your photographic projects into categories (POR-002), upload high-res images, and reorder shots to showcase your best angles to studios.
              </p>
            </div>

            {/* Quick Metrics */}
            <div className="flex items-center gap-3 shrink-0">
              <div className="bg-slate-950/80 border border-slate-800 px-4 py-3 rounded-2xl text-center min-w-[100px]">
                <span className="text-[10px] uppercase font-semibold text-slate-500 block">Categories</span>
                <span className="text-xl font-extrabold text-teal-400">{portfolio?.totalCategories || 0}</span>
              </div>
              <div className="bg-slate-950/80 border border-slate-800 px-4 py-3 rounded-2xl text-center min-w-[100px]">
                <span className="text-[10px] uppercase font-semibold text-slate-500 block">Images</span>
                <span className="text-xl font-extrabold text-emerald-400">{portfolio?.totalImages || 0}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Status Alerts */}
        {statusMessage && (
          <div
            className={`p-4 rounded-2xl border flex items-center gap-3 text-sm ${
              statusMessage.type === 'success'
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
            }`}
          >
            {statusMessage.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 shrink-0" />
            ) : (
              <AlertCircle className="w-5 h-5 shrink-0" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        {/* Categories Bar & Active Workspace */}
        <div className="space-y-6">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-4">
            {/* Category Tabs */}
            <div className="flex flex-wrap items-center gap-2">
              {portfolio?.categories && portfolio.categories.length > 0 ? (
                portfolio.categories.map((cat) => {
                  const isActive = cat.id === activeCategoryId;
                  return (
                    <div
                      key={cat.id}
                      className={`flex items-center gap-1.5 px-3.5 py-2 rounded-2xl text-xs font-semibold border transition-all ${
                        isActive
                          ? 'bg-teal-500/20 border-teal-500 text-teal-300 shadow-md shadow-teal-500/10'
                          : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white hover:border-slate-700'
                      }`}
                    >
                      <button
                        type="button"
                        onClick={() => setActiveCategoryId(cat.id)}
                        className="flex items-center gap-2"
                      >
                        <span>{cat.name}</span>
                        <span
                          className={`text-[10px] px-1.5 py-0.5 rounded-full ${
                            isActive ? 'bg-teal-500/30 text-teal-200' : 'bg-slate-800 text-slate-400'
                          }`}
                        >
                          {cat.imageCount}
                        </span>
                      </button>

                      {isActive && (
                        <div className="flex items-center gap-1 ml-1 pl-1.5 border-l border-teal-500/30">
                          <button
                            type="button"
                            onClick={() => {
                              setEditingCategory(cat);
                              setEditCategoryName(cat.name);
                            }}
                            className="p-1 text-teal-300 hover:text-white hover:bg-teal-500/30 rounded"
                            title="Rename Category"
                          >
                            <Edit2 className="w-3 h-3" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDeleteCategory(cat.id)}
                            className="p-1 text-rose-400 hover:text-rose-200 hover:bg-rose-500/20 rounded"
                            title="Delete Category"
                          >
                            <Trash2 className="w-3 h-3" />
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })
              ) : (
                <span className="text-xs text-slate-500 italic">No categories created yet.</span>
              )}
            </div>

            {/* Add Category Button */}
            <button
              type="button"
              onClick={() => setShowAddCategoryModal(true)}
              className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-slate-950 bg-teal-400 hover:bg-teal-300 transition-colors shadow-sm"
            >
              <Plus className="w-4 h-4" />
              <span>Add Category (POR-002)</span>
            </button>
          </div>

          {/* Active Category Content */}
          {activeCategory ? (
            <div className="space-y-6">
              {/* Upload Dropzone */}
              <div className="bg-slate-900/80 border-2 border-dashed border-slate-800 hover:border-teal-500/50 rounded-3xl p-6 sm:p-8 text-center transition-colors">
                <input
                  ref={fileInputRef}
                  type="file"
                  multiple
                  accept="image/jpeg,image/png,image/webp"
                  onChange={handleFileChange}
                  className="hidden"
                  id="portfolio-file-upload"
                  disabled={uploading}
                />
                <label
                  htmlFor="portfolio-file-upload"
                  className="cursor-pointer flex flex-col items-center gap-3"
                >
                  <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
                    {uploading ? (
                      <div className="w-6 h-6 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <Upload className="w-6 h-6" />
                    )}
                  </div>
                  <div>
                    <span className="text-sm font-bold text-white block">
                      {uploading ? 'Uploading to AWS S3...' : `Upload Images to "${activeCategory.name}"`}
                    </span>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Select multiple photos (JPEG, PNG, WEBP up to 10MB each)
                    </p>
                  </div>
                  <span className="inline-block px-4 py-2 rounded-xl bg-slate-800 text-teal-400 hover:bg-slate-700 text-xs font-semibold border border-slate-700">
                    Browse Local Files
                  </span>
                </label>
              </div>

              {/* Images Grid */}
              {activeCategory.images && activeCategory.images.length > 0 ? (
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-400">
                    <span>
                      Showing {activeCategory.images.length} shot{activeCategory.images.length > 1 ? 's' : ''} in{' '}
                      <strong className="text-white">{activeCategory.name}</strong>
                    </span>
                    <span className="text-[11px] text-slate-500 italic">
                      Use left/right arrows on cards to reorder images (POR-006).
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
                    {activeCategory.images.map((img, index) => (
                      <div
                        key={img.id}
                        className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden group shadow-lg flex flex-col justify-between"
                      >
                        {/* Image Preview Container */}
                        <div className="relative aspect-[4/3] bg-slate-950 overflow-hidden">
                          <img
                            src={img.imageUrl}
                            alt={img.originalFilename || `Portfolio shot ${index + 1}`}
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                          />

                          {/* Order Badge */}
                          <div className="absolute top-2 left-2 px-2 py-0.5 rounded-lg bg-slate-950/80 backdrop-blur-sm text-[10px] font-bold text-teal-400 border border-slate-800">
                            #{index + 1}
                          </div>

                          {/* Quick Preview Icon */}
                          <button
                            type="button"
                            onClick={() => setLightboxImage(img)}
                            className="absolute top-2 right-2 p-1.5 rounded-lg bg-slate-950/80 backdrop-blur-sm text-slate-300 hover:text-white border border-slate-800 opacity-0 group-hover:opacity-100 transition-opacity"
                            title="Expand Full Screen"
                          >
                            <Maximize2 className="w-3.5 h-3.5" />
                          </button>
                        </div>

                        {/* Card Info & Reordering Toolbar */}
                        <div className="p-3 bg-slate-900/90 border-t border-slate-800/80 flex items-center justify-between text-xs">
                          <div className="truncate max-w-[120px] text-[11px] text-slate-400">
                            {img.originalFilename || 'portfolio-image'}
                          </div>

                          <div className="flex items-center gap-1">
                            {/* Reorder Left */}
                            <button
                              type="button"
                              disabled={index === 0}
                              onClick={() => handleMoveImage(index, 'left')}
                              className="p-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-teal-400 disabled:opacity-30 disabled:hover:text-slate-400"
                              title="Move Left"
                            >
                              <MoveLeft className="w-3 h-3" />
                            </button>

                            {/* Reorder Right */}
                            <button
                              type="button"
                              disabled={index === activeCategory.images.length - 1}
                              onClick={() => handleMoveImage(index, 'right')}
                              className="p-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-teal-400 disabled:opacity-30 disabled:hover:text-slate-400"
                              title="Move Right"
                            >
                              <MoveRight className="w-3 h-3" />
                            </button>

                            {/* Delete */}
                            <button
                              type="button"
                              onClick={() => handleDeleteImage(img.id)}
                              className="p-1 rounded-lg bg-slate-800 hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 ml-1"
                              title="Delete Image"
                            >
                              <Trash2 className="w-3 h-3" />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="text-center py-12 bg-slate-900/40 rounded-3xl border border-slate-800/60 p-8 space-y-3">
                  <ImageIcon className="w-10 h-10 text-slate-600 mx-auto" />
                  <h3 className="text-sm font-bold text-white">No images in "{activeCategory.name}"</h3>
                  <p className="text-xs text-slate-400">
                    Use the upload area above to add your first photos to this category.
                  </p>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-16 bg-slate-900/50 rounded-3xl border border-slate-800 p-8 space-y-4">
              <FolderPlus className="w-12 h-12 text-teal-400 mx-auto" />
              <h3 className="text-base font-bold text-white">Get Started with Your Portfolio</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto">
                Create your first project category (e.g., Weddings, Fashion, Drone Cinematography) to start uploading imagery.
              </p>
              <button
                type="button"
                onClick={() => setShowAddCategoryModal(true)}
                className="px-5 py-2.5 rounded-xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold text-xs inline-flex items-center gap-2"
              >
                <Plus className="w-4 h-4" />
                <span>Create First Category</span>
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Modal: Add Category */}
      {showAddCategoryModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <FolderPlus className="w-4 h-4 text-teal-400" /> New Portfolio Category
              </h3>
              <button
                onClick={() => setShowAddCategoryModal(false)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <p className="text-xs text-slate-400">
              Categorize your shoots (e.g., Candid Weddings, Commercial Portraits, Drone Films).
            </p>
            <form onSubmit={handleCreateCategory} className="space-y-4">
              <input
                type="text"
                required
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
                placeholder="Category name"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddCategoryModal(false)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950 hover:bg-teal-400"
                >
                  Create Category
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Rename Category */}
      {editingCategory && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-md space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Edit2 className="w-4 h-4 text-teal-400" /> Rename Category
              </h3>
              <button
                onClick={() => setEditingCategory(null)}
                className="p-1 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <form onSubmit={handleUpdateCategory} className="space-y-4">
              <input
                type="text"
                required
                value={editCategoryName}
                onChange={(e) => setEditCategoryName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-500"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setEditingCategory(null)}
                  className="px-4 py-2 rounded-xl text-xs bg-slate-800 text-slate-300 hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-xl text-xs font-semibold bg-teal-500 text-slate-950 hover:bg-teal-400"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Lightbox View */}
      {lightboxImage && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md p-4"
          onClick={() => setLightboxImage(null)}
        >
          <div
            className="relative max-w-5xl max-h-[90vh] flex flex-col items-center"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              onClick={() => setLightboxImage(null)}
              className="absolute -top-10 right-0 p-1.5 rounded-full bg-slate-800 text-white hover:bg-slate-700"
              title="Close"
            >
              <X className="w-5 h-5" />
            </button>
            <img
              src={lightboxImage.imageUrl}
              alt={lightboxImage.originalFilename || 'Portfolio full preview'}
              className="max-w-full max-h-[80vh] rounded-2xl object-contain shadow-2xl border border-slate-800"
            />
            <div className="text-center mt-3 text-xs text-slate-400">
              {lightboxImage.originalFilename}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
