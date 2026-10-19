import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { Mail, Phone, MapPin, Send, MessageSquare } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext';

export default function Contact() {
  const { t } = useLanguage();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });
  const [status, setStatus] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    setStatus('sending');
    setTimeout(() => {
      setStatus('success');
      setFormData({ name: '', email: '', message: '' });
    }, 1200);
  };

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Header Banner */}
      <section className="bg-slate-50 border-b border-slate-100 py-20 text-center">
        <div className="max-w-4xl mx-auto px-4 space-y-4">
          <span className="text-xs font-extrabold uppercase text-agrogreen-700 tracking-wider bg-agrogreen-50 px-4 py-1.5 rounded-full border border-agrogreen-100/50">{t('getInTouch')}</span>
          <h1 className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight">{t('contactTeam')}</h1>
          <p className="text-slate-500 text-lg max-w-xl mx-auto leading-relaxed">
            {t('contactDesc')}
          </p>
        </div>
      </section>

      {/* Main Grid Content */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-stretch">
          
          {/* Details Column */}
          <div className="lg:col-span-5 space-y-8 flex flex-col justify-between">
            <div className="space-y-4 text-left">
              <h3 className="text-2xl font-bold text-slate-800 tracking-tight">{t('contactInfo')}</h3>
              <p className="text-slate-500 text-sm leading-relaxed">
                {t('contactInfoDesc')}
              </p>
            </div>

            <div className="space-y-6">
              
              <div className="flex items-center space-x-4 bg-white p-5 rounded-2xl border border-slate-100 shadow-sm text-left">
                <div className="bg-agrogreen-50 text-agrogreen-700 p-3.5 rounded-xl">
                  <Phone className="h-5 w-5" />
                </div>
                <div className="space-y-1">
                  <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider">{t('callUs')}</span>
                  <div className="text-sm font-bold text-slate-800">
                    <div>Rohan Gaikwad: <a href="tel:9012345678" className="text-agrogreen-700 hover:underline font-mono">+91 90123 45678</a></div>
                    <div>Krushnal Chaudhari: <span className="text-agrogreen-700 font-mono">+91 98765 43211</span></div>
                    <div>Sanjana Bagul: <span className="text-agrogreen-700 font-mono">+91 98765 43212</span></div>
                  </div>
                </div>
              </div>

              <div className="flex items-center space-x-4 bg-white p-5 rounded-2xl border border-slate-100 shadow-sm text-left">
                <div className="bg-agrogreen-50 text-agrogreen-700 p-3.5 rounded-xl">
                  <Mail className="h-5 w-5" />
                </div>
                <div>
                  <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">{t('emailSupport')}</span>
                  <a href="mailto:admin@gmail.com" className="block text-sm font-bold text-slate-800 hover:text-agrogreen-700 transition-colors">admin@gmail.com</a>
                </div>
              </div>

              <div className="flex items-center space-x-4 bg-white p-5 rounded-2xl border border-slate-100 shadow-sm text-left">
                <div className="bg-agrogreen-50 text-agrogreen-700 p-3.5 rounded-xl">
                  <MapPin className="h-5 w-5" />
                </div>
                <div>
                  <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">{t('officeAddress')}</span>
                  <span className="block text-sm font-bold text-slate-800">Mandi+ Office, Nashik, Maharashtra</span>
                </div>
              </div>

            </div>

            {/* Micro branding block */}
            <div className="bg-slate-50 border border-slate-200/50 p-4 rounded-2xl text-xs text-slate-400 text-left">
              <span>Looking to partner? Contact <strong className="text-slate-600 font-bold">Mandi+ Team</strong> direct lines.</span>
            </div>
          </div>

          {/* Form Column */}
          <div className="lg:col-span-7 bg-white p-8 rounded-[32px] border border-slate-100 shadow-sm text-left">
            <h3 className="text-2xl font-bold text-slate-800 tracking-tight mb-6">{t('sendInquiry')}</h3>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              
              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">{t('name')}</label>
                <input 
                  type="text" 
                  required
                  placeholder="Enter name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all placeholder:text-slate-300 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">{t('email')}</label>
                <input 
                  type="email" 
                  required
                  placeholder="Enter email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all placeholder:text-slate-300 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">{t('message')}</label>
                <textarea 
                  required
                  rows={4}
                  placeholder="Type inquiry description"
                  value={formData.message}
                  onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all placeholder:text-slate-300 text-sm resize-none"
                />
              </div>

              <button
                type="submit"
                disabled={status === 'sending'}
                className="w-full bg-agrogreen-950 hover:bg-agrogreen-900 text-white py-3.5 rounded-xl font-bold flex items-center justify-center space-x-2.5 shadow-md shadow-slate-100 transition-all disabled:opacity-50"
              >
                <span>{status === 'sending' ? t('sending') : t('submit')}</span>
                <Send className="h-4 w-4" />
              </button>

              {status === 'success' && (
                <div className="bg-emerald-50 border border-emerald-100 text-emerald-700 p-4 rounded-xl text-sm font-semibold flex items-center space-x-2">
                  <MessageSquare className="h-5 w-5 shrink-0" />
                  <span>{t('messageSentSuccess')}</span>
                </div>
              )}

            </form>
          </div>

        </div>
      </section>

      <Footer />
    </div>
  );
}
