import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import api from '../services/api';
import { useLanguage } from '../context/LanguageContext';
import ComplaintsDesk from '../components/ComplaintsDesk';
import { 
  Check, X, Warehouse, Calendar, 
  User, ClipboardList, ShieldCheck, Thermometer 
} from 'lucide-react';

export default function ColdStorageDashboard({ tab = 'overview' }) {
  const { t } = useLanguage();
  const [bookings, setBookings] = useState([]);
  const [facility, setFacility] = useState(null);

  // Facility management form state
  const [facName, setFacName] = useState('');
  const [facTotalCapacity, setFacTotalCapacity] = useState('');
  const [facAvailableCapacity, setFacAvailableCapacity] = useState('');
  const [facPrice, setFacPrice] = useState('');
  const [facLocation, setFacLocation] = useState('');
  const [facDescription, setFacDescription] = useState('');
  const [facMessage, setFacMessage] = useState('');
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const fetchManagerData = async () => {
    try {
      const bookRes = await api.get('/cold-storage/bookings');
      setBookings(bookRes.data);

      const facRes = await api.get('/cold-storage/my-facility');
      setFacility(facRes.data);
      if (facRes.data) {
        setFacName(facRes.data.name || '');
        setFacTotalCapacity(facRes.data.totalCapacityTons || '');
        setFacAvailableCapacity(facRes.data.availableCapacityTons || '');
        setFacPrice(facRes.data.pricePerTonDay || '');
        setFacLocation(facRes.data.location || '');
        setFacDescription(facRes.data.description || '');
      }
    } catch (error) {
      console.error("Error loading cold storage manager data", error);
    }
  };

  const handleUpdateFacility = async (e) => {
    e.preventDefault();
    setFacMessage('');

    const totalCap = parseFloat(facTotalCapacity);
    const availCap = parseFloat(facAvailableCapacity);
    const priceVal = parseFloat(facPrice);

    if (isNaN(totalCap) || totalCap <= 0) {
      setFacMessage('Error: Total Capacity must be a positive number.');
      return;
    }
    if (isNaN(availCap) || availCap < 0 || availCap > totalCap) {
      setFacMessage('Error: Available Capacity must be positive and cannot exceed Total Capacity.');
      return;
    }
    if (isNaN(priceVal) || priceVal <= 0) {
      setFacMessage('Error: Price must be a positive number.');
      return;
    }

    try {
      const updated = await api.put('/cold-storage/my-facility', {
        name: facName,
        totalCapacityTons: totalCap,
        availableCapacityTons: availCap,
        pricePerTonDay: priceVal,
        location: facLocation,
        description: facDescription
      });
      setFacility(updated.data);
      setFacMessage('Facility configurations updated successfully!');
      
      window.dispatchEvent(new CustomEvent('mandiplus-toast', {
        detail: {
          title: "Facility Updated",
          message: "Your cold storage facility details have been saved.",
          category: "COLD_STORAGE",
          timestamp: new Date()
        }
      }));
    } catch (error) {
      setFacMessage('Error: Failed to save changes. ' + (error.response?.data || error.message));
    }
  };

  useEffect(() => {
    fetchManagerData();

    const handleWebSocketAlert = (e) => {
      const data = e.detail;
      if (data.category === 'COLD_STORAGE') {
        fetchManagerData();
      }
    };

    window.addEventListener('mandiplus-alert', handleWebSocketAlert);
    return () => {
      window.removeEventListener('mandiplus-alert', handleWebSocketAlert);
    };
  }, [tab]);  // IoT Telemetry: Temperature fluctuate live
  const [liveTemp, setLiveTemp] = useState(4.2);
  useEffect(() => {
    const interval = setInterval(() => {
      setLiveTemp(prev => {
        const delta = (Math.random() - 0.5) * 0.2;
        const nextTemp = prev + delta;
        return Math.max(2.0, Math.min(6.0, Number(nextTemp.toFixed(1))));
      });
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  // Calculations
  const pendingRequests = bookings.filter(b => b.status === 'PENDING');
  const activeRecords = bookings.filter(b => b.status === 'APPROVED' || b.status === 'ACTIVE');

  // Total capacity calculations
  const totalCapacity = facility ? facility.totalCapacityTons : 1000;
  const availableCapacity = facility ? facility.availableCapacityTons : totalCapacity;
  const occupiedCapacity = totalCapacity - availableCapacity;
  const occupancyPercentage = Math.round((occupiedCapacity / totalCapacity) * 100);

  // Circular gauge parameters
  const radius = 42;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (occupancyPercentage / 100) * circumference;

  // Manage Bookings
  const handleBookingAction = async (bookingId, action, reason = "") => {
    if (action === 'REJECTED' && !reason) {
      setRejectingId(bookingId);
      setRejectionReason("");
      return;
    }
    try {
      const url = action === 'REJECTED'
        ? `/cold-storage/bookings/${bookingId}/status?status=REJECTED&rejectionReason=${encodeURIComponent(reason)}`
        : `/cold-storage/bookings/${bookingId}/status?status=${action}`;
      await api.put(url);
      setRejectingId(null);
      setRejectionReason("");
      fetchManagerData();
    } catch (error) {
      alert("Verification update failed! " + (error.response?.data || error.message));
    }
  };

  return (
    <div className="flex bg-[#fafbfb] min-h-screen font-sans">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-y-auto">
        <DashboardHeader title={
          tab === 'overview' ? t('coldStorageCommand') :
          tab === 'requests' ? t('bookingSpaceQueue') :
          tab === 'complaints' ? t('complaintsDesk') :
          t('activeWarehousingRecords')
        } />

        <main className="p-8 max-w-7xl w-full mx-auto space-y-8">
          
          {/* TAB 1: OVERVIEW & CAPACITY GAUGES */}
          {tab === 'overview' && (
            <div className="space-y-8">
              
              {/* Capacity Widget Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                
                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-indigo-50 text-indigo-600 p-3.5 rounded-2xl">
                    <Warehouse className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Total Space</span>
                    <span className="block text-xl font-extrabold text-slate-800">{totalCapacity} Tons</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-emerald-50 text-emerald-600 p-3.5 rounded-2xl">
                    <Thermometer className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">IoT Air Temp</span>
                    <span className="block text-xl font-extrabold text-slate-800">{liveTemp}°C</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-amber-50 text-amber-600 p-3.5 rounded-2xl">
                    <ClipboardList className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Pending Leases</span>
                    <span className="block text-xl font-extrabold text-slate-800">{pendingRequests.length} Requests</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-blue-50 text-blue-600 p-3.5 rounded-2xl">
                    <ShieldCheck className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Active Leases</span>
                    <span className="block text-xl font-extrabold text-slate-800">{activeRecords.length} Units</span>
                  </div>
                </div>

              </div>

              {/* Occupancy gauge analysis */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                
                <div className="lg:col-span-5 bg-white rounded-3xl border border-slate-100 p-8 shadow-sm text-left flex flex-col justify-between min-h-[340px]">
                  <div className="space-y-1">
                    <h4 className="font-extrabold text-lg text-slate-800 tracking-tight">Occupancy Gauge</h4>
                    <p className="text-xs text-slate-400">Current spatial volume utilization ratios.</p>
                  </div>

                  <div className="py-4 flex items-center justify-center relative">
                    <svg className="w-36 h-36 transform -rotate-90 overflow-visible" viewBox="0 0 100 100">
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="stroke-slate-100"
                        strokeWidth="8"
                        fill="transparent"
                      />
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="stroke-indigo-600 transition-all duration-1000 ease-out"
                        strokeWidth="8"
                        strokeDasharray={circumference}
                        strokeDashoffset={strokeDashoffset}
                        strokeLinecap="round"
                        fill="transparent"
                      />
                    </svg>
                    <div className="absolute flex flex-col items-center justify-center">
                      <span className="text-3xl font-black text-slate-800">{occupancyPercentage}%</span>
                      <span className="text-[9px] text-slate-400 font-bold uppercase tracking-wider">Occupied</span>
                    </div>
                  </div>

                  <p className="text-xs text-slate-400">Cold Facility: <strong>{facility?.name || 'Sahyadri Mega Cold Storage'}</strong> • Location: <strong>{facility?.location || 'Pimpalgaon, Nashik'}</strong></p>
                </div>

                <div className="lg:col-span-7 bg-white rounded-3xl border border-slate-100 p-8 shadow-sm text-left">
                  <h4 className="font-extrabold text-lg text-slate-800 tracking-tight mb-6">Recent Leases Booking Requests</h4>
                  
                  {pendingRequests.length === 0 ? (
                    <p className="text-sm text-slate-400">No pending lease requests in queue.</p>
                  ) : (
                    <div className="space-y-4">
                      {pendingRequests.slice(0, 3).map(req => (
                        <div key={req.id} className="p-4 border border-slate-100 rounded-2xl flex justify-between items-center hover:bg-slate-50/50">
                          <div>
                            <h5 className="font-bold text-slate-700">{req.farmerName}</h5>
                            <span className="text-[10px] text-slate-400 block mt-0.5">Qty: {req.quantityTons} Tons • Fee: ₹{req.totalPrice}</span>
                          </div>
                          <div className="flex space-x-2">
                            <button
                              onClick={() => handleBookingAction(req.id, 'APPROVED')}
                              className="bg-emerald-50 text-emerald-600 p-2 rounded-xl border border-emerald-100 hover:bg-emerald-100 transition-colors"
                            >
                              <Check className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleBookingAction(req.id, 'REJECTED')}
                              className="bg-rose-50 text-rose-600 p-2 rounded-xl border border-rose-100 hover:bg-rose-100 transition-colors"
                            >
                              <X className="h-4 w-4" />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

              </div>

              {/* Facility Details Management Form */}
              <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left max-w-3xl">
                <h3 className="text-xl font-bold text-slate-800 tracking-tight mb-2">Edit Facility Information</h3>
                <p className="text-xs text-slate-400 mb-6">Modify the pricing, available capacity thresholds, address, and description of this warehouse.</p>

                {facMessage && (
                  <div className={`p-4 rounded-xl text-xs font-bold mb-6 ${
                    facMessage.startsWith('Error') 
                      ? 'bg-rose-50 border border-rose-100 text-rose-700' 
                      : 'bg-emerald-50 border border-emerald-100 text-emerald-700'
                  }`}>
                    {facMessage}
                  </div>
                )}

                <form onSubmit={handleUpdateFacility} className="space-y-5">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Facility Name</label>
                      <input 
                        type="text" required placeholder="Sahyadri Mega Cold Storage" value={facName}
                        onChange={(e) => setFacName(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 focus:border-indigo-650 transition-colors"
                      />
                    </div>

                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Rate per Ton/Day (₹)</label>
                      <input 
                        type="number" required placeholder="120" value={facPrice}
                        onChange={(e) => setFacPrice(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-355 focus:border-indigo-650 transition-colors"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Total Capacity (Tons)</label>
                      <input 
                        type="number" required placeholder="1000" value={facTotalCapacity}
                        onChange={(e) => setFacTotalCapacity(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-355 focus:border-indigo-650 transition-colors"
                      />
                    </div>

                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Available Capacity (Tons)</label>
                      <input 
                        type="number" required placeholder="850" value={facAvailableCapacity}
                        onChange={(e) => setFacAvailableCapacity(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-355 focus:border-indigo-650 transition-colors"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Address / Location</label>
                    <input 
                      type="text" required placeholder="Pimpalgaon, Nashik, MH" value={facLocation}
                      onChange={(e) => setFacLocation(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 focus:border-indigo-650 transition-colors"
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Description & Contact Number</label>
                    <textarea 
                      rows={3} placeholder="Provide contact info and facility highlights..." value={facDescription}
                      onChange={(e) => setFacDescription(e.target.value)}
                      className="w-full px-4 py-2 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 resize-none focus:border-indigo-650 transition-colors"
                    />
                  </div>

                  <button
                    type="submit"
                    className="bg-indigo-950 hover:bg-indigo-900 text-white font-bold py-3.5 px-6 rounded-xl text-xs transition-colors shadow-sm flex items-center justify-center"
                  >
                    Save Changes
                  </button>
                </form>
              </div>

            </div>
          )}

          {/* TAB 2: BOOKING SPACE QUEUE */}
          {tab === 'requests' && (
            <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left">
              <h3 className="text-xl font-bold text-slate-800 mb-6">Pending Space Booking Requests</h3>

              {pendingRequests.length === 0 ? (
                <p className="text-sm text-slate-400">No pending requests are currently in the verification queue.</p>
              ) : (
                <div className="space-y-6">
                  {pendingRequests.map((req) => (
                    <div key={req.id} className="p-6 border border-slate-100 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-6 hover:shadow-sm transition-shadow">
                      <div className="space-y-2">
                        <div className="flex items-center space-x-2.5">
                          <span className="font-bold text-slate-800 text-lg">#BOOK-{req.id}</span>
                          <span className="px-2 py-0.5 text-[9px] font-bold uppercase rounded-md bg-amber-50 text-amber-600">Pending Approval</span>
                        </div>
                        <h4 className="font-bold text-slate-700 flex items-center space-x-2 text-sm">
                          {req.farmerProfilePhoto || req.buyerProfilePhoto ? (
                            <img 
                              src={req.farmerProfilePhoto || req.buyerProfilePhoto} 
                              alt="Avatar" 
                              className="h-6 w-6 rounded-full object-cover border border-slate-200" 
                            />
                          ) : (
                            <User className="h-4 w-4 text-slate-400" />
                          )}
                          <span>
                            {req.farmerName ? `Farmer: ${req.farmerName}` : `Buyer: ${req.buyerName}`} ({req.farmerPhone || req.buyerPhone})
                          </span>
                        </h4>
                        <p className="text-xs text-slate-400 flex items-center space-x-1">
                          <Calendar className="h-4 w-4 text-slate-400" />
                          <span>Lease Range: {req.startDate} to {req.endDate}</span>
                        </p>
                        <p className="text-xs text-slate-400">Requested Capacity: <strong>{req.quantityTons} Tons</strong></p>
                      </div>

                      <div className="text-left md:text-right space-y-3 min-w-[200px]">
                        <div>
                          <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider">Estimated Revenue</span>
                          <span className="block text-lg font-extrabold text-slate-800">₹{req.totalPrice.toLocaleString('en-IN')}</span>
                        </div>

                        <div className="flex space-x-2.5 justify-start md:justify-end">
                          <button
                            onClick={() => handleBookingAction(req.id, 'APPROVED')}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-4 py-2 rounded-xl text-xs transition-colors"
                          >
                            Approve Lease
                          </button>
                          <button
                            onClick={() => handleBookingAction(req.id, 'REJECTED')}
                            className="bg-rose-600 hover:bg-rose-700 text-white font-bold px-4 py-2 rounded-xl text-xs transition-colors"
                          >
                            Reject
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 3: RECORDS */}
          {tab === 'records' && (
            <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left">
              <h3 className="text-xl font-bold text-slate-800 mb-6">Active Warehousing Records</h3>

              {activeRecords.length === 0 ? (
                <p className="text-sm text-slate-400">No active leases currently stored.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm text-slate-500">
                    <thead className="text-xs text-slate-400 bg-slate-50 uppercase rounded-xl">
                      <tr>
                        <th className="px-6 py-3.5 rounded-l-xl text-left">Lease ID</th>
                        <th className="px-6 py-3.5 text-left">Farmer</th>
                        <th className="px-6 py-3.5 text-left">Dates Range</th>
                        <th className="px-6 py-3.5 text-left">Lease Volume</th>
                        <th className="px-6 py-3.5 text-right rounded-r-xl">Collected Rent</th>
                      </tr>
                    </thead>
                    <tbody>
                      {activeRecords.map((rec) => (
                        <tr key={rec.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50/50">
                          <td className="px-6 py-4 font-bold text-slate-700">#BOOK-{rec.id}</td>
                          <td className="px-6 py-4 flex items-center space-x-3">
                            {rec.farmerProfilePhoto || rec.buyerProfilePhoto ? (
                              <img 
                                src={rec.farmerProfilePhoto || rec.buyerProfilePhoto} 
                                alt="Avatar" 
                                className="h-8 w-8 rounded-full object-cover border border-slate-200" 
                              />
                            ) : (
                              <div className="h-8 w-8 rounded-full bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-400">
                                <User className="h-4 w-4" />
                              </div>
                            )}
                            <div className="text-left">
                              <span className="font-semibold text-slate-800 block">
                                {rec.farmerName || rec.buyerName}
                              </span>
                              <span className="text-[10px] text-slate-400 font-medium block">
                                {rec.farmerPhone || rec.buyerPhone}
                              </span>
                            </div>
                          </td>
                          <td className="px-6 py-4">{rec.startDate} to {rec.endDate}</td>
                          <td className="px-6 py-4 font-semibold text-slate-800">{rec.quantityTons} Tons</td>
                          <td className="px-6 py-4 font-extrabold text-slate-800 text-right">₹{rec.totalPrice.toLocaleString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 4: COMPLAINTS */}
          {tab === 'complaints' && (
            <ComplaintsDesk />
          )}

        </main>
      </div>

      {/* Rejection Reason Modal */}
      {rejectingId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-8 max-w-md w-full mx-4 shadow-2xl border border-slate-100 text-left space-y-6">
            <div>
              <h3 className="text-lg font-black text-slate-800 tracking-tight">Specify Rejection Reason</h3>
              <p className="text-xs text-slate-400 mt-1">Please provide a clear explanation for rejecting booking request #BOOK-{rejectingId}.</p>
            </div>
            
            <div className="space-y-2">
              <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider">Rejection Reason</label>
              <textarea
                rows={4}
                required
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                placeholder="e.g. Requested capacity exceeds current spatial limit, scheduled maintenance..."
                className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-350 resize-none focus:border-indigo-650 transition-colors"
              />
            </div>

            <div className="flex space-x-3.5 pt-2">
              <button
                onClick={() => {
                  setRejectingId(null);
                  setRejectionReason("");
                }}
                className="flex-1 bg-slate-50 hover:bg-slate-100 text-slate-650 font-bold py-3 px-4 rounded-xl text-xs transition-colors border border-slate-200"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (!rejectionReason.trim()) {
                    alert("Please enter a rejection reason.");
                    return;
                  }
                  handleBookingAction(rejectingId, 'REJECTED', rejectionReason);
                }}
                className="flex-1 bg-rose-600 hover:bg-rose-700 text-white font-bold py-3 px-4 rounded-xl text-xs transition-colors shadow-sm"
              >
                Reject Lease Request
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
