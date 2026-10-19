import React from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { Truck, Warehouse, CreditCard, ShieldCheck, CheckCircle2 } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext';

export default function Services() {
  const { t } = useLanguage();

  const list = [
    {
      title: t('smartLogistics'),
      description: t('smartLogisticsDesc'),
      icon: Truck,
      color: 'bg-indigo-50 text-indigo-600'
    },
    {
      title: t('smartLogistics'),
      description: t('smartLogisticsDesc'),
      icon: Warehouse,
      color: 'bg-emerald-50 text-emerald-600'
    },
    {
      title: t('escrowHold'),
      description: t('escrowHoldDesc'),
      icon: CreditCard,
      color: 'bg-amber-50 text-amber-600'
    },
    {
      title: t('directTrade'),
      description: t('directTradeDesc'),
      icon: ShieldCheck,
      color: 'bg-cyan-50 text-cyan-600'
    }
  ];

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Header Banner */}
      <section className="bg-slate-50 border-b border-slate-100 py-20 text-center">
        <div className="max-w-4xl mx-auto px-4 space-y-4">
          <span className="text-xs font-extrabold uppercase text-agrogreen-700 tracking-wider bg-agrogreen-50 px-4 py-1.5 rounded-full border border-agrogreen-100/50">{t('ourServices')}</span>
          <h1 className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight">{t('platformServices')}</h1>
          <p className="text-slate-500 text-lg max-w-xl mx-auto leading-relaxed">
            {t('servicesDesc')}
          </p>
        </div>
      </section>

      {/* Services Grid list */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
          {list.map((service, index) => {
            const Icon = service.icon;
            return (
              <div 
                key={index} 
                className="bg-white rounded-3xl p-8 border border-slate-100 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow group"
              >
                <div className="space-y-5">
                  <div className={`p-4 rounded-2xl inline-block ${service.color}`}>
                    <Icon className="h-6 w-6" />
                  </div>
                  <h3 className="text-2xl font-bold text-slate-800 tracking-tight">{service.title}</h3>
                  <p className="text-slate-500 text-sm leading-relaxed">{service.description}</p>
                </div>
                <div className="pt-6 mt-6 border-t border-slate-50 flex items-center space-x-2 text-agrogreen-700 text-xs font-bold uppercase tracking-wider">
                  <CheckCircle2 className="h-4 w-4" />
                  <span>Platform Standard</span>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      <Footer />
    </div>
  );
}
