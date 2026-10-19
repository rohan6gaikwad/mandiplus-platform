import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import LanguageSelector from '../components/LanguageSelector';
import Navbar from '../components/Navbar';
import { 
  UserCheck, Leaf, Warehouse, ShoppingBag, 
  Mail, Key, Phone, MapPin, CreditCard, ChevronRight, AlertCircle, ArrowLeft, TrendingUp 
} from 'lucide-react';

const PRESET_AVATARS = [
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23dcfce7'/><text x='50' y='60' font-size='40' text-anchor='middle'>🌱</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23fef3c7'/><text x='50' y='60' font-size='40' text-anchor='middle'>🌾</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23dbeafe'/><text x='50' y='60' font-size='40' text-anchor='middle'>🚜</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23e0e7ff'/><text x='50' y='60' font-size='40' text-anchor='middle'>🍎</text></svg>"
];

const MAHARASHTRA_DISTRICTS = [
  "Ahmednagar", "Akola", "Amravati", "Beed", "Bhandara", "Buldhana", 
  "Chandrapur", "Chhatrapati Sambhajinagar", "Dhule", "Gadchiroli", "Gondia", 
  "Hingoli", "Jalgaon", "Jalna", "Kolhapur", "Latur", "Mumbai City", 
  "Mumbai Suburban", "Nagpur", "Nanded", "Nandurbar", "Nashik", 
  "Osmanabad", "Palghar", "Parbhani", "Pune", "Raigad", "Ratnagiri", 
  "Sangli", "Satara", "Sindhudurg", "Solapur", "Thane", "Wardha", 
  "Washim", "Yavatmal"
];

export default function Register() {
  const { t } = useLanguage();
  const [searchParams] = useSearchParams();
  const initialRole = searchParams.get('role') || 'FARMER';

  const [step, setStep] = useState(1);
  const [role, setRole] = useState(initialRole);
  
  // Credentials
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Farmer Details
  const [farmerName, setFarmerName] = useState('');
  const [farmerPhone, setFarmerPhone] = useState('');
  const [farmerState, setFarmerState] = useState('Maharashtra');
  const [farmerDistrict, setFarmerDistrict] = useState('Pune');
  const [farmSize, setFarmSize] = useState('');
  const [upiId, setUpiId] = useState('');

  // Buyer Details
  const [companyName, setCompanyName] = useState('');
  const [buyerName, setBuyerName] = useState('');
  const [buyerPhone, setBuyerPhone] = useState('');
  const [buyerState, setBuyerState] = useState('Maharashtra');
  const [buyerDistrict, setBuyerDistrict] = useState('Pune');
  const [buyerAddressDetails, setBuyerAddressDetails] = useState('');
  const [gstin, setGstin] = useState('');

  // Cold Storage Manager Details
  const [storageName, setStorageName] = useState('');
  const [storageState, setStorageState] = useState('Maharashtra');
  const [storageDistrict, setStorageDistrict] = useState('Pune');
  const [storageAddressDetails, setStorageAddressDetails] = useState('');
  const [storageCapacity, setStorageCapacity] = useState('');
  const [storageRate, setStorageRate] = useState('');
  const [storageDesc, setStorageDesc] = useState('');

  // Delivery Partner Details
  const [deliveryName, setDeliveryName] = useState('');
  const [deliveryPhone, setDeliveryPhone] = useState('');
  const [deliveryVehicleType, setDeliveryVehicleType] = useState('BIKE');
  const [deliveryVehicleNumber, setDeliveryVehicleNumber] = useState('');
  const [deliveryLicenseNumber, setDeliveryLicenseNumber] = useState('');
  const [deliveryDistrict, setDeliveryDistrict] = useState('Nashik');
  const [deliveryUpi, setDeliveryUpi] = useState('');

  const [profilePhoto, setProfilePhoto] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleNextStep = (e) => {
    e.preventDefault();
    if (step === 1) {
      if (!email || !password) {
        setError('Please fill in credentials first.');
        return;
      }
      if (password.length < 6) {
        setError('Password must be at least 6 characters long.');
        return;
      }
      setError('');
      setStep(2);
    } else if (step === 2) {
      setError('');
      setStep(3);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const trimmedEmail = email ? email.trim().toLowerCase() : '';
    const payload = {
      email: trimmedEmail,
      password,
      role,
      profilePhoto,
    };

    if (role === 'FARMER') {
      payload.name = farmerName;
      payload.phone = farmerPhone;
      payload.state = farmerState;
      payload.district = farmerDistrict;
      payload.farmSizeAcres = parseFloat(farmSize) || 0;
      payload.upiId = upiId;
    } else if (role === 'BUYER') {
      payload.companyName = companyName;
      payload.name = buyerName;
      payload.phone = buyerPhone;
      payload.address = `${buyerAddressDetails}, ${buyerDistrict}, ${buyerState}`;
      payload.gstin = gstin;
    } else if (role === 'COLD_STORAGE_MANAGER') {
      payload.storageName = storageName;
      payload.storageLocation = `${storageAddressDetails}, ${storageDistrict}, ${storageState}`;
      payload.storageCapacityTons = parseFloat(storageCapacity) || 0;
      payload.storagePricePerTonDay = parseFloat(storageRate) || 0;
      payload.storageDescription = storageDesc;
    } else if (role === 'DELIVERY_PARTNER') {
      payload.name = deliveryName;
      payload.phone = deliveryPhone;
      payload.vehicleType = deliveryVehicleType;
      payload.vehicleNumber = deliveryVehicleNumber;
      payload.licenseNumber = deliveryLicenseNumber;
      payload.district = deliveryDistrict;
      payload.preferredHub = `${deliveryDistrict} APMC Hub`;
      payload.upiId = deliveryUpi;
    }

    try {
      await register(payload);
      navigate(`/login?role=${role}`);
    } catch (err) {
      setError(err.message || 'Onboarding failed.');
    } finally {
      setLoading(false);
    }
  };

  const roleConfig = {
    FARMER: {
      leftTitle: 'Join the Mandi+ Community',
      leftDesc: 'Empowering Indian farmers with direct market access, cold storage tracking, and modern financial tools.',
      pill1Text: 'Real-time Mandi Prices',
      pill1Icon: <TrendingUp className="h-5 w-5 mr-3" />,
      pill2Text: 'Cold Storage Logistics',
      pill2Icon: <Warehouse className="h-5 w-5 mr-3" />,
      imageText: '"Mandi+ helped me double my storage efficiency."',
      imageSrc: '/images/hero.png'
    },
    BUYER: {
      leftTitle: 'Connect with 50,000+ Farmers Directly.',
      leftDesc: 'Mandi+ bridges the gap between the field and your facility. Streamline your procurement with real-time mandi rates, quality-verified listings, and integrated cold storage logistics.',
      pill1Text: 'Bulk Sourcing',
      pill1Icon: <ShoppingBag className="h-5 w-5 text-slate-700" />,
      pill1Sub: 'Direct access to harvest-ready crops.',
      pill2Text: 'Transparent Pricing',
      pill2Icon: <CreditCard className="h-5 w-5 text-slate-700" />,
      pill2Sub: 'Zero hidden fees, 100% visibility.',
      imageText: '"Mandi+ reduced our procurement lead time by 40% in just one season." — Cold Storage Manager, Nashik',
      imageSrc: '/images/logistics.png'
    },
    COLD_STORAGE_MANAGER: {
      leftTitle: 'List Your Cold Storage Facility',
      leftDesc: 'Join our network of IoT-enabled facilities. Increase your capacity utilization by connecting directly with farmers and bulk buyers in your region.',
      pill1Text: 'Capacity Tracking',
      pill1Icon: <Warehouse className="h-5 w-5 text-slate-700" />,
      pill1Sub: 'Manage your storage inventory.',
      pill2Text: 'Guaranteed Payments',
      pill2Icon: <CreditCard className="h-5 w-5 text-slate-700" />,
      pill2Sub: 'Secure escrow payouts.',
      imageText: '"We increased our utilization by 60% after joining the network."',
      imageSrc: '/images/tablet.png'
    },
    DELIVERY_PARTNER: {
      leftTitle: 'Deliver Agri Produce. Earn Daily.',
      leftDesc: 'Join Maharashtra\'s agricultural quick-commerce transport network. Transport fresh farm produce batches directly from Farmers, Farmgates & Cold Storages to Buyers with guaranteed daily payouts.',
      pill1Text: 'Instant Daily UPI Payouts',
      pill1Icon: <TrendingUp className="h-5 w-5 mr-3 text-emerald-800" />,
      pill2Text: 'Free Accidental Insurance',
      pill2Icon: <Warehouse className="h-5 w-5 mr-3 text-emerald-800" />,
      imageText: '"Delivering with Mandi+ increased my monthly earnings to over ₹32,000."',
      imageSrc: '/images/logistics.png'
    }
  };

  const currentConfig = roleConfig[role] || roleConfig.FARMER;

  return (
    <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
      <div className="flex justify-between items-center px-8 py-4 border-b border-slate-100 bg-white">
        <Link to="/" className="flex items-center group" title="Return to Mandi+ Home">
          <div className="bg-white rounded-2xl p-1.5 shadow-sm border border-slate-200/80 hover:shadow-md transition-all duration-300 group-hover:scale-105 flex items-center">
            <img 
              src="/images/logo.png" 
              alt="Mandi+ Market Solutions" 
              className="h-11 w-auto object-contain rounded-xl" 
            />
          </div>
        </Link>
        <div className="flex space-x-4 items-center text-sm font-bold text-slate-500">
           <LanguageSelector />
           <span className="cursor-pointer hover:text-slate-800">Help</span>
        </div>
      </div>

      <main className="flex-1 flex flex-col md:flex-row max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-12 gap-12">
        
        {/* Left Pane */}
        <div className="w-full md:w-1/2 flex flex-col justify-center space-y-8 pr-0 md:pr-8">
          {role === 'BUYER' && (
             <span className="inline-block bg-agrogreen-200 text-agrogreen-900 font-bold text-[10px] uppercase tracking-wider px-3 py-1.5 rounded-full w-max mb-[-1rem]">
                TRUSTED BY 5,000+ ENTERPRISES
             </span>
          )}

          <h1 className="text-4xl lg:text-5xl font-black text-slate-900 tracking-tight leading-[1.1]">
            {currentConfig.leftTitle}
          </h1>
          <p className="text-slate-600 text-lg max-w-md">
            {currentConfig.leftDesc}
          </p>

          {role === 'FARMER' ? (
             <div className="space-y-3 max-w-sm">
                <div className="flex items-center bg-agrogreen-200 text-agrogreen-900 px-5 py-4 rounded-2xl font-bold">
                   {currentConfig.pill1Icon} {currentConfig.pill1Text}
                </div>
                <div className="flex items-center bg-slate-200 text-slate-800 px-5 py-4 rounded-2xl font-bold">
                   {currentConfig.pill2Icon} {currentConfig.pill2Text}
                </div>
             </div>
          ) : (
             <div className="flex space-x-4">
                <div className="flex flex-col bg-white p-4 rounded-2xl shadow-sm border border-slate-100 flex-1">
                   <div className="bg-slate-50 p-3 rounded-xl w-max mb-3 border border-slate-100">
                     {currentConfig.pill1Icon}
                   </div>
                   <span className="font-bold text-slate-900 text-sm">{currentConfig.pill1Text}</span>
                   <span className="text-xs text-slate-500 leading-tight mt-1">{currentConfig.pill1Sub}</span>
                </div>
                <div className="flex flex-col bg-white p-4 rounded-2xl shadow-sm border border-slate-100 flex-1">
                   <div className="bg-slate-50 p-3 rounded-xl w-max mb-3 border border-slate-100">
                     {currentConfig.pill2Icon}
                   </div>
                   <span className="font-bold text-slate-900 text-sm">{currentConfig.pill2Text}</span>
                   <span className="text-xs text-slate-500 leading-tight mt-1">{currentConfig.pill2Sub}</span>
                </div>
             </div>
          )}

          <div className="relative w-full max-w-sm aspect-[2/1] rounded-[24px] overflow-hidden mt-4 shadow-lg border border-slate-200/50">
             <img src={currentConfig.imageSrc} alt="Registration reference" className="absolute inset-0 w-full h-full object-cover" />
             <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/40 to-transparent"></div>
             <p className="absolute bottom-4 left-4 right-4 text-white text-sm font-medium italic drop-shadow-md">
                {currentConfig.imageText}
             </p>
          </div>
        </div>

        {/* Right Pane Form */}
        <div className="w-full md:w-1/2 flex items-center justify-center">
          <div className="w-full bg-white rounded-[32px] p-5 sm:p-10 shadow-premium border border-slate-100 relative">
            
            <div className="flex justify-between items-center mb-8 text-xs font-bold uppercase tracking-wider text-slate-400">
               <div className="flex items-center space-x-2">
                 <span className={`w-8 h-2 rounded-full ${step >= 1 ? 'bg-[#143d2c]' : 'bg-slate-200'}`}></span>
                 <span className={`w-2 h-2 rounded-full ${step >= 2 ? 'bg-[#143d2c]' : 'bg-slate-200'}`}></span>
                 <span className={`w-2 h-2 rounded-full ${step >= 3 ? 'bg-[#143d2c]' : 'bg-slate-200'}`}></span>
               </div>
               <span>Step {step} of 3</span>
            </div>

            {error && (
              <div className="bg-rose-50 border border-rose-100 text-rose-700 p-4 rounded-xl text-xs font-bold mb-6 flex items-center space-x-2">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {step === 1 && (
              <form onSubmit={handleNextStep} className="space-y-6 animate-fade-in">
                <h2 className="text-2xl font-black text-slate-800 tracking-tight">{t('registerTitle')}</h2>
                
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">Registering As</label>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                    <button type="button" onClick={() => setRole('FARMER')} className={`py-3 rounded-xl border text-xs font-bold transition-all ${role === 'FARMER' ? 'border-[#143d2c] bg-[#143d2c] text-white' : 'border-slate-200 text-slate-500 hover:bg-slate-50'}`}>{t('farmer')}</button>
                    <button type="button" onClick={() => setRole('BUYER')} className={`py-3 rounded-xl border text-xs font-bold transition-all ${role === 'BUYER' ? 'border-[#143d2c] bg-[#143d2c] text-white' : 'border-slate-200 text-slate-500 hover:bg-slate-50'}`}>{t('buyer')}</button>
                    <button type="button" onClick={() => setRole('COLD_STORAGE_MANAGER')} className={`py-3 rounded-xl border text-xs font-bold transition-all ${role === 'COLD_STORAGE_MANAGER' ? 'border-[#143d2c] bg-[#143d2c] text-white' : 'border-slate-200 text-slate-500 hover:bg-slate-50'}`}>{t('coldStorageManager')}</button>
                    <button type="button" onClick={() => setRole('DELIVERY_PARTNER')} className={`py-3 rounded-xl border text-xs font-bold transition-all ${role === 'DELIVERY_PARTNER' ? 'border-emerald-700 bg-emerald-700 text-white shadow-sm' : 'border-slate-200 text-emerald-800 bg-emerald-50/50 hover:bg-emerald-100'}`}>Delivery Fleet</button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-2">{t('email')}</label>
                  <input type="email" required placeholder="corporate@company.com" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full px-4 py-3.5 rounded-xl border border-slate-200 focus:outline-none focus:border-[#143d2c] text-sm" />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-2">{t('password')}</label>
                  <input type="password" required placeholder="Create a strong password" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full px-4 py-3.5 rounded-xl border border-slate-200 focus:outline-none focus:border-[#143d2c] text-sm" />
                </div>

                <button type="submit" className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center transition-all mt-4">
                  {t('getStarted')}
                </button>
              </form>
            )}

            {step === 2 && (
              <form onSubmit={handleNextStep} className="space-y-5 animate-fade-in">
                <button type="button" onClick={() => setStep(1)} className="flex items-center text-[10px] font-bold text-slate-400 hover:text-slate-800 uppercase tracking-wider mb-2">
                  <ArrowLeft className="h-3 w-3 mr-1" /> Back
                </button>

                <h2 className="text-2xl font-black text-slate-800 tracking-tight mb-4">
                  {role === 'FARMER' ? 'Tell us about yourself' : role === 'BUYER' ? 'Business Information' : 'Facility Details'}
                </h2>

                {/* Profile Photo Selector Onboarding */}
                <div className="bg-slate-50 border border-slate-100 p-4 rounded-2xl space-y-3 text-left">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-700">{t('uploadAvatar')}</span>
                    <label className="text-[10px] bg-slate-900 hover:bg-slate-800 text-white font-bold px-3 py-1.5 rounded-lg cursor-pointer transition-colors shadow-sm">
                      Upload Custom
                      <input 
                        type="file" accept="image/*" className="hidden" 
                        onChange={(e) => {
                          const file = e.target.files[0];
                          if (file) {
                            const reader = new FileReader();
                            reader.onloadend = () => setProfilePhoto(reader.result);
                            reader.readAsDataURL(file);
                          }
                        }} 
                      />
                    </label>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    {profilePhoto ? (
                      <img src={profilePhoto} alt="Selected avatar" className="h-10 w-10 rounded-xl object-cover border border-slate-200" />
                    ) : (
                      <div className="h-10 w-10 rounded-xl bg-slate-200 flex items-center justify-center text-slate-400">
                        <UserCheck className="h-5 w-5" />
                      </div>
                    )}
                    
                    <div className="flex space-x-2">
                      {PRESET_AVATARS.map((preset, idx) => (
                        <button
                          key={idx} type="button" onClick={() => setProfilePhoto(preset)}
                          className={`h-9 w-9 rounded-xl border transition-all flex items-center justify-center bg-white ${profilePhoto === preset ? 'border-indigo-650 scale-[1.05]' : 'border-slate-200 hover:scale-[1.02]'}`}
                        >
                          <img src={preset} alt="preset" className="h-7 w-7 object-contain" />
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                {role === 'FARMER' && (
                  <>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">{t('name')}</label>
                      <input type="text" required placeholder="e.g. Rajesh Kumar" value={farmerName} onChange={(e) => setFarmerName(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">{t('phone')}</label>
                      <input type="tel" required placeholder="9876543210" value={farmerPhone} onChange={(e) => setFarmerPhone(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                  </>
                )}

                {role === 'BUYER' && (
                  <>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Business Name</label>
                      <input type="text" required placeholder="e.g. FreshPicks Retail Ltd." value={companyName} onChange={(e) => setCompanyName(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">GST/VAT Number (Optional)</label>
                      <input type="text" placeholder="22AAAAA0000A1Z5" value={gstin} onChange={(e) => setGstin(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                  </>
                )}

                {role === 'COLD_STORAGE_MANAGER' && (
                  <>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Facility Name</label>
                      <input type="text" required placeholder="Kisan Cold Room" value={storageName} onChange={(e) => setStorageName(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Facility Description</label>
                      <textarea required rows={3} placeholder="IoT temperature control..." value={storageDesc} onChange={(e) => setStorageDesc(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm resize-none" />
                    </div>
                  </>
                )}

                {role === 'DELIVERY_PARTNER' && (
                  <>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Full Name</label>
                      <input type="text" required placeholder="e.g. Rahul Shinde" value={deliveryName} onChange={(e) => setDeliveryName(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Mobile Phone (WhatsApp)</label>
                      <input type="tel" required placeholder="98XXXXXXXX" value={deliveryPhone} onChange={(e) => setDeliveryPhone(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Vehicle Type</label>
                      <select value={deliveryVehicleType} onChange={(e) => setDeliveryVehicleType(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white">
                        <option value="BIKE">Motorcycle / Bike (60 kg capacity)</option>
                        <option value="SCOOTER">Electric Scooter (50 kg capacity)</option>
                        <option value="TEMPO">Auto Tempo (500 kg capacity)</option>
                        <option value="MINI_TRUCK">Mini Truck (1.5 Ton capacity)</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Vehicle Plate Number</label>
                      <input type="text" required placeholder="e.g. MH-15-BL-2026" value={deliveryVehicleNumber} onChange={(e) => setDeliveryVehicleNumber(e.target.value.toUpperCase())} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                    </div>
                  </>
                )}

                <button type="submit" className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all mt-4">
                  <span>{t('getStarted')}</span>
                </button>
              </form>
            )}

            {step === 3 && (
              <form onSubmit={handleRegisterSubmit} className="space-y-5 animate-fade-in">
                <button type="button" onClick={() => setStep(2)} className="flex items-center text-[10px] font-bold text-slate-400 hover:text-slate-800 uppercase tracking-wider mb-2">
                  <ArrowLeft className="h-3 w-3 mr-1" /> {t('back')}
                </button>

                <h2 className="text-2xl font-black text-slate-800 tracking-tight mb-4">
                  {role === 'FARMER' ? t('step3Farmer') : role === 'BUYER' ? t('step3Buyer') : t('step3Storage')}
                </h2>

                {role === 'FARMER' && (
                  <>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-750 mb-1.5">{t('state')}</label>
                        <select 
                          value={farmerState} 
                          onChange={(e) => setFarmerState(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          <option value="Maharashtra">Maharashtra</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-755 mb-1.5">{t('district')}</label>
                        <select 
                          value={farmerDistrict} 
                          onChange={(e) => setFarmerDistrict(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          {MAHARASHTRA_DISTRICTS.map((dist) => (
                            <option key={dist} value={dist}>{dist}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-760 mb-1.5">{t('farmSize')}</label>
                        <input type="number" step="0.1" required placeholder="12.5" value={farmSize} onChange={(e) => setFarmSize(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-765 mb-1.5">{t('upiId')}</label>
                        <input type="text" required placeholder="name@upi" value={upiId} onChange={(e) => setUpiId(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                    </div>
                  </>
                )}

                {role === 'BUYER' && (
                  <>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-770 mb-1.5">{t('contactPerson')}</label>
                        <input type="text" required placeholder="Full Name" value={buyerName} onChange={(e) => setBuyerName(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-775 mb-1.5">{t('phone')}</label>
                        <input type="tel" required placeholder="9988776655" value={buyerPhone} onChange={(e) => setBuyerPhone(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-780 mb-1.5">{t('state')}</label>
                        <select 
                          value={buyerState} 
                          onChange={(e) => setBuyerState(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          <option value="Maharashtra">Maharashtra</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-785 mb-1.5">{t('district')}</label>
                        <select 
                          value={buyerDistrict} 
                          onChange={(e) => setBuyerDistrict(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          {MAHARASHTRA_DISTRICTS.map((dist) => (
                            <option key={dist} value={dist}>{dist}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-790 mb-1.5">{t('streetAddress')}</label>
                      <textarea 
                        required 
                        rows={2} 
                        placeholder="e.g. Shop 24, Market Yard" 
                        value={buyerAddressDetails} 
                        onChange={(e) => setBuyerAddressDetails(e.target.value)} 
                        className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm resize-none focus:outline-none focus:border-[#143d2c]" 
                      />
                    </div>
                  </>
                )}

                {role === 'COLD_STORAGE_MANAGER' && (
                  <>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-795 mb-1.5">{t('state')}</label>
                        <select 
                          value={storageState} 
                          onChange={(e) => setStorageState(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          <option value="Maharashtra">Maharashtra</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-800 mb-1.5">{t('district')}</label>
                        <select 
                          value={storageDistrict} 
                          onChange={(e) => setStorageDistrict(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          {MAHARASHTRA_DISTRICTS.map((dist) => (
                            <option key={dist} value={dist}>{dist}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-805 mb-1.5">{t('streetAddressStorage')}</label>
                      <input 
                        type="text" 
                        required 
                        placeholder="e.g. MIDC Industrial Area, Block C-12" 
                        value={storageAddressDetails} 
                        onChange={(e) => setStorageAddressDetails(e.target.value)} 
                        className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]" 
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-810 mb-1.5">{t('storageCapacityLabel')}</label>
                        <input type="number" required placeholder="500" value={storageCapacity} onChange={(e) => setStorageCapacity(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-815 mb-1.5">{t('storageRateLabel')}</label>
                        <input type="number" required placeholder="15" value={storageRate} onChange={(e) => setStorageRate(e.target.value)} className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm" />
                      </div>
                    </div>
                  </>
                )}

                {role === 'DELIVERY_PARTNER' && (
                  <>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Operating District</label>
                        <select 
                          value={deliveryDistrict} 
                          onChange={(e) => setDeliveryDistrict(e.target.value)}
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                        >
                          {MAHARASHTRA_DISTRICTS.map((dist) => (
                            <option key={dist} value={dist}>{dist}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-700 mb-1.5">Driving License (DL) Number</label>
                        <input 
                          type="text" 
                          required 
                          placeholder="e.g. MH15202400987" 
                          value={deliveryLicenseNumber} 
                          onChange={(e) => setDeliveryLicenseNumber(e.target.value.toUpperCase())} 
                          className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]" 
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-[10px] font-bold text-slate-700 mb-1.5">UPI ID for Daily Instant Settlement</label>
                      <input 
                        type="text" 
                        required 
                        placeholder="e.g. rahul@okaxis" 
                        value={deliveryUpi} 
                        onChange={(e) => setDeliveryUpi(e.target.value)} 
                        className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]" 
                      />
                      <span className="text-[10px] text-slate-400 mt-1 block">Trip earnings are sent directly to this UPI address every evening.</span>
                    </div>

                    <div className="bg-emerald-50 border border-emerald-200 p-3 rounded-xl text-[11px] text-emerald-800">
                      💡 <strong>Tip:</strong> Prefer a faster 15-minute quick-commerce onboarding? <Link to="/apply-delivery" className="font-bold underline">Use the Express Fleet Application page →</Link>
                    </div>
                  </>
                )}

                <button type="submit" disabled={loading} className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all mt-4">
                  <span>{loading ? t('loading') : t('registerProfile')}</span>
                </button>
              </form>
            )}

            {step === 1 && (
              <p className="text-center text-xs text-slate-600 font-bold mt-8">
                Already a member? <Link to={`/login?role=${role}`} className="text-slate-900 hover:underline">Log in</Link>
              </p>
            )}
          </div>
        </div>
      </main>

      <div className="bg-slate-100/50 py-6 px-8 border-t border-slate-100 flex flex-col md:flex-row justify-between items-center text-xs font-bold text-slate-500">
         <Link to="/" className="inline-flex items-center mb-4 md:mb-0" title="Return to Mandi+ Home">
           <img src="/images/logo.png" alt="Mandi+" className="h-9 w-auto object-contain rounded-lg bg-white p-1 border border-slate-200 shadow-sm" />
         </Link>
         <div className="space-x-6 mb-4 md:mb-0">
           <Link to="#" className="hover:text-slate-800">Privacy Policy</Link>
           <Link to="#" className="hover:text-slate-800">Terms of Service</Link>
           <Link to="#" className="hover:text-slate-800">Support</Link>
           <Link to="#" className="hover:text-slate-800">Contact Us</Link>
         </div>
         <span>© 2026 Mandi+. Empowering Indian Agriculture.</span>
      </div>
    </div>
  );
}
