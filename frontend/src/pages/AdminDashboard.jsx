import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import api from '../services/api';
import { useLanguage } from '../context/LanguageContext';
import { 
  Users, User, ShoppingBag, Leaf, ShieldAlert, 
  IndianRupee, ToggleLeft, ToggleRight, Trash2, CheckCircle2, Plus,
  Check, X, Eye, AlertCircle, Edit, Sliders, Clock, ZoomIn, ArrowRight, ShieldCheck,
  Filter, Search, MessageSquare, ThumbsUp, ThumbsDown, Bike, Truck, History, Radio, MapPin, Navigation
} from 'lucide-react';

export default function AdminDashboard({ tab = 'dashboard' }) {
  const { t } = useLanguage();
  const [metrics, setMetrics] = useState({
    totalUsers: 0,
    totalFarmers: 0,
    totalBuyers: 0,
    totalProducts: 0,
    pendingProducts: 0,
    totalOrders: 0,
    totalComplaints: 0
  });
  const [usersList, setUsersList] = useState([]);
  const [productsList, setProductsList] = useState([]);
  const [complaintsList, setComplaintsList] = useState([]);
  const [eventLog, setEventLog] = useState([]);
  const [commission, setCommission] = useState(2.0);
  const [insurance, setInsurance] = useState(50000);
  const [settingsMsg, setSettingsMsg] = useState('');

  // Produce Moderation Desk State
  const [productFilter, setProductFilter] = useState('ALL'); // 'ALL' | 'PENDING' | 'AVAILABLE' | 'REJECTED'
  const [productSearch, setProductSearch] = useState('');
  const [inspectingProduct, setInspectingProduct] = useState(null); // for inspect & adjust modal
  const [rejectingProduct, setRejectingProduct] = useState(null); // for rejection reason modal
  const [rejectionReasonInput, setRejectionReasonInput] = useState('');
  const [photoPreviewModal, setPhotoPreviewModal] = useState(null);

  // Edit / Approve fields
  const [editApprovedPrice, setEditApprovedPrice] = useState('');
  const [editCropName, setEditCropName] = useState('');
  const [editVariety, setEditVariety] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editLocation, setEditLocation] = useState('');
  const [editImageUrl, setEditImageUrl] = useState('');
  const [editAdminFeedback, setEditAdminFeedback] = useState('');

  // Mandi Desk State
  const [mandiList, setMandiList] = useState([]);
  const [mandiModalOpen, setMandiModalOpen] = useState(false);
  const [editingMandi, setEditingMandi] = useState(null);

  // Mandi Form parameters
  const [mCropName, setMCropName] = useState('');
  const [mVariety, setMVariety] = useState('');
  const [mMarketName, setMMarketName] = useState('');
  const [mState, setMState] = useState('Maharashtra');
  const [mPricePerQuintal, setMPricePerQuintal] = useState('');
  const [mPriceChange, setMPriceChange] = useState('0');

  // Delivery Logistics Oversight Desk State
  const [deliveriesList, setDeliveriesList] = useState([]);
  const [unacceptedDeliveries, setUnacceptedDeliveries] = useState([]);
  const [deliveryFilter, setDeliveryFilter] = useState('ALL');
  const [reassignModalOpen, setReassignModalOpen] = useState(false);
  const [selectedDeliveryForReassign, setSelectedDeliveryForReassign] = useState(null);
  const [reassignPartnerId, setReassignPartnerId] = useState('');
  const [auditModalOpen, setAuditModalOpen] = useState(false);
  const [auditLogs, setAuditLogs] = useState([]);
  const [selectedDeliveryCode, setSelectedDeliveryCode] = useState('');
  const [auditLoading, setAuditLoading] = useState(false);

  const fetchAdminData = async () => {
    try {
      const metRes = await api.get('/admin/metrics');
      setMetrics(metRes.data);

      const usrRes = await api.get('/admin/users');
      setUsersList(usrRes.data);

      const prodRes = await api.get('/admin/products');
      setProductsList(prodRes.data);

      const compRes = await api.get('/admin/complaints');
      setComplaintsList(compRes.data);

      const mandiRes = await api.get('/admin/mandi');
      setMandiList(mandiRes.data);

      try {
        const delivRes = await api.get('/admin/deliveries');
        setDeliveriesList(delivRes.data || []);
        const unaccRes = await api.get('/admin/deliveries/unaccepted');
        setUnacceptedDeliveries(unaccRes.data || []);
      } catch (err) {
        console.warn('Delivery oversight fetch notice:', err);
      }
    } catch (error) {
      console.error("Error loading admin dashboard data", error);
    }
  };

  useEffect(() => {
    fetchAdminData();

    const handleWebSocketAlert = (e) => {
      const data = e.detail;
      setEventLog((prev) => [data, ...prev].slice(0, 10));
      fetchAdminData();
    };

    window.addEventListener('mandiplus-alert', handleWebSocketAlert);
    return () => {
      window.removeEventListener('mandiplus-alert', handleWebSocketAlert);
    };
  }, [tab]);

  // Toggle User Active Status
  const handleToggleUser = async (userId) => {
    try {
      await api.patch(`/admin/users/${userId}/toggle`);
      fetchAdminData();
    } catch (error) {
      alert("Failed to toggle user status!");
    }
  };

  // Quick Approve produce listing at farmer's suggested rate
  const handleQuickApprove = async (prod) => {
    try {
      const rate = prod.suggestedPricePerQuintal || prod.pricePerQuintal;
      await api.put(`/admin/products/${prod.id}/approve`, {
        pricePerQuintal: rate,
        adminFeedback: "Approved by Mandi+ Desk at suggested rate"
      });
      fetchAdminData();
    } catch (error) {
      alert("Failed to approve listing: " + (error.response?.data || error.message));
    }
  };

  // Open inspection modal to decide rate or inspect photo
  const handleOpenInspect = (prod) => {
    setInspectingProduct(prod);
    setEditApprovedPrice(prod.pricePerQuintal || prod.suggestedPricePerQuintal || '');
    setEditCropName(prod.cropName || '');
    setEditVariety(prod.variety || '');
    setEditQuantity(prod.quantityQuintals || '');
    setEditLocation(prod.location || '');
    setEditImageUrl(prod.imageUrl || '');
    setEditAdminFeedback(prod.adminFeedback || 'Approved by Admin Desk');
  };

  // Submit adjustments and approve
  const handleSaveAndApprove = async (e) => {
    e.preventDefault();
    if (!inspectingProduct) return;
    try {
      await api.put(`/admin/products/${inspectingProduct.id}/approve`, {
        cropName: editCropName,
        variety: editVariety,
        quantityQuintals: parseFloat(editQuantity),
        pricePerQuintal: parseFloat(editApprovedPrice),
        location: editLocation,
        imageUrl: editImageUrl,
        adminFeedback: editAdminFeedback
      });
      setInspectingProduct(null);
      fetchAdminData();
    } catch (error) {
      alert("Failed to approve listing: " + (error.response?.data || error.message));
    }
  };

  // Open rejection modal
  const handleOpenReject = (prod) => {
    setRejectingProduct(prod);
    setRejectionReasonInput('Photo quality unclear / suggested rate is outside fair market value.');
  };

  // Submit rejection
  const handleConfirmReject = async (e) => {
    e.preventDefault();
    if (!rejectingProduct) return;
    try {
      await api.put(`/admin/products/${rejectingProduct.id}/reject`, {
        reason: rejectionReasonInput || 'Photo quality unclear or suggested price is outside fair market value.'
      });
      setRejectingProduct(null);
      setRejectionReasonInput('');
      fetchAdminData();
    } catch (error) {
      alert("Failed to reject listing: " + (error.response?.data || error.message));
    }
  };

  // Delete product listing
  const handleDeleteProduct = async (prodId) => {
    if (!window.confirm("Are you sure you want to remove this crop listing from marketplace?")) return;
    try {
      await api.delete(`/admin/products/${prodId}`);
      fetchAdminData();
    } catch (error) {
      alert("Failed to delete product: " + (error.response?.data || error.message));
    }
  };

  // Resolve complaint
  const handleResolveComplaint = async (compId, newStatus) => {
    try {
      await api.put(`/admin/complaints/${compId}/status?status=${newStatus}`);
      fetchAdminData();
    } catch (error) {
      alert("Resolution failed!");
    }
  };

  const handleSaveSettings = async (e) => {
    e.preventDefault();
    setSettingsMsg('');
    try {
      await api.post(`/admin/settings?commission=${commission}&insurance=${insurance}`);
      setSettingsMsg('Global system parameters successfully adjusted and broadcasted.');
    } catch (error) {
      setSettingsMsg('Failed to apply system updates.');
    }
  };

  // Mandi rate management handlers
  const handleOpenAddMandi = () => {
    setEditingMandi(null);
    setMCropName('');
    setMVariety('');
    setMMarketName('');
    setMState('Maharashtra');
    setMPricePerQuintal('');
    setMPriceChange('0');
    setMandiModalOpen(true);
  };

  const handleOpenEditMandi = (item) => {
    setEditingMandi(item);
    setMCropName(item.cropName);
    setMVariety(item.variety);
    setMMarketName(item.marketName);
    setMState(item.state || 'Maharashtra');
    setMPricePerQuintal(item.pricePerQuintal.toString());
    setMPriceChange((item.priceChangeYesterday || 0).toString());
    setMandiModalOpen(true);
  };

  const handleSaveMandi = async (e) => {
    e.preventDefault();
    const payload = {
      cropName: mCropName,
      variety: mVariety,
      marketName: mMarketName,
      state: mState,
      pricePerQuintal: parseFloat(mPricePerQuintal),
      priceChangeYesterday: parseFloat(mPriceChange)
    };

    try {
      if (editingMandi) {
        await api.put(`/admin/mandi/${editingMandi.id}`, payload);
      } else {
        await api.post('/admin/mandi', payload);
      }
      setMandiModalOpen(false);
      fetchAdminData();
    } catch (error) {
      alert("Failed to save Mandi price record: " + (error.response?.data || error.message));
    }
  };

  const handleDeleteMandi = async (id) => {
    if (!window.confirm(t('confirmDelete') || "Are you sure you want to delete this mandi price record?")) return;
    try {
      await api.delete(`/admin/mandi/${id}`);
      fetchAdminData();
    } catch (error) {
      alert("Failed to delete Mandi price record: " + (error.response?.data || error.message));
    }
  };

  // Delivery Oversight Handlers
  const handleOpenAuditModal = async (delivery) => {
    setSelectedDeliveryCode(delivery.taskCode);
    setAuditModalOpen(true);
    setAuditLoading(true);
    try {
      const res = await api.get(`/admin/deliveries/${delivery.id}/audit`);
      setAuditLogs(res.data || []);
    } catch (e) {
      alert("Failed to load audit trail: " + (e.response?.data || e.message));
    } finally {
      setAuditLoading(false);
    }
  };

  const handleOpenReassignModal = (delivery) => {
    setSelectedDeliveryForReassign(delivery);
    setReassignPartnerId(delivery.partnerId ? String(delivery.partnerId) : '');
    setReassignModalOpen(true);
  };

  const handleReassignSubmit = async (e) => {
    e.preventDefault();
    if (!selectedDeliveryForReassign || !reassignPartnerId) return;
    try {
      await api.post(`/admin/deliveries/${selectedDeliveryForReassign.id}/reassign`, {
        partnerId: Number(reassignPartnerId)
      });
      setReassignModalOpen(false);
      setSelectedDeliveryForReassign(null);
      setReassignPartnerId('');
      fetchAdminData();
    } catch (e) {
      alert("Failed to reassign delivery: " + (e.response?.data || e.message));
    }
  };

  return (
    <div className="flex bg-[#fafbfb] min-h-screen font-sans">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-y-auto">
        <DashboardHeader title={
          tab === 'dashboard' ? t('adminPortal') :
          tab === 'users' ? t('usersControl') :
          tab === 'products' ? t('productsControl') :
          tab === 'deliveries' ? 'Delivery Logistics & Oversight Desk' :
          tab === 'complaints' ? t('complaintsDesk') :
          tab === 'mandi' ? t('mandiDashboard') :
          t('adminPortal')
        } />

        <main className="p-8 max-w-7xl w-full mx-auto space-y-8">
          
          {/* TAB 1: SYSTEM STATISTICS DASHBOARD */}
          {tab === 'dashboard' && (
            <div className="space-y-8">
              
              {/* Urgent Action Banner: Pending Crop Approvals */}
              {metrics.pendingProducts > 0 && (
                <div className="bg-gradient-to-r from-amber-500 via-orange-500 to-amber-600 rounded-3xl p-6 text-white shadow-lg flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 text-left">
                  <div className="flex items-center space-x-4">
                    <div className="h-12 w-12 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center shrink-0">
                      <AlertCircle className="h-6 w-6 text-white" />
                    </div>
                    <div>
                      <h4 className="font-extrabold text-base sm:text-lg flex items-center space-x-2">
                        <span>{metrics.pendingProducts} Produce Listing(s) Awaiting Review</span>
                        <span className="bg-white/20 text-white text-[10px] font-black uppercase px-2 py-0.5 rounded-full">Requires Approval</span>
                      </h4>
                      <p className="text-xs text-amber-100 opacity-90 mt-0.5">
                        Farmers have uploaded crop photos and suggested selling prices. Inspect photos and approve to make crops live to buyers.
                      </p>
                    </div>
                  </div>
                  <Link 
                    to="/admin/products"
                    className="bg-white text-amber-800 hover:bg-amber-50 font-black text-xs px-5 py-3 rounded-xl transition-all shadow-sm shrink-0 flex items-center space-x-1.5"
                  >
                    <span>Inspect & Approve</span>
                    <ArrowRight className="h-4 w-4" />
                  </Link>
                </div>
              )}

              {/* Core summary counters */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6">
                
                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-agrogreen-50 text-agrogreen-700 p-3.5 rounded-2xl">
                    <Users className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Total Users</span>
                    <span className="block text-xl font-extrabold text-slate-800">{metrics.totalUsers} Profiles</span>
                  </div>
                </div>

                <div className={`p-6 rounded-3xl border shadow-sm flex items-center space-x-4 text-left transition-all ${
                  metrics.pendingProducts > 0 
                    ? 'bg-amber-50/60 border-amber-200 ring-2 ring-amber-400/20' 
                    : 'bg-white border-slate-100'
                }`}>
                  <div className="bg-amber-100 text-amber-700 p-3.5 rounded-2xl">
                    <Clock className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-amber-800 font-bold uppercase tracking-wider mb-0.5">Pending Review</span>
                    <span className="block text-xl font-black text-amber-900">{metrics.pendingProducts} Crops</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-emerald-50 text-emerald-600 p-3.5 rounded-2xl">
                    <Leaf className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Active Crops</span>
                    <span className="block text-xl font-extrabold text-slate-800">{metrics.totalProducts} Listings</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-sky-50 text-sky-600 p-3.5 rounded-2xl">
                    <ShoppingBag className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Cleared Orders</span>
                    <span className="block text-xl font-extrabold text-slate-800">{metrics.totalOrders} Shipments</span>
                  </div>
                </div>

                <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-rose-50 text-rose-600 p-3.5 rounded-2xl">
                    <ShieldAlert className="h-6 w-6" />
                  </div>
                  <div>
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Pending Disputes</span>
                    <span className="block text-xl font-extrabold text-slate-800">{metrics.totalComplaints} Cases</span>
                  </div>
                </div>

              </div>

              {/* Roles split and mockup graphs */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                
                <div className="bg-white rounded-3xl border border-slate-100 p-8 shadow-sm text-left space-y-4">
                  <h4 className="font-extrabold text-lg text-slate-800 tracking-tight">Active Profiles Ratio</h4>
                  <div className="space-y-4 py-4">
                    <div>
                      <div className="flex justify-between text-xs font-semibold text-slate-500 mb-1">
                        <span>Farmers</span>
                        <span>{metrics.totalFarmers} Profiles</span>
                      </div>
                      <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                        <div className="bg-emerald-600 h-full rounded-full" style={{ width: `${(metrics.totalFarmers / (metrics.totalUsers || 1)) * 100}%` }}></div>
                      </div>
                    </div>
                    <div>
                      <div className="flex justify-between text-xs font-semibold text-slate-500 mb-1">
                        <span>Buyers & Exporters</span>
                        <span>{metrics.totalBuyers} Profiles</span>
                      </div>
                      <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                        <div className="bg-indigo-600 h-full rounded-full" style={{ width: `${(metrics.totalBuyers / (metrics.totalUsers || 1)) * 100}%` }}></div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-3xl border border-slate-100 p-8 shadow-sm text-left flex flex-col justify-between">
                  <h4 className="font-extrabold text-lg text-slate-800 tracking-tight mb-1">Live Platform Event Stream</h4>
                  <p className="text-xs text-slate-400 mb-4">WebSocket notifications log of live actions on the marketplace.</p>
                  <div className="space-y-3 overflow-y-auto max-h-[160px] pr-2">
                    {eventLog.length === 0 ? (
                      <p className="text-xs text-slate-400 italic">Listening for live WebSocket notifications...</p>
                    ) : (
                      eventLog.map((log, idx) => (
                        <div key={idx} className="p-3 bg-slate-50 border border-slate-100 rounded-2xl flex items-start space-x-3 text-xs">
                          <span className="font-extrabold text-[9px] uppercase px-1.5 py-0.5 rounded bg-agrogreen-100 text-agrogreen-800">{log.category}</span>
                          <div className="flex-1">
                            <p className="font-bold text-slate-700">{log.title}</p>
                            <p className="text-slate-500 mt-0.5">{log.message}</p>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>

              </div>

            </div>
          )}

          {/* TAB 2: MANAGE USERS */}
          {tab === 'users' && (
            <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left">
              <h3 className="text-xl font-bold text-slate-800 mb-6">User Accounts Registry</h3>

              {usersList.length === 0 ? (
                <p className="text-sm text-slate-400">No user accounts registered.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm text-slate-500">
                    <thead className="text-xs text-slate-400 bg-slate-50 uppercase rounded-xl">
                      <tr>
                        <th className="px-6 py-3.5 rounded-l-xl text-left">User ID</th>
                        <th className="px-6 py-3.5 text-left">Email Address</th>
                        <th className="px-6 py-3.5 text-left">Role Profile</th>
                        <th className="px-6 py-3.5 text-center">Status</th>
                        <th className="px-6 py-3.5 text-right rounded-r-xl">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {usersList.map((user) => (
                        <tr key={user.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50/50">
                          <td className="px-6 py-4 font-bold text-slate-700 flex items-center space-x-3">
                            {user.profilePhoto ? (
                              <img 
                                src={user.profilePhoto} 
                                alt="User Avatar" 
                                className="h-8 w-8 rounded-full object-cover border border-slate-200" 
                              />
                            ) : (
                              <div className="h-8 w-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                                <User className="h-4 w-4" />
                              </div>
                            )}
                            <span>#USR-{user.id}</span>
                          </td>
                          <td className="px-6 py-4">{user.email}</td>
                          <td className="px-6 py-4 font-semibold text-slate-700">{user.role}</td>
                          <td className="px-6 py-4 text-center">
                            <span className={`px-2.5 py-1 text-[10px] font-bold rounded-lg ${
                              user.active ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
                            }`}>{user.active ? 'ACTIVE' : 'BLOCKED'}</span>
                          </td>
                          <td className="px-6 py-4 text-right">
                            <button
                              onClick={() => handleToggleUser(user.id)}
                              className="text-slate-400 hover:text-slate-800 transition-colors inline-block"
                            >
                              {user.active ? (
                                <ToggleRight className="h-7 w-7 text-emerald-500" />
                              ) : (
                                <ToggleLeft className="h-7 w-7 text-rose-500" />
                              )}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: PRODUCE APPROVAL & CATALOG MODERATION DESK */}
          {tab === 'products' && (() => {
            const pendingCrops = productsList.filter(p => p.status === 'PENDING_APPROVAL');
            const availableCrops = productsList.filter(p => p.status === 'AVAILABLE');
            const rejectedCrops = productsList.filter(p => p.status === 'REJECTED');

            const filteredList = productsList.filter(p => {
              if (productFilter === 'PENDING' && p.status !== 'PENDING_APPROVAL') return false;
              if (productFilter === 'AVAILABLE' && p.status !== 'AVAILABLE') return false;
              if (productFilter === 'REJECTED' && p.status !== 'REJECTED') return false;
              if (productSearch) {
                const q = productSearch.toLowerCase();
                const matchCrop = (p.cropName || '').toLowerCase().includes(q);
                const matchVar = (p.variety || '').toLowerCase().includes(q);
                const matchFarmer = (p.farmerName || '').toLowerCase().includes(q);
                const matchLoc = (p.location || '').toLowerCase().includes(q);
                return matchCrop || matchVar || matchFarmer || matchLoc;
              }
              return true;
            });

            return (
              <div className="space-y-6 text-left">
                {/* Header & Quick Stats */}
                <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium space-y-6">
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div>
                      <h3 className="text-xl font-black text-slate-800 tracking-tight flex items-center space-x-2.5">
                        <span>Produce Moderation & Price Approval Desk</span>
                        {pendingCrops.length > 0 && (
                          <span className="bg-amber-100 text-amber-800 text-xs font-black px-2.5 py-0.5 rounded-full animate-pulse">
                            {pendingCrops.length} Awaiting Decision
                          </span>
                        )}
                      </h3>
                      <p className="text-xs text-slate-400 mt-1">
                        Farmers upload harvest photos and suggest selling prices. Review photo quality, verify fair pricing, and approve listings before buyers can purchase.
                      </p>
                    </div>

                    {/* Summary Quick Metric Chips */}
                    <div className="flex flex-wrap gap-2">
                      <button
                        onClick={() => setProductFilter('PENDING')}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 ${
                          productFilter === 'PENDING'
                            ? 'bg-amber-500 text-white shadow-sm ring-2 ring-amber-500/20'
                            : 'bg-amber-50 text-amber-700 hover:bg-amber-100'
                        }`}
                      >
                        <Clock className="h-3.5 w-3.5" />
                        <span>Awaiting Review ({pendingCrops.length})</span>
                      </button>

                      <button
                        onClick={() => setProductFilter('AVAILABLE')}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 ${
                          productFilter === 'AVAILABLE'
                            ? 'bg-emerald-600 text-white shadow-sm ring-2 ring-emerald-600/20'
                            : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                        }`}
                      >
                        <CheckCircle2 className="h-3.5 w-3.5" />
                        <span>Live on Market ({availableCrops.length})</span>
                      </button>

                      <button
                        onClick={() => setProductFilter('REJECTED')}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 ${
                          productFilter === 'REJECTED'
                            ? 'bg-rose-500 text-white shadow-sm ring-2 ring-rose-500/20'
                            : 'bg-rose-50 text-rose-700 hover:bg-rose-100'
                        }`}
                      >
                        <X className="h-3.5 w-3.5" />
                        <span>Rejected ({rejectedCrops.length})</span>
                      </button>

                      <button
                        onClick={() => setProductFilter('ALL')}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                          productFilter === 'ALL'
                            ? 'bg-slate-800 text-white shadow-sm'
                            : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                        }`}
                      >
                        All ({productsList.length})
                      </button>
                    </div>
                  </div>

                  {/* Search Bar */}
                  <div className="relative">
                    <Search className="h-4 w-4 text-slate-400 absolute left-4 top-1/2 -translate-y-1/2" />
                    <input 
                      type="text"
                      placeholder="Search produce by crop name, variety, farmer name, or harvest location..."
                      value={productSearch}
                      onChange={(e) => setProductSearch(e.target.value)}
                      className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:border-agrogreen-650 bg-slate-50/50 focus:bg-white transition-all"
                    />
                    {productSearch && (
                      <button 
                        onClick={() => setProductSearch('')}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </div>

                {/* Produce Table / Moderation Queue */}
                <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium">
                  {filteredList.length === 0 ? (
                    <div className="py-20 text-center text-slate-400 space-y-3">
                      <ShoppingBag className="h-12 w-12 mx-auto text-slate-200 stroke-[1.5]" />
                      <p className="text-sm font-semibold text-slate-600">No produce listings found matching your current filter.</p>
                      <p className="text-xs text-slate-400">
                        {productFilter === 'PENDING' ? 'No produce items are currently awaiting admin review.' : 'Try changing your search terms or filters.'}
                      </p>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm text-slate-500">
                        <thead className="text-xs text-slate-400 bg-slate-50 uppercase rounded-xl">
                          <tr>
                            <th className="px-5 py-3.5 rounded-l-xl text-left">Harvest Photo</th>
                            <th className="px-5 py-3.5 text-left">Crop Details</th>
                            <th className="px-5 py-3.5 text-left">Farmer / Seller</th>
                            <th className="px-5 py-3.5 text-left">Farmer's Suggested Rate</th>
                            <th className="px-5 py-3.5 text-left">Market Rate / Status</th>
                            <th className="px-5 py-3.5 text-right rounded-r-xl">Decision Actions</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {filteredList.map((prod) => {
                            const isPending = prod.status === 'PENDING_APPROVAL';
                            const isAvailable = prod.status === 'AVAILABLE';
                            const isRejected = prod.status === 'REJECTED';
                            const suggestedRate = prod.suggestedPricePerQuintal || prod.pricePerQuintal;

                            return (
                              <tr key={prod.id} className={`hover:bg-slate-50/60 transition-colors ${isPending ? 'bg-amber-50/20' : ''}`}>
                                
                                {/* Photo column with Inspection zoom */}
                                <td className="px-5 py-4">
                                  <div className="relative group/photo inline-block">
                                    {prod.imageUrl ? (
                                      <div 
                                        onClick={() => setPhotoPreviewModal({ url: prod.imageUrl, cropName: prod.cropName, farmer: prod.farmerName })}
                                        className="h-16 w-16 rounded-2xl overflow-hidden border border-slate-200 cursor-pointer relative shadow-sm hover:ring-2 hover:ring-agrogreen-650 transition-all"
                                        title="Click to inspect full-size photo"
                                      >
                                        <img src={prod.imageUrl} alt={prod.cropName} className="h-full w-full object-cover group-hover/photo:scale-105 transition-transform" />
                                        <div className="absolute inset-0 bg-slate-900/30 opacity-0 group-hover/photo:opacity-100 transition-opacity flex items-center justify-center text-white">
                                          <ZoomIn className="h-4 w-4" />
                                        </div>
                                      </div>
                                    ) : (
                                      <div className="h-16 w-16 rounded-2xl bg-slate-100 border border-dashed border-slate-300 flex flex-col items-center justify-center text-slate-400 p-1 text-center">
                                        <Leaf className="h-4 w-4 text-slate-300 mb-0.5" />
                                        <span className="text-[8px] font-bold uppercase text-slate-400">No Photo</span>
                                      </div>
                                    )}
                                    <span className={`block text-[9px] font-bold mt-1 uppercase text-center ${prod.imageUrl ? 'text-emerald-600' : 'text-amber-600'}`}>
                                      {prod.imageUrl ? 'Photo Attached' : 'Missing Photo'}
                                    </span>
                                  </div>
                                </td>

                                {/* Crop Details */}
                                <td className="px-5 py-4">
                                  <div className="space-y-1">
                                    <div className="flex items-center space-x-2">
                                      <span className="font-extrabold text-slate-800 text-sm">{prod.cropName}</span>
                                      <span className="text-[10px] bg-slate-100 font-bold px-2 py-0.5 rounded text-slate-500 uppercase">
                                        {prod.variety}
                                      </span>
                                    </div>
                                    <p className="text-xs text-slate-600 font-semibold">
                                      {prod.quantityQuintals} qtl <span className="text-slate-400 font-normal">({prod.quantityQuintals * 100} kg)</span>
                                    </p>
                                    <p className="text-[10px] text-slate-400">
                                      Harvested at: <strong className="text-slate-600">{prod.location}</strong>
                                    </p>
                                    {prod.description && (
                                      <p className="text-[10px] text-slate-400 italic max-w-xs truncate" title={prod.description}>
                                        "{prod.description}"
                                      </p>
                                    )}
                                  </div>
                                </td>

                                {/* Farmer / Seller Info */}
                                <td className="px-5 py-4">
                                  <div className="space-y-0.5">
                                    <span className="font-bold text-slate-800 text-xs block">{prod.farmerName}</span>
                                    <span className="text-[11px] text-slate-400 block">{prod.farmerPhone || 'Verified Farmer'}</span>
                                    <span className="text-[9px] bg-emerald-50 text-emerald-700 font-bold px-1.5 py-0.5 rounded inline-block">
                                      Farmer #{prod.farmerId}
                                    </span>
                                  </div>
                                </td>

                                {/* Farmer's Suggested Rate */}
                                <td className="px-5 py-4">
                                  <div className="bg-amber-50/70 border border-amber-100 p-2.5 rounded-2xl inline-block text-left">
                                    <span className="text-[9px] font-black uppercase text-amber-800 tracking-wider block mb-0.5">
                                      Farmer's Suggested
                                    </span>
                                    <span className="text-sm font-black text-amber-900 block">
                                      ₹{suggestedRate.toLocaleString('en-IN')}<span className="text-xs font-semibold text-amber-700">/qtl</span>
                                    </span>
                                    <span className="text-[10px] text-amber-600 font-bold block">
                                      ₹{(suggestedRate / 100).toFixed(2)}/kg
                                    </span>
                                  </div>
                                </td>

                                {/* Listed Rate & Status */}
                                <td className="px-5 py-4">
                                  <div className="space-y-1.5 text-left">
                                    <div>
                                      <span className={`px-2.5 py-1 text-[10px] font-black uppercase rounded-lg inline-flex items-center space-x-1 ${
                                        isPending ? 'bg-amber-100 text-amber-800 ring-1 ring-amber-300' :
                                        isAvailable ? 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' :
                                        isRejected ? 'bg-rose-50 text-rose-700 ring-1 ring-rose-200' :
                                        'bg-slate-100 text-slate-600'
                                      }`}>
                                        {isPending && <Clock className="h-3 w-3 mr-1 inline" />}
                                        {isAvailable && <CheckCircle2 className="h-3 w-3 mr-1 inline" />}
                                        {isRejected && <X className="h-3 w-3 mr-1 inline" />}
                                        <span>{isPending ? 'Awaiting Approval' : prod.status}</span>
                                      </span>
                                    </div>

                                    {isAvailable && (
                                      <div className="text-xs font-bold text-slate-700">
                                        Live Price: <span className="text-emerald-700 font-extrabold">₹{prod.pricePerQuintal}/qtl</span>
                                      </div>
                                    )}

                                    {prod.adminFeedback && (
                                      <p className="text-[10px] text-slate-500 italic max-w-xs bg-slate-50 p-1.5 rounded-lg border border-slate-100">
                                        <strong>Admin note:</strong> {prod.adminFeedback}
                                      </p>
                                    )}
                                  </div>
                                </td>

                                {/* Action Buttons */}
                                <td className="px-5 py-4 text-right">
                                  <div className="flex items-center justify-end space-x-2">
                                    {/* Quick Approve Button */}
                                    {isPending && (
                                      <button
                                        onClick={() => handleQuickApprove(prod)}
                                        className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-3 py-2 rounded-xl text-xs transition-all shadow-sm flex items-center space-x-1"
                                        title={`Approve at suggested rate ₹${suggestedRate}/qtl`}
                                      >
                                        <Check className="h-3.5 w-3.5" />
                                        <span className="hidden xl:inline">Quick Approve</span>
                                      </button>
                                    )}

                                    {/* Inspect Photo & Decide Price Button */}
                                    <button
                                      onClick={() => handleOpenInspect(prod)}
                                      className="bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold px-3 py-2 rounded-xl text-xs transition-all flex items-center space-x-1 border border-indigo-200"
                                      title="Inspect photo, adjust price, or edit details before approving"
                                    >
                                      <Sliders className="h-3.5 w-3.5" />
                                      <span className="hidden xl:inline">{isPending ? 'Inspect & Price' : 'Edit / Adjust'}</span>
                                    </button>

                                    {/* Reject Button */}
                                    {isPending && (
                                      <button
                                        onClick={() => handleOpenReject(prod)}
                                        className="bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold px-2.5 py-2 rounded-xl text-xs transition-all border border-rose-200"
                                        title="Reject with feedback reason for farmer"
                                      >
                                        <X className="h-3.5 w-3.5" />
                                        <span className="hidden xl:inline">Reject</span>
                                      </button>
                                    )}

                                    {/* Delete Button */}
                                    <button
                                      onClick={() => handleDeleteProduct(prod.id)}
                                      className="text-slate-400 hover:text-rose-600 p-2 hover:bg-rose-50 rounded-xl transition-all"
                                      title="Remove from platform"
                                    >
                                      <Trash2 className="h-4 w-4" />
                                    </button>
                                  </div>
                                </td>

                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            );
          })()}

          {/* TAB 4: RESOLVE COMPLAINTS */}
          {tab === 'complaints' && (
            <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left">
              <h3 className="text-xl font-bold text-slate-800 mb-6">Grievance Resolution Desk</h3>

              {complaintsList.length === 0 ? (
                <p className="text-sm text-slate-400">No dispute complaints in resolution desk.</p>
              ) : (
                <div className="space-y-6">
                  {complaintsList.map((comp) => (
                    <div key={comp.id} className="p-6 border border-slate-100 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-6 hover:shadow-sm transition-shadow">
                      <div className="space-y-2">
                        <div className="flex items-center space-x-2.5">
                          <span className="font-bold text-slate-800 text-lg">#COMP-{comp.id}</span>
                          <span className={`px-2 py-0.5 text-[9px] font-bold uppercase rounded-md ${
                            comp.status === 'RESOLVED' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'
                          }`}>{comp.status}</span>
                        </div>
                        <h4 className="font-bold text-slate-700">{comp.title}</h4>
                        <div className="flex items-center space-x-2 text-xs text-slate-400">
                          {comp.user?.profilePhoto ? (
                            <img 
                              src={comp.user.profilePhoto} 
                              alt="Grievant Avatar" 
                              className="h-6 w-6 rounded-full object-cover border border-slate-200 shadow-sm" 
                            />
                          ) : (
                            <div className="h-6 w-6 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                              <User className="h-3 w-3" />
                            </div>
                          )}
                          <p>Grievant Email: <strong>{comp.user?.email || 'farmer@mandiplus.com'}</strong></p>
                        </div>
                        <p className="text-xs text-slate-500 italic max-w-xl">"{comp.description}"</p>
                      </div>

                      <div className="text-left md:text-right space-y-3 min-w-[200px]">
                        {comp.status === 'PENDING' && (
                          <button
                            onClick={() => handleResolveComplaint(comp.id, 'RESOLVED')}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2 px-4 rounded-xl text-xs transition-colors flex items-center space-x-1 justify-start md:justify-end"
                          >
                            <CheckCircle2 className="h-4 w-4 mr-1" />
                            <span>Resolve Dispute</span>
                          </button>
                        )}
                        {comp.status === 'RESOLVED' && (
                          <span className="text-emerald-600 text-xs font-bold flex items-center justify-start md:justify-end space-x-1">
                            <CheckCircle2 className="h-4.5 w-4.5" />
                            <span>Resolved Case</span>
                          </span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 6: APMC MANDI PRICE CONTROL DESK */}
          {tab === 'mandi' && (
            <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium text-left">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h3 className="text-xl font-bold text-slate-800">{t('mandiDashboard')}</h3>
                  <p className="text-xs text-slate-400 mt-1">Configure and manage live APMC Mandi rates populated on the website</p>
                </div>
                <button
                  onClick={handleOpenAddMandi}
                  className="bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-2.5 px-5 rounded-xl text-xs transition-all flex items-center space-x-1.5"
                >
                  <Plus className="h-4 w-4" />
                  <span>{t('addMandiRate')}</span>
                </button>
              </div>

              {mandiList.length === 0 ? (
                <p className="text-sm text-slate-400">No APMC Mandi rates listed.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm text-slate-500">
                    <thead className="text-xs text-slate-400 bg-slate-50 uppercase rounded-xl">
                      <tr>
                        <th className="px-6 py-3.5 rounded-l-xl text-left">ID</th>
                        <th className="px-6 py-3.5 text-left">{t('cropName')}</th>
                        <th className="px-6 py-3.5 text-left">{t('marketName')}</th>
                        <th className="px-6 py-3.5 text-left">{t('state')}</th>
                        <th className="px-6 py-3.5 text-left">{t('pricePerQuintal')}</th>
                        <th className="px-6 py-3.5 text-left">{t('priceChangeYesterday')}</th>
                        <th className="px-6 py-3.5 text-right rounded-r-xl">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {mandiList.map((item) => (
                        <tr key={item.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50/50">
                          <td className="px-6 py-4 font-bold text-slate-700">#MND-{item.id}</td>
                          <td className="px-6 py-4">
                            <div className="text-left">
                              <span className="font-bold text-slate-800 block">{item.cropName}</span>
                              <span className="text-[10px] text-slate-400 font-semibold leading-none">{item.variety}</span>
                            </div>
                          </td>
                          <td className="px-6 py-4">{item.marketName}</td>
                          <td className="px-6 py-4">{item.state}</td>
                          <td className="px-6 py-4 font-semibold text-slate-800">₹{item.pricePerQuintal}/qtl (₹{item.pricePerQuintal / 100}/kg)</td>
                          <td className="px-6 py-4 font-semibold">
                            <span className={item.priceChangeYesterday >= 0 ? 'text-emerald-600' : 'text-rose-600'}>
                              {item.priceChangeYesterday >= 0 ? '+' : ''}{item.priceChangeYesterday}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-right space-x-2">
                            <button
                              onClick={() => handleOpenEditMandi(item)}
                              className="text-indigo-600 hover:text-indigo-700 px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 rounded-xl transition-all font-bold text-xs"
                            >
                              Edit
                            </button>
                            <button
                              onClick={() => handleDeleteMandi(item.id)}
                              className="text-rose-600 hover:text-rose-700 p-2 bg-rose-50 hover:bg-rose-100 rounded-xl transition-all inline-block"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 7: DELIVERY LOGISTICS & OVERSIGHT DESK */}
          {tab === 'deliveries' && (
            <div className="space-y-6 text-left">
              {/* Top Overview Card */}
              <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium space-y-6">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                  <div>
                    <h3 className="text-xl font-black text-slate-800 tracking-tight flex items-center space-x-2.5">
                      <Bike className="h-6 w-6 text-emerald-600" />
                      <span>Delivery Logistics & Fleet Oversight</span>
                      {unacceptedDeliveries.length > 0 && (
                        <span className="bg-amber-100 text-amber-800 text-xs font-black px-2.5 py-0.5 rounded-full animate-pulse">
                          {unacceptedDeliveries.length} Unaccepted Consignments
                        </span>
                      )}
                    </h3>
                    <p className="text-xs text-slate-400 mt-1">
                      Enforce partner confirmation workflow: tracking activates only upon verified partner acceptance. Review audit history, monitor live GPS, and reassign unaccepted dispatches.
                    </p>
                  </div>

                  {/* Summary Metric Chips */}
                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={() => setDeliveryFilter('ALL')}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                        deliveryFilter === 'ALL'
                          ? 'bg-slate-900 text-white shadow-sm'
                          : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                      }`}
                    >
                      All ({deliveriesList.length})
                    </button>
                    <button
                      onClick={() => setDeliveryFilter('DELIVERY_PENDING')}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                        deliveryFilter === 'DELIVERY_PENDING'
                          ? 'bg-amber-500 text-white shadow-sm'
                          : 'bg-amber-50 text-amber-700 hover:bg-amber-100'
                      }`}
                    >
                      Awaiting Partner ({deliveriesList.filter(d => !d.partnerConfirmed).length})
                    </button>
                    <button
                      onClick={() => setDeliveryFilter('IN_TRANSIT')}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                        deliveryFilter === 'IN_TRANSIT'
                          ? 'bg-blue-600 text-white shadow-sm'
                          : 'bg-blue-50 text-blue-700 hover:bg-blue-100'
                      }`}
                    >
                      In Transit ({deliveriesList.filter(d => d.status === 'IN_TRANSIT').length})
                    </button>
                    <button
                      onClick={() => setDeliveryFilter('DELIVERED')}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                        deliveryFilter === 'DELIVERED'
                          ? 'bg-emerald-600 text-white shadow-sm'
                          : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                      }`}
                    >
                      Delivered ({deliveriesList.filter(d => d.status === 'DELIVERED').length})
                    </button>
                  </div>
                </div>

                {/* Quick stats counters */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  <div className="bg-slate-50 border border-slate-100 p-4 rounded-2xl">
                    <span className="text-[10px] font-black uppercase text-slate-400 block tracking-wider">Total Consignments</span>
                    <span className="text-2xl font-black text-slate-800">{deliveriesList.length}</span>
                  </div>
                  <div className="bg-amber-50/60 border border-amber-200/80 p-4 rounded-2xl">
                    <span className="text-[10px] font-black uppercase text-amber-700 block tracking-wider">Awaiting Confirmation</span>
                    <span className="text-2xl font-black text-amber-800">{deliveriesList.filter(d => !d.partnerConfirmed).length}</span>
                  </div>
                  <div className="bg-blue-50/60 border border-blue-200/80 p-4 rounded-2xl">
                    <span className="text-[10px] font-black uppercase text-blue-700 block tracking-wider">Out for Delivery</span>
                    <span className="text-2xl font-black text-blue-800">{deliveriesList.filter(d => d.status === 'IN_TRANSIT').length}</span>
                  </div>
                  <div className="bg-emerald-50/60 border border-emerald-200/80 p-4 rounded-2xl">
                    <span className="text-[10px] font-black uppercase text-emerald-700 block tracking-wider">Delivered with OTP</span>
                    <span className="text-2xl font-black text-emerald-800">{deliveriesList.filter(d => d.status === 'DELIVERED').length}</span>
                  </div>
                </div>
              </div>

              {/* Unaccepted / Overdue Alert Desk */}
              {unacceptedDeliveries.length > 0 && (
                <div className="bg-amber-50 border border-amber-200 rounded-[28px] p-6 space-y-4 shadow-sm">
                  <div className="flex items-center space-x-3">
                    <div className="h-10 w-10 rounded-xl bg-amber-500 text-white flex items-center justify-center font-bold">
                      <AlertCircle className="h-5 w-5" />
                    </div>
                    <div>
                      <h4 className="font-black text-amber-950 text-base">Unaccepted & Overdue Delivery Assignments</h4>
                      <p className="text-xs text-amber-800">
                        These orders are awaiting partner confirmation. Reassign them immediately to prevent delivery delays.
                      </p>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {unacceptedDeliveries.map(t => (
                      <div key={t.id} className="bg-white p-4 rounded-2xl border border-amber-200 shadow-xs flex flex-col justify-between space-y-3">
                        <div>
                          <div className="flex justify-between items-start">
                            <span className="font-mono text-xs font-black text-slate-800">{t.taskCode}</span>
                            <span className="text-[10px] font-black uppercase px-2 py-0.5 rounded-full bg-amber-100 text-amber-800">
                              {t.status}
                            </span>
                          </div>
                          <p className="font-bold text-sm text-slate-800 mt-1">{t.cropName} ({t.quantityQuintals} qtl)</p>
                          <p className="text-xs text-slate-400 mt-0.5 line-clamp-1">From: {t.pickupLocation}</p>
                          <p className="text-xs text-slate-400 line-clamp-1">To: {t.dropLocation}</p>
                        </div>
                        <button
                          onClick={() => handleOpenReassignModal(t)}
                          className="w-full bg-amber-500 hover:bg-amber-600 text-white font-bold py-2 rounded-xl text-xs transition-colors flex items-center justify-center space-x-1"
                        >
                          <RefreshCw className="h-3.5 w-3.5" />
                          <span>Reassign Partner</span>
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Full Deliveries Table */}
              <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium space-y-6">
                <div className="flex justify-between items-center">
                  <div>
                    <h4 className="text-lg font-black text-slate-800 tracking-tight">Consignment Registry</h4>
                    <p className="text-xs text-slate-400">Complete listing of all platform logistics dispatches</p>
                  </div>
                  <button
                    onClick={fetchAdminData}
                    className="p-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors text-xs font-bold flex items-center space-x-1.5"
                  >
                    <RefreshCw className="h-3.5 w-3.5" />
                    <span>Refresh</span>
                  </button>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-slate-600">
                    <thead className="bg-slate-50 uppercase text-[10px] text-slate-400 font-bold rounded-xl">
                      <tr>
                        <th className="px-4 py-3 text-left rounded-l-xl">Consignment / Order</th>
                        <th className="px-4 py-3 text-left">Crop & Quantity</th>
                        <th className="px-4 py-3 text-left">Farmgate Origin</th>
                        <th className="px-4 py-3 text-left">Assigned Partner</th>
                        <th className="px-4 py-3 text-left">Status</th>
                        <th className="px-4 py-3 text-left">Confirmation</th>
                        <th className="px-4 py-3 text-right rounded-r-xl">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {deliveriesList
                        .filter(d => deliveryFilter === 'ALL' || d.status === deliveryFilter)
                        .map(item => (
                          <tr key={item.id} className="hover:bg-slate-50/60 transition-colors">
                            <td className="px-4 py-3.5">
                              <span className="font-mono font-bold text-slate-800 block">{item.taskCode}</span>
                              <span className="text-[10px] text-slate-400">Order #AGR-{item.orderId || item.id}</span>
                            </td>
                            <td className="px-4 py-3.5">
                              <span className="font-bold text-slate-800 block">{item.cropName}</span>
                              <span className="text-[10px] text-slate-400">{item.quantityQuintals} Quintals · {item.variety}</span>
                            </td>
                            <td className="px-4 py-3.5 max-w-xs">
                              <span className="line-clamp-1 block text-slate-700">{item.pickupLocation}</span>
                              <span className="text-[10px] text-slate-400 line-clamp-1">To: {item.dropLocation}</span>
                            </td>
                            <td className="px-4 py-3.5">
                              {item.partnerName ? (
                                <div>
                                  <span className="font-bold text-slate-800 block">{item.partnerName}</span>
                                  <span className="text-[10px] text-slate-400 font-mono">{item.vehicleNumber || 'Bike'}</span>
                                </div>
                              ) : (
                                <span className="text-slate-400 italic">Unassigned</span>
                              )}
                            </td>
                            <td className="px-4 py-3.5">
                              <span className={`px-2.5 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${
                                item.status === 'DELIVERED' ? 'bg-emerald-100 text-emerald-800' :
                                item.status === 'IN_TRANSIT' ? 'bg-blue-100 text-blue-800' :
                                item.status === 'PICKUP_CONFIRMED' ? 'bg-purple-100 text-purple-800' :
                                item.status === 'DELIVERY_ACCEPTED' ? 'bg-teal-100 text-teal-800' :
                                'bg-amber-100 text-amber-800'
                              }`}>
                                {item.status}
                              </span>
                            </td>
                            <td className="px-4 py-3.5">
                              {item.partnerConfirmed ? (
                                <span className="text-emerald-700 font-bold flex items-center space-x-1 text-[11px]">
                                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                                  <span>Confirmed</span>
                                </span>
                              ) : (
                                <span className="text-amber-700 font-bold flex items-center space-x-1 text-[11px] animate-pulse">
                                  <Clock className="h-3.5 w-3.5 text-amber-600" />
                                  <span>Pending Acceptance</span>
                                </span>
                              )}
                            </td>
                            <td className="px-4 py-3.5 text-right space-x-1.5 whitespace-nowrap">
                              <button
                                onClick={() => handleOpenAuditModal(item)}
                                className="px-2.5 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-bold transition-colors inline-flex items-center space-x-1"
                                title="View status audit trail"
                              >
                                <History className="h-3.5 w-3.5" />
                                <span>Audit</span>
                              </button>
                              <button
                                onClick={() => handleOpenReassignModal(item)}
                                className="px-2.5 py-1.5 bg-amber-50 hover:bg-amber-100 text-amber-700 rounded-lg text-xs font-bold transition-colors"
                              >
                                Reassign
                              </button>
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

        </main>
      </div>

      {/* Mandi Edit/Add Overlay Modal */}
      {mandiModalOpen && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4 z-50 animate-fade-in">
          <div className="bg-white rounded-[32px] border border-slate-100 p-8 shadow-premium w-full max-w-md text-left space-y-6">
            <div>
              <h3 className="text-xl font-bold text-slate-800">
                {editingMandi ? t('editMandiRate') : t('addMandiRate')}
              </h3>
              <p className="text-xs text-slate-400 mt-1">Submit mandi crop rates to populate dynamically on frontend widgets.</p>
            </div>

            <form onSubmit={handleSaveMandi} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('cropName')}</label>
                <input 
                  type="text" required placeholder="e.g. Onion" value={mCropName} 
                  onChange={(e) => setMCropName(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" 
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('variety')}</label>
                <input 
                  type="text" required placeholder="e.g. Nasik Red" value={mVariety} 
                  onChange={(e) => setMVariety(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" 
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('marketName')}</label>
                  <input 
                    type="text" required placeholder="Lasalgaon Mandi" value={mMarketName} 
                    onChange={(e) => setMMarketName(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" 
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('state')}</label>
                  <select 
                    value={mState} onChange={(e) => setMState(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm bg-white"
                  >
                    <option value="Maharashtra">Maharashtra</option>
                    <option value="Gujarat">Gujarat</option>
                    <option value="Karnataka">Karnataka</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('pricePerQuintal')}</label>
                  <input 
                    type="number" step="0.01" required placeholder="1850" value={mPricePerQuintal} 
                    onChange={(e) => setMPricePerQuintal(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" 
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">{t('priceChangeYesterday')}</label>
                  <input 
                    type="number" step="0.01" required placeholder="0" value={mPriceChange} 
                    onChange={(e) => setMPriceChange(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" 
                  />
                </div>
              </div>

              <div className="flex space-x-3 pt-4">
                <button 
                  type="submit"
                  className="flex-1 bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-3 rounded-xl text-xs transition-all"
                >
                  {t('save')}
                </button>
                <button 
                  type="button" onClick={() => setMandiModalOpen(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-3 rounded-xl text-xs transition-all"
                >
                  {t('cancel')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {/* Photo Lightbox Modal */}
      {photoPreviewModal && (
        <div 
          onClick={() => setPhotoPreviewModal(null)}
          className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 z-[100] animate-fade-in"
        >
          <div 
            onClick={(e) => e.stopPropagation()}
            className="bg-white rounded-[28px] overflow-hidden shadow-2xl max-w-2xl w-full border border-slate-100 flex flex-col max-h-[90vh]"
          >
            <div className="p-4 sm:p-5 border-b border-slate-100 flex items-center justify-between bg-white shrink-0">
              <div className="text-left">
                <h4 className="font-extrabold text-base text-slate-800">{photoPreviewModal.cropName} - Harvest Inspection Photo</h4>
                <p className="text-xs text-slate-400">Uploaded by farmer {photoPreviewModal.farmer}</p>
              </div>
              <button 
                onClick={() => setPhotoPreviewModal(null)}
                className="p-2 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="p-4 bg-slate-900 flex items-center justify-center overflow-auto max-h-[75vh]">
              <img 
                src={photoPreviewModal.url} 
                alt="Full preview" 
                className="max-h-[70vh] max-w-full object-contain rounded-xl shadow-lg"
              />
            </div>
            <div className="p-4 bg-slate-50 border-t border-slate-100 text-xs text-slate-500 text-left flex justify-between items-center">
              <span>Photo Quality Verification Desk</span>
              <button
                onClick={() => setPhotoPreviewModal(null)}
                className="bg-slate-800 hover:bg-slate-900 text-white font-bold px-4 py-2 rounded-xl text-xs"
              >
                Close Preview
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Inspect Photo & Decide Rate Modal */}
      {inspectingProduct && (
        <div className="fixed inset-0 bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4 z-[90] animate-fade-in overflow-y-auto">
          <div className="bg-white rounded-[32px] border border-slate-100 shadow-2xl w-full max-w-4xl text-left overflow-hidden my-8">
            <div className="p-6 sm:p-8 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
              <div>
                <span className="text-[10px] font-black uppercase text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full border border-indigo-100">
                  Produce Quality & Price Evaluation
                </span>
                <h3 className="text-xl font-black text-slate-800 mt-2">
                  Inspect & Decide Market Rate for {inspectingProduct.cropName}
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Verify photo coordinates and decide whether to approve at farmer's suggested rate or adjust before publishing live to buyers.
                </p>
              </div>
              <button 
                onClick={() => setInspectingProduct(null)}
                className="p-2 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleSaveAndApprove} className="p-6 sm:p-8 space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                
                {/* Left Column: Photo & Farmer Reference */}
                <div className="lg:col-span-5 space-y-4">
                  <div className="rounded-2xl border border-slate-200 overflow-hidden bg-slate-50 aspect-square relative flex items-center justify-center shadow-sm">
                    {editImageUrl ? (
                      <img src={editImageUrl} alt="Crop inspection preview" className="w-full h-full object-cover" />
                    ) : (
                      <div className="text-center p-6 text-slate-300">
                        <Leaf className="h-12 w-12 mx-auto mb-2 text-slate-300" />
                        <span className="text-xs font-bold uppercase">No photo uploaded</span>
                      </div>
                    )}
                    {editImageUrl && (
                      <button
                        type="button"
                        onClick={() => setPhotoPreviewModal({ url: editImageUrl, cropName: editCropName, farmer: inspectingProduct.farmerName })}
                        className="absolute bottom-3 right-3 bg-slate-900/80 hover:bg-slate-900 text-white p-2 rounded-xl text-xs backdrop-blur-sm shadow flex items-center space-x-1"
                      >
                        <ZoomIn className="h-3.5 w-3.5" />
                        <span className="text-[11px] font-bold">Zoom</span>
                      </button>
                    )}
                  </div>

                  {/* Farmer Benchmark Card */}
                  <div className="bg-amber-50/80 border border-amber-200 rounded-2xl p-4 space-y-2 text-left">
                    <span className="text-[10px] font-black uppercase text-amber-800 tracking-wider block">
                      Farmer's Suggested Rate
                    </span>
                    <div className="flex items-baseline space-x-2">
                      <span className="text-2xl font-black text-amber-950">
                        ₹{(inspectingProduct.suggestedPricePerQuintal || inspectingProduct.pricePerQuintal)?.toLocaleString('en-IN')}
                      </span>
                      <span className="text-xs font-bold text-amber-700">/ quintal</span>
                    </div>
                    <p className="text-[11px] text-amber-800">
                      Equivalent: ₹{(((inspectingProduct.suggestedPricePerQuintal || inspectingProduct.pricePerQuintal) || 0) / 100).toFixed(2)}/kg
                    </p>
                    <div className="pt-2 border-t border-amber-200/60 text-[11px] text-slate-600">
                      Seller: <strong>{inspectingProduct.farmerName}</strong> ({inspectingProduct.farmerPhone || 'Verified'})
                    </div>
                  </div>
                </div>

                {/* Right Column: Moderation Input Fields */}
                <div className="lg:col-span-7 space-y-4">
                  {/* Decision Pricing Control */}
                  <div className="bg-emerald-50 border border-emerald-200 p-4 rounded-2xl text-left">
                    <label className="block text-xs font-black text-emerald-900 uppercase tracking-wider mb-1">
                      Final Approved Marketplace Price (₹ per Quintal) *
                    </label>
                    <p className="text-[11px] text-emerald-700 mb-2.5">
                      You can keep the farmer's suggested rate or adjust to a fair Mandi benchmark rate.
                    </p>
                    <div className="relative">
                      <span className="absolute left-3.5 top-1/2 -translate-y-1/2 font-black text-emerald-800 text-sm">₹</span>
                      <input 
                        type="number"
                        step="0.01"
                        required
                        value={editApprovedPrice}
                        onChange={(e) => setEditApprovedPrice(e.target.value)}
                        placeholder="e.g. 2400"
                        className="w-full pl-8 pr-4 py-2.5 rounded-xl border border-emerald-300 font-black text-emerald-950 text-base focus:outline-none focus:ring-2 focus:ring-emerald-500 bg-white"
                      />
                    </div>
                    {editApprovedPrice && (
                      <span className="block text-[11px] font-bold text-emerald-800 mt-1.5">
                        = ₹{(parseFloat(editApprovedPrice || 0) / 100).toFixed(2)} per kg on live marketplace
                      </span>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Crop Name</label>
                      <input 
                        type="text" required value={editCropName}
                        onChange={(e) => setEditCropName(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none focus:border-agrogreen-650"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Variety</label>
                      <input 
                        type="text" required value={editVariety}
                        onChange={(e) => setEditVariety(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none focus:border-agrogreen-650"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Quantity (Quintals)</label>
                      <input 
                        type="number" step="0.1" required value={editQuantity}
                        onChange={(e) => setEditQuantity(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none focus:border-agrogreen-650"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Harvest Location</label>
                      <input 
                        type="text" required value={editLocation}
                        onChange={(e) => setEditLocation(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none focus:border-agrogreen-650"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Crop Image (URL or Base64)</label>
                    <input 
                      type="text" value={editImageUrl}
                      onChange={(e) => setEditImageUrl(e.target.value)}
                      placeholder="https://... or data:image/..."
                      className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs text-slate-800 focus:outline-none focus:border-agrogreen-650"
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1">Admin Approval Note / Feedback for Farmer</label>
                    <textarea 
                      rows={2}
                      value={editAdminFeedback}
                      onChange={(e) => setEditAdminFeedback(e.target.value)}
                      placeholder="e.g. Quality verified. Approved for buyer marketplace."
                      className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs text-slate-800 focus:outline-none focus:border-agrogreen-650 resize-none"
                    />
                  </div>
                </div>

              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-100">
                <button 
                  type="button" 
                  onClick={() => setInspectingProduct(null)}
                  className="px-5 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold text-xs transition-colors"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="bg-emerald-600 hover:bg-emerald-700 text-white font-black px-6 py-2.5 rounded-xl text-xs transition-all shadow-md shadow-emerald-700/20 flex items-center space-x-1.5"
                >
                  <Check className="h-4 w-4" />
                  <span>Approve & Publish to Marketplace</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reject with Feedback Modal */}
      {rejectingProduct && (
        <div className="fixed inset-0 bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4 z-[90] animate-fade-in">
          <div className="bg-white rounded-[32px] border border-slate-100 shadow-2xl w-full max-w-lg text-left overflow-hidden">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-rose-50/50">
              <div>
                <span className="text-[10px] font-black uppercase text-rose-600 bg-rose-100 px-2.5 py-1 rounded-full">
                  Reject Produce Listing
                </span>
                <h3 className="text-lg font-black text-slate-800 mt-2">
                  Return {rejectingProduct.cropName} ({rejectingProduct.variety})
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Provide a clear reason so farmer {rejectingProduct.farmerName} can update and re-submit.
                </p>
              </div>
              <button 
                onClick={() => setRejectingProduct(null)}
                className="p-2 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleConfirmReject} className="p-6 space-y-4">
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">
                  Quick Feedback Templates
                </label>
                <div className="flex flex-wrap gap-2">
                  {[
                    "Photo quality is blurry / dark. Please upload clear daylight harvest photos.",
                    "Suggested selling rate is significantly higher than APMC Mandi rates.",
                    "Incomplete harvest details or variety coordinates.",
                    "Produce quality does not meet marketplace standards."
                  ].map((preset, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => setRejectionReasonInput(preset)}
                      className="text-[10px] font-semibold text-slate-600 bg-slate-50 hover:bg-slate-100 border border-slate-200 px-2.5 py-1.5 rounded-lg text-left transition-colors"
                    >
                      {preset}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Rejection Reason / Guidance for Farmer *
                </label>
                <textarea 
                  rows={3}
                  required
                  value={rejectionReasonInput}
                  onChange={(e) => setRejectionReasonInput(e.target.value)}
                  placeholder="Explain why this listing is being returned..."
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs text-slate-800 focus:outline-none focus:border-rose-500 resize-none"
                />
              </div>

              <div className="flex items-center justify-end space-x-3 pt-2">
                <button 
                  type="button" 
                  onClick={() => setRejectingProduct(null)}
                  className="px-5 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-600 font-bold text-xs"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="bg-rose-600 hover:bg-rose-700 text-white font-black px-6 py-2.5 rounded-xl text-xs transition-all shadow-md shadow-rose-700/20 flex items-center space-x-1.5"
                >
                  <X className="h-4 w-4" />
                  <span>Confirm Rejection</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── DELIVERY REASSIGN MODAL ── */}
      {reassignModalOpen && selectedDeliveryForReassign && (
        <div className="fixed inset-0 bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4 z-[500] animate-fade-in text-left">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-md w-full shadow-2xl border border-slate-100 space-y-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="h-10 w-10 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center">
                  <RefreshCw className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="text-base font-black text-slate-800">Reassign Delivery Task</h3>
                  <span className="text-xs text-slate-400 font-mono">Consignment #{selectedDeliveryForReassign.taskCode}</span>
                </div>
              </div>
              <button
                onClick={() => setReassignModalOpen(false)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleReassignSubmit} className="space-y-4">
              <div className="bg-slate-50 p-3 rounded-2xl border border-slate-100 text-xs space-y-1">
                <p><strong>Crop:</strong> {selectedDeliveryForReassign.cropName} ({selectedDeliveryForReassign.quantityQuintals} qtl)</p>
                <p><strong>Current Status:</strong> {selectedDeliveryForReassign.status}</p>
                <p><strong>Current Partner:</strong> {selectedDeliveryForReassign.partnerName || 'None'}</p>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Select New Delivery Partner *
                </label>
                <select
                  required
                  value={reassignPartnerId}
                  onChange={(e) => setReassignPartnerId(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:outline-none focus:border-amber-500"
                >
                  <option value="">-- Choose Eligible Fleet Partner --</option>
                  <option value="1">Rahul Shinde (MH-15-BL-2026 · Verified Partner #1)</option>
                  <option value="2">Ajay Gaikwad (MH-15-AG-4412 · Verified Partner #2)</option>
                  <option value="3">Vikas Patil (MH-15-VP-9988 · Verified Partner #3)</option>
                  <option value="4">Pooja Deshmukh (MH-15-PD-7721 · Verified Partner #4)</option>
                  {usersList
                    .filter(u => u.role === 'DELIVERY_PARTNER' && ![1, 2, 3, 4].includes(u.id))
                    .map(u => (
                      <option key={u.id} value={u.id}>
                        {u.name || u.email} (Fleet ID #{u.id})
                      </option>
                    ))}
                </select>
                <span className="text-[10px] text-slate-400 mt-1 block">
                  Reassignment will offer the trip in DELIVERY_ASSIGNED status awaiting explicit acceptance.
                </span>
              </div>

              <div className="flex items-center justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setReassignModalOpen(false)}
                  className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 font-bold text-xs hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-black text-xs transition-colors shadow-md shadow-amber-500/20"
                >
                  Confirm Reassignment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── DELIVERY AUDIT TRAIL MODAL ── */}
      {auditModalOpen && (
        <div className="fixed inset-0 bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4 z-[500] animate-fade-in text-left">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-2xl w-full shadow-2xl border border-slate-100 space-y-6 max-h-[85vh] flex flex-col">
            <div className="flex items-center justify-between shrink-0">
              <div className="flex items-center space-x-3">
                <div className="h-10 w-10 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
                  <History className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="text-base font-black text-slate-800">Immutable Delivery Audit Log</h3>
                  <span className="text-xs text-slate-400 font-mono">Consignment #{selectedDeliveryCode}</span>
                </div>
              </div>
              <button
                onClick={() => setAuditModalOpen(false)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto space-y-3 pr-1">
              {auditLoading ? (
                <div className="py-12 text-center text-slate-400 text-xs">Loading audit trail...</div>
              ) : auditLogs.length === 0 ? (
                <div className="py-12 text-center text-slate-400 text-xs">No status change events logged for this consignment.</div>
              ) : (
                <div className="space-y-3">
                  {auditLogs.map((log) => (
                    <div key={log.id} className="p-3.5 rounded-2xl border border-slate-100 bg-slate-50/70 space-y-1.5">
                      <div className="flex items-center justify-between">
                        <span className="font-mono text-[10px] font-black uppercase tracking-wider bg-slate-200 text-slate-700 px-2 py-0.5 rounded-md">
                          {log.action}
                        </span>
                        <span className="text-[10px] text-slate-400">
                          {log.createdAt ? new Date(log.createdAt).toLocaleString('en-IN') : 'Just now'}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2 text-xs font-bold text-slate-800">
                        <span>Transition:</span>
                        <span className="font-mono text-slate-500">{log.previousStatus || 'INIT'}</span>
                        <span>→</span>
                        <span className="font-mono text-emerald-700">{log.newStatus}</span>
                      </div>
                      <p className="text-xs text-slate-600">{log.remarks}</p>
                      <div className="text-[10px] text-slate-400 font-medium pt-0.5 flex items-center space-x-2">
                        <span>Actor: <strong>{log.actorEmail}</strong></span>
                        <span>•</span>
                        <span>Role: <strong>{log.actorRole}</strong></span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="pt-2 border-t border-slate-100 flex justify-end shrink-0">
              <button
                onClick={() => setAuditModalOpen(false)}
                className="px-5 py-2.5 rounded-xl bg-slate-900 text-white font-bold text-xs hover:bg-slate-800"
              >
                Close Audit Log
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
