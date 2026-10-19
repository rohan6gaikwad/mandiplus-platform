import React from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { Target, Eye, Users2, Award } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext';

export default function About() {
  const { t } = useLanguage();

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Hero Banner */}
      <section className="bg-slate-50 border-b border-slate-100 py-20 text-center">
        <div className="max-w-4xl mx-auto px-4 space-y-4">
          <span className="text-xs font-extrabold uppercase text-agrogreen-700 tracking-wider bg-agrogreen-50 px-4 py-1.5 rounded-full border border-agrogreen-100/50">{t('ourStory')}</span>
          <h1 className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight">{t('empoweringAgri')}</h1>
          <p className="text-slate-500 text-lg max-w-xl mx-auto leading-relaxed">
            {t('storyDesc')}
          </p>
        </div>
      </section>

      {/* Vision & Mission grid */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-stretch">
          
          <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm flex flex-col space-y-4">
            <div className="bg-agrogreen-50 text-agrogreen-700 p-3 rounded-2xl self-start">
              <Target className="h-6 w-6" />
            </div>
            <h3 className="text-2xl font-bold text-slate-800 tracking-tight">{t('ourMission')}</h3>
            <p className="text-slate-500 text-sm leading-relaxed">
              {t('missionDesc')}
            </p>
          </div>

          <div className="bg-white p-8 rounded-3xl border border-slate-100 shadow-sm flex flex-col space-y-4">
            <div className="bg-agrogreen-50 text-agrogreen-700 p-3 rounded-2xl self-start">
              <Eye className="h-6 w-6" />
            </div>
            <h3 className="text-2xl font-bold text-slate-800 tracking-tight">{t('ourVision')}</h3>
            <p className="text-slate-500 text-sm leading-relaxed">
              {t('visionDesc')}
            </p>
          </div>

        </div>
      </section>

      {/* Team / Mandi+ Nashik Office Section */}
      <section className="bg-slate-50 py-24 border-y border-slate-100 text-center">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="max-w-2xl mx-auto mb-16 space-y-3">
            <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">{t('teamBehind')}</h2>
            <p className="text-slate-500 text-sm">{t('teamDesc')}</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            
            <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
              <div className="h-16 w-16 bg-agrogreen-100 text-agrogreen-800 rounded-full mx-auto mb-4 flex items-center justify-center font-black text-lg shadow-sm">M+</div>
              <h4 className="font-bold text-slate-800">{t('hqAddress')}</h4>
              <p className="text-xs text-agrogreen-700 mt-1 uppercase font-semibold tracking-wider">Nashik Hub & Operations</p>
            </div>

            <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
              <div className="h-16 w-16 bg-gradient-to-tr from-agrogreen-600 to-agrogreen-500 text-white rounded-full mx-auto mb-4 flex items-center justify-center font-black text-lg shadow-sm">RG</div>
              <h4 className="font-bold text-slate-800">Rohan Gaikwad</h4>
              <p className="text-xs text-slate-500 mt-1 uppercase font-semibold tracking-wider">Founder & Lead Architect</p>
            </div>

            <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
              <div className="h-16 w-16 bg-gradient-to-tr from-emerald-600 to-teal-500 text-white rounded-full mx-auto mb-4 flex items-center justify-center font-black text-lg shadow-sm">KC</div>
              <h4 className="font-bold text-slate-800">Krushnal Chaudhari</h4>
              <p className="text-xs text-slate-500 mt-1 uppercase font-semibold tracking-wider">Co-Founder & Operations Lead</p>
            </div>

            <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
              <div className="h-16 w-16 bg-gradient-to-tr from-teal-500 to-emerald-700 text-white rounded-full mx-auto mb-4 flex items-center justify-center font-black text-lg shadow-sm">SB</div>
              <h4 className="font-bold text-slate-800">Sanjana Bagul</h4>
              <p className="text-xs text-slate-500 mt-1 uppercase font-semibold tracking-wider">Co-Founder & Product Strategist</p>
            </div>

          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
