import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import ComplaintsDesk from '../components/ComplaintsDesk';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import LiveDeliveryMapModal from '../components/LiveDeliveryMapModal';
import { 
  Bike, Truck, Navigation, MapPin, Clock, Phone, 
  CheckCircle2, AlertCircle, ShieldAlert, IndianRupee, 
  TrendingUp, Award, Zap, Compass, ChevronRight, Check,
  ToggleLeft, ToggleRight, ArrowUpRight, Star, RefreshCw, 
  Send, ShieldCheck, HeartHandshake, Bell, Key, Package, Sparkles
} from 'lucide-react';

export default function DeliveryDashboard({ tab = 'dashboard' }) {
  const { user } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const [partner, setPartner] = useState({
    name: 'Rahul Shinde',
    phone: '9823012345',
    vehicleType: 'SCOOTER',
    vehicleNumber: 'MH-15-BL-2026',
    licenseNumber: 'MH15-DL-88741',
    preferredHub: 'Nashik APMC Main Terminal',
    upiId: 'rahul@okaxis',
    isOnline: true,
    rating: 4.92,
    todayEarnings: 1420.00,
    totalEarnings: 28450.00,
    todayDeliveries: 4,
    totalDeliveries: 76,
    status: 'ACTIVE'
  });

  const [availableTasks, setAvailableTasks] = useState([]);
  const [myTasks, setMyTasks] = useState([]);
  const [activeTask, setActiveTask] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [showDeliveryMapModal, setShowDeliveryMapModal] = useState(false);

  // Delivery OTP Modal
  const [otpModalOpen, setOtpModalOpen] = useState(false);
  const [enteredOtp, setEnteredOtp] = useState('');
  const [otpError, setOtpError] = useState('');

  // Cash Out / Payout Modal
  const [cashoutModalOpen, setCashoutModalOpen] = useState(false);
  const [cashoutAmount, setCashoutAmount] = useState('');
  const [cashoutUpi, setCashoutUpi] = useState('');
  const [cashoutSuccess, setCashoutSuccess] = useState('');
  const [cashoutLoading, setCashoutLoading] = useState(false);

  // SOS Emergency Modal
  const [sosModalOpen, setSosModalOpen] = useState(false);

  // Partner Profile Edit State
  const [editName, setEditName] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const [editVehicleType, setEditVehicleType] = useState('BIKE');
  const [editVehicleNumber, setEditVehicleNumber] = useState('');
  const [editUpiId, setEditUpiId] = useState('');
  const [profileMsg, setProfileMsg] = useState('');

  // Audio chime for incoming orders
  const playAlertChime = () => {
    try {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      if (AudioContextClass) {
        const ctx = new AudioContextClass();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.type = 'triangle';
        const now = ctx.currentTime;
        osc.frequency.setValueAtTime(587.33, now); // D5
        osc.frequency.setValueAtTime(880, now + 0.12); // A5
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.35);
        osc.start(now);
        osc.stop(now + 0.35);
      }
    } catch (e) {
      // Audio fallback silent
    }
  };

  const fetchDeliveryData = async () => {
    try {
      setLoading(true);
      // Fetch partner profile
      const profRes = await api.get('/delivery/profile');
      if (profRes.data) {
        setPartner(profRes.data);
        setEditName(profRes.data.name || '');
        setEditPhone(profRes.data.phone || '');
        setEditVehicleType(profRes.data.vehicleType || 'BIKE');
        setEditVehicleNumber(profRes.data.vehicleNumber || '');
        setEditUpiId(profRes.data.upiId || '');
      }

      // Fetch tasks
      const availRes = await api.get('/delivery/available-tasks');
      setAvailableTasks(availRes.data || []);

      const myRes = await api.get('/delivery/my-tasks');
      const allMy = myRes.data || [];
      setMyTasks(allMy);

      // Find currently active trip (in progress: DELIVERY_ASSIGNED, DELIVERY_ACCEPTED, PICKUP_CONFIRMED, IN_TRANSIT)
      const current = allMy.find(t => ['DELIVERY_ASSIGNED', 'ASSIGNED', 'DELIVERY_ACCEPTED', 'PICKUP_CONFIRMED', 'PICKED_UP', 'REACHED_PICKUP', 'IN_TRANSIT'].includes(t.status));
      setActiveTask(current || null);
    } catch (err) {
      console.warn("Notice: Delivery API running with local simulation context:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDeliveryData();
    const interval = setInterval(fetchDeliveryData, 10000);
    return () => clearInterval(interval);
  }, [tab]);

  // Real GPS Geolocation Watcher: streams real partner coordinates when active delivery is accepted or in transit
  useEffect(() => {
    if (!activeTask || !['DELIVERY_ACCEPTED', 'PICKUP_CONFIRMED', 'IN_TRANSIT'].includes(activeTask.status)) {
      return;
    }
    if (!navigator.geolocation) return;

    const watchId = navigator.geolocation.watchPosition(
      async (pos) => {
        try {
          await api.post(`/delivery/tasks/${activeTask.id}/location`, {
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
            isPermissionGranted: true
          });
        } catch (e) {
          console.warn('GPS location push warning:', e);
        }
      },
      async (err) => {
        console.warn('GPS permission denied or signal unavailable:', err.message);
        try {
          await api.post(`/delivery/tasks/${activeTask.id}/location`, {
            latitude: null,
            longitude: null,
            isPermissionGranted: false
          });
        } catch (e) {}
      },
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 10000 }
    );

    return () => {
      try {
        navigator.geolocation.clearWatch(watchId);
      } catch (e) {}
    };
  }, [activeTask?.id, activeTask?.status]);

  // Online / Offline Toggle
  const handleToggleDuty = async () => {
    try {
      const res = await api.put('/delivery/toggle-duty');
      if (res.data) {
        setPartner(res.data);
        if (res.data.isOnline) {
          playAlertChime();
        }
      }
    } catch (err) {
      setPartner(prev => ({ ...prev, isOnline: !prev.isOnline }));
    }
  };

  // Accept incoming task with explicit confirmation
  const handleAcceptTask = async (taskId) => {
    try {
      setActionLoading(true);
      playAlertChime();
      const res = await api.post(`/delivery/tasks/${taskId}/accept-delivery`);
      setActiveTask(res.data);
      fetchDeliveryData();
    } catch (err) {
      alert("Failed to accept task: " + (err.response?.data || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  // Reject an offered/assigned task
  const handleRejectTask = async (taskId) => {
    if (!window.confirm("Are you sure you want to decline this delivery request? It will be offered to another partner.")) return;
    try {
      setActionLoading(true);
      await api.post(`/delivery/tasks/${taskId}/reject`, { reason: "Partner declined assignment" });
      if (activeTask && activeTask.id === taskId) {
        setActiveTask(null);
      }
      fetchDeliveryData();
    } catch (err) {
      alert("Failed to reject task: " + (err.response?.data || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  // Confirm Produce Pickup at Farmgate
  const handleConfirmPickup = async (taskId) => {
    try {
      setActionLoading(true);
      playAlertChime();
      const res = await api.post(`/delivery/tasks/${taskId}/confirm-pickup`);
      setActiveTask(res.data);
      fetchDeliveryData();
    } catch (err) {
      alert("Failed to confirm pickup: " + (err.response?.data || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  // Start Delivery Transit
  const handleStartTransit = async (taskId) => {
    try {
      setActionLoading(true);
      playAlertChime();
      const res = await api.post(`/delivery/tasks/${taskId}/start-transit`);
      setActiveTask(res.data);
      fetchDeliveryData();
    } catch (err) {
      alert("Failed to start transit: " + (err.response?.data || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  // Transition Active Trip
  const handleAdvanceTrip = async (newStatus) => {
    if (!activeTask) return;

    if (newStatus === 'DELIVERED') {
      setEnteredOtp('');
      setOtpError('');
      setOtpModalOpen(true);
      return;
    }

    try {
      setActionLoading(true);
      playAlertChime();
      const res = await api.put(`/delivery/tasks/${activeTask.id}/status?status=${newStatus}`);
      setActiveTask(res.data);
      fetchDeliveryData();
    } catch (err) {
      alert("Failed to update status: " + (err.response?.data || err.message));
    } finally {
      setActionLoading(false);
    }
  };

  // Confirm Delivery with OTP
  const handleConfirmDeliveryWithOtp = async (e) => {
    e.preventDefault();
    if (!enteredOtp) {
      setOtpError("Please enter the 4-digit verification OTP provided by the buyer.");
      return;
    }

    try {
      setActionLoading(true);
      setOtpError('');
      const res = await api.put(`/delivery/tasks/${activeTask.id}/status?status=DELIVERED&otp=${enteredOtp.trim()}`);
      setActiveTask(null);
      setOtpModalOpen(false);
      playAlertChime();
      fetchDeliveryData();
    } catch (err) {
      setOtpError(err.response?.data || "Invalid OTP code. Please check with customer.");
    } finally {
      setActionLoading(false);
    }
  };

  // Handle Instant UPI Cash Out
  const handleWithdrawPayout = async (e) => {
    e.preventDefault();
    setCashoutLoading(true);
    setCashoutSuccess('');
    try {
      const amount = parseFloat(cashoutAmount);
      if (!amount || amount <= 0) {
        alert("Please enter a valid amount to withdraw.");
        return;
      }
      const res = await api.post('/delivery/withdraw-payout', {
        amount: amount,
        upiId: cashoutUpi || partner.upiId
      });
      setCashoutSuccess(res.data?.message || "₹" + amount + " transferred to your UPI successfully!");
      setCashoutAmount('');
      fetchDeliveryData();
    } catch (err) {
      alert("Withdrawal failed: " + (err.response?.data || err.message));
    } finally {
      setCashoutLoading(false);
    }
  };

  // Save Profile Updates
  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setProfileMsg('');
    try {
      const payload = {
        name: editName,
        phone: editPhone,
        vehicleType: editVehicleType,
        vehicleNumber: editVehicleNumber,
        upiId: editUpiId
      };
      const res = await api.put('/delivery/profile', payload);
      setPartner(res.data);
      setProfileMsg('Fleet Profile coordinates updated successfully!');
    } catch (err) {
      alert('Failed to update profile.');
    }
  };

  return (
    <div className="flex h-screen bg-[#fafbfb] overflow-hidden font-sans">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <DashboardHeader />

        <div className="p-4 sm:p-8 max-w-7xl mx-auto w-full space-y-6">

          {/* Blinkit-Style Top Duty Status Bar */}
          <div className="bg-white rounded-3xl p-5 border border-slate-100 shadow-premium flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className={`h-12 w-12 rounded-2xl flex items-center justify-center font-bold transition-all ${
                  partner.isOnline ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30' : 'bg-slate-200 text-slate-500'
                }`}>
                  <Bike className="h-6 w-6" />
                </div>
                <span className={`absolute -top-1 -right-1 h-4 w-4 rounded-full border-2 border-white ${
                  partner.isOnline ? 'bg-emerald-500 animate-ping' : 'bg-slate-400'
                }`}></span>
                <span className={`absolute -top-1 -right-1 h-4 w-4 rounded-full border-2 border-white ${
                  partner.isOnline ? 'bg-emerald-500' : 'bg-slate-400'
                }`}></span>
              </div>

              <div className="text-left">
                <div className="flex items-center space-x-2">
                  <h2 className="text-lg font-black text-slate-900 tracking-tight">
                    {partner.name}
                  </h2>
                  <span className={`text-[10px] font-black uppercase tracking-wider px-2.5 py-0.5 rounded-full ${
                    partner.isOnline ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-500'
                  }`}>
                    {partner.isOnline ? 'ON DUTY (LIVE)' : 'OFF DUTY (PAUSED)'}
                  </span>
                </div>
                <div className="flex items-center space-x-3 text-xs text-slate-400 mt-0.5">
                  <span className="flex items-center"><MapPin className="h-3 w-3 mr-1 text-emerald-600" /> {partner.preferredHub}</span>
                  <span>•</span>
                  <span className="flex items-center"><Star className="h-3 w-3 mr-1 text-amber-500 fill-amber-500" /> {partner.rating} Rating</span>
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="flex items-center space-x-3 w-full md:w-auto justify-end">
              <button
                onClick={() => setSosModalOpen(true)}
                className="bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold px-4 py-2.5 rounded-xl text-xs flex items-center space-x-1.5 transition-colors border border-rose-200"
                title="Emergency Assistance"
              >
                <ShieldAlert className="h-4 w-4" />
                <span>Emergency SOS</span>
              </button>

              <button
                onClick={handleToggleDuty}
                className={`px-5 py-2.5 rounded-xl font-bold text-xs transition-all flex items-center space-x-2 shadow-sm ${
                  partner.isOnline 
                    ? 'bg-emerald-600 hover:bg-emerald-700 text-white' 
                    : 'bg-slate-800 hover:bg-slate-900 text-white'
                }`}
              >
                {partner.isOnline ? <ToggleRight className="h-4 w-4" /> : <ToggleLeft className="h-4 w-4" />}
                <span>{partner.isOnline ? 'Go Offline' : 'Go Online for Orders'}</span>
              </button>
            </div>
          </div>

          {/* Quick Metrics Bar */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm text-left">
              <div className="flex justify-between items-center text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">
                <span>Today's Earnings</span>
                <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600"><IndianRupee className="h-4 w-4" /></div>
              </div>
              <div className="flex items-baseline space-x-2">
                <span className="text-2xl sm:text-3xl font-black text-slate-900">₹{(partner.todayEarnings || 0).toLocaleString()}</span>
                <span className="text-xs text-emerald-600 font-bold flex items-center">+₹180 bonus</span>
              </div>
              <button 
                onClick={() => { setCashoutAmount(partner.todayEarnings?.toString() || '500'); setCashoutModalOpen(true); }}
                className="mt-3 text-[11px] font-bold text-[#143d2c] hover:underline flex items-center"
              >
                Instant UPI Withdrawal <ArrowUpRight className="h-3 w-3 ml-1" />
              </button>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm text-left">
              <div className="flex justify-between items-center text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">
                <span>Today's Trips</span>
                <div className="p-1.5 rounded-lg bg-sky-50 text-sky-600"><Bike className="h-4 w-4" /></div>
              </div>
              <div className="flex items-baseline space-x-2">
                <span className="text-2xl sm:text-3xl font-black text-slate-900">{partner.todayDeliveries || 0}</span>
                <span className="text-xs text-slate-400 font-semibold">/ 6 target</span>
              </div>
              <span className="mt-3 text-[11px] font-bold text-sky-600 block">
                2 more trips for ₹200 surge bonus
              </span>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm text-left">
              <div className="flex justify-between items-center text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">
                <span>Fleet Rating</span>
                <div className="p-1.5 rounded-lg bg-amber-50 text-amber-600"><Star className="h-4 w-4 fill-amber-500 text-amber-500" /></div>
              </div>
              <div className="flex items-baseline space-x-2">
                <span className="text-2xl sm:text-3xl font-black text-slate-900">{partner.rating || 4.92}</span>
                <span className="text-xs text-slate-400 font-semibold">★ (120+ ratings)</span>
              </div>
              <span className="mt-3 text-[11px] font-bold text-emerald-600 block">
                Top 5% Partner in Nashik
              </span>
            </div>

            <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm text-left">
              <div className="flex justify-between items-center text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">
                <span>On-Time Rate</span>
                <div className="p-1.5 rounded-lg bg-indigo-50 text-indigo-600"><Clock className="h-4 w-4" /></div>
              </div>
              <div className="flex items-baseline space-x-2">
                <span className="text-2xl sm:text-3xl font-black text-slate-900">98.4%</span>
                <span className="text-xs text-emerald-600 font-bold">Excellent</span>
              </div>
              <span className="mt-3 text-[11px] font-bold text-slate-400 block">
                Avg Delivery: 12–14 mins (15 mins target)
              </span>
            </div>
          </div>

          {/* TAB 1: COMMAND CENTER / ACTIVE DELIVERY (Blinkit style active trip stepper) */}
          {tab === 'dashboard' && (
            <div className="space-y-6">

              {/* ACTIVE DELIVERY TRIP CARD */}
              {activeTask ? (
                <div className="bg-gradient-to-br from-[#143d2c] to-slate-900 text-white rounded-3xl p-6 sm:p-8 shadow-2xl relative overflow-hidden text-left animate-fade-in">
                  <div className="absolute top-0 right-0 p-8 opacity-10 pointer-events-none">
                    <Navigation className="h-64 w-64 -mr-16 -mt-16 text-white" />
                  </div>

                  <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-white/10 relative z-10">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="bg-emerald-400 text-slate-950 font-black text-[10px] uppercase tracking-wider px-2.5 py-0.5 rounded-full">
                          ACTIVE TRIP IN PROGRESS
                        </span>
                        <span className="text-xs text-slate-300 font-mono">#{activeTask.taskCode}</span>
                      </div>
                      <h3 className="text-2xl font-black mt-1">
                        Transporting {activeTask.quantityQuintals} qtl {activeTask.cropName} ({activeTask.variety})
                      </h3>
                    </div>

                    <div className="text-right">
                      <span className="text-xs text-slate-400 uppercase font-bold tracking-wider block">Trip Payout</span>
                      <span className="text-2xl sm:text-3xl font-black text-emerald-400">
                        ₹{(activeTask.payoutAmount + (activeTask.surgeBonus || 0)).toFixed(2)}
                      </span>
                    </div>
                  </div>

                  {/* Interactive Blinkit-Style Stepper */}
                  <div className="py-6 relative z-10">
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
                      <div className={`p-3 rounded-2xl border ${
                        ['ASSIGNED', 'REACHED_PICKUP', 'PICKED_UP', 'IN_TRANSIT'].includes(activeTask.status)
                          ? 'bg-white/10 border-emerald-400 text-white' : 'bg-white/5 border-white/10 text-slate-400'
                      }`}>
                        <div className="flex items-center space-x-2 mb-1">
                          <span className="h-5 w-5 rounded-full bg-emerald-500 text-slate-950 font-bold flex items-center justify-center text-[10px]">1</span>
                          <span className="font-bold">Heading to Pickup</span>
                        </div>
                        <span className="text-[10px] text-slate-300 block truncate">{activeTask.pickupLocation.split(',')[0]}</span>
                      </div>

                      <div className={`p-3 rounded-2xl border ${
                        ['REACHED_PICKUP', 'PICKED_UP', 'IN_TRANSIT'].includes(activeTask.status)
                          ? 'bg-white/10 border-emerald-400 text-white' : 'bg-white/5 border-white/10 text-slate-400'
                      }`}>
                        <div className="flex items-center space-x-2 mb-1">
                          <span className="h-5 w-5 rounded-full bg-emerald-500 text-slate-950 font-bold flex items-center justify-center text-[10px]">2</span>
                          <span className="font-bold">Reached Farmer / Farmgate</span>
                        </div>
                        <span className="text-[10px] text-slate-300 block">Inspection & Weighment</span>
                      </div>

                      <div className={`p-3 rounded-2xl border ${
                        ['PICKED_UP', 'IN_TRANSIT'].includes(activeTask.status)
                          ? 'bg-white/10 border-emerald-400 text-white' : 'bg-white/5 border-white/10 text-slate-400'
                      }`}>
                        <div className="flex items-center space-x-2 mb-1">
                          <span className="h-5 w-5 rounded-full bg-emerald-500 text-slate-950 font-bold flex items-center justify-center text-[10px]">3</span>
                          <span className="font-bold">Out for Delivery</span>
                        </div>
                        <span className="text-[10px] text-slate-300 block">{activeTask.distanceKm} km transit</span>
                      </div>

                      <div className={`p-3 rounded-2xl border ${
                        activeTask.status === 'DELIVERED'
                          ? 'bg-white/10 border-emerald-400 text-white' : 'bg-white/5 border-white/10 text-slate-400'
                      }`}>
                        <div className="flex items-center space-x-2 mb-1">
                          <span className="h-5 w-5 rounded-full bg-slate-400 text-slate-950 font-bold flex items-center justify-center text-[10px]">4</span>
                          <span className="font-bold">Drop Off & OTP</span>
                        </div>
                        <span className="text-[10px] text-slate-300 block">Collect delivery code</span>
                      </div>
                    </div>
                  </div>

                  {/* Route & Contact Coordinates */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t border-white/10 relative z-10">
                    <div className="bg-white/5 p-4 rounded-2xl space-y-2">
                      <div className="flex items-center justify-between text-xs text-emerald-400 font-bold uppercase tracking-wider">
                        <span className="flex items-center"><MapPin className="h-3.5 w-3.5 mr-1" /> 1. Pickup Origin (Farmer Farmgate)</span>
                        <a href={`tel:${activeTask.pickupContactPhone}`} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded text-white flex items-center space-x-1">
                          <Phone className="h-3 w-3" />
                          <span>Call Farmer</span>
                        </a>
                      </div>
                      <p className="font-bold text-sm text-white">{activeTask.pickupLocation}</p>
                      <p className="text-xs text-slate-300">Contact: {activeTask.pickupContactName} ({activeTask.pickupContactPhone})</p>
                    </div>

                    <div className="bg-white/5 p-4 rounded-2xl space-y-2">
                      <div className="flex items-center justify-between text-xs text-lime-300 font-bold uppercase tracking-wider">
                        <span className="flex items-center"><Navigation className="h-3.5 w-3.5 mr-1" /> 2. Drop Destination</span>
                        <a href={`tel:${activeTask.dropContactPhone}`} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded text-white flex items-center space-x-1">
                          <Phone className="h-3 w-3" />
                          <span>Call Buyer</span>
                        </a>
                      </div>
                      <p className="font-bold text-sm text-white">{activeTask.dropLocation}</p>
                      <p className="text-xs text-slate-300">Contact: {activeTask.dropContactName} ({activeTask.dropContactPhone})</p>
                    </div>
                  </div>

                  {/* Stepper Advancement Buttons */}
                  <div className="pt-6 flex flex-wrap items-center justify-end gap-3 relative z-10">
                    {/* View Delivery Tracking Map */}
                    <button
                      type="button"
                      onClick={() => setShowDeliveryMapModal(true)}
                      className="bg-slate-900 hover:bg-slate-800 text-white font-black py-3 px-5 rounded-xl text-sm transition-all shadow-lg flex items-center space-x-2 border border-slate-700 active:scale-95"
                    >
                      <span>🗺️ View Live Tracking Map</span>
                    </button>

                    {(activeTask.status === 'DELIVERY_ASSIGNED' || activeTask.status === 'ASSIGNED') && (
                      <>
                        <button
                          onClick={() => handleRejectTask(activeTask.id)}
                          disabled={actionLoading}
                          className="bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-300 font-black py-3 px-5 rounded-xl text-sm transition-all shadow flex items-center space-x-2"
                        >
                          <X className="h-4 w-4" />
                          <span>Reject Delivery</span>
                        </button>
                        <button
                          onClick={() => handleAcceptTask(activeTask.id)}
                          disabled={actionLoading}
                          className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black py-3 px-6 rounded-xl text-sm transition-all shadow-lg flex items-center space-x-2"
                        >
                          <CheckCircle2 className="h-4 w-4" />
                          <span>Accept Delivery Assignment</span>
                        </button>
                      </>
                    )}

                    {activeTask.status === 'DELIVERY_ACCEPTED' && (
                      <button
                        onClick={() => handleConfirmPickup(activeTask.id)}
                        disabled={actionLoading}
                        className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black py-3 px-6 rounded-xl text-sm transition-all shadow-lg flex items-center space-x-2"
                      >
                        <Package className="h-4 w-4" />
                        <span>Confirm Pickup (Loaded at Farmgate)</span>
                      </button>
                    )}

                    {(activeTask.status === 'PICKUP_CONFIRMED' || activeTask.status === 'PICKED_UP' || activeTask.status === 'REACHED_PICKUP') && (
                      <button
                        onClick={() => handleStartTransit(activeTask.id)}
                        disabled={actionLoading}
                        className="bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-black py-3 px-6 rounded-xl text-sm transition-all shadow-lg flex items-center space-x-2"
                      >
                        <Navigation className="h-4 w-4" />
                        <span>Start Delivery Transit</span>
                      </button>
                    )}

                    {activeTask.status === 'IN_TRANSIT' && (
                      <button
                        onClick={() => handleAdvanceTrip('DELIVERED')}
                        disabled={actionLoading}
                        className="bg-emerald-400 hover:bg-emerald-300 text-slate-950 font-black py-3 px-8 rounded-xl text-sm transition-all shadow-xl flex items-center space-x-2 animate-pulse"
                      >
                        <Key className="h-4 w-4" />
                        <span>Complete Delivery (Verify OTP)</span>
                      </button>
                    )}
                  </div>
                </div>
              ) : (
                <div className="bg-emerald-50/50 border border-emerald-100 rounded-3xl p-8 text-center space-y-3">
                  <div className="h-14 w-14 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center mx-auto">
                    <Compass className="h-7 w-7 animate-spin-slow" />
                  </div>
                  <h3 className="text-xl font-bold text-slate-800">
                    {partner.isOnline ? 'Scanning for Direct Farmer & Cold Storage Dispatches...' : 'Duty is Currently Paused'}
                  </h3>
                  <p className="text-xs text-slate-500 max-w-md mx-auto">
                    {partner.isOnline 
                      ? 'You are active on the fleet radar. New crop consignments directly from local farmers and cold storages will chime immediately below.' 
                      : 'Toggle your status to "Go Online" at the top to receive fresh harvest delivery requests.'}
                  </p>
                </div>
              )}

              {/* AVAILABLE DISPATCH ORDERS (Radar Feed like Blinkit) */}
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-100 shadow-premium text-left">
                <div className="flex justify-between items-center mb-6">
                  <div>
                    <div className="flex items-center space-x-2">
                      <h3 className="text-xl font-black text-slate-900 tracking-tight">Available Delivery Jobs Nearby</h3>
                      <span className="bg-emerald-100 text-emerald-800 font-extrabold text-[10px] px-2.5 py-0.5 rounded-full">
                        {availableTasks.length} Live
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 mt-0.5">High payout farmgate harvest batches ready for pickup directly from farmers</p>
                  </div>

                  <button
                    onClick={fetchDeliveryData}
                    className="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-50 transition-colors"
                    title="Refresh Feed"
                  >
                    <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
                  </button>
                </div>

                {availableTasks.length === 0 ? (
                  <div className="py-12 text-center text-slate-400 text-sm">
                    No open dispatches at this moment. New harvest orders chime automatically.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                    {availableTasks.map((t) => (
                      <div
                        key={t.id}
                        className="bg-slate-50/50 hover:bg-white rounded-2xl p-5 border border-slate-200/80 hover:border-emerald-500/50 hover:shadow-lg transition-all flex flex-col justify-between space-y-4 group"
                      >
                        <div className="space-y-3">
                          <div className="flex justify-between items-start">
                            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                              #{t.taskCode}
                            </span>
                            <span className="text-lg font-black text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-xl">
                              ₹{(t.payoutAmount + (t.surgeBonus || 0)).toFixed(2)}
                            </span>
                          </div>

                          <div>
                            <h4 className="font-extrabold text-slate-900 text-base group-hover:text-emerald-700 transition-colors">
                              {t.cropName} ({t.quantityQuintals} Quintals)
                            </h4>
                            <span className="text-xs text-slate-500 font-medium">{t.variety}</span>
                          </div>

                          <div className="space-y-2 text-xs text-slate-600 pt-1">
                            <div className="flex items-start space-x-2">
                              <MapPin className="h-3.5 w-3.5 text-emerald-600 shrink-0 mt-0.5" />
                              <span className="line-clamp-1"><strong>Pickup:</strong> {t.pickupLocation}</span>
                            </div>
                            <div className="flex items-start space-x-2">
                              <Navigation className="h-3.5 w-3.5 text-lime-600 shrink-0 mt-0.5" />
                              <span className="line-clamp-1"><strong>Drop:</strong> {t.dropLocation}</span>
                            </div>
                          </div>

                          <div className="flex items-center space-x-4 text-[11px] text-slate-400 font-semibold pt-1">
                            <span className="flex items-center"><Compass className="h-3 w-3 mr-1" /> {t.distanceKm} km</span>
                            <span className="flex items-center"><Clock className="h-3 w-3 mr-1" /> ~{t.estimatedMinutes} mins</span>
                            {t.surgeBonus > 0 && (
                              <span className="text-emerald-700 font-bold bg-emerald-100 px-1.5 py-0.5 rounded">
                                +₹{t.surgeBonus} Surge
                              </span>
                            )}
                          </div>
                        </div>

                        <div className="flex items-center space-x-2 pt-1">
                          <button
                            type="button"
                            onClick={() => handleRejectTask(t.id)}
                            disabled={actionLoading}
                            className="px-3 py-3 rounded-xl border border-rose-200 text-rose-600 hover:bg-rose-50 text-xs font-bold transition-all"
                            title="Decline this delivery request"
                          >
                            <X className="h-4 w-4" />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleAcceptTask(t.id)}
                            disabled={actionLoading || !!activeTask}
                            className={`flex-1 py-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center space-x-2 shadow-sm ${
                              activeTask 
                                ? 'bg-slate-200 text-slate-400 cursor-not-allowed'
                                : 'bg-[#143d2c] hover:bg-emerald-800 text-white active:scale-95'
                            }`}
                          >
                            <Zap className="h-3.5 w-3.5" />
                            <span>{activeTask ? 'Complete Active Trip First' : 'Accept Delivery'}</span>
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 2: ALL TASKS & TRIP HISTORY */}
          {tab === 'tasks' && (
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-100 shadow-premium text-left space-y-6">
              <div>
                <h3 className="text-xl font-black text-slate-900 tracking-tight">Delivery Trip Ledger</h3>
                <p className="text-xs text-slate-400 mt-0.5">Historical records of all your farm-to-buyer deliveries</p>
              </div>

              {myTasks.length === 0 ? (
                <div className="py-16 text-center text-slate-400">
                  You haven't accepted any delivery trips yet. Accept an order from the Command Center!
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-slate-600">
                    <thead className="bg-slate-50 uppercase text-[10px] text-slate-400 font-bold rounded-xl">
                      <tr>
                        <th className="py-3 px-4 text-left rounded-l-xl">Task Code</th>
                        <th className="py-3 px-4 text-left">Produce</th>
                        <th className="py-3 px-4 text-left">Pickup & Drop</th>
                        <th className="py-3 px-4 text-left">Distance</th>
                        <th className="py-3 px-4 text-left">Payout</th>
                        <th className="py-3 px-4 text-left rounded-r-xl">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {myTasks.map((t) => (
                        <tr key={t.id} className="hover:bg-slate-50/50">
                          <td className="py-4 px-4 font-mono font-bold text-slate-800">#{t.taskCode}</td>
                          <td className="py-4 px-4">
                            <span className="font-bold text-slate-800 block">{t.cropName}</span>
                            <span className="text-[10px] text-slate-400">{t.quantityQuintals} qtl • {t.variety}</span>
                          </td>
                          <td className="py-4 px-4 max-w-xs">
                            <span className="block truncate text-slate-700 font-medium">From: {t.pickupLocation}</span>
                            <span className="block truncate text-slate-500">To: {t.dropLocation}</span>
                          </td>
                          <td className="py-4 px-4 font-semibold">{t.distanceKm} km</td>
                          <td className="py-4 px-4 font-black text-emerald-600 text-sm">
                            ₹{(t.payoutAmount + (t.surgeBonus || 0)).toFixed(2)}
                          </td>
                          <td className="py-4 px-4">
                            <span className={`px-2.5 py-1 rounded-full text-[10px] font-black uppercase ${
                              t.status === 'DELIVERED' 
                                ? 'bg-emerald-100 text-emerald-800'
                                : ['ASSIGNED', 'REACHED_PICKUP', 'PICKED_UP', 'IN_TRANSIT'].includes(t.status)
                                ? 'bg-amber-100 text-amber-800'
                                : 'bg-slate-100 text-slate-600'
                            }`}>
                              {t.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: EARNINGS & DAILY PAYOUTS */}
          {tab === 'earnings' && (
            <div className="space-y-6 text-left">
              <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-100 shadow-premium flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                  <span className="text-xs font-bold uppercase text-slate-400 tracking-wider">Available Wallet Balance</span>
                  <div className="flex items-baseline space-x-3 mt-1">
                    <span className="text-4xl font-black text-slate-900">₹{(partner.todayEarnings || 0).toLocaleString()}</span>
                    <span className="text-xs text-emerald-600 font-bold bg-emerald-50 px-2 py-0.5 rounded-full">
                      Ready for Instant IMPS UPI
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 mt-1">Settlement UPI ID: <strong className="text-slate-700">{partner.upiId || 'partner@okaxis'}</strong></p>
                </div>

                <button
                  onClick={() => { setCashoutAmount(partner.todayEarnings?.toString() || '500'); setCashoutModalOpen(true); }}
                  className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 px-8 rounded-xl text-sm transition-all shadow-md flex items-center space-x-2"
                >
                  <IndianRupee className="h-4 w-4" />
                  <span>Withdraw Payout to UPI</span>
                </button>
              </div>

              {/* Earnings Breakdown */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm space-y-4">
                  <h4 className="text-sm font-bold text-slate-700 uppercase tracking-wider">Earning Components</h4>
                  <div className="space-y-3 text-xs">
                    <div className="flex justify-between">
                      <span className="text-slate-500">Base Trip Rate:</span>
                      <span className="font-bold text-slate-800">₹35 - ₹280 / trip</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Distance Travelled:</span>
                      <span className="font-bold text-slate-800">₹12 / km (Bike) • ₹25 / km (Tempo)</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Heavy Quintal Loading:</span>
                      <span className="font-bold text-emerald-600">+₹50 / Quintal</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-500">Rain & Harvest Peak Surge:</span>
                      <span className="font-bold text-emerald-600">+₹60 - ₹100 / trip</span>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm space-y-4">
                  <h4 className="text-sm font-bold text-slate-700 uppercase tracking-wider">Incentive Tiers (Maharashtra)</h4>
                  <div className="space-y-3 text-xs">
                    <div className="p-3 rounded-2xl bg-emerald-50 border border-emerald-100 flex justify-between items-center">
                      <div>
                        <span className="font-bold text-emerald-900 block">Tier 1: Daily Target</span>
                        <span className="text-[10px] text-emerald-700">Complete 6 trips today</span>
                      </div>
                      <span className="font-black text-emerald-700">+₹200</span>
                    </div>

                    <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 flex justify-between items-center">
                      <div>
                        <span className="font-bold text-slate-800 block">Tier 2: Weekly Champion</span>
                        <span className="text-[10px] text-slate-500">Complete 35 trips this week</span>
                      </div>
                      <span className="font-black text-slate-800">+₹1,500</span>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-sm space-y-4">
                  <h4 className="text-sm font-bold text-slate-700 uppercase tracking-wider">Total Cumulative Earnings</h4>
                  <div className="text-3xl font-black text-slate-900">
                    ₹{(partner.totalEarnings || 28450).toLocaleString()}
                  </div>
                  <p className="text-xs text-slate-400">
                    Lifetime earnings processed across {partner.totalDeliveries || 76} completed crop transit batches.
                  </p>
                  <div className="p-3 rounded-2xl bg-amber-50 text-amber-900 text-xs font-medium flex items-center space-x-2">
                    <Award className="h-4 w-4 text-amber-600 shrink-0" />
                    <span>Gold Star Delivery Partner Status</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: COMPLAINTS / GRIEVANCE DESK ("so delivery can complain") */}
          {tab === 'complaints' && (
            <div className="space-y-6">
              <div className="bg-gradient-to-r from-rose-900 via-slate-900 to-rose-950 text-white rounded-3xl p-6 sm:p-8 text-left relative overflow-hidden">
                <div className="relative z-10 space-y-2">
                  <span className="bg-rose-500 text-white font-black text-[10px] uppercase tracking-wider px-3 py-1 rounded-full inline-block">
                    Delivery Partner Grievance Desk
                  </span>
                  <h3 className="text-2xl font-black">Facing an Issue on Duty? File a Dispute Case</h3>
                  <p className="text-xs text-slate-300 max-w-xl">
                    Whether you encountered an unreasonable delay at the mandi gate, customer was unreachable, produce weight mismatched the invoice, or your payout was short, the Mandi+ Admin Board reviews delivery grievances in high priority.
                  </p>
                </div>
              </div>

              {/* Dedicated ComplaintsDesk Component */}
              <ComplaintsDesk />
            </div>
          )}

          {/* TAB 5: FLEET PROFILE & VEHICLE */}
          {tab === 'profile' && (
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-100 shadow-premium text-left space-y-6">
              <div>
                <h3 className="text-xl font-black text-slate-900 tracking-tight">Fleet Profile & Vehicle Parameters</h3>
                <p className="text-xs text-slate-400 mt-0.5">Manage your transport equipment, license coordinates, and payout UPI</p>
              </div>

              {profileMsg && (
                <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-2xl text-xs font-bold flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4" />
                  <span>{profileMsg}</span>
                </div>
              )}

              <form onSubmit={handleSaveProfile} className="space-y-6 max-w-2xl">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Full Name</label>
                    <input
                      type="text"
                      required
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Phone Number</label>
                    <input
                      type="tel"
                      required
                      value={editPhone}
                      onChange={(e) => setEditPhone(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Vehicle Type</label>
                    <select
                      value={editVehicleType}
                      onChange={(e) => setEditVehicleType(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm bg-white focus:outline-none focus:border-[#143d2c]"
                    >
                      <option value="BIKE">Motorcycle / Bike (60 kg capacity)</option>
                      <option value="SCOOTER">Electric Scooter (50 kg capacity)</option>
                      <option value="TEMPO">Commercial Tempo (500 kg capacity)</option>
                      <option value="MINI_TRUCK">Mini Truck (1.5 Ton capacity)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1.5">Vehicle Plate Number</label>
                    <input
                      type="text"
                      required
                      value={editVehicleNumber}
                      onChange={(e) => setEditVehicleNumber(e.target.value.toUpperCase())}
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Daily Settlement UPI ID</label>
                  <input
                    type="text"
                    required
                    value={editUpiId}
                    onChange={(e) => setEditUpiId(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                  />
                  <span className="text-[10px] text-slate-400 mt-1 block">Instant daily payouts will be transferred to this UPI address.</span>
                </div>

                <div className="pt-2">
                  <button
                    type="submit"
                    className="bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3.5 px-8 rounded-xl text-sm transition-all shadow-md"
                  >
                    Save Fleet Parameters
                  </button>
                </div>
              </form>
            </div>
          )}

        </div>
      </div>

      {/* DELIVERY OTP VERIFICATION MODAL */}
      {otpModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-8 max-w-md w-full shadow-2xl space-y-6 text-left relative">
            <div className="h-12 w-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Key className="h-6 w-6" />
            </div>

            <div className="space-y-1">
              <h3 className="text-xl font-black text-slate-900">Verify Buyer Delivery OTP</h3>
              <p className="text-xs text-slate-500">
                Ask the buyer for the 4-digit delivery verification PIN to complete this consignment.
              </p>
              {activeTask?.deliveryOtp && (
                <div className="mt-2 p-2.5 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs">
                  <span className="font-bold">Demo Test Code:</span> Use <strong>{activeTask.deliveryOtp}</strong>
                </div>
              )}
            </div>

            {otpError && (
              <div className="bg-rose-50 text-rose-700 p-3 rounded-xl text-xs font-bold">
                {otpError}
              </div>
            )}

            <form onSubmit={handleConfirmDeliveryWithOtp} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1.5">Enter 4-Digit OTP</label>
                <input
                  type="text"
                  maxLength={6}
                  required
                  placeholder="e.g. 5821"
                  value={enteredOtp}
                  onChange={(e) => setEnteredOtp(e.target.value)}
                  className="w-full text-center text-2xl font-black tracking-widest px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:border-emerald-600"
                />
              </div>

              <div className="flex space-x-3 pt-2">
                <button
                  type="button"
                  onClick={() => setOtpModalOpen(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-3 rounded-xl text-xs transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-3 rounded-xl text-xs transition-all shadow-md"
                >
                  {actionLoading ? 'Verifying...' : 'Confirm Delivery & Collect Pay'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CASHOUT / WITHDRAWAL MODAL */}
      {cashoutModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-8 max-w-md w-full shadow-2xl space-y-6 text-left relative">
            <div className="h-12 w-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <IndianRupee className="h-6 w-6" />
            </div>

            <div className="space-y-1">
              <h3 className="text-xl font-black text-slate-900">Instant IMPS UPI Cashout</h3>
              <p className="text-xs text-slate-500">
                Withdraw your daily earnings directly into your bank account. Zero transfer fees.
              </p>
            </div>

            {cashoutSuccess ? (
              <div className="space-y-4">
                <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-2xl text-xs font-bold text-center">
                  <CheckCircle2 className="h-8 w-8 text-emerald-600 mx-auto mb-2" />
                  <span>{cashoutSuccess}</span>
                </div>
                <button
                  onClick={() => setCashoutModalOpen(false)}
                  className="w-full bg-[#143d2c] text-white font-bold py-3 rounded-xl text-xs"
                >
                  Close
                </button>
              </div>
            ) : (
              <form onSubmit={handleWithdrawPayout} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Amount to Withdraw (₹)</label>
                  <input
                    type="number"
                    step="1"
                    required
                    max={partner.todayEarnings || 5000}
                    value={cashoutAmount}
                    onChange={(e) => setCashoutAmount(e.target.value)}
                    className="w-full text-xl font-black px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:border-[#143d2c]"
                  />
                  <span className="text-[10px] text-slate-400 mt-1 block">
                    Available: ₹{(partner.todayEarnings || 0).toLocaleString()}
                  </span>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Destination UPI ID</label>
                  <input
                    type="text"
                    value={cashoutUpi || partner.upiId || 'partner@okaxis'}
                    onChange={(e) => setCashoutUpi(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#143d2c]"
                  />
                </div>

                <div className="flex space-x-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setCashoutModalOpen(false)}
                    className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-3 rounded-xl text-xs transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={cashoutLoading}
                    className="flex-1 bg-[#143d2c] hover:bg-agrogreen-950 text-white font-bold py-3 rounded-xl text-xs transition-all shadow-md"
                  >
                    {cashoutLoading ? 'Processing IMPS...' : 'Transfer Now'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* SOS EMERGENCY MODAL */}
      {sosModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-8 max-w-md w-full shadow-2xl space-y-6 text-left relative border border-rose-100">
            <div className="h-12 w-12 rounded-2xl bg-rose-100 text-rose-600 flex items-center justify-center">
              <ShieldAlert className="h-6 w-6 animate-pulse" />
            </div>

            <div className="space-y-1">
              <h3 className="text-xl font-black text-rose-700">Mandi+ Emergency Fleet Helpline</h3>
              <p className="text-xs text-slate-500">
                In case of accident, roadside breakdown, theft, or medical emergency during produce transit:
              </p>
            </div>

            <div className="space-y-3 text-xs">
              <a href="tel:18002008899" className="block p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-900 font-bold hover:bg-rose-100 transition-colors">
                <span className="block text-[10px] text-rose-600 uppercase font-black">24x7 Fleet SOS Control Line</span>
                <span className="text-base font-black">📞 1800-200-8899 (Toll Free)</span>
              </a>

              <a href="tel:112" className="block p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-slate-900 font-bold hover:bg-slate-100 transition-colors">
                <span className="block text-[10px] text-slate-500 uppercase font-black">Police & Medical Emergency</span>
                <span className="text-base font-black">🚨 112 (National Helpline)</span>
              </a>

              <div className="p-3 bg-emerald-50 rounded-2xl text-emerald-800 text-[11px] font-medium">
                🛡️ Your active ₹5,00,000 accidental & hospital coverage policy is logged under fleet ID <strong>MND-FLEET-{partner.id || 5}</strong>.
              </div>
            </div>

            <button
              onClick={() => setSosModalOpen(false)}
              className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold py-3 rounded-xl text-xs transition-colors"
            >
              Dismiss Emergency Alert
            </button>
          </div>
        </div>
      )}

      {/* Real-time Delivery Tracking Modal */}
      <LiveDeliveryMapModal
        isOpen={showDeliveryMapModal}
        onClose={() => setShowDeliveryMapModal(false)}
        mode="tracking"
        orderId={activeTask?.orderId || activeTask?.id}
        initialAddress={activeTask?.dropLocation || 'Sant Savata Mali Nagar, Nashik'}
        deliveryPartnerName={partner.name}
        deliveryPartnerPhone={partner.phone}
        vehicleNumber={partner.vehicleNumber}
      />
    </div>
  );
}
