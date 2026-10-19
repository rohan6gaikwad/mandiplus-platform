import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useLanguage } from '../context/LanguageContext';
import { AlertCircle, CheckCircle2, Clock, Plus, ShieldAlert, FileText, Send } from 'lucide-react';

export default function ComplaintsDesk() {
  const { t } = useLanguage();
  const [complaints, setComplaints] = useState([]);
  const [compTitle, setCompTitle] = useState('');
  const [compDesc, setCompDesc] = useState('');
  const [compMessage, setCompMessage] = useState('');
  const [compError, setCompError] = useState('');
  const [compLoading, setCompLoading] = useState(false);

  const fetchComplaints = async () => {
    try {
      const response = await api.get('/complaints');
      setComplaints(response.data || []);
    } catch (err) {
      console.error("Failed to load complaints.", err);
    }
  };

  useEffect(() => {
    fetchComplaints();
  }, []);

  const handleSubmitComplaint = async (e) => {
    e.preventDefault();
    setCompLoading(true);
    setCompMessage('');
    setCompError('');

    try {
      await api.post('/complaints', {
        title: compTitle,
        description: compDesc
      });
      setCompMessage(t('complaintFiledSuccess') || "Your dispute case has been successfully filed with the Admin Board.");
      setCompTitle('');
      setCompDesc('');
      fetchComplaints();
    } catch (err) {
      setCompError(err.response?.data || "Failed to submit dispute complaint.");
    } finally {
      setCompLoading(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 text-left items-stretch">
      {/* Left Column: Submit Dispute Form */}
      <div className="lg:col-span-5 bg-white rounded-[32px] border border-slate-100 p-5 sm:p-8 shadow-premium flex flex-col justify-between hover:border-agrogreen-500/20 transition-all duration-300">
        <div className="space-y-6">
          <div className="space-y-2">
            <span className="inline-flex items-center space-x-2 bg-rose-50 border border-rose-100 rounded-full px-3 py-1 text-[10px] font-bold text-rose-600 tracking-wide uppercase">
              <ShieldAlert className="h-3.5 w-3.5" />
              <span>Grievance Center</span>
            </span>
            <h3 className="text-xl font-black text-slate-800 tracking-tight">
              File a Dispute Ticket
            </h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Submit your concern directly to the Mandi+ Admin Board. We review and resolve escrow payouts, quality checks, cold-storage booking overruns, and logistics transit disputes.
            </p>
          </div>

          {compMessage && (
            <div className="bg-emerald-50 border border-emerald-100 text-emerald-700 p-4 rounded-2xl text-xs font-bold animate-fade-in">
              {compMessage}
            </div>
          )}

          {compError && (
            <div className="bg-rose-50 border border-rose-100 text-rose-700 p-4 rounded-2xl text-xs font-bold flex items-center space-x-2 animate-fade-in">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{compError}</span>
            </div>
          )}

          <form onSubmit={handleSubmitComplaint} className="space-y-4">
            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">
                Issue Subject / Title
              </label>
              <input
                type="text"
                required
                value={compTitle}
                onChange={(e) => setCompTitle(e.target.value)}
                placeholder="e.g. Escrow payout not cleared / Quantity mismatch"
                className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-650 transition-all bg-slate-50/50"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">
                Detailed Description
              </label>
              <textarea
                rows={5}
                required
                value={compDesc}
                onChange={(e) => setCompDesc(e.target.value)}
                placeholder="Please explain the details of the issue including Order IDs, logistics partners, and specific milestones. Provide enough context for the Admin board to investigate..."
                className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 resize-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-650 transition-all bg-slate-50/50"
              />
            </div>

            <button
              type="submit"
              disabled={compLoading}
              className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 rounded-xl text-xs transition-all shadow-sm flex items-center justify-center space-x-2"
            >
              {compLoading ? (
                <span>Submitting Case...</span>
              ) : (
                <>
                  <Send className="h-3.5 w-3.5" />
                  <span>File Secure Dispute</span>
                </>
              )}
            </button>
          </form>
        </div>
      </div>

      {/* Right Column: Dispute History */}
      <div className="lg:col-span-7 bg-white rounded-[32px] border border-slate-100 p-5 sm:p-8 shadow-premium flex flex-col justify-between hover:border-agrogreen-500/20 transition-all duration-300">
        <div className="w-full">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h3 className="text-xl font-bold text-slate-800 tracking-tight">Your Dispute Registry</h3>
              <p className="text-xs text-slate-400 mt-0.5">Track reviews and resolution updates in real-time.</p>
            </div>
            <span className="text-xs bg-slate-50 text-slate-650 border border-slate-100 font-bold px-3 py-1.5 rounded-full">
              {complaints.length} Tickets
            </span>
          </div>

          {complaints.length === 0 ? (
            <div className="py-24 text-center text-slate-400 space-y-4">
              <div className="h-12 w-12 rounded-full bg-slate-50 border border-slate-100 flex items-center justify-center mx-auto text-slate-300">
                <FileText className="h-6 w-6 stroke-[1.5]" />
              </div>
              <div className="max-w-xs mx-auto space-y-1">
                <p className="text-sm font-semibold text-slate-700">No disputes filed</p>
                <p className="text-xs text-slate-400 leading-normal">You currently have no active or historical dispute tickets in our system.</p>
              </div>
            </div>
          ) : (
            <div className="space-y-4 max-h-[60vh] overflow-y-auto pr-1 scrollbar-thin">
              {complaints.map((comp) => (
                <div
                  key={comp.id}
                  className="p-5 border border-slate-100 rounded-3xl flex flex-col sm:flex-row justify-between items-start gap-4 hover:bg-slate-50/30 transition-all group"
                >
                  <div className="space-y-2 flex-1">
                    <div className="flex items-center space-x-2">
                      <span className="text-[10px] bg-slate-100 font-bold px-2 py-0.5 rounded text-slate-500">
                        #TKT-{comp.id}
                      </span>
                      <span className="text-[10px] text-slate-400">
                        {new Date(comp.createdAt || Date.now()).toLocaleDateString('en-IN', {
                          day: 'numeric',
                          month: 'short',
                          year: 'numeric',
                        })}
                      </span>
                    </div>
                    <h4 className="font-extrabold text-slate-800 text-sm leading-snug group-hover:text-agrogreen-750 transition-colors">
                      {comp.title}
                    </h4>
                    <p className="text-slate-500 text-xs leading-relaxed font-medium">
                      {comp.description}
                    </p>
                  </div>

                  <div className="shrink-0">
                    <span
                      className={`inline-flex items-center space-x-1.5 px-3 py-1 rounded-xl text-[10px] font-black uppercase ${
                        comp.status === 'RESOLVED'
                          ? 'bg-emerald-50 text-emerald-600 border border-emerald-100'
                          : 'bg-amber-50 text-amber-600 border border-amber-100'
                      }`}
                    >
                      {comp.status === 'RESOLVED' ? (
                        <CheckCircle2 className="h-3 w-3" />
                      ) : (
                        <Clock className="h-3 w-3 animate-spin-slow" />
                      )}
                      <span>{comp.status}</span>
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
