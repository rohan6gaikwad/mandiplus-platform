import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  ArrowRight, ShieldCheck, CreditCard, 
  Thermometer, CheckCircle2, ChevronRight,
  TrendingUp, TrendingDown, Sun, Truck, Warehouse, HelpCircle, Landmark
} from 'lucide-react';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import MandiWidget from '../components/MandiWidget';
import WeatherWidget from '../components/WeatherWidget';
import { useLanguage } from '../context/LanguageContext';

export default function Home() {
  const navigate = useNavigate();
  const { t } = useLanguage();

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-32 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
          
          {/* Left Text */}
          <div className="lg:col-span-7 space-y-8 text-left z-10">
            <span className="inline-flex items-center space-x-2 bg-agrogreen-50 border border-agrogreen-100 rounded-full px-4 py-1.5 text-xs font-bold text-agrogreen-700 tracking-wide uppercase">
              <CheckCircle2 className="h-3.5 w-3.5" />
              <span>Direct Farm-to-Buyer Access</span>
            </span>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 leading-[1.1]">
              {t('heroTitle')}
            </h1>

            <p className="text-slate-500 text-lg max-w-xl leading-relaxed">
              {t('heroSubtitle')}
            </p>

            <div className="flex flex-col sm:flex-row gap-4 pt-2">
              <Link
                to="/register"
                className="bg-agrogreen-950 hover:bg-agrogreen-900 text-white text-center px-8 py-4 rounded-xl font-bold transition-all shadow-lg shadow-slate-200 hover:-translate-y-0.5"
              >
                {t('getStarted')}
              </Link>
              <Link
                to="/services"
                className="bg-white hover:bg-slate-50 text-slate-700 text-center px-8 py-4 rounded-xl font-bold border border-slate-200 transition-all hover:-translate-y-0.5"
              >
                {t('learnMore')}
              </Link>
            </div>

            {/* Quick ticker */}
            <div className="pt-6 border-t border-slate-100 flex flex-wrap gap-x-8 gap-y-3.5">
              <div className="flex items-center space-x-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100/50">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Wheat</span>
                <span className="text-xs font-extrabold text-slate-800">₹2,450 / qtl</span>
                <span className="text-[10px] font-bold text-emerald-600 flex items-center">
                  <TrendingUp className="h-3 w-3 mr-0.5" />
                  <span>8%</span>
                </span>
              </div>

              <div className="flex items-center space-x-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100/50">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">Rice (Basmati)</span>
                <span className="text-xs font-extrabold text-slate-800">₹4,200 / qtl</span>
                <span className="text-[10px] font-bold text-rose-600 flex items-center">
                  <TrendingDown className="h-3 w-3 mr-0.5" />
                  <span>0.5%</span>
                </span>
              </div>
            </div>
          </div>

          {/* Right Image/Mockup widgets */}
          <div className="lg:col-span-5 relative flex justify-center lg:justify-end">
            
            {/* Visual background blob */}
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-agrogreen-100/60 rounded-full blur-3xl -z-10"></div>

            {/* Image Wrapper */}
            <div className="relative w-full max-w-lg rounded-[36px] bg-slate-100 p-2.5 shadow-xl border border-slate-200/50 transform hover:scale-[1.01] transition-transform duration-500">
              
              {/* Real Picture displaying farmer */}
              <div className="w-full aspect-[4/3] rounded-[28px] overflow-hidden bg-slate-200 relative flex items-center justify-center">
                <img src="/images/hero.png" alt="Indian Farmer" className="w-full h-full object-cover" />
              </div>

              {/* Float Widget 1: Capacity */}
              <div className="absolute -left-6 bottom-16 bg-white/95 border border-slate-100/80 rounded-2xl p-4 shadow-xl shadow-slate-200/50 backdrop-blur-md max-w-[210px] animate-pulse-slow">
                <div className="flex justify-between items-center text-[10px] font-bold text-slate-400 uppercase tracking-wide mb-1.5">
                  <span>Storage Capacity</span>
                  <span className="text-agrogreen-600">82%</span>
                </div>
                <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden mb-2">
                  <div className="bg-agrogreen-600 h-full rounded-full" style={{ width: '82%' }}></div>
                </div>
                <div className="flex items-center space-x-1 text-[10px] font-bold text-slate-600 uppercase tracking-wide">
                  <Warehouse className="h-3.5 w-3.5 text-slate-400" />
                  <span>Cold Storage Unit 3</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Live Insights Section */}
      <section className="bg-slate-50 py-20 border-y border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:justify-between md:items-end mb-12">
            <div className="text-left">
              <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">Live Insights</h2>
              <p className="text-slate-500 text-sm mt-1 max-w-md">Stay ahead with real-time market data and hyper-local weather alerts tailored for your farm's location.</p>
            </div>
            <Link 
              to="/services" 
              className="mt-4 md:mt-0 inline-flex items-center space-x-1 text-agrogreen-700 font-bold text-sm hover:text-agrogreen-800"
            >
              <span>View Full Market</span>
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
            {/* Mandi Widget */}
            <div className="lg:col-span-8">
              <MandiWidget limit={2} />
            </div>

            {/* Weather Widget */}
            <div className="lg:col-span-4">
              <WeatherWidget locationName="Pune, Maharashtra" />
            </div>
          </div>
        </div>
      </section>

      {/* Why Mandi+ Section */}
      <section className="py-24 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="max-w-3xl mx-auto mb-16">
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight mb-4">Why Mandi+?</h2>
          <p className="text-slate-500 text-lg">Building the technology layer for India's food supply chain, from the farm gate to the dinner plate.</p>
        </div>

        {/* Grid cards */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-stretch text-left">
          
          {/* Card 1: Logistics */}
          <div className="md:col-span-8 bg-slate-950 rounded-[32px] overflow-hidden relative flex flex-col justify-end p-8 min-h-[300px] group border border-slate-900">
            <img src="/images/logistics.png" alt="Logistics" className="absolute inset-0 w-full h-full object-cover z-0 opacity-60 mix-blend-overlay group-hover:scale-105 transition-transform duration-700" />
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/80 to-transparent z-10"></div>
            
            <div className="z-20 space-y-3">
              <h3 className="text-2xl font-bold text-white tracking-tight">Integrated Logistics</h3>
              <p className="text-slate-300 text-sm max-w-md leading-relaxed">Real-time GPS tracking and temperature-monitored transport to ensure your produce stays fresh until delivery.</p>
            </div>
          </div>

          {/* Card 2: Quality */}
          <div className="md:col-span-4 bg-agrogreen-800 rounded-[32px] p-8 flex flex-col justify-between min-h-[300px] border border-agrogreen-900">
            <span className="bg-white/10 text-white p-3 rounded-2xl self-start backdrop-blur-md">
              <ShieldCheck className="h-6 w-6" />
            </span>
            <div className="space-y-3">
              <h3 className="text-2xl font-bold text-white tracking-tight">Quality Certification</h3>
              <p className="text-agrogreen-100 text-sm leading-relaxed">Every seller and buyer is verified. We provide digital quality certificates for all listed inventory.</p>
            </div>
          </div>

          {/* Card 3: Instant Payments */}
          <div className="md:col-span-4 bg-[#143d2c] rounded-[32px] p-8 flex flex-col justify-between min-h-[300px] border border-agrogreen-950">
            <span className="bg-white/10 text-white p-3 rounded-2xl self-start backdrop-blur-md">
              <CreditCard className="h-6 w-6 text-agrogreen-400" />
            </span>
            <div className="space-y-3">
              <h3 className="text-2xl font-bold text-white tracking-tight">Instant Payments</h3>
              <p className="text-agrogreen-200 text-sm leading-relaxed">Secure escrow-based payments ensure farmers get paid immediately upon buyer confirmation.</p>
            </div>
          </div>

          {/* Card 4: Cold Storage */}
          <div className="md:col-span-8 bg-slate-100 rounded-[32px] p-8 flex flex-col md:flex-row md:items-center justify-between gap-8 min-h-[300px] border border-slate-200/50">
            <div className="space-y-4 flex-1">
              <span className="bg-slate-200/60 p-3 rounded-2xl inline-block">
                <Warehouse className="h-6 w-6 text-slate-700" />
              </span>
              <h3 className="text-2xl font-bold text-slate-800 tracking-tight">Smart Cold Storage</h3>
              <p className="text-slate-500 text-sm leading-relaxed max-w-sm">Access our network of IoT-enabled cold storage facilities to reduce post-harvest losses by up to 40%.</p>
            </div>
            
            {/* Visual cold gauge status */}
            <div className="bg-white p-5 rounded-2xl shadow-md border border-slate-200/30 flex items-center space-x-6 min-w-[200px]">
              <div>
                <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-1">Temp</span>
                <span className="block text-2xl font-black text-slate-800">4.2°C</span>
              </div>
              <div className="border-l border-slate-100 h-8"></div>
              <div>
                <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-1">Status</span>
                <span className="block text-sm font-extrabold text-emerald-600 uppercase">Optimal</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Government Schemes Section */}
      <section className="py-24 bg-white border-y border-slate-100 text-left">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end mb-12">
            <div>
              <span className="inline-flex items-center space-x-2 bg-emerald-50 border border-emerald-100 rounded-full px-4 py-1.5 text-xs font-bold text-emerald-700 tracking-wide uppercase mb-3">
                <Landmark className="h-3.5 w-3.5" />
                <span>Government Schemes Desk</span>
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">Welfare Schemes & Subsidies</h2>
              <p className="text-slate-500 text-base mt-2 max-w-xl">Access financial support, low-interest credits, and insurance schemes designed by the Government of India to protect and empower farmers.</p>
            </div>
            <Link
              to="/schemes"
              className="mt-6 md:mt-0 bg-slate-900 hover:bg-slate-850 text-white font-bold py-3.5 px-6 rounded-xl text-xs transition-colors flex items-center space-x-1.5 shadow-sm"
            >
              <span>Explore All Schemes</span>
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-[#fafbfb] p-8 rounded-[32px] border border-slate-100 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="space-y-4">
                <span className="text-[10px] bg-amber-50 text-amber-600 font-extrabold px-2.5 py-1 rounded-md uppercase tracking-wider">Financial Aid</span>
                <h3 className="text-xl font-bold text-slate-850">PM-KISAN</h3>
                <p className="text-slate-500 text-xs leading-relaxed">Direct income support of ₹6,000 per year in three equal installments to help small and marginal landholding farmers manage cultivation inputs.</p>
              </div>
              <Link to="/schemes" className="text-agrogreen-700 hover:text-agrogreen-850 font-bold text-xs flex items-center space-x-1 mt-6">
                <span>View Eligibility & Apply</span>
                <ChevronRight className="h-4.5 w-4.5" />
              </Link>
            </div>

            <div className="bg-[#fafbfb] p-8 rounded-[32px] border border-slate-100 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="space-y-4">
                <span className="text-[10px] bg-blue-50 text-blue-600 font-extrabold px-2.5 py-1 rounded-md uppercase tracking-wider">Crop Insurance</span>
                <h3 className="text-xl font-bold text-slate-850">PM Fasal Bima Yojana</h3>
                <p className="text-slate-500 text-xs leading-relaxed">Comprehensive insurance cover against crop damage from sowing to post-harvest stages due to natural disasters, drought, or pest infestations.</p>
              </div>
              <Link to="/schemes" className="text-agrogreen-700 hover:text-agrogreen-850 font-bold text-xs flex items-center space-x-1 mt-6">
                <span>View Eligibility & Apply</span>
                <ChevronRight className="h-4.5 w-4.5" />
              </Link>
            </div>

            <div className="bg-[#fafbfb] p-8 rounded-[32px] border border-slate-100 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="space-y-4">
                <span className="text-[10px] bg-indigo-50 text-indigo-600 font-extrabold px-2.5 py-1 rounded-md uppercase tracking-wider">Agriculture Credit</span>
                <h3 className="text-xl font-bold text-slate-850">Kisan Credit Card (KCC)</h3>
                <p className="text-slate-500 text-xs leading-relaxed">Access instant, hassle-free short-term crop loans up to ₹3 Lakhs at subsidized interest rates starting from 4% per annum.</p>
              </div>
              <Link to="/schemes" className="text-agrogreen-700 hover:text-agrogreen-850 font-bold text-xs flex items-center space-x-1 mt-6">
                <span>View Eligibility & Apply</span>
                <ChevronRight className="h-4.5 w-4.5" />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* How it works Section */}
      <section className="bg-slate-50 py-24 border-y border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
            
            {/* Left Tablet Image */}
            <div className="flex justify-center mx-auto w-full max-w-md shadow-2xl rounded-[40px] overflow-hidden transform hover:scale-[1.02] transition-transform duration-500 border border-slate-200/50">
              <img src="/images/tablet.png" alt="How it works tablet app" className="w-full h-auto object-cover" />
            </div>

            {/* Right Steps */}
            <div className="space-y-12 text-left">
              <div>
                <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight mb-4">How it works</h2>
                <p className="text-slate-500 text-base">Simple 3-step modern agricultural transactions portal.</p>
              </div>

              <div className="space-y-8">
                
                {/* Step 1 */}
                <div className="flex items-start space-x-5">
                  <div className="bg-agrogreen-950 text-white rounded-full h-10 w-10 shrink-0 flex items-center justify-center font-bold text-sm">1</div>
                  <div>
                    <h4 className="font-bold text-lg text-slate-800 mb-1">List Your Produce</h4>
                    <p className="text-slate-500 text-sm max-w-md">Snap a photo and upload your crop details. Our AI helps suggest the best price based on current Mandi rates.</p>
                  </div>
                </div>

                {/* Step 2 */}
                <div className="flex items-start space-x-5">
                  <div className="bg-agrogreen-950 text-white rounded-full h-10 w-10 shrink-0 flex items-center justify-center font-bold text-sm">2</div>
                  <div>
                    <h4 className="font-bold text-lg text-slate-800 mb-1">Connect with Verified Buyers</h4>
                    <p className="text-slate-500 text-sm max-w-md">Receive offers from wholesalers, retailers, and exporters directly on the app. No middlemen involved.</p>
                  </div>
                </div>

                {/* Step 3 */}
                <div className="flex items-start space-x-5">
                  <div className="bg-agrogreen-950 text-white rounded-full h-10 w-10 shrink-0 flex items-center justify-center font-bold text-sm">3</div>
                  <div>
                    <h4 className="font-bold text-lg text-slate-800 mb-1">Secure Delivery & Payment</h4>
                    <p className="text-slate-500 text-sm max-w-md">Mandi+ handles the logistics and ensures you get paid as soon as the produce reaches the buyer.</p>
                  </div>
                </div>

              </div>
            </div>

          </div>
        </div>
      </section>

      {/* Blinkit-Style Delivery Fleet Spotlight */}
      <section className="py-20 bg-gradient-to-br from-emerald-950 via-slate-900 to-emerald-900 text-white text-left relative overflow-hidden">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#10b981_1px,transparent_1px)] [background-size:20px_20px]"></div>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div className="lg:col-span-8 space-y-6">
              <span className="inline-flex items-center space-x-2 bg-emerald-500/20 border border-emerald-500/40 rounded-full px-4 py-1.5 text-xs font-bold text-emerald-400 uppercase tracking-wider">
                <Truck className="h-4 w-4" />
                <span>Join Maharashtra's Agri Express Delivery Fleet</span>
              </span>

              <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight leading-[1.15]">
                Own a Bike, Tempo or Mini-Truck? <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-lime-300">
                  Earn Up to ₹35,000/Month with Daily Payouts.
                </span>
              </h2>

              <p className="text-slate-300 text-base max-w-2xl leading-relaxed">
                Transport fresh farm harvest directly from Farmers, Farmgates, and Cold Storages to wholesale buyers and retailers. Enjoy instant daily settlements to UPI, free ₹5 Lakh accidental insurance, and dedicated delivery partner dashboard.
              </p>

              <div className="flex flex-wrap gap-4 pt-2">
                <Link
                  to="/apply-delivery"
                  className="bg-emerald-400 hover:bg-emerald-300 text-slate-950 font-black px-8 py-4 rounded-xl text-sm transition-all shadow-xl shadow-emerald-500/20 flex items-center space-x-2 hover:-translate-y-0.5"
                >
                  <span>Apply as Delivery Partner (15 Mins)</span>
                  <ArrowRight className="h-4 w-4" />
                </Link>

                <Link
                  to="/login?role=DELIVERY_PARTNER"
                  className="bg-white/10 hover:bg-white/20 text-white font-bold px-6 py-4 rounded-xl text-sm transition-all border border-white/20 hover:-translate-y-0.5"
                >
                  Partner Portal Login
                </Link>
              </div>
            </div>

            <div className="lg:col-span-4 bg-white/5 border border-white/10 rounded-3xl p-6 backdrop-blur-md space-y-4">
              <div className="flex justify-between items-center pb-3 border-b border-white/10">
                <span className="text-xs uppercase font-extrabold text-emerald-400">Partner Perks</span>
                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded font-bold">Maharashtra</span>
              </div>

              <div className="space-y-3 text-xs text-slate-200">
                <div className="flex items-center space-x-3">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span>Instant Daily Payouts via UPI at 9:00 PM</span>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span>High-density Mandi and Cold Storage batches</span>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span>Heavy loading and rain peak surge bonuses</span>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span>Integrated grievance & dispute resolution desk</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="bg-gradient-to-r from-agrogreen-950 to-[#0e2c1c] rounded-[40px] px-8 py-16 sm:px-16 text-center text-white relative overflow-hidden shadow-2xl border border-agrogreen-900">
          <div className="absolute inset-0 bg-[radial-gradient(#16a34a_1.2px,transparent_0)] [background-size:24px_24px] opacity-10"></div>
          
          <div className="relative z-10 max-w-3xl mx-auto space-y-8">
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight">Ready to revolutionize your farming business?</h2>
            <p className="text-agrogreen-200/80 text-lg leading-relaxed max-w-xl mx-auto">
              Join over 50,000 farmers and buyers across India already using Mandi+ to modernize the supply chain.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
              <Link
                to="/register?role=FARMER"
                className="bg-agrogreen-400 hover:bg-agrogreen-300 text-agrogreen-950 px-8 py-4 rounded-xl font-bold transition-all hover:-translate-y-0.5"
              >
                Register as Farmer
              </Link>
              <Link
                to="/register?role=BUYER"
                className="bg-white hover:bg-slate-50 text-slate-800 px-8 py-4 rounded-xl font-bold transition-all hover:-translate-y-0.5"
              >
                Register as Buyer
              </Link>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
