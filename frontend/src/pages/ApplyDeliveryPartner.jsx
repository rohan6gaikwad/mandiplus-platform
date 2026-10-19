import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import { 
  Bike, Truck, ShieldCheck, Zap, DollarSign, 
  MapPin, CheckCircle2, AlertCircle, ArrowRight, ArrowLeft,
  Clock, Award, Phone, UserCheck, ChevronRight, Sparkles, Navigation
} from 'lucide-react';

const MAHARASHTRA_HUBS = [
  "Nashik APMC Main Terminal (Lasalgaon Corridor)",
  "Pune APMC Market Yard (Gultekdi)",
  "Vashi Navi Mumbai Agricultural Terminal",
  "Nagpur Cotton & Orange Mandi Hub",
  "Kolhapur Sugarcane & Jaggery Market Hub",
  "Chhatrapati Sambhajinagar APMC Hub",
  "Ahmednagar Onion Dispatch Hub",
  "Solapur Pomegranate & Grains Hub"
];

const VEHICLE_OPTIONS = [
  { id: 'BIKE', label: 'Motorcycle / Bike', capacity: 'Up to 60 kg (Small crates & produce packs)', icon: Bike, baseRate: '₹35/drop + ₹12/km' },
  { id: 'SCOOTER', label: 'Electric Scooter (EV)', capacity: 'Up to 50 kg (Low running cost)', icon: Zap, baseRate: '₹35/drop + ₹10/km' },
  { id: 'TEMPO', label: 'Auto Tempo / 3-Wheeler', capacity: 'Up to 500 kg (Wholesale mandi crates)', icon: Truck, baseRate: '₹120/drop + ₹25/km' },
  { id: 'MINI_TRUCK', label: 'Mini Truck (Tata Ace / Bolero)', capacity: 'Up to 1.5 Tons (Bulk farm consignments)', icon: Truck, baseRate: '₹280/drop + ₹40/km' }
];

export default function ApplyDeliveryPartner() {
  const { t } = useLanguage();
  const { login } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successData, setSuccessData] = useState(null);

  // Form Fields
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [profilePhoto, setProfilePhoto] = useState('');
  
  const [vehicleType, setVehicleType] = useState('BIKE');
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [panOrAadhaar, setPanOrAadhaar] = useState('');

  const [preferredHub, setPreferredHub] = useState(MAHARASHTRA_HUBS[0]);
  const [upiId, setUpiId] = useState('');
  const [emergencyContactName, setEmergencyContactName] = useState('');
  const [emergencyContactPhone, setEmergencyContactPhone] = useState('');

  const handleNext = (e) => {
    e.preventDefault();
    setError('');
    if (step === 1) {
      if (!name || !phone || !email || !password) {
        setError('Please fill in all required personal details.');
        return;
      }
      if (password.length < 6) {
        setError('Password must be at least 6 characters.');
        return;
      }
      setStep(2);
    } else if (step === 2) {
      if (!vehicleNumber || !licenseNumber) {
        setError('Please provide your vehicle number and driving license number.');
        return;
      }
      setStep(3);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const payload = {
      name,
      phone,
      email: email.trim().toLowerCase(),
      password,
      profilePhoto: profilePhoto || "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%2310b981'/><text x='50' y='60' font-size='40' text-anchor='middle'>🛵</text></svg>",
      vehicleType,
      vehicleNumber,
      licenseNumber,
      panOrAadhaar,
      preferredHub,
      upiId,
      emergencyContactName,
      emergencyContactPhone,
      city: "Nashik",
      district: "Nashik"
    };

    try {
      const response = await api.post('/public/delivery/apply', payload);
      setSuccessData(response.data);
      // Automatically attempt login for instant onboarding
      try {
        await login(email.trim().toLowerCase(), password);
      } catch (loginErr) {
        console.warn('Auto-login notice:', loginErr);
      }
    } catch (err) {
      setError(err.response?.data || err.message || 'Application submission failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInstantDemoLogin = async () => {
    try {
      setLoading(true);
      await login('delivery@mandiplus.com', 'password123');
      navigate('/delivery');
    } catch (err) {
      // If demo account not registered yet, navigate to delivery login
      navigate('/login?role=DELIVERY_PARTNER');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <Navbar />

      {/* Hero Banner Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-agrogreen-950 via-slate-900 to-agrogreen-900 text-white py-16 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#10b981_1px,transparent_1px)] [background-size:16px_16px]"></div>
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-12 items-center relative z-10">
          
          <div className="lg:col-span-7 space-y-6 text-left">
            <div className="inline-flex items-center space-x-2 bg-emerald-500/10 border border-emerald-500/30 rounded-full px-4 py-1.5 text-xs font-bold text-emerald-400 uppercase tracking-wider">
              <Sparkles className="h-4 w-4 animate-spin-slow" />
              <span>Blinkit-Style Agri Quick Delivery Fleet</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight leading-[1.1]">
              Deliver Fresh Harvest. <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-lime-300">
                Earn Daily Payouts.
              </span>
            </h1>

            <p className="text-slate-300 text-lg max-w-xl leading-relaxed">
              Join Maharashtra's direct farmgate-to-door logistics network. Transport fresh produce batches directly from local farmers and cold storage facilities to commercial bulk buyers and retailers.
            </p>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-4">
              <div className="bg-white/5 border border-white/10 rounded-2xl p-4 backdrop-blur-sm">
                <span className="text-2xl font-black text-emerald-400 block">₹32,000+</span>
                <span className="text-xs text-slate-400 font-medium">Avg Monthly Pay</span>
              </div>
              <div className="bg-white/5 border border-white/10 rounded-2xl p-4 backdrop-blur-sm">
                <span className="text-2xl font-black text-lime-400 block">Daily</span>
                <span className="text-xs text-slate-400 font-medium">Instant UPI Transfer</span>
              </div>
              <div className="bg-white/5 border border-white/10 rounded-2xl p-4 backdrop-blur-sm">
                <span className="text-2xl font-black text-sky-400 block">₹5 Lakh</span>
                <span className="text-xs text-slate-400 font-medium">Accident Insurance</span>
              </div>
              <div className="bg-white/5 border border-white/10 rounded-2xl p-4 backdrop-blur-sm">
                <span className="text-2xl font-black text-amber-400 block">15 Mins</span>
                <span className="text-xs text-slate-400 font-medium">Fast Activation</span>
              </div>
            </div>

            <div className="pt-2 flex flex-wrap gap-4 items-center">
              <button
                onClick={handleInstantDemoLogin}
                className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold px-6 py-3 rounded-xl text-sm transition-all shadow-lg shadow-emerald-500/20 flex items-center space-x-2"
              >
                <span>⚡ Test Drive Demo Partner Account</span>
                <ArrowRight className="h-4 w-4" />
              </button>
              <Link
                to="/login?role=DELIVERY_PARTNER"
                className="text-slate-300 hover:text-white font-semibold text-sm underline underline-offset-4"
              >
                Already registered? Sign In
              </Link>
            </div>
          </div>

          <div className="lg:col-span-5 flex justify-center">
            <div className="w-full max-w-md bg-white/10 backdrop-blur-md rounded-3xl p-6 border border-white/20 shadow-2xl space-y-4 text-left">
              <div className="flex items-center justify-between pb-4 border-b border-white/10">
                <div className="flex items-center space-x-3">
                  <div className="h-10 w-10 rounded-xl bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-emerald-400">
                    <Bike className="h-5 w-5" />
                  </div>
                  <div>
                    <h4 className="font-bold text-sm text-white">Mandi+ Express Fleet</h4>
                    <span className="text-[10px] text-emerald-400 uppercase tracking-wider font-extrabold">Active Across Maharashtra Farms</span>
                  </div>
                </div>
                <span className="bg-emerald-500 text-slate-950 text-xs font-black px-2.5 py-1 rounded-full">
                  HIRING NOW
                </span>
              </div>

              <div className="space-y-2.5 text-xs text-slate-200">
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                  <span>Choose flexible shifts: Morning, Evening, or Flexi-Hours</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                  <span>Zero joining fees or upfront deposits</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                  <span>Special heavy quintal loading & rain surge allowances</span>
                </div>
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                  <span>Integrated Delivery Partner Dashboard with Grievance Desk</span>
                </div>
              </div>

              <div className="bg-black/20 p-3 rounded-2xl text-[11px] text-slate-300 italic border border-white/5">
                "Joined Mandi+ last month with my commercial tempo. I pick up fresh onion batches directly from farmers in Niphad and deliver straight to wholesale buyers. I get payments every evening directly to my GPay!"
                <span className="block mt-1 font-bold text-white not-italic">— Sachin K., Delivery Partner, Nashik</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Form Section */}
      <main className="flex-1 max-w-4xl mx-auto w-full px-4 sm:px-6 py-12">
        {successData ? (
          <div className="bg-white rounded-3xl p-5 sm:p-12 border border-slate-100 shadow-premium text-center space-y-6 animate-fade-in">
            <div className="h-20 w-20 rounded-full bg-emerald-50 border border-emerald-200 flex items-center justify-center mx-auto text-emerald-600">
              <CheckCircle2 className="h-10 w-10 animate-bounce" />
            </div>

            <div className="space-y-2">
              <span className="text-xs uppercase font-extrabold tracking-wider bg-emerald-100 text-emerald-800 px-3 py-1 rounded-full">
                Application Approved & Activated
              </span>
              <h2 className="text-3xl font-black text-slate-900 tracking-tight">
                Welcome to the Mandi+ Fleet, {successData.partner?.name || name}!
              </h2>
              <p className="text-slate-500 text-sm max-w-md mx-auto">
                Your delivery partner profile has been configured. Your fleet tracking ID is{' '}
                <strong className="text-slate-900">{successData.trackingId || 'MND-FLEET-OK'}</strong>.
              </p>
            </div>

            <div className="bg-slate-50 rounded-2xl p-6 border border-slate-100 max-w-md mx-auto text-left space-y-3 text-xs">
              <div className="flex justify-between border-b border-slate-200 pb-2">
                <span className="text-slate-500">Registered Email:</span>
                <span className="font-bold text-slate-800">{email}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200 pb-2">
                <span className="text-slate-500">Assigned Operating Hub:</span>
                <span className="font-bold text-slate-800">{preferredHub.split('(')[0]}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200 pb-2">
                <span className="text-slate-500">Vehicle Type:</span>
                <span className="font-bold text-slate-800">{vehicleType} ({vehicleNumber})</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Daily Payout UPI:</span>
                <span className="font-bold text-emerald-600">{upiId || 'partner@okaxis'}</span>
              </div>
            </div>

            <div className="pt-4 flex flex-col sm:flex-row justify-center gap-4">
              <button
                onClick={() => navigate('/delivery')}
                className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 px-8 rounded-xl text-sm transition-all shadow-md flex items-center justify-center space-x-2"
              >
                <span>Launch Delivery Dashboard</span>
                <ArrowRight className="h-4 w-4" />
              </button>
              <Link
                to="/delivery/complaints"
                className="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-3.5 px-6 rounded-xl text-sm transition-all text-center"
              >
                Grievance & Support Desk
              </Link>
            </div>
          </div>
        ) : (
          <div className="bg-white rounded-3xl p-5 sm:p-12 border border-slate-100 shadow-premium">
            {/* Step Progress Tracker */}
            <div className="flex items-center justify-between mb-8 pb-6 border-b border-slate-100">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1">
                  Mandi+ Fleet Onboarding
                </span>
                <h3 className="text-2xl font-black text-slate-900 tracking-tight">
                  Delivery Partner Application
                </h3>
              </div>

              <div className="flex items-center space-x-2 text-xs font-bold">
                <div className={`h-8 w-8 rounded-full flex items-center justify-center ${step >= 1 ? 'bg-[#143d2c] text-white' : 'bg-slate-100 text-slate-400'}`}>
                  1
                </div>
                <span className="h-0.5 w-6 bg-slate-200"></span>
                <div className={`h-8 w-8 rounded-full flex items-center justify-center ${step >= 2 ? 'bg-[#143d2c] text-white' : 'bg-slate-100 text-slate-400'}`}>
                  2
                </div>
                <span className="h-0.5 w-6 bg-slate-200"></span>
                <div className={`h-8 w-8 rounded-full flex items-center justify-center ${step >= 3 ? 'bg-[#143d2c] text-white' : 'bg-slate-100 text-slate-400'}`}>
                  3
                </div>
              </div>
            </div>

            {error && (
              <div className="mb-6 bg-rose-50 border border-rose-100 text-rose-700 p-4 rounded-2xl text-xs font-bold flex items-center space-x-2">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* STEP 1: Personal & Account */}
            {step === 1 && (
              <form onSubmit={handleNext} className="space-y-6 text-left animate-fade-in">
                <div className="space-y-1">
                  <h4 className="text-lg font-bold text-slate-800">Step 1: Personal Coordinates & Credentials</h4>
                  <p className="text-xs text-slate-400">You will use this email and password to log in to your partner portal.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Full Name *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Rahul Shinde"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Mobile Phone Number (WhatsApp) *</label>
                    <input
                      type="tel"
                      required
                      placeholder="98XXXXXXXX"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Email Address *</label>
                    <input
                      type="email"
                      required
                      placeholder="partner@example.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Create Login Password *</label>
                    <input
                      type="password"
                      required
                      placeholder="At least 6 characters"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Aadhaar or PAN Number (For verification)</label>
                  <input
                    type="text"
                    placeholder="e.g. XXXX-XXXX-1234 or ABCDE1234F"
                    value={panOrAadhaar}
                    onChange={(e) => setPanOrAadhaar(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                  />
                </div>

                <div className="pt-4 flex justify-between items-center">
                  <span className="text-xs text-slate-400">Step 1 of 3</span>
                  <button
                    type="submit"
                    className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 px-8 rounded-xl text-sm transition-all flex items-center space-x-2"
                  >
                    <span>Proceed to Vehicle Info</span>
                    <ArrowRight className="h-4 w-4" />
                  </button>
                </div>
              </form>
            )}

            {/* STEP 2: Vehicle & Driving Credentials */}
            {step === 2 && (
              <form onSubmit={handleNext} className="space-y-6 text-left animate-fade-in">
                <div className="space-y-1">
                  <h4 className="text-lg font-bold text-slate-800">Step 2: Fleet & Vehicle Specifications</h4>
                  <p className="text-xs text-slate-400">Select the vehicle you will use for agricultural produce transport.</p>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-2">Select Vehicle Type *</label>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {VEHICLE_OPTIONS.map((v) => {
                      const Icon = v.icon;
                      const isSelected = vehicleType === v.id;
                      return (
                        <div
                          key={v.id}
                          onClick={() => setVehicleType(v.id)}
                          className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                            isSelected
                              ? 'border-[#143d2c] bg-agrogreen-50/50 ring-2 ring-[#143d2c]/20'
                              : 'border-slate-200 hover:border-slate-300 bg-white'
                          }`}
                        >
                          <div className="flex items-center space-x-3 mb-2">
                            <div className={`p-2 rounded-xl ${isSelected ? 'bg-[#143d2c] text-white' : 'bg-slate-100 text-slate-600'}`}>
                              <Icon className="h-5 w-5" />
                            </div>
                            <span className="font-extrabold text-sm text-slate-900">{v.label}</span>
                          </div>
                          <p className="text-xs text-slate-500 leading-snug">{v.capacity}</p>
                          <span className="inline-block mt-2 text-[10px] font-bold text-agrogreen-800 bg-agrogreen-100 px-2.5 py-0.5 rounded-full">
                            Rate: {v.baseRate}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Vehicle Plate / Registration Number *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. MH-15-AB-1234"
                      value={vehicleNumber}
                      onChange={(e) => setVehicleNumber(e.target.value.toUpperCase())}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Driving License (DL) Number *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. MH1520210098765"
                      value={licenseNumber}
                      onChange={(e) => setLicenseNumber(e.target.value.toUpperCase())}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div className="pt-4 flex justify-between items-center">
                  <button
                    type="button"
                    onClick={() => setStep(1)}
                    className="flex items-center text-xs font-bold text-slate-500 hover:text-slate-800"
                  >
                    <ArrowLeft className="h-4 w-4 mr-1" />
                    <span>Back</span>
                  </button>

                  <button
                    type="submit"
                    className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 px-8 rounded-xl text-sm transition-all flex items-center space-x-2"
                  >
                    <span>Proceed to Hub & Payout</span>
                    <ArrowRight className="h-4 w-4" />
                  </button>
                </div>
              </form>
            )}

            {/* STEP 3: Hub & Daily Payout Details */}
            {step === 3 && (
              <form onSubmit={handleSubmit} className="space-y-6 text-left animate-fade-in">
                <div className="space-y-1">
                  <h4 className="text-lg font-bold text-slate-800">Step 3: Mandi Hub Preference & Payout UPI</h4>
                  <p className="text-xs text-slate-400">Choose your base terminal and where your daily delivery earnings will be deposited.</p>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Preferred Mandi Hub / Terminal *</label>
                  <select
                    value={preferredHub}
                    onChange={(e) => setPreferredHub(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                  >
                    {MAHARASHTRA_HUBS.map((hub) => (
                      <option key={hub} value={hub}>{hub}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">UPI ID for Daily Instant Settlement *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. rahul@okaxis, rahul@okhdfcbank"
                    value={upiId}
                    onChange={(e) => setUpiId(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                  />
                  <span className="text-[10px] text-slate-400 mt-1 block">
                    Earnings are credited daily at 9:00 PM via IMPS / UPI with zero transfer fees.
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Emergency Contact Person</label>
                    <input
                      type="text"
                      placeholder="e.g. Ramesh Shinde (Brother / Father)"
                      value={emergencyContactName}
                      onChange={(e) => setEmergencyContactName(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Emergency Phone Number</label>
                    <input
                      type="tel"
                      placeholder="98XXXXXXXX"
                      value={emergencyContactPhone}
                      onChange={(e) => setEmergencyContactPhone(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div className="bg-emerald-50 border border-emerald-100 p-4 rounded-2xl flex items-center space-x-3 text-xs text-emerald-800">
                  <ShieldCheck className="h-6 w-6 text-emerald-600 shrink-0" />
                  <span>
                    By submitting, you agree to Mandi+ partner terms and accept produce transit guidelines. Free ₹5 Lakh accidental insurance policy is active upon first delivery.
                  </span>
                </div>

                <div className="pt-4 flex justify-between items-center">
                  <button
                    type="button"
                    onClick={() => setStep(2)}
                    className="flex items-center text-xs font-bold text-slate-500 hover:text-slate-800"
                  >
                    <ArrowLeft className="h-4 w-4 mr-1" />
                    <span>Back</span>
                  </button>

                  <button
                    type="submit"
                    disabled={loading}
                    className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-4 px-10 rounded-xl text-sm transition-all shadow-md flex items-center space-x-2 disabled:opacity-50"
                  >
                    {loading ? (
                      <span>Activating Fleet Account...</span>
                    ) : (
                      <>
                        <span>Submit Application & Start Delivering</span>
                        <CheckCircle2 className="h-4 w-4" />
                      </>
                    )}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </main>

      <Footer />
    </div>
  );
}
