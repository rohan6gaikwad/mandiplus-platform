import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import api from '../services/api';
import { UserCheck, ShieldCheck, ArrowRight, AlertCircle, Sparkles } from 'lucide-react';

export default function SetupProfile() {
  const { user, updateUserContext, loading } = useAuth();
  const [fullName, setFullName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const getDashboardPath = (role) => {
    switch (role) {
      case 'FARMER': return '/farmer';
      case 'BUYER': return '/buyer';
      case 'COLD_STORAGE_MANAGER': return '/storage';
      case 'DELIVERY_PARTNER': return '/delivery';
      case 'ADMIN': return '/admin';
      default: return '/';
    }
  };

  useEffect(() => {
    if (!loading) {
      if (!user) {
        navigate('/login', { replace: true });
      } else if (user.isProfileComplete === true) {
        // Already completed - prevent unnecessary prompt and redirect to dashboard
        navigate(getDashboardPath(user.role), { replace: true });
      }
    }
  }, [user, loading, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const trimmed = fullName.trim();
    if (!trimmed) {
      setError('Full name is mandatory and cannot be empty or whitespace only.');
      return;
    }

    if (trimmed.length < 2 || trimmed.length > 100) {
      setError('Full name must be between 2 and 100 characters.');
      return;
    }

    // Reject non-alphabetic/invalid symbols while supporting unicode letters
    if (!/^[\p{L}\s.\-']+$/u.test(trimmed)) {
      setError('Full name can only contain letters, spaces, dots, hyphens, and apostrophes.');
      return;
    }

    setSubmitting(true);
    try {
      const response = await api.post('/auth/profile/setup-name', {
        fullName: trimmed
      });

      if (response.data && response.data.token) {
        // Update local session state
        updateUserContext({
          ...response.data,
          name: response.data.name,
          fullName: response.data.name,
          isProfileComplete: true,
          profileSetupRequired: false
        });

        // Redirect to role dashboard
        const destination = getDashboardPath(response.data.role || user.role);
        navigate(destination, { replace: true });
      } else {
        throw new Error('Unexpected response received from server.');
      }
    } catch (err) {
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response.data : null) || err.message || 'Failed to update profile name.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 font-sans">
        <div className="h-8 w-8 rounded-full border-4 border-agrogreen-200 border-t-agrogreen-600 animate-spin"></div>
      </div>
    );
  }

  const roleNameMap = {
    FARMER: 'Farmer Producer',
    BUYER: 'Commercial Buyer',
    COLD_STORAGE_MANAGER: 'Cold Storage Operator',
    DELIVERY_PARTNER: 'Delivery Fleet Partner',
    ADMIN: 'System Administrator'
  };

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      <main className="flex-1 flex items-center justify-center py-16 px-4">
        <div className="w-full max-w-lg bg-white rounded-3xl border border-slate-100 p-8 sm:p-10 shadow-premium">
          <div className="text-center mb-8">
            <div className="h-16 w-16 bg-emerald-50 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-emerald-100 shadow-sm">
              <UserCheck className="h-8 w-8 text-emerald-600" />
            </div>
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 bg-amber-50 border border-amber-200 rounded-full text-[11px] font-bold text-amber-900 uppercase tracking-wider mb-3">
              <Sparkles className="h-3.5 w-3.5 text-amber-600" />
              <span>Mandatory Profile Step</span>
            </div>
            <h2 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">Complete Your Profile</h2>
            <p className="text-slate-500 text-sm mt-2">
              Authentication verified. Please provide your legal full name to complete your Mandi+ account setup.
            </p>
          </div>

          <div className="bg-slate-50 border border-slate-200/80 rounded-2xl p-4 mb-6 text-xs text-slate-700 flex flex-col space-y-1.5">
            <div className="flex justify-between items-center">
              <span className="text-slate-500 font-semibold">Verified Mobile / ID:</span>
              <span className="font-mono font-bold text-slate-800">{user.phone ? `+91 ${user.phone}` : user.email}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500 font-semibold">Assigned Role:</span>
              <span className="font-bold text-agrogreen-700 bg-agrogreen-50 px-2 py-0.5 rounded-md border border-agrogreen-200">
                {roleNameMap[user.role] || user.role}
              </span>
            </div>
          </div>

          {error && (
            <div className="bg-rose-50 border border-rose-200 text-rose-700 p-4 rounded-xl text-xs font-semibold mb-6 flex items-start space-x-2">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <div className="flex justify-between items-center mb-2">
                <label htmlFor="fullNameInput" className="block text-xs font-bold text-slate-700">
                  Full Name <span className="text-rose-500">*</span>
                </label>
                <span className="text-[11px] text-slate-400 font-mono">
                  {fullName.trim().length} / 100
                </span>
              </div>
              <input
                id="fullNameInput"
                type="text"
                required
                maxLength={100}
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Rohan Gaikwad"
                className="w-full px-4 py-3.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all text-sm font-medium text-slate-800"
                autoFocus
              />
              <p className="text-[11px] text-slate-400 mt-1.5">
                Enter your real display name. This will appear on all APMC mandi transactions, receipts, and invoices.
              </p>
            </div>

            <button
              type="submit"
              disabled={submitting || !fullName.trim()}
              className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all disabled:opacity-50 shadow-md"
            >
              <span>{submitting ? 'Saving Profile...' : 'Save & Continue to Dashboard'}</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </form>

          <div className="mt-8 pt-6 border-t border-slate-100 flex items-center justify-center space-x-2 text-xs text-slate-400">
            <ShieldCheck className="h-4 w-4 text-emerald-600" />
            <span>Securely verified & encrypted on Mandi+ Platform</span>
          </div>
        </div>
      </main>
    </div>
  );
}
