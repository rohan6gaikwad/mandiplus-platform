import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { useLanguage } from '../context/LanguageContext';
import { 
  Search, ExternalLink, ShieldCheck, Award, FileText, CheckCircle2,
  BookOpen, HelpCircle, Landmark
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';

export default function GovernmentSchemes() {
  const { t } = useLanguage();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');

  const schemes = [
    {
      id: 1,
      name: "PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)",
      category: "FINANCIAL",
      description: "A central sector scheme providing income support to all landholding farmers' families in the country to enable them to take care of agricultural and domestic expenses.",
      eligibility: "Small and marginal landholder farmer families with cultivable land holding up to 2 hectares (subject to exclusion criteria for high-income individuals).",
      benefits: "Direct financial assistance of ₹6,000 per year, paid in three equal installments of ₹2,000 directly into the bank accounts of farmers.",
      url: "https://pmkisan.gov.in/"
    },
    {
      id: 2,
      name: "PM Fasal Bima Yojana (PMFBY)",
      category: "INSURANCE",
      description: "A government-sponsored crop insurance scheme integrating multiple stakeholders to protect farmers from crop losses due to natural calamities, pests, and diseases.",
      eligibility: "All farmers including sharecroppers and tenant farmers growing notified crops in notified areas are eligible for coverage.",
      benefits: "Low premium rates (2% for Kharif, 1.5% for Rabi, 5% for commercial/horticultural crops) with complete insurance cover against crop damage from sowing to post-harvest.",
      url: "https://pmfby.gov.in/"
    },
    {
      id: 3,
      name: "Kisan Credit Card (KCC)",
      category: "FINANCIAL",
      description: "Provides farmers with timely access to short-term credit for cultivation, crop production, post-harvest expenses, and other credit needs without complex documentation.",
      eligibility: "All farmers, owner-cultivators, tenant farmers, sharecroppers, and self-help groups (SHGs) of farmers.",
      benefits: "Short-term loans up to ₹3 Lakhs at a low interest rate of 4% (after prompt repayment subsidy) with flexible repayment terms based on crop harvesting cycles.",
      url: "https://www.sbi.co.in/web/personal-banking/loans/agriculture-loans/kisan-credit-card"
    },
    {
      id: 4,
      name: "Soil Health Card (SHC)",
      category: "SOIL_HEALTH",
      description: "A government initiative to issue soil cards to farmers containing crop-wise recommendations of nutrients and fertilizers required for individual farms.",
      eligibility: "All landholding farmers across the country are eligible to get their soil tested and receive cards.",
      benefits: "Detailed soil report highlighting nutrient deficiencies, helping farmers optimize fertilizer usage, improve soil health, and boost crop productivity by 10-15%.",
      url: "https://soilhealth.dac.gov.in/"
    },
    {
      id: 5,
      name: "Agriculture Infrastructure Fund (AIF)",
      category: "INFRASTRUCTURE",
      description: "A financing facility for investment in viable projects for post-harvest management infrastructure and community farming assets.",
      eligibility: "Primary Agricultural Credit Societies (PACS), Agri-entrepreneurs, Startups, Farmers, and Farmer Producer Organizations (FPOs).",
      benefits: "3% interest subvention per annum on loans up to ₹2 Crores for a maximum period of 7 years, along with credit guarantee coverage under CGTMSE scheme.",
      url: "https://agriinfra.dac.gov.in/"
    },
    {
      id: 6,
      name: "e-NAM (National Agriculture Market)",
      category: "MARKETPLACE",
      description: "A pan-India electronic trading portal networking the existing APMC mandis to create a unified national market for agricultural commodities.",
      eligibility: "Farmers, traders, commission agents, and Farmer Producer Organizations (FPOs) registered with state APMCs.",
      benefits: "Direct digital trading of produce, access to nation-wide buyers, transparent price discovery, and online payment transfers directly to bank accounts.",
      url: "https://enam.gov.in/"
    },
    {
      id: 7,
      name: "PM Krishi Sinchai Yojana (PMKSY)",
      category: "SOIL_HEALTH",
      description: "Launched to achieve convergence of investments in irrigation at the field level, expand cultivable area, and improve on-farm water use efficiency.",
      eligibility: "Individual landholders, farming cooperatives, self-help groups, and tenant farmers with cultivable land and access to water resources.",
      benefits: "Subsidies up to 55% for small/marginal farmers and 45% for other farmers for installing micro-irrigation systems (drip/sprinkler irrigation).",
      url: "https://pmksy.gov.in/"
    }
  ];

  const categories = [
    { value: 'ALL', label: 'All Schemes' },
    { value: 'FINANCIAL', label: 'Financial Support' },
    { value: 'INSURANCE', label: 'Crop Insurance' },
    { value: 'SOIL_HEALTH', label: 'Soil & Irrigation' },
    { value: 'INFRASTRUCTURE', label: 'Infrastructure Funds' },
    { value: 'MARKETPLACE', label: 'Market Portals' }
  ];

  const filteredSchemes = schemes.filter(scheme => {
    const matchesSearch = scheme.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          scheme.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          scheme.benefits.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'ALL' || scheme.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const { user } = useAuth();

  if (user) {
    return (
      <div className="flex bg-[#fafbfb] min-h-screen font-sans">
        <Sidebar />
        <div className="flex-1 flex flex-col overflow-y-auto">
          <DashboardHeader title={t('govtSchemes')} />
          <main className="flex-1 p-8 bg-[#fafbfb] space-y-8">
            <div className="bg-slate-900 rounded-[32px] p-8 text-white relative overflow-hidden shadow-premium">
              <div className="absolute inset-0 bg-[radial-gradient(#16a34a_1.2px,transparent_0)] [background-size:24px_24px] opacity-10"></div>
              <div className="relative z-10 text-left space-y-2">
                <span className="inline-flex items-center space-x-2 bg-agrogreen-500/20 border border-agrogreen-500/30 rounded-full px-3 py-1 text-[10px] font-bold text-agrogreen-400 tracking-wide uppercase">
                  <Landmark className="h-3 w-3" />
                  <span>Welfare Desk</span>
                </span>
                <h2 className="text-2xl font-black">{t('govtSchemes')}</h2>
                <p className="text-slate-300 text-xs max-w-xl">
                  Discover active central government welfare schemes, direct benefit transfers (DBT), crop insurance subsidies, and easy credit options for Indian farmers.
                </p>
              </div>
            </div>

            {/* Filter Toolbar */}
            <div className="bg-white rounded-3xl border border-slate-100 p-6 shadow-sm flex flex-col lg:flex-row gap-4 justify-between items-stretch lg:items-center">
              {/* Search bar */}
              <div className="relative w-full lg:max-w-md">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-350">
                  <Search className="h-4.5 w-4.5" />
                </div>
                <input 
                  type="text" 
                  placeholder="Search scheme name or benefits..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-100 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-650 transition-all placeholder:text-slate-350 text-sm bg-slate-50/50"
                />
              </div>

              {/* Categories */}
              <div className="flex flex-wrap gap-2 w-full lg:w-auto justify-start lg:justify-end">
                {categories.map((cat) => (
                  <button
                    key={cat.value}
                    onClick={() => setSelectedCategory(cat.value)}
                    className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all transform hover:scale-[1.02] active:scale-[0.98] duration-200 ${
                      selectedCategory === cat.value
                        ? 'bg-agrogreen-950 text-white shadow-sm'
                        : 'bg-slate-50 hover:bg-slate-100 text-slate-650 border border-slate-100/50'
                    }`}
                  >
                    {cat.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Schemes Grid Cards */}
            {filteredSchemes.length === 0 ? (
              <div className="py-20 text-center text-slate-400 space-y-3 bg-white rounded-3xl border border-slate-100 shadow-sm">
                <BookOpen className="h-12 w-12 mx-auto text-slate-200 stroke-[1.5]" />
                <p className="text-sm font-medium">No government schemes matches your query.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {filteredSchemes.map((scheme) => (
                  <div 
                    key={scheme.id} 
                    className="bg-white rounded-[32px] border border-slate-100 shadow-sm hover:shadow-premium hover:-translate-y-2 hover:border-agrogreen-500/20 transition-all duration-500 flex flex-col justify-between overflow-hidden group text-left"
                  >
                    <div className={`h-2.5 w-full bg-gradient-to-r ${
                      scheme.category === 'FINANCIAL' ? 'from-amber-500 to-orange-400' :
                      scheme.category === 'INSURANCE' ? 'from-blue-500 to-indigo-500' :
                      scheme.category === 'SOIL_HEALTH' ? 'from-emerald-500 to-teal-400' :
                      scheme.category === 'INFRASTRUCTURE' ? 'from-indigo-500 to-purple-500' : 'from-purple-500 to-pink-500'
                    }`}></div>

                    <div className="p-8 flex-1 flex flex-col justify-between space-y-6">
                      <div className="space-y-4">
                        <div className="space-y-1">
                          <span className={`inline-block px-2.5 py-1 rounded-md text-[9px] font-extrabold uppercase tracking-wider ${
                            scheme.category === 'FINANCIAL' ? 'bg-amber-50 text-amber-600' :
                            scheme.category === 'INSURANCE' ? 'bg-blue-50 text-blue-600' :
                            scheme.category === 'SOIL_HEALTH' ? 'bg-emerald-50 text-emerald-600' :
                            scheme.category === 'INFRASTRUCTURE' ? 'bg-indigo-50 text-indigo-600' : 'bg-purple-50 text-purple-600'
                          }`}>
                            {scheme.category ? scheme.category.replace('_', ' ') : ''}
                          </span>
                          <h3 className="font-extrabold text-slate-800 text-lg tracking-tight group-hover:text-agrogreen-750 transition-colors leading-snug">
                            {scheme.name}
                          </h3>
                        </div>

                        <p className="text-slate-500 text-xs leading-relaxed line-clamp-3">
                          {scheme.description}
                        </p>

                        <div className="space-y-1.5 pt-2">
                          <span className="flex items-center space-x-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                            <CheckCircle2 className="h-3.5 w-3.5 text-slate-400" />
                            <span>Eligibility criteria</span>
                          </span>
                          <p className="text-slate-650 text-xs pl-5 leading-relaxed font-medium">
                            {scheme.eligibility}
                          </p>
                        </div>

                        <div className="space-y-1.5 pt-2">
                          <span className="flex items-center space-x-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                            <Award className="h-3.5 w-3.5 text-slate-400" />
                            <span>Welfare Benefits</span>
                          </span>
                          <p className="text-slate-650 text-xs pl-5 leading-relaxed font-medium">
                            {scheme.benefits}
                          </p>
                        </div>

                        <div className="space-y-1 pt-1">
                          <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Official Portal URL</span>
                          <a 
                            href={scheme.url} 
                            target="_blank" 
                            rel="noreferrer" 
                            className="text-xs text-agrogreen-700 hover:text-agrogreen-800 font-semibold truncate block hover:underline pl-0.5"
                          >
                            {scheme.url}
                          </a>
                        </div>
                      </div>

                      <a
                        href={scheme.url}
                        target="_blank"
                        rel="noreferrer"
                        className="w-full bg-slate-900 hover:bg-agrogreen-950 text-white font-bold py-3.5 rounded-2xl text-xs transition-all duration-300 flex items-center justify-center space-x-1.5 shadow-sm group/btn"
                      >
                        <span>Visit Website</span>
                        <ExternalLink className="h-3.5 w-3.5 transition-transform duration-300 group-hover/btn:translate-x-0.5 group-hover/btn:-translate-y-0.5" />
                      </a>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Hero Banner */}
      <section className="relative overflow-hidden bg-slate-900 text-white py-16 sm:py-24">
        {/* Decorative Grid */}
        <div className="absolute inset-0 bg-[radial-gradient(#16a34a_1.2px,transparent_0)] [background-size:24px_24px] opacity-10"></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-agrogreen-500/20 rounded-full blur-3xl -z-10"></div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
          <span className="inline-flex items-center space-x-2 bg-agrogreen-500/20 border border-agrogreen-500/30 rounded-full px-4 py-1.5 text-xs font-bold text-agrogreen-400 tracking-wide uppercase">
            <Landmark className="h-3.5 w-3.5" />
            <span>Government Welfare Desk</span>
          </span>
          <h1 className="text-4xl sm:text-5xl font-black tracking-tight leading-tight">
            Government Schemes & Subsidies
          </h1>
          <p className="text-slate-350 text-base sm:text-lg max-w-2xl mx-auto leading-relaxed">
            Discover active central government welfare schemes, direct benefit transfers (DBT), crop insurance subsidies, and easy credit options for Indian farmers.
          </p>
        </div>
      </section>

      {/* Main Filter & Grid Container */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 space-y-10">
        
        {/* Search and Category Filter Toolbar */}
        <div className="bg-white rounded-3xl border border-slate-100 p-6 shadow-sm flex flex-col lg:flex-row gap-4 justify-between items-stretch lg:items-center">
          {/* Search bar */}
          <div className="relative w-full lg:max-w-md">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-350">
              <Search className="h-4.5 w-4.5" />
            </div>
            <input 
              type="text" 
              placeholder="Search scheme name or benefits..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-100 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-650 transition-all placeholder:text-slate-355 text-sm bg-slate-50/50"
            />
          </div>

          {/* Categories Tab list */}
          <div className="flex flex-wrap gap-2 w-full lg:w-auto justify-start lg:justify-end">
            {categories.map((cat) => (
              <button
                key={cat.value}
                onClick={() => setSelectedCategory(cat.value)}
                className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all transform hover:scale-[1.02] active:scale-[0.98] duration-200 ${
                  selectedCategory === cat.value
                    ? 'bg-agrogreen-950 text-white shadow-sm'
                    : 'bg-slate-50 hover:bg-slate-100 text-slate-650 border border-slate-100/50'
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Schemes Grid Cards */}
        {filteredSchemes.length === 0 ? (
          <div className="py-20 text-center text-slate-400 space-y-3 bg-white rounded-3xl border border-slate-100 shadow-sm">
            <BookOpen className="h-12 w-12 mx-auto text-slate-200 stroke-[1.5]" />
            <p className="text-sm font-medium">No government schemes matches your query.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {filteredSchemes.map((scheme) => (
              <div 
                key={scheme.id} 
                className="bg-white rounded-[32px] border border-slate-100 shadow-sm hover:shadow-premium hover:-translate-y-2 hover:border-agrogreen-500/20 transition-all duration-500 flex flex-col justify-between overflow-hidden group text-left"
              >
                {/* Header card color ribbon based on Category */}
                <div className={`h-2.5 w-full bg-gradient-to-r ${
                  scheme.category === 'FINANCIAL' ? 'from-amber-500 to-orange-400' :
                  scheme.category === 'INSURANCE' ? 'from-blue-500 to-indigo-500' :
                  scheme.category === 'SOIL_HEALTH' ? 'from-emerald-500 to-teal-400' :
                  scheme.category === 'INFRASTRUCTURE' ? 'from-indigo-500 to-purple-500' : 'from-purple-500 to-pink-500'
                }`}></div>

                {/* Card Content body */}
                <div className="p-8 flex-1 flex flex-col justify-between space-y-6">
                  <div className="space-y-4">
                    <div className="space-y-1">
                      <span className={`inline-block px-2.5 py-1 rounded-md text-[9px] font-extrabold uppercase tracking-wider ${
                        scheme.category === 'FINANCIAL' ? 'bg-amber-50 text-amber-600' :
                        scheme.category === 'INSURANCE' ? 'bg-blue-50 text-blue-600' :
                        scheme.category === 'SOIL_HEALTH' ? 'bg-emerald-50 text-emerald-600' :
                        scheme.category === 'INFRASTRUCTURE' ? 'bg-indigo-50 text-indigo-600' : 'bg-purple-50 text-purple-600'
                      }`}>
                        {scheme.category ? scheme.category.replace('_', ' ') : ''}
                      </span>
                      <h3 className="font-extrabold text-slate-800 text-lg tracking-tight group-hover:text-agrogreen-750 transition-colors leading-snug">
                        {scheme.name}
                      </h3>
                    </div>

                    <p className="text-slate-500 text-xs leading-relaxed line-clamp-3">
                      {scheme.description}
                    </p>

                    {/* Eligibility details */}
                    <div className="space-y-1.5 pt-2">
                      <span className="flex items-center space-x-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                        <CheckCircle2 className="h-3.5 w-3.5 text-slate-400" />
                        <span>Eligibility criteria</span>
                      </span>
                      <p className="text-slate-650 text-xs pl-5 leading-relaxed font-medium">
                        {scheme.eligibility}
                      </p>
                    </div>

                    {/* Benefits details */}
                    <div className="space-y-1.5 pt-2">
                      <span className="flex items-center space-x-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                        <Award className="h-3.5 w-3.5 text-slate-400" />
                        <span>Welfare Benefits</span>
                      </span>
                      <p className="text-slate-650 text-xs pl-5 leading-relaxed font-medium">
                        {scheme.benefits}
                      </p>
                    </div>

                    {/* Official apply link */}
                    <div className="space-y-1 pt-1">
                      <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Official Portal URL</span>
                      <a 
                        href={scheme.url} 
                        target="_blank" 
                        rel="noreferrer" 
                        className="text-xs text-agrogreen-700 hover:text-agrogreen-800 font-semibold truncate block hover:underline pl-0.5"
                      >
                        {scheme.url}
                      </a>
                    </div>
                  </div>

                  {/* Visit website button */}
                  <a
                    href={scheme.url}
                    target="_blank"
                    rel="noreferrer"
                    className="w-full bg-slate-900 hover:bg-agrogreen-950 text-white font-bold py-3.5 rounded-2xl text-xs transition-all duration-300 flex items-center justify-center space-x-1.5 shadow-sm group/btn"
                  >
                    <span>Visit Website</span>
                    <ExternalLink className="h-3.5 w-3.5 transition-transform duration-300 group-hover/btn:translate-x-0.5 group-hover/btn:-translate-y-0.5" />
                  </a>
                </div>
              </div>
            ))}
          </div>
        )}

      </section>

      <Footer />
    </div>
  );
}
