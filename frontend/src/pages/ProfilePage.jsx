import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import api from '../services/api';
import LiveDeliveryMapModal from '../components/LiveDeliveryMapModal';
import {
  User, Phone, MapPin, CreditCard, Warehouse, Leaf, AlertCircle,
  Camera, Check, ChevronRight, ShoppingBag, Banknote, MessageCircle,
  Sun, Moon, Eye, EyeOff, BookOpen, Heart, FileText, Gift, Award,
  Shield, Share2, Info, Bell, Settings, ArrowLeft, Sparkles, Plus,
  Trash2, Crosshair, X, Download, HelpCircle, HeartHandshake, CheckCircle2,
  ExternalLink, Copy, Key
} from 'lucide-react';

const PRESET_AVATARS = [
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23dcfce7'/><text x='50' y='60' font-size='40' text-anchor='middle'>🌱</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23fef3c7'/><text x='50' y='60' font-size='40' text-anchor='middle'>🌾</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23dbeafe'/><text x='50' y='60' font-size='40' text-anchor='middle'>🚜</text></svg>",
  "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><circle cx='50' cy='50' r='50' fill='%23e0e7ff'/><text x='50' y='60' font-size='40' text-anchor='middle'>🍎</text></svg>"
];

export default function ProfilePage() {
  const { user, updateUserContext } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  // Basic Profile details from backend
  const [profilePhoto, setProfilePhoto] = useState('');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState(user?.role === 'ADMIN' ? '9012345678' : (user?.phone || ''));
  const [state, setState] = useState('Maharashtra');
  const [district, setDistrict] = useState('Nashik');
  const [farmSize, setFarmSize] = useState('');
  const [upiId, setUpiId] = useState(user?.role === 'ADMIN' ? '9012345678@upi' : (user?.phone ? `${user.phone}@upi` : ''));

  // Buyer Specific
  const [companyName, setCompanyName] = useState('Mandi Commercial Enterprises');
  const [address, setAddress] = useState('Sant Savata Mali Nagar, Nashik, Maharashtra 422003');
  const [gstin, setGstin] = useState('27AABCM9124L1Z8');

  // Storage Specific
  const [storageName, setStorageName] = useState('');
  const [storageLocation, setStorageLocation] = useState('');
  const [storageCapacity, setStorageCapacity] = useState('');
  const [storageRate, setStorageRate] = useState('');
  const [storageDescription, setStorageDescription] = useState('');

  // UI Interactive Controls (Blinkit Experience)
  const [appearance, setAppearance] = useState('LIGHT');
  const [hideSensitive, setHideSensitive] = useState(false);
  const [birthday, setBirthday] = useState('14 Aug 1998');
  const [walletBalance, setWalletBalance] = useState(1250.00);
  const [activeModal, setActiveModal] = useState(null); // 'address_book' | 'wallet' | 'gst' | 'birthday' | 'recipes' | 'rewards' | 'impact' | 'beta' | 'edit_credentials'
  const [showMapPicker, setShowMapPicker] = useState(false);
  const [copiedLink, setCopiedLink] = useState(false);

  // Status / Feedback
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Dispute / Complaints
  const [complaints, setComplaints] = useState([]);
  const [compTitle, setCompTitle] = useState('');
  const [compDesc, setCompDesc] = useState('');
  const [compMessage, setCompMessage] = useState('');
  const [compError, setCompError] = useState('');
  const [compLoading, setCompLoading] = useState(false);

  // Saved Addresses (matching Screenshot 2)
  const [savedAddresses, setSavedAddresses] = useState([
    {
      id: 'hostel',
      name: 'Hostel',
      tag: "You're here",
      full: "2R7F+GWG, Matru Darshan Society, Amrutdham, Nashik, Maharashtra 422003, India, Amrutdham, Nashik",
      phone: user?.role === 'ADMIN' ? "9012345678" : (user?.phone || "9823012345"),
      lastOrdered: "Last ordered 2 months ago",
      lat: 20.0112,
      lng: 73.7902,
      isCurrent: true
    },
    {
      id: 'home',
      name: 'Home',
      tag: "500.53 m",
      full: "23, Hanuman Nagar, Nashik",
      phone: user?.role === 'ADMIN' ? "9012345678" : (user?.phone || "9823012345"),
      lat: 20.0150,
      lng: 73.7950,
      isCurrent: false
    },
    {
      id: 'mandi_hub',
      name: 'APMC Hub Warehouse',
      tag: "Express Dispatch",
      full: "Commercial Bay 4, Nashik APMC Main Terminal Yard, Panchavati, Nashik 422001",
      phone: "9823012345",
      lat: 20.0185,
      lng: 73.8055,
      isCurrent: false
    }
  ]);

  useEffect(() => {
    const loadProfileData = async () => {
      try {
        const response = await api.get('/auth/profile');
        const data = response.data;
        if (data.profilePhoto) setProfilePhoto(data.profilePhoto);
        if (data.name) setName(data.name);
        if (data.phone) {
          setPhone(data.phone);
        } else if (data.role === 'ADMIN' || user?.role === 'ADMIN') {
          setPhone('9012345678');
        }
        if (data.state) setState(data.state);
        if (data.district) setDistrict(data.district);
        if (data.farmSizeAcres) setFarmSize(data.farmSizeAcres);
        if (data.upiId) setUpiId(data.upiId);

        if (data.companyName) setCompanyName(data.companyName);
        if (data.address) setAddress(data.address);
        if (data.gstin) setGstin(data.gstin);

        if (data.storageName) setStorageName(data.storageName);
        if (data.storageLocation) setStorageLocation(data.storageLocation);
        if (data.storageCapacityTons) setStorageCapacity(data.storageCapacityTons);
        if (data.storagePricePerTonDay) setStorageRate(data.storagePricePerTonDay);
        if (data.storageDescription) setStorageDescription(data.storageDescription);

        if (data.role !== 'ADMIN') {
          const compRes = await api.get('/complaints');
          setComplaints(compRes.data || []);
        }
      } catch (err) {
        console.warn("Notice: Loaded mock/stored profile parameters:", err);
      }
    };
    loadProfileData();
  }, []);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.src = event.target.result;
        img.onload = () => {
          const canvas = document.createElement('canvas');
          const MAX_WIDTH = 150;
          const MAX_HEIGHT = 150;
          let width = img.width;
          let height = img.height;
          if (width > height) {
            if (width > MAX_WIDTH) {
              height *= MAX_WIDTH / width;
              width = MAX_WIDTH;
            }
          } else {
            if (height > MAX_HEIGHT) {
              width *= MAX_HEIGHT / height;
              height = MAX_HEIGHT;
            }
          }
          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, width, height);
          const dataUrl = canvas.toDataURL('image/jpeg', 0.7);
          setProfilePhoto(dataUrl);
        };
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSaveProfile = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    const payload = {
      profilePhoto,
      name,
      phone,
      state,
      district,
      farmSizeAcres: parseFloat(farmSize) || 0,
      upiId,
      companyName,
      address,
      gstin,
      storageName,
      storageLocation,
      storageCapacityTons: parseFloat(storageCapacity) || 0,
      storagePricePerTonDay: parseFloat(storageRate) || 0,
      storageDescription
    };

    try {
      await api.put('/auth/profile', payload);
      setMessage("Profile parameters successfully saved.");
      updateUserContext({ profilePhoto });
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setError(err.response?.data || "Failed to update profile details.");
    } finally {
      setLoading(false);
    }
  };

  const handleAddAddressFromMap = ({ address: newAddr, city: c, area: a, lat, lng }) => {
    const newEntry = {
      id: 'addr_' + Date.now(),
      name: a || c || 'Pinned Location',
      tag: 'New Pin',
      full: newAddr,
      phone: phone || (user?.role === 'ADMIN' ? '9012345678' : (user?.phone || '')),
      lat,
      lng,
      isCurrent: false
    };
    setSavedAddresses(prev => [newEntry, ...prev]);
    setAddress(newAddr);
    setShowMapPicker(false);
  };

  const handleCopyShareLink = () => {
    navigator.clipboard.writeText(window.location.origin);
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 2500);
  };

  // Navigate to appropriate orders tab based on role
  const handleGoToOrders = () => {
    switch (user?.role) {
      case 'BUYER':
        navigate('/buyer/orders');
        break;
      case 'FARMER':
        navigate('/farmer/orders');
        break;
      case 'DELIVERY_PARTNER':
        navigate('/delivery/tasks');
        break;
      case 'COLD_STORAGE_MANAGER':
        navigate('/storage/requests');
        break;
      case 'ADMIN':
        navigate('/admin/products');
        break;
      default:
        navigate('/buyer');
    }
  };

  return (
    <div className="flex bg-[#fafbfb] min-h-screen font-sans">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <DashboardHeader title="Your Account" />

        <main className="p-4 sm:p-6 lg:p-8 max-w-3xl w-full mx-auto space-y-5 text-left">

          {/* ══════════════════════════════════════════════════════════════════
              1. ACCOUNT HEADER CARD (Matching Screenshots 3 & 4)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="bg-gradient-to-b from-[#fefce8] via-[#fef9c3]/30 to-white rounded-3xl border border-amber-100 shadow-sm overflow-hidden p-6 relative">
            
            {/* Top Bar with Back Arrow */}
            <div className="flex items-center justify-between mb-4">
              <button
                onClick={() => navigate(-1)}
                className="h-10 w-10 rounded-full bg-white shadow-xs border border-slate-150 flex items-center justify-center text-slate-700 hover:bg-slate-50 transition-colors"
                title="Go back"
              >
                <ArrowLeft className="h-5 w-5" />
              </button>
              <span className="text-xs font-black uppercase tracking-wider text-slate-400">
                Mandi+ Account ID #{user?.id || '4821'}
              </span>
            </div>

            {/* User Big Avatar & Info */}
            <div className="flex flex-col items-center text-center space-y-3">
              <div className="relative group">
                {profilePhoto ? (
                  <img
                    src={profilePhoto}
                    alt="Profile"
                    className="h-24 w-24 rounded-full object-cover border-4 border-white shadow-xl ring-2 ring-amber-200"
                  />
                ) : (
                  <div className="h-24 w-24 rounded-full bg-gradient-to-tr from-slate-200 to-slate-100 border-4 border-white shadow-xl flex items-center justify-center text-slate-500 ring-2 ring-amber-200">
                    <User className="h-12 w-12" />
                  </div>
                )}
                <label className="absolute bottom-0 right-0 bg-slate-900 hover:bg-slate-800 text-white p-2 rounded-full cursor-pointer shadow-lg border-2 border-white transition-transform active:scale-90">
                  <Camera className="h-3.5 w-3.5" />
                  <input type="file" accept="image/*" className="hidden" onChange={handleImageChange} />
                </label>
              </div>

              <div>
                <h2 className="text-2xl font-black text-slate-900 tracking-tight">Your account</h2>
                <div className="flex items-center justify-center space-x-2 mt-1">
                  <span className="text-sm font-bold text-slate-600">{phone || user?.email?.split('@')[0]}</span>
                  <span className="text-[10px] font-black uppercase bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full">
                    {user?.role?.replace(/_/g, ' ')}
                  </span>
                </div>
              </div>
            </div>

            {/* Festive Birthday Banner (Screenshot 3) */}
            <div
              onClick={() => setActiveModal('birthday')}
              className="mt-6 bg-gradient-to-r from-amber-50 via-orange-50/70 to-yellow-50 rounded-2xl p-4 border border-amber-200/70 flex items-center justify-between cursor-pointer hover:shadow-md transition-all group"
            >
              <div className="space-y-0.5">
                <span className="font-black text-slate-850 text-sm block">Add your birthday</span>
                <span className="text-xs font-bold text-emerald-700 flex items-center space-x-1 group-hover:underline">
                  <span>Enter details</span>
                  <ChevronRight className="h-3.5 w-3.5 group-hover:translate-x-0.5 transition-transform" />
                </span>
              </div>
              <div className="text-3xl filter drop-shadow-sm animate-bounce select-none">
                🎂
              </div>
            </div>

            {/* 3 Quick Action Cards (Orders, Blinkit Money, Support - Screenshot 3) */}
            <div className="grid grid-cols-3 gap-3 mt-4">
              <button
                type="button"
                onClick={handleGoToOrders}
                className="bg-white rounded-2xl p-4 border border-slate-150 shadow-xs hover:border-slate-300 hover:shadow-md transition-all text-center flex flex-col items-center justify-center space-y-2 group active:scale-95"
              >
                <div className="h-10 w-10 rounded-2xl bg-slate-50 border border-slate-150 flex items-center justify-center text-slate-700 group-hover:scale-110 transition-transform">
                  <ShoppingBag className="h-5 w-5" />
                </div>
                <span className="text-xs font-black text-slate-800">Your orders</span>
              </button>

              <button
                type="button"
                onClick={() => setActiveModal('wallet')}
                className="bg-white rounded-2xl p-4 border border-slate-150 shadow-xs hover:border-slate-300 hover:shadow-md transition-all text-center flex flex-col items-center justify-center space-y-2 group active:scale-95"
              >
                <div className="h-10 w-10 rounded-2xl bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-600 group-hover:scale-110 transition-transform">
                  <Banknote className="h-5 w-5" />
                </div>
                <div>
                  <span className="text-xs font-black text-slate-800 block">Mandi+ Money</span>
                  <span className="text-[10px] font-black text-emerald-700 block">₹{walletBalance.toFixed(0)}</span>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setActiveModal('support')}
                className="bg-white rounded-2xl p-4 border border-slate-150 shadow-xs hover:border-slate-300 hover:shadow-md transition-all text-center flex flex-col items-center justify-center space-y-2 group active:scale-95"
              >
                <div className="h-10 w-10 rounded-2xl bg-sky-50 border border-sky-200 flex items-center justify-center text-sky-600 group-hover:scale-110 transition-transform">
                  <MessageCircle className="h-5 w-5" />
                </div>
                <span className="text-xs font-black text-slate-800">Need help?</span>
              </button>
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              2. APPEARANCE & HIDE SENSITIVE ITEMS (Screenshot 3)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="bg-white rounded-2xl border border-slate-150 divide-y divide-slate-100 shadow-xs overflow-hidden">
            
            {/* Appearance selector */}
            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <Sun className="h-5 w-5 text-amber-500" />
                <span className="font-extrabold text-sm text-slate-850">Appearance</span>
              </div>
              <button
                type="button"
                onClick={() => setAppearance(appearance === 'LIGHT' ? 'DARK' : 'LIGHT')}
                className="text-xs font-black text-slate-700 bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-xl flex items-center space-x-1.5 transition-colors"
              >
                <span>{appearance}</span>
                <ChevronRight className="h-3.5 w-3.5 rotate-90" />
              </button>
            </div>

            {/* Hide sensitive items toggle */}
            <div className="p-4 flex items-start justify-between gap-4">
              <div className="flex items-start space-x-3 min-w-0">
                <div className="h-8 w-8 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0 mt-0.5">
                  <EyeOff className="h-4 w-4" />
                </div>
                <div className="min-w-0">
                  <span className="font-extrabold text-sm text-slate-850 block">Hide sensitive items</span>
                  <p className="text-[11px] text-slate-400 font-medium leading-relaxed">
                    Sexual wellness, nicotine products and other sensitive items will be hidden
                  </p>
                  <button
                    type="button"
                    onClick={() => alert("When active, all age-restricted farm items and nicotine products are strictly filtered from your catalog.")}
                    className="text-[11px] font-bold text-emerald-700 underline mt-0.5 block"
                  >
                    Know more
                  </button>
                </div>
              </div>

              {/* Interactive Switch */}
              <button
                type="button"
                onClick={() => setHideSensitive(!hideSensitive)}
                className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                  hideSensitive ? 'bg-emerald-600' : 'bg-slate-200'
                }`}
              >
                <span
                  className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                    hideSensitive ? 'translate-x-5' : 'translate-x-0'
                  }`}
                />
              </button>
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              3. SECTION: YOUR INFORMATION (Screenshot 3)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="space-y-2">
            <span className="text-xs font-black uppercase tracking-wider text-slate-400 block px-2">
              Your information
            </span>

            <div className="bg-white rounded-2xl border border-slate-150 divide-y divide-slate-100 shadow-xs overflow-hidden">
              {[
                { id: 'address_book', label: 'Address book', icon: BookOpen, action: () => setActiveModal('address_book') },
                { id: 'recipes', label: 'Bookmarked recipes', icon: Leaf, action: () => setActiveModal('recipes') },
                { id: 'wishlist', label: 'Your wishlist', icon: Heart, action: () => navigate('/buyer/favorites') },
                { id: 'gst', label: 'GST details', icon: FileText, action: () => setActiveModal('gst') },
                { id: 'gift_cards', label: 'E-gift cards', icon: Gift, action: () => setActiveModal('gift_cards') },
                { id: 'prescriptions', label: 'Your prescriptions / APMC Permits', icon: FileText, action: () => setActiveModal('prescriptions') },
              ].map(item => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={item.action}
                    className="w-full p-4 flex items-center justify-between text-left hover:bg-slate-50 transition-colors group"
                  >
                    <div className="flex items-center space-x-3.5">
                      <Icon className="h-4.5 w-4.5 text-slate-500 group-hover:text-emerald-700 transition-colors" />
                      <span className="font-extrabold text-sm text-slate-800 group-hover:text-emerald-800 transition-colors">
                        {item.label}
                      </span>
                    </div>
                    <ChevronRight className="h-4 w-4 text-slate-400 group-hover:text-emerald-700 group-hover:translate-x-0.5 transition-all" />
                  </button>
                );
              })}
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              4. SECTION: PAYMENT AND COUPONS (Screenshot 4)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="space-y-2">
            <span className="text-xs font-black uppercase tracking-wider text-slate-400 block px-2">
              Payment and coupons
            </span>

            <div className="bg-white rounded-2xl border border-slate-150 divide-y divide-slate-100 shadow-xs overflow-hidden">
              {[
                { id: 'payment_settings', label: 'Payment settings', icon: CreditCard, action: () => setActiveModal('payment_settings') },
                { id: 'mandi_money', label: 'Mandi+ Money', icon: Banknote, action: () => setActiveModal('wallet') },
                { id: 'claim_gift', label: 'Claim Gift card', icon: Gift, action: () => setActiveModal('gift_cards') },
                { id: 'rewards', label: 'Your collected rewards', icon: Award, action: () => setActiveModal('rewards') },
              ].map(item => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={item.action}
                    className="w-full p-4 flex items-center justify-between text-left hover:bg-slate-50 transition-colors group"
                  >
                    <div className="flex items-center space-x-3.5">
                      <Icon className="h-4.5 w-4.5 text-slate-500 group-hover:text-emerald-700 transition-colors" />
                      <span className="font-extrabold text-sm text-slate-800 group-hover:text-emerald-800 transition-colors">
                        {item.label}
                      </span>
                    </div>
                    <ChevronRight className="h-4 w-4 text-slate-400 group-hover:text-emerald-700 group-hover:translate-x-0.5 transition-all" />
                  </button>
                );
              })}
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              5. SECTION: ETERNAL FOUNDATION / MANDI IMPACT (Screenshot 4)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="space-y-2">
            <span className="text-xs font-black uppercase tracking-wider text-slate-400 block px-2">
              Eternal Foundation & Direct Farmer Impact
            </span>

            <div className="bg-white rounded-2xl border border-slate-150 divide-y divide-slate-100 shadow-xs overflow-hidden">
              <button
                type="button"
                onClick={() => setActiveModal('impact')}
                className="w-full p-4 flex items-center justify-between text-left hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center space-x-3.5">
                  <HeartHandshake className="h-4.5 w-4.5 text-slate-500 group-hover:text-emerald-700 transition-colors" />
                  <span className="font-extrabold text-sm text-slate-800 group-hover:text-emerald-800 transition-colors">
                    Direct Farmer Support impact
                  </span>
                </div>
                <ChevronRight className="h-4 w-4 text-slate-400 group-hover:text-emerald-700 group-hover:translate-x-0.5 transition-all" />
              </button>

              <button
                type="button"
                onClick={() => alert("Your 80G Tax-exempt donation receipt (FY 2026-27) has been downloaded.")}
                className="w-full p-4 flex items-center justify-between text-left hover:bg-slate-50 transition-colors group"
              >
                <div className="flex items-center space-x-3.5">
                  <FileText className="h-4.5 w-4.5 text-slate-500 group-hover:text-emerald-700 transition-colors" />
                  <span className="font-extrabold text-sm text-slate-800 group-hover:text-emerald-800 transition-colors">
                    Get donation receipt
                  </span>
                </div>
                <ChevronRight className="h-4 w-4 text-slate-400 group-hover:text-emerald-700 group-hover:translate-x-0.5 transition-all" />
              </button>
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              6. SECTION: OTHER INFORMATION (Screenshot 4)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="space-y-2">
            <span className="text-xs font-black uppercase tracking-wider text-slate-400 block px-2">
              Other Information
            </span>

            <div className="bg-white rounded-2xl border border-slate-150 divide-y divide-slate-100 shadow-xs overflow-hidden">
              {[
                { id: 'share', label: 'Share the app', icon: Share2, action: handleCopyShareLink },
                { id: 'about', label: 'About us', icon: Info, action: () => setActiveModal('about') },
                { id: 'sell', label: 'Sell on Mandi+', icon: ShoppingBag, action: () => navigate('/farmer/add-product') },
                { id: 'privacy', label: 'Account privacy & Escrow policy', icon: Shield, action: () => setActiveModal('privacy') },
                { id: 'notifs', label: 'Notification preferences', icon: Bell, action: () => alert('Notification preferences: SMS, WhatsApp, and App push alerts are enabled.') },
                { id: 'beta', label: 'Manage beta program (15-Min Express Delivery)', icon: Sparkles, action: () => setActiveModal('beta') },
              ].map(item => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={item.action}
                    className="w-full p-4 flex items-center justify-between text-left hover:bg-slate-50 transition-colors group"
                  >
                    <div className="flex items-center space-x-3.5">
                      <Icon className="h-4.5 w-4.5 text-slate-500 group-hover:text-emerald-700 transition-colors" />
                      <span className="font-extrabold text-sm text-slate-800 group-hover:text-emerald-800 transition-colors">
                        {item.label}
                      </span>
                    </div>
                    {item.id === 'share' && copiedLink ? (
                      <span className="text-xs font-bold text-emerald-600">Copied!</span>
                    ) : (
                      <ChevronRight className="h-4 w-4 text-slate-400 group-hover:text-emerald-700 group-hover:translate-x-0.5 transition-all" />
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════════
              7. EDIT USER CREDENTIALS & PARAMETERS FORM (All Roles)
             ══════════════════════════════════════════════════════════════════ */}
          <div className="bg-white rounded-3xl border border-slate-150 p-6 shadow-xs space-y-5 text-left">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
              <div>
                <h3 className="text-base font-black text-slate-900 tracking-tight">Edit Profile Parameters</h3>
                <p className="text-xs text-slate-400 font-medium">Update phone, farm or business registry details</p>
              </div>
              <span className="bg-slate-100 text-slate-700 text-[10px] font-black uppercase px-2.5 py-1 rounded-full">
                {user?.role}
              </span>
            </div>

            {message && (
              <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-3.5 rounded-xl text-xs font-bold">
                ✓ {message}
              </div>
            )}

            {error && (
              <div className="bg-rose-50 border border-rose-200 text-rose-800 p-3.5 rounded-xl text-xs font-bold flex items-center space-x-2">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSaveProfile} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Full Legal Name</label>
                  <input
                    type="text"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    placeholder="Legal name"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Contact Phone</label>
                  <input
                    type="tel"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    placeholder={user?.role === 'ADMIN' ? '9012345678' : 'Enter mobile number'}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">State</label>
                  <input
                    type="text"
                    value={state}
                    onChange={e => setState(e.target.value)}
                    placeholder="Maharashtra"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">District</label>
                  <input
                    type="text"
                    value={district}
                    onChange={e => setDistrict(e.target.value)}
                    placeholder="Nashik"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                  />
                </div>
              </div>

              {/* Role specific inputs */}
              {user?.role === 'FARMER' && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-100">
                  <div>
                    <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Farm Land Size (Acres)</label>
                    <input
                      type="number"
                      step="0.1"
                      value={farmSize}
                      onChange={e => setFarmSize(e.target.value)}
                      placeholder="e.g. 5.5"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Settlement UPI ID</label>
                    <input
                      type="text"
                      value={upiId}
                      onChange={e => setUpiId(e.target.value)}
                      placeholder="farmer@upi"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                    />
                  </div>
                </div>
              )}

              {user?.role === 'BUYER' && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-100">
                  <div>
                    <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Company / Trade Name</label>
                    <input
                      type="text"
                      value={companyName}
                      onChange={e => setCompanyName(e.target.value)}
                      placeholder="Trade entity"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block mb-1">GSTIN Number</label>
                    <input
                      type="text"
                      value={gstin}
                      onChange={e => setGstin(e.target.value)}
                      placeholder="27AABCM9124L1Z8"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none"
                    />
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-[#0a2d1e] hover:bg-[#0d3825] active:scale-98 text-white font-black py-3 rounded-xl text-xs transition-all shadow-md shadow-emerald-950/20 disabled:opacity-50"
              >
                {loading ? 'Saving Profile...' : 'Save Profile Changes'}
              </button>
            </form>
          </div>
        </main>
      </div>

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: ADDRESS BOOK (Matching Screenshot 2)
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'address_book' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-lg shadow-2xl border border-slate-150 overflow-hidden flex flex-col max-h-[85vh] animate-step-enter text-left">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-black text-slate-900 text-base">Select delivery location</h3>
              <button onClick={() => setActiveModal(null)} className="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="p-4 overflow-y-auto space-y-4">
              {/* Use current location */}
              <div
                onClick={() => {
                  setAddress("Sant Savata Mali Nagar, Nashik Division, Nashik, Maharashtra, 422003, India");
                  setActiveModal(null);
                }}
                className="p-3.5 bg-emerald-50/50 hover:bg-emerald-50 rounded-2xl border border-emerald-200 cursor-pointer flex items-center justify-between"
              >
                <div className="flex items-center space-x-3">
                  <div className="h-10 w-10 rounded-xl bg-emerald-500 text-white flex items-center justify-center">
                    <Crosshair className="h-5 w-5" />
                  </div>
                  <div>
                    <span className="font-extrabold text-xs text-emerald-800 block">Use current location</span>
                    <span className="text-[11px] text-slate-500 block truncate max-w-[280px]">
                      Sant Savata Mali Nagar, Nashik Division, Nashik, Maharashtra, 422003
                    </span>
                  </div>
                </div>
                <ChevronRight className="h-4 w-4 text-emerald-700" />
              </div>

              {/* Pin On Live Map button */}
              <button
                type="button"
                onClick={() => { setShowMapPicker(true); }}
                className="w-full bg-slate-900 hover:bg-slate-800 text-white p-3.5 rounded-2xl text-xs font-black flex items-center justify-between"
              >
                <div className="flex items-center space-x-2.5">
                  <span>🗺️</span>
                  <span>Pin New Location on Interactive Leaflet Map</span>
                </div>
                <ChevronRight className="h-4 w-4 text-emerald-400" />
              </button>

              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block pt-1">
                Your saved addresses
              </span>

              <div className="space-y-2.5">
                {savedAddresses.map(addr => (
                  <div key={addr.id} className="p-3.5 rounded-2xl border border-slate-150 hover:border-emerald-500 bg-white transition-all space-y-1">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <span className="font-black text-xs text-slate-850">{addr.name}</span>
                        {addr.tag && (
                          <span className={`text-[9px] font-black uppercase px-2 py-0.5 rounded-full ${
                            addr.isCurrent ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'
                          }`}>
                            {addr.tag}
                          </span>
                        )}
                      </div>
                    </div>
                    <p className="text-[11px] text-slate-500 font-medium leading-snug">{addr.full}</p>
                    <p className="text-[10px] text-slate-400 font-semibold">Phone number: {addr.phone}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: MANDIPLUS MONEY WALLET (Screenshot 3)
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'wallet' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl border border-slate-150 overflow-hidden animate-step-enter text-left">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-gradient-to-r from-amber-50 to-yellow-50">
              <div className="flex items-center space-x-2.5">
                <Banknote className="h-5 w-5 text-amber-600" />
                <h3 className="font-black text-slate-900 text-base">Mandi+ Money Wallet</h3>
              </div>
              <button onClick={() => setActiveModal(null)} className="p-1 rounded-lg text-slate-400 hover:bg-white/80">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="p-6 space-y-5">
              <div className="bg-slate-900 text-white rounded-2xl p-5 shadow-xl relative overflow-hidden">
                <span className="text-[10px] font-black uppercase tracking-wider text-amber-400 block">Total Balance</span>
                <span className="text-3xl font-black text-white block mt-1">₹{walletBalance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                <span className="text-[10px] text-emerald-400 font-bold block mt-1">✓ Instant Escrow Refunds Enabled</span>
              </div>

              <div className="space-y-2">
                <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block">Quick Top-Up</span>
                <div className="grid grid-cols-3 gap-2">
                  {[500, 1000, 2500].map(amt => (
                    <button
                      key={amt}
                      type="button"
                      onClick={() => {
                        setWalletBalance(prev => prev + amt);
                        alert(`₹${amt} added to Mandi+ Money wallet successfully!`);
                      }}
                      className="p-2.5 rounded-xl border border-slate-200 text-xs font-black text-slate-800 hover:border-emerald-600 hover:bg-emerald-50 transition-all text-center"
                    >
                      +₹{amt}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-2 pt-2 border-t border-slate-100">
                <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block">Wallet Perks</span>
                <ul className="text-xs text-slate-600 space-y-1.5 font-medium">
                  <li>• Zero checkout payment failure</li>
                  <li>• 1-click order confirmation for 15-min deliveries</li>
                  <li>• Escrow protected by Razorpay & APMC Board</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: GST DETAILS (Screenshot 3 & 4)
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'gst' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl border border-slate-150 overflow-hidden animate-step-enter text-left">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-black text-slate-900 text-base">GST Tax Details</h3>
              <button onClick={() => setActiveModal(null)} className="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="p-6 space-y-4">
              <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center space-x-3">
                <CheckCircle2 className="h-6 w-6 text-emerald-600 shrink-0" />
                <div>
                  <span className="font-black text-xs text-emerald-900 block">Verified Commercial APMC Entity</span>
                  <span className="text-[10px] text-emerald-700 block">GSTIN: {gstin || '27AABCM9124L1Z8'}</span>
                </div>
              </div>

              <div className="space-y-3 text-xs">
                <div>
                  <label className="text-[10px] font-bold text-slate-400 uppercase block mb-1">Legal Trade Name</label>
                  <input
                    type="text"
                    value={companyName}
                    onChange={e => setCompanyName(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 font-bold text-slate-800 text-xs"
                  />
                </div>
                <div>
                  <label className="text-[10px] font-bold text-slate-400 uppercase block mb-1">GSTIN</label>
                  <input
                    type="text"
                    value={gstin}
                    onChange={e => setGstin(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 font-bold text-slate-800 text-xs"
                  />
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  alert('GST details updated and verified.');
                  setActiveModal(null);
                }}
                className="w-full bg-[#0a2d1e] text-white font-black py-3 rounded-xl text-xs hover:bg-[#0d3825] transition-all"
              >
                Save GST Information
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: BIRTHDAY CELEBRATION (Screenshot 3)
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'birthday' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-sm shadow-2xl border border-slate-150 overflow-hidden animate-step-enter text-center p-6 space-y-4">
            <div className="text-5xl select-none animate-bounce">🎂</div>
            <h3 className="font-black text-slate-900 text-lg">Your Birthday Treats</h3>
            <p className="text-xs text-slate-500">
              Enter your birth date to receive special Mandi+ harvest reward vouchers & discounts on fresh seasonal fruit boxes!
            </p>
            <input
              type="date"
              defaultValue="1998-08-14"
              onChange={e => setBirthday(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 text-center"
            />
            <button
              type="button"
              onClick={() => {
                alert(`Birthday set to ${birthday}! You've unlocked a ₹150 Mandi+ Birthday voucher.`);
                setActiveModal(null);
              }}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-black py-3 rounded-xl text-xs transition-all shadow-md shadow-emerald-200"
            >
              Save Birthday & Claim Treat
            </button>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: BOOKMARKED RECIPES
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'recipes' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-md shadow-2xl border border-slate-150 overflow-hidden animate-step-enter text-left">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-black text-slate-900 text-base">Farm-to-Table Recipes</h3>
              <button onClick={() => setActiveModal(null)} className="p-1 rounded-lg text-slate-400 hover:bg-slate-100">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="p-4 space-y-3">
              {[
                { name: 'Organic Nashik Misal Pav', time: '25 min', crop: 'Sprouted Matki & Onion', emoji: '🍲' },
                { name: 'Fresh Tomato Rasam', time: '15 min', crop: 'Country Tomatoes & Curry Leaves', emoji: '🍅' },
                { name: 'Crisp Methi Thepla', time: '20 min', crop: 'Fresh Methi Greens', emoji: '🫓' },
              ].map((rec, i) => (
                <div key={i} className="p-3 bg-slate-50 border border-slate-100 rounded-xl flex items-center space-x-3">
                  <span className="text-2xl">{rec.emoji}</span>
                  <div className="min-w-0 flex-1">
                    <span className="font-extrabold text-xs text-slate-800 block truncate">{rec.name}</span>
                    <span className="text-[10px] text-slate-400 block">{rec.crop} · ⏱️ {rec.time}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          MODAL: SUPPORT DESK & 24/7 WHATSAPP
         ══════════════════════════════════════════════════════════════════ */}
      {activeModal === 'support' && (
        <div className="fixed inset-0 z-[500] bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-sm shadow-2xl border border-slate-150 overflow-hidden animate-step-enter text-center p-6 space-y-4">
            <div className="h-12 w-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center mx-auto text-2xl font-bold">
              💬
            </div>
            <h3 className="font-black text-slate-900 text-lg">Mandi+ Help Desk</h3>
            <p className="text-xs text-slate-500 leading-relaxed">
              Have questions about an active 15-minute order, mandi rates, or escrow settlement? Our team is live 24/7.
            </p>
            <div className="space-y-2 pt-2">
              <a
                href="https://wa.me/919012345678"
                target="_blank"
                rel="noreferrer"
                className="w-full bg-[#25d366] hover:bg-[#20ba59] text-white font-black py-3 rounded-xl text-xs flex items-center justify-center space-x-2 transition-all shadow-md shadow-emerald-200"
              >
                <span>Chat on WhatsApp</span>
              </a>
              <button
                type="button"
                onClick={() => navigate('/chat')}
                className="w-full bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-2.5 rounded-xl text-xs transition-colors"
              >
                Open In-App Assistant
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════
          INTERACTIVE LEAFLET MAP PIN SELECTOR (For Profile Address Book)
         ══════════════════════════════════════════════════════════════════ */}
      <LiveDeliveryMapModal
        isOpen={showMapPicker}
        onClose={() => setShowMapPicker(false)}
        mode="picker"
        initialAddress={address}
        onConfirmLocation={handleAddAddressFromMap}
      />
    </div>
  );
}
