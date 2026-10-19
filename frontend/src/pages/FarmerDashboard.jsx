import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import WeatherWidget from '../components/WeatherWidget';
import MandiWidget from '../components/MandiWidget';
import api from '../services/api';
import { useLanguage } from '../context/LanguageContext';
import { useAuth } from '../context/AuthContext';
import ComplaintsDesk from '../components/ComplaintsDesk';
import { 
  Plus, CheckCircle, Clock, ShoppingBag, 
  IndianRupee, Leaf, Warehouse, Calendar, ArrowRight, ShieldCheck,
  Edit3, Trash2, User, AlertCircle, XCircle, Info
} from 'lucide-react';

const CROP_SUGGESTIONS = [
  "Apple",
  "Banana",
  "Beetroot",
  "Brinjal (Eggplant)",
  "Cabbage",
  "Carrot",
  "Cauliflower",
  "Chickpeas",
  "Chili (Red/Green)",
  "Coconut",
  "Coriander",
  "Cotton",
  "Cucumber",
  "Garlic",
  "Ginger",
  "Grapes",
  "Groundnut",
  "Guava",
  "Lemon",
  "Maize",
  "Mango",
  "Mustard",
  "Okra (Bhindi)",
  "Onion",
  "Orange",
  "Papaya",
  "Pineapple",
  "Pomegranate",
  "Potato",
  "Rice (Paddy)",
  "Soybean",
  "Spinach",
  "Strawberry",
  "Sugarcane",
  "Tomato",
  "Turmeric",
  "Watermelon",
  "Wheat"
];

export default function FarmerDashboard({ tab = 'dashboard' }) {
  const { t } = useLanguage();
  const { user } = useAuth();
  
  const farmerLocation = user && user.district && user.state 
    ? `${user.district}, ${user.state}`
    : 'Nashik, Maharashtra';

  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [facilities, setFacilities] = useState([]);

  // Add Produce form state
  const [cropName, setCropName] = useState('');
  const [isCustomCrop, setIsCustomCrop] = useState(false);
  const [variety, setVariety] = useState('');
  const [quantity, setQuantity] = useState('');
  const [price, setPrice] = useState('');
  const [desc, setDesc] = useState('');
  const [location, setLocation] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [message, setMessage] = useState('');
  const [editingProductId, setEditingProductId] = useState(null);
  const [rejectConfirmOrderId, setRejectConfirmOrderId] = useState(null);
  const [approveOrderId, setApproveOrderId] = useState(null);
  const [estDeliveryDateInput, setEstDeliveryDateInput] = useState('');
  const [farmerProductFilter, setFarmerProductFilter] = useState('ALL'); // 'ALL' | 'PENDING' | 'AVAILABLE' | 'REJECTED'

  // Book Storage form state
  const [selectedFacility, setSelectedFacility] = useState('');
  const [bookingQty, setBookingQty] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [bookingMessage, setBookingMessage] = useState('');

  const fetchFarmerData = async () => {
    try {
      const prodRes = await api.get('/farmer/products');
      setProducts(prodRes.data);

      const ordRes = await api.get('/farmer/orders');
      setOrders(ordRes.data);

      const bookRes = await api.get('/farmer/cold-storage/bookings');
      setBookings(bookRes.data);

      const facRes = await api.get('/public/cold-storage');
      setFacilities(facRes.data);
    } catch (error) {
      console.error("Error loading farmer dashboard data", error);
    }
  };

  useEffect(() => {
    fetchFarmerData();

    const handleWebSocketAlert = (e) => {
      const data = e.detail;
      if (data.category === 'ORDER' || data.category === 'COLD_STORAGE' || data.category === 'MARKETPLACE' || data.category === 'PAYMENT') {
        fetchFarmerData();
      }
    };
    
    window.addEventListener('mandiplus-alert', handleWebSocketAlert);
    return () => {
      window.removeEventListener('mandiplus-alert', handleWebSocketAlert);
    };
  }, [tab]);

  // Calculations
  const totalEarnings = orders
    .filter(o => o.status === 'DELIVERED')
    .reduce((sum, o) => sum + o.totalPrice, 0);

  const pendingOrders = orders.filter(o => o.status === 'PENDING' || o.status === 'APPROVED');
  const activeBookings = bookings.filter(b => b.status === 'APPROVED' || b.status === 'ACTIVE');

  // Submit new crop product or edit existing
  const handleAddProduce = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const payload = {
        cropName,
        variety,
        quantityQuintals: parseFloat(quantity),
        pricePerQuintal: parseFloat(price),
        suggestedPricePerQuintal: parseFloat(price),
        description: desc,
        location: location,
        imageUrl: imageUrl
      };

      if (editingProductId) {
        await api.put(`/farmer/products/${editingProductId}`, payload);
        setMessage('Crop listing updated and re-submitted for Admin review!');
      } else {
        await api.post('/farmer/products', payload);
        setMessage('Crop listing submitted for review! Mandi+ Admin will inspect photos and price before publishing live to buyers.');
      }

      setCropName('');
      setIsCustomCrop(false);
      setVariety('');
      setQuantity('');
      setPrice('');
      setDesc('');
      setLocation('');
      setImageUrl('');
      setEditingProductId(null);
      fetchFarmerData();
    } catch (err) {
      setMessage('Failed to save listing. ' + (err.response?.data || err.message));
    }
  };

  // Start editing a product
  const handleStartEdit = (product) => {
    setCropName(product.cropName);
    setIsCustomCrop(!CROP_SUGGESTIONS.includes(product.cropName));
    setVariety(product.variety);
    setQuantity(product.quantityQuintals.toString());
    setPrice((product.suggestedPricePerQuintal != null ? product.suggestedPricePerQuintal : product.pricePerQuintal).toString());
    setDesc(product.description || '');
    setLocation(product.location);
    setImageUrl(product.imageUrl || '');
    setEditingProductId(product.id);
    setMessage('');
  };

  // Cancel edit mode
  const handleCancelEdit = () => {
    setCropName('');
    setIsCustomCrop(false);
    setVariety('');
    setQuantity('');
    setPrice('');
    setDesc('');
    setLocation('');
    setImageUrl('');
    setEditingProductId(null);
    setMessage('');
  };

  // Delete product listing
  const handleDeleteProduct = async (productId) => {
    if (!window.confirm('Are you sure you want to delete this crop listing?')) return;
    try {
      await api.delete(`/farmer/products/${productId}`);
      setMessage('Crop listing deleted successfully!');
      fetchFarmerData();
    } catch (err) {
      setMessage('Failed to delete listing. ' + (err.response?.data || err.message));
    }
  };

  // Submit cold storage booking
  const handleBookStorageSubmit = async (e) => {
    e.preventDefault();
    setBookingMessage('');
    try {
      await api.post('/farmer/cold-storage/bookings', {
        coldStorageId: parseInt(selectedFacility),
        quantityTons: parseFloat(bookingQty),
        startDate,
        endDate
      });
      setBookingMessage('Storage space reservation submitted successfully!');
      setSelectedFacility('');
      setBookingQty('');
      setStartDate('');
      setEndDate('');
      fetchFarmerData();
    } catch (err) {
      setBookingMessage('Booking request failed. ' + (err.response?.data || err.message));
    }
  };

  // Update shipment status
  const handleShipOrder = async (orderId, newStatus) => {
    try {
      await api.put(`/orders/${orderId}/status?status=${newStatus}`);
      fetchFarmerData();
    } catch (error) {
      alert("Status update failed!");
    }
  };

  const handleApproveOrderSubmit = async (e) => {
    e.preventDefault();
    if (!approveOrderId || !estDeliveryDateInput) return;
    try {
      await api.put(`/orders/${approveOrderId}/status?status=APPROVED&estimatedDeliveryDate=${estDeliveryDateInput}`);
      
      window.dispatchEvent(new CustomEvent('mandiplus-toast', {
        detail: {
          title: "Order Approved",
          message: `Order #AGR-${approveOrderId} has been approved. Estimated delivery: ${estDeliveryDateInput}`,
          category: "ORDER",
          timestamp: new Date()
        }
      }));

      setApproveOrderId(null);
      setEstDeliveryDateInput('');
      fetchFarmerData();
    } catch (error) {
      alert("Failed to approve order: " + (error.response?.data || error.message));
    }
  };

  const handleRejectConfirmSubmit = async () => {
    if (!rejectConfirmOrderId) return;
    try {
      await api.put(`/orders/${rejectConfirmOrderId}/status?status=REJECTED`);
      
      window.dispatchEvent(new CustomEvent('mandiplus-toast', {
        detail: {
          title: "Order Rejected",
          message: `Order #AGR-${rejectConfirmOrderId} has been successfully rejected.`,
          category: "ORDER",
          timestamp: new Date()
        }
      }));

      setRejectConfirmOrderId(null);
      fetchFarmerData();
    } catch (error) {
      alert("Failed to reject order: " + (error.response?.data || error.message));
    }
  };

  return (
    <div className="flex min-h-screen bg-[#fafbfb] font-sans relative overflow-x-hidden">
      <Sidebar />

      <div className="flex-1 flex flex-col min-w-0 w-full overflow-y-auto">
        <DashboardHeader title={
          tab === 'dashboard' ? t('farmerCommandCenter') :
          tab === 'add-product' ? t('listCrop') :
          tab === 'orders' ? t('incomingOrders') :
          tab === 'earnings' ? t('earningsRegistry') :
          tab === 'cold-storage' ? t('reserveColdStorage') :
          t('complaintsDesk')
        } />

        <main className="p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto space-y-6 sm:space-y-8">
          
          {/* TAB 1: MAIN DASHBOARD OVERVIEW */}
          {tab === 'dashboard' && (
            <>
              {/* Analytics Summary Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
                
                <div className="bg-white p-4 sm:p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-emerald-50 text-emerald-600 p-3 sm:p-3.5 rounded-2xl shrink-0">
                    <IndianRupee className="h-6 w-6" />
                  </div>
                  <div className="min-w-0">
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Total Earnings</span>
                    <span className="block text-xl font-extrabold text-slate-800 truncate">₹{totalEarnings.toLocaleString('en-IN')}</span>
                  </div>
                </div>

                <div className="bg-white p-4 sm:p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-agrogreen-50 text-agrogreen-700 p-3 sm:p-3.5 rounded-2xl shrink-0">
                    <Leaf className="h-6 w-6" />
                  </div>
                  <div className="min-w-0">
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Crops Listed</span>
                    <span className="block text-xl font-extrabold text-slate-800 truncate">{products.length} Units</span>
                  </div>
                </div>

                <div className="bg-white p-4 sm:p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-amber-50 text-amber-600 p-3 sm:p-3.5 rounded-2xl shrink-0">
                    <Clock className="h-6 w-6" />
                  </div>
                  <div className="min-w-0">
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Active Sales</span>
                    <span className="block text-xl font-extrabold text-slate-800 truncate">{pendingOrders.length} Orders</span>
                  </div>
                </div>

                <div className="bg-white p-4 sm:p-6 rounded-3xl border border-slate-100 shadow-sm flex items-center space-x-4 text-left">
                  <div className="bg-blue-50 text-blue-600 p-3 sm:p-3.5 rounded-2xl shrink-0">
                    <Warehouse className="h-6 w-6" />
                  </div>
                  <div className="min-w-0">
                    <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-0.5">Cold Rooms Booked</span>
                    <span className="block text-xl font-extrabold text-slate-800 truncate">{activeBookings.length} Spaces</span>
                  </div>
                </div>

              </div>

              {/* Weather & Mandi price widgets */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8">
                <div className="lg:col-span-8 bg-white rounded-3xl border border-slate-100 p-4 sm:p-6 shadow-sm">
                  <MandiWidget limit={2} />
                </div>
                <div className="lg:col-span-4">
                  <WeatherWidget locationName={farmerLocation} />
                </div>
              </div>

              {/* Recent Orders table */}
              <div className="bg-white rounded-3xl border border-slate-100 p-4 sm:p-6 shadow-sm text-left">
                <h3 className="text-lg font-bold text-slate-800 tracking-tight mb-4">Recent Incoming Orders</h3>
                
                {orders.length === 0 ? (
                  <p className="text-xs text-slate-400">No transactions listed yet.</p>
                ) : (
                  <div className="overflow-x-auto w-full scrollbar-thin">
                    <table className="w-full text-sm text-left text-slate-500 min-w-[620px]">
                      <thead className="text-xs text-slate-400 uppercase bg-slate-50 rounded-xl">
                        <tr>
                          <th className="px-4 sm:px-6 py-3.5 rounded-l-xl">Order ID</th>
                          <th className="px-4 sm:px-6 py-3.5">Produce</th>
                          <th className="px-4 sm:px-6 py-3.5">Quantity</th>
                          <th className="px-4 sm:px-6 py-3.5">Price</th>
                          <th className="px-4 sm:px-6 py-3.5">Buyer Business</th>
                          <th className="px-4 sm:px-6 py-3.5 rounded-r-xl">Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {orders.slice(0, 5).map((order) => (
                          <tr key={order.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50/40">
                            <td className="px-4 sm:px-6 py-4 font-bold text-slate-800">#AGR-{order.id}</td>
                            <td className="px-4 sm:px-6 py-4">{order.cropName} ({order.variety})</td>
                            <td className="px-4 sm:px-6 py-4 font-semibold text-slate-800">{(order.quantityQuintals * 100).toLocaleString('en-IN')} kg <span className="text-slate-400 font-normal text-xs">({order.quantityQuintals} qtl)</span></td>
                            <td className="px-4 sm:px-6 py-4 font-semibold text-slate-800">₹{order.totalPrice.toLocaleString('en-IN')}</td>
                            <td className="px-4 sm:px-6 py-4">{order.buyerCompanyName}</td>
                            <td className="px-4 sm:px-6 py-4">
                              <span className={`px-2.5 py-1 text-[10px] font-bold rounded-lg ${
                                order.status === 'DELIVERED' ? 'bg-emerald-50 text-emerald-600' :
                                order.status === 'PENDING' ? 'bg-amber-50 text-amber-600' :
                                order.status === 'REJECTED' ? 'bg-rose-50 text-rose-600' : 'bg-blue-50 text-blue-600'
                              }`}>{order.status}</span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </>
          )}

          {/* TAB 2: ADD & VIEW/EDIT PRODUCTS SIDE-BY-SIDE */}
          {tab === 'add-product' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 sm:gap-8 text-left">
              
              {/* Left Column: Form (Add or Edit) */}
              <div className="lg:col-span-5 bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium flex flex-col justify-between">
                <div>
                  <div className="flex items-center space-x-2 text-agrogreen-700 mb-2">
                    <ShieldCheck className="h-5 w-5 text-agrogreen-650" />
                    <span className="text-xs font-black uppercase tracking-wider">Quality Verified Marketplace</span>
                  </div>
                  <h3 className="text-xl font-bold text-slate-800 mb-2">
                    {editingProductId ? 'Edit Crop Listing' : 'Submit Produce for Admin Review'}
                  </h3>
                  <p className="text-xs text-slate-400 mb-5 leading-relaxed">
                    {editingProductId 
                      ? 'Update harvest photo, quantity or adjust suggested rate for Admin re-evaluation.' 
                      : 'Upload crop harvest photo and suggest your selling price. Mandi+ Admin will inspect quality and approve listing before it goes live to buyers.'}
                  </p>

                  <div className="bg-amber-50/70 border border-amber-200/60 p-3.5 rounded-2xl text-[11px] text-amber-900 mb-6 flex items-start space-x-2.5">
                    <Info className="h-4 w-4 text-amber-600 shrink-0 mt-0.5" />
                    <p className="leading-relaxed">
                      <strong>How it works:</strong> Farmers only provide harvest photos, quantity, and suggested rate. Mandi+ Admin assesses quality grade from your photo and verifies the selling rate before publishing to wholesalers.
                    </p>
                  </div>

                  {message && (
                    <div className="bg-emerald-50 border border-emerald-100 text-emerald-700 p-4 rounded-xl text-xs font-bold mb-6">
                      {message}
                    </div>
                  )}

                  <form onSubmit={handleAddProduce} className="space-y-4">
                    
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Crop Name</label>
                        <select
                          required
                          value={isCustomCrop ? 'Other' : (CROP_SUGGESTIONS.includes(cropName) ? cropName : '')}
                          onChange={(e) => {
                            const val = e.target.value;
                            if (val === 'Other') {
                              setIsCustomCrop(true);
                              setCropName('');
                            } else {
                              setIsCustomCrop(false);
                              setCropName(val);
                            }
                          }}
                          className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs bg-white focus:border-agrogreen-650 transition-colors"
                        >
                          <option value="" disabled>Select a crop...</option>
                          {CROP_SUGGESTIONS.map((crop) => (
                            <option key={crop} value={crop}>{crop}</option>
                          ))}
                          <option value="Other">Other (Enter manually)</option>
                        </select>

                        {isCustomCrop && (
                          <input 
                            type="text" 
                            required 
                            placeholder="Enter custom crop name..." 
                            value={cropName}
                            onChange={(e) => setCropName(e.target.value)}
                            className="w-full px-4 py-2.5 mt-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 focus:border-agrogreen-650 transition-colors"
                          />
                        )}
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Variety</label>
                        <input 
                          type="text" required placeholder="e.g. Hybrid Sharbati" value={variety}
                          onChange={(e) => setVariety(e.target.value)}
                          className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 focus:border-agrogreen-650 transition-colors"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                      <div>
                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Quantity (Quintals)</label>
                        <input 
                          type="number" step="0.1" required placeholder="e.g. 50" value={quantity}
                          onChange={(e) => setQuantity(e.target.value)}
                          className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 focus:border-agrogreen-650 transition-colors"
                        />
                        <span className="text-[9px] text-slate-400 mt-1 block">1 Quintal = 100 Kilograms</span>
                      </div>
                      <div>
                        <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">
                          Suggested Price / Quintal (₹)
                        </label>
                        <input 
                          type="number" required placeholder="e.g. 2800" value={price}
                          onChange={(e) => setPrice(e.target.value)}
                          className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 focus:border-agrogreen-650 transition-colors font-bold text-slate-800"
                        />
                        <span className="text-[9px] text-agrogreen-700 font-semibold mt-1 block">Admin reviews & approves</span>
                      </div>
                    </div>

                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Harvest Location</label>
                      <input 
                        type="text" placeholder="e.g. Nashik, Maharashtra" value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 focus:border-agrogreen-650 transition-colors"
                      />
                    </div>

                    <div className="bg-slate-50 border border-slate-100 p-3.5 sm:p-4 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                      <div className="space-y-0.5 text-left">
                        <span className="block text-xs font-bold text-slate-700">Crop Harvest Photo</span>
                        <span className="block text-[9px] text-slate-400">Admin evaluates crop grade & quality from this photo</span>
                      </div>
                      <div className="flex items-center space-x-3 shrink-0">
                        {imageUrl ? (
                          <img src={imageUrl} alt="Crop preview" className="h-12 w-12 rounded-xl object-cover border border-slate-200 shadow-sm" />
                        ) : (
                          <div className="h-12 w-12 rounded-xl bg-slate-200 flex items-center justify-center text-slate-400">
                            <Leaf className="h-5 w-5" />
                          </div>
                        )}
                        <label className="bg-slate-900 hover:bg-slate-800 text-white font-bold px-3 py-2 rounded-xl text-[10px] cursor-pointer transition-colors shadow-sm">
                          {imageUrl ? 'Change Photo' : 'Upload Photo'}
                          <input 
                            type="file" accept="image/*" className="hidden" 
                            onChange={(e) => {
                              const file = e.target.files[0];
                              if (file) {
                                const reader = new FileReader();
                                reader.onload = (event) => {
                                  const img = new Image();
                                  img.src = event.target.result;
                                  img.onload = () => {
                                    const canvas = document.createElement('canvas');
                                    const MAX_WIDTH = 500;
                                    const MAX_HEIGHT = 500;
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
                                    const dataUrl = canvas.toDataURL('image/jpeg', 0.75);
                                    setImageUrl(dataUrl);
                                  };
                                };
                                reader.readAsDataURL(file);
                              }
                            }} 
                          />
                        </label>
                      </div>
                    </div>

                    <div>
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Crop Details / Harvest Description</label>
                      <textarea 
                        rows={2} placeholder="Moisture content details, harvesting date, pesticide-free certification..." value={desc}
                        onChange={(e) => setDesc(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs placeholder:text-slate-300 resize-none focus:border-agrogreen-650 transition-colors"
                      />
                    </div>

                    <div className="grid grid-cols-2 gap-2 pt-2">
                      {editingProductId && (
                        <button
                          type="button" onClick={handleCancelEdit}
                          className="border border-slate-200 hover:bg-slate-50 py-3 rounded-xl font-bold text-xs text-center text-slate-600 transition-colors"
                        >
                          Cancel
                        </button>
                      )}
                      <button
                        type="submit"
                        className={`bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-3 rounded-xl text-xs transition-colors shadow-sm flex items-center justify-center space-x-1.5 ${
                          editingProductId ? 'col-span-1' : 'col-span-2'
                        }`}
                      >
                        <Plus className="h-4 w-4" />
                        <span>{editingProductId ? 'Re-submit for Review' : 'Submit for Admin Approval'}</span>
                      </button>
                    </div>

                  </form>
                </div>
              </div>

              {/* Right Column: List of Added Products with Moderation Status */}
              <div className="lg:col-span-7 bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium flex flex-col justify-between">
                <div>
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
                    <div>
                      <h3 className="text-xl font-bold text-slate-800">My Crop Listings & Status</h3>
                      <p className="text-xs text-slate-400">Track approval status, Admin feedback, and live market listings.</p>
                    </div>
                    <span className="text-xs bg-agrogreen-50 text-agrogreen-700 font-bold px-3 py-1.5 rounded-full border border-agrogreen-100/50 self-start sm:self-auto">
                      {products.length} Total Crops
                    </span>
                  </div>

                  {/* Filter Pills */}
                  <div className="flex flex-wrap gap-2 mb-6">
                    {[
                      { key: 'ALL', label: 'All Crops', count: products.length },
                      { key: 'PENDING', label: 'Pending Review', count: products.filter(p => p.status === 'PENDING_APPROVAL').length, color: 'text-amber-700 bg-amber-50 border-amber-200' },
                      { key: 'AVAILABLE', label: 'Approved & Live', count: products.filter(p => p.status === 'AVAILABLE').length, color: 'text-emerald-700 bg-emerald-50 border-emerald-200' },
                      { key: 'REJECTED', label: 'Needs Revision', count: products.filter(p => p.status === 'REJECTED').length, color: 'text-rose-700 bg-rose-50 border-rose-200' }
                    ].map(tabItem => (
                      <button
                        key={tabItem.key}
                        onClick={() => setFarmerProductFilter(tabItem.key)}
                        className={`text-xs px-3.5 py-1.5 rounded-xl font-bold border transition-all ${
                          farmerProductFilter === tabItem.key
                            ? 'bg-slate-900 text-white border-slate-900 shadow-sm'
                            : 'bg-slate-50 text-slate-600 border-slate-200/80 hover:bg-slate-100'
                        }`}
                      >
                        {tabItem.label} ({tabItem.count})
                      </button>
                    ))}
                  </div>

                  {(() => {
                    const filtered = products.filter(p => {
                      if (farmerProductFilter === 'PENDING') return p.status === 'PENDING_APPROVAL';
                      if (farmerProductFilter === 'AVAILABLE') return p.status === 'AVAILABLE';
                      if (farmerProductFilter === 'REJECTED') return p.status === 'REJECTED';
                      return true;
                    });

                    if (filtered.length === 0) {
                      return (
                        <div className="py-20 text-center text-slate-400 space-y-3">
                          <ShoppingBag className="h-12 w-12 mx-auto text-slate-200 stroke-[1.5]" />
                          <p className="text-sm font-medium">No crops found in this category.</p>
                        </div>
                      );
                    }

                    return (
                      <div className="space-y-4 max-h-[62vh] overflow-y-auto pr-1 scrollbar-thin">
                        {filtered.map((product) => {
                          const isPending = product.status === 'PENDING_APPROVAL';
                          const isApproved = product.status === 'AVAILABLE';
                          const isRejected = product.status === 'REJECTED';

                          return (
                            <div 
                              key={product.id} 
                              className={`p-4 border rounded-3xl transition-all group ${
                                isPending 
                                  ? 'border-amber-100 bg-amber-50/20 hover:bg-amber-50/40' 
                                  : isApproved 
                                  ? 'border-emerald-100/80 bg-emerald-50/10 hover:bg-emerald-50/30' 
                                  : isRejected 
                                  ? 'border-rose-200 bg-rose-50/20' 
                                  : 'border-slate-100 hover:bg-slate-50/30'
                              }`}
                            >
                              <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                                <div className="flex items-start space-x-3.5 min-w-0 flex-1">
                                  {/* Crop photo preview */}
                                  <div className="h-16 w-16 rounded-2xl bg-white border border-slate-200 overflow-hidden flex items-center justify-center shrink-0 shadow-xs">
                                    {product.imageUrl ? (
                                      <img src={product.imageUrl} alt={product.cropName} className="h-full w-full object-cover" />
                                    ) : (
                                      <Leaf className="h-6 w-6 text-slate-350" />
                                    )}
                                  </div>

                                  <div className="space-y-1 min-w-0 flex-1">
                                    <div className="flex flex-wrap items-center gap-2">
                                      <span className="font-extrabold text-slate-850 text-sm truncate">{product.cropName}</span>
                                      <span className="text-[10px] bg-slate-100 font-bold px-2 py-0.5 rounded-md text-slate-600 uppercase tracking-wider">
                                        {product.variety}
                                      </span>

                                      {/* Status Badge */}
                                      {isPending && (
                                        <span className="inline-flex items-center space-x-1 text-[10px] bg-amber-100 text-amber-800 font-bold px-2 py-0.5 rounded-full border border-amber-200">
                                          <Clock className="h-2.5 w-2.5" />
                                          <span>Pending Admin Approval</span>
                                        </span>
                                      )}
                                      {isApproved && (
                                        <span className="inline-flex items-center space-x-1 text-[10px] bg-emerald-100 text-emerald-800 font-bold px-2 py-0.5 rounded-full border border-emerald-200">
                                          <CheckCircle className="h-2.5 w-2.5" />
                                          <span>Live for Buyers</span>
                                        </span>
                                      )}
                                      {isRejected && (
                                        <span className="inline-flex items-center space-x-1 text-[10px] bg-rose-100 text-rose-800 font-bold px-2 py-0.5 rounded-full border border-rose-200">
                                          <XCircle className="h-2.5 w-2.5" />
                                          <span>Revision Required</span>
                                        </span>
                                      )}
                                    </div>

                                    <div className="text-[11px] text-slate-600 font-medium">
                                      Qty: <strong className="text-slate-800">{(product.quantityQuintals * 100).toLocaleString('en-IN')} kg</strong>
                                      <span className="mx-2">•</span>
                                      Rate: <strong className="text-slate-800">₹{((product.suggestedPricePerQuintal || product.pricePerQuintal) / 100).toFixed(2)}/kg</strong>
                                      {isApproved && (
                                        <>
                                          <span className="mx-2">•</span>
                                          Live Rate: <strong className="text-emerald-700 font-bold">₹{(product.pricePerQuintal / 100).toFixed(2)}/kg</strong>
                                        </>
                                      )}
                                      <span className="text-slate-400 font-normal ml-2">(₹{product.pricePerQuintal}/qtl)</span>
                                    </div>

                                    <div className="text-[10px] text-slate-400">
                                      Location: {product.location || 'Local Farm'}
                                    </div>
                                  </div>
                                </div>

                                <div className="flex items-center space-x-1.5 shrink-0 self-end sm:self-start">
                                  <button
                                    onClick={() => handleStartEdit(product)}
                                    className="p-2 rounded-xl border border-slate-200 hover:bg-white text-slate-600 hover:text-slate-800 transition-colors shadow-xs"
                                    title="Edit Produce"
                                  >
                                    <Edit3 className="h-3.5 w-3.5" />
                                  </button>
                                  <button
                                    onClick={() => handleDeleteProduct(product.id)}
                                    className="p-2 rounded-xl border border-rose-100 bg-rose-50/50 hover:bg-rose-50 text-rose-500 hover:text-rose-600 transition-colors shadow-xs"
                                    title="Delete Listing"
                                  >
                                    <Trash2 className="h-3.5 w-3.5" />
                                  </button>
                                </div>
                              </div>

                              {/* Admin Feedback Box for Rejected Products */}
                              {isRejected && (
                                <div className="mt-3 p-3 bg-white rounded-2xl border border-rose-200/80 text-xs">
                                  <div className="flex items-center space-x-1.5 font-bold text-rose-700 mb-1 text-[11px]">
                                    <AlertCircle className="h-3.5 w-3.5 shrink-0" />
                                    <span>Admin Moderation Feedback:</span>
                                  </div>
                                  <p className="text-[11px] text-slate-600 italic">
                                    "{product.adminFeedback || 'Produce rejected due to quality check or pricing mismatch. Please adjust photo clarity or suggested rate.'}"
                                  </p>
                                  <div className="mt-2.5 flex justify-end">
                                    <button
                                      onClick={() => handleStartEdit(product)}
                                      className="inline-flex items-center space-x-1.5 px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-[10px] font-bold transition-colors shadow-sm"
                                    >
                                      <Edit3 className="h-3 w-3" />
                                      <span>Edit & Re-submit for Review</span>
                                    </button>
                                  </div>
                                </div>
                              )}

                              {/* Review Pending Info */}
                              {isPending && (
                                <div className="mt-2.5 px-3 py-1.5 rounded-xl bg-amber-100/60 border border-amber-200/50 text-[10px] text-amber-900 flex items-center justify-between">
                                  <span>🔒 Hidden from buyers until approved by Mandi+ Admin desk</span>
                                  <span className="font-semibold text-amber-800">In Review Queue</span>
                                </div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    );
                  })()}
                </div>
              </div>

            </div>
          )}

          {/* TAB 3: INCOMING ORDERS */}
          {tab === 'orders' && (
            <div className="bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium text-left">
              <h3 className="text-xl font-bold text-slate-800 mb-6">Received Sales Orders</h3>

              {orders.length === 0 ? (
                <p className="text-sm text-slate-400">You do not have any incoming crop orders yet.</p>
              ) : (
                <div className="space-y-4 sm:space-y-6">
                  {orders.map((order) => (
                    <div key={order.id} className="p-4 sm:p-6 border border-slate-100 rounded-2xl sm:rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-4 sm:gap-6 hover:shadow-sm transition-shadow">
                      <div className="space-y-2">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-bold text-slate-800 text-lg">#AGR-{order.id}</span>
                          <span className={`px-2 py-0.5 text-[9px] font-bold uppercase rounded-md ${
                            order.status === 'DELIVERED' ? 'bg-emerald-50 text-emerald-600' :
                            order.status === 'PENDING' ? 'bg-amber-50 text-amber-600' :
                            order.status === 'REJECTED' ? 'bg-rose-50 text-rose-600' : 'bg-blue-50 text-blue-600'
                          }`}>{order.status}</span>
                          {order.paymentMethod && (
                            <span className={`px-2.5 py-0.5 text-[9px] font-black uppercase rounded-md border ${
                              order.paymentMethod === 'RAZORPAY' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' : 'bg-slate-100 text-slate-700'
                            }`}>
                              {order.paymentMethod === 'RAZORPAY' ? '💳 Escrow Hold (Razorpay)' : '💵 Cash on Delivery'}
                            </span>
                          )}
                        </div>
                        <h4 className="font-bold text-slate-700">{order.cropName} ({order.variety})</h4>
                        <div className="flex items-center space-x-2 text-xs text-slate-400 py-1">
                          {order.buyerProfilePhoto ? (
                            <img 
                              src={order.buyerProfilePhoto} 
                              alt="Buyer Avatar" 
                              className="h-6 w-6 rounded-full object-cover border border-slate-200 shadow-sm" 
                            />
                          ) : (
                            <div className="h-6 w-6 rounded-full bg-slate-50 border border-slate-150 flex items-center justify-center text-slate-400">
                              <User className="h-3 w-3" />
                            </div>
                          )}
                          <p>Buyer: <strong>{order.buyerCompanyName}</strong> • {order.buyerRepresentativeName} ({order.buyerPhone})</p>
                        </div>
                        <p className="text-xs text-slate-400">Shipping Address: {order.buyerAddress}</p>
                        {order.estimatedDeliveryDate && (
                          <div className="inline-flex items-center space-x-1.5 bg-emerald-50/50 border border-emerald-100 text-emerald-850 px-2.5 py-1 rounded-xl text-[10px] font-black mt-2">
                            <span>📅 Est. Delivery: {new Date(order.estimatedDeliveryDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</span>
                          </div>
                        )}
                      </div>

                      <div className="text-left md:text-right space-y-3 w-full md:w-auto md:min-w-[200px]">
                        <div>
                          <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider">Earnings Expected</span>
                          <span className="block text-lg font-extrabold text-slate-800">₹{order.totalPrice.toLocaleString('en-IN')}</span>
                        </div>

                        {order.status === 'PENDING' && (
                          <div className="flex flex-wrap gap-2 justify-start md:justify-end">
                            <button
                              onClick={() => {
                                setApproveOrderId(order.id);
                                setEstDeliveryDateInput('');
                              }}
                              className="bg-agrogreen-950 hover:bg-agrogreen-900 text-white text-xs font-bold px-4 py-2 rounded-xl transition-all"
                            >
                              Approve Order
                            </button>
                            <button
                              onClick={() => setRejectConfirmOrderId(order.id)}
                              className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold px-4 py-2 rounded-xl transition-all"
                            >
                              Reject Order
                            </button>
                          </div>
                        )}
                        {order.status === 'APPROVED' && (
                          <button
                            onClick={() => handleShipOrder(order.id, 'SHIPPED')}
                            className="bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold px-4 py-2 rounded-xl transition-all"
                          >
                            Dispatch Shipment
                          </button>
                        )}
                        {order.status === 'SHIPPED' && (
                          <div className="flex flex-col space-y-1.5 items-start md:items-end">
                            <span className="text-[10px] text-blue-500 font-bold uppercase tracking-wider">In Transit</span>
                            <button
                              onClick={() => handleShipOrder(order.id, 'DELIVERED')}
                              className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-4 py-2 rounded-xl transition-all"
                            >
                              Mark Delivered
                            </button>
                          </div>
                        )}
                        {order.status === 'DELIVERED' && (
                          <div className="text-emerald-600 text-xs font-bold flex items-center space-x-1 justify-start md:justify-end">
                            <ShieldCheck className="h-4 w-4" />
                            <span>Escrow Cleared!</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 4: EARNINGS REGISTRY */}
          {tab === 'earnings' && (
            <div className="bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium text-left">
              <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4 mb-6">
                <div>
                  <h3 className="text-xl font-bold text-slate-800">Earnings Registry</h3>
                  <p className="text-xs text-slate-400">Summation of all orders cleared from Escrow accounts</p>
                </div>
                <div className="bg-emerald-50 border border-emerald-100 text-emerald-700 font-bold px-4 py-2 rounded-2xl flex items-center space-x-2 self-start sm:self-auto">
                  <IndianRupee className="h-4 w-4" />
                  <span>₹{totalEarnings.toLocaleString('en-IN')} Total</span>
                </div>
              </div>

              {orders.filter(o => o.status === 'DELIVERED').length === 0 ? (
                <p className="text-sm text-slate-400">No completed earnings listed in registry.</p>
              ) : (
                <div className="overflow-x-auto w-full scrollbar-thin">
                  <table className="w-full text-sm text-slate-500 min-w-[580px]">
                    <thead className="text-xs text-slate-400 bg-slate-50 uppercase rounded-xl">
                      <tr>
                        <th className="px-4 sm:px-6 py-3.5 rounded-l-xl text-left">Txn Hash ID</th>
                        <th className="px-4 sm:px-6 py-3.5 text-left">Crops Dispatched</th>
                        <th className="px-4 sm:px-6 py-3.5 text-left">Quantity</th>
                        <th className="px-4 sm:px-6 py-3.5 text-right rounded-r-xl">Cleared Profit</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.filter(o => o.status === 'DELIVERED').map((order) => (
                        <tr key={order.id} className="border-b border-slate-50 last:border-0 hover:bg-slate-50/50">
                          <td className="px-4 sm:px-6 py-4 font-bold text-slate-700">#TXN-AGR-{order.id}</td>
                          <td className="px-4 sm:px-6 py-4">{order.cropName} ({order.variety})</td>
                          <td className="px-4 sm:px-6 py-4 font-semibold text-slate-800">{(order.quantityQuintals * 100).toLocaleString('en-IN')} kg <span className="text-slate-400 font-normal text-xs">({order.quantityQuintals} qtl)</span></td>
                          <td className="px-4 sm:px-6 py-4 font-extrabold text-slate-800 text-right">₹{order.totalPrice.toLocaleString('en-IN')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 5: COLD STORAGE BOOKINGS */}
          {tab === 'cold-storage' && (
            <div className="space-y-6 sm:space-y-8">
              
              {/* Facility book form */}
              <div className="bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium text-left max-w-2xl">
                <h3 className="text-xl font-bold text-slate-800 mb-6">Reserve Smart Cold Storage</h3>

                {bookingMessage && (
                  <div className="bg-indigo-50 border border-indigo-100 text-indigo-700 p-4 rounded-xl text-sm font-semibold mb-6">
                    {bookingMessage}
                  </div>
                )}

                <form onSubmit={handleBookStorageSubmit} className="space-y-5">
                  
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Select Storage Facility</label>
                    <select
                      required
                      value={selectedFacility}
                      onChange={(e) => setSelectedFacility(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm placeholder:text-slate-300"
                    >
                      <option value="">-- Choose Cold Storage Room --</option>
                      {facilities.map((fac) => {
                        const isFull = fac.availableCapacityTons <= 0;
                        return (
                          <option key={fac.id} value={fac.id} disabled={isFull}>
                            {fac.name} - {fac.location} {isFull ? '(No Space - Check for another)' : `(₹${fac.pricePerTonDay}/ton/day, Avail: ${fac.availableCapacityTons} tons)`}
                          </option>
                        );
                      })}
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Space Requested (Tons)</label>
                    <input 
                      type="number" required placeholder="e.g. 10" value={bookingQty}
                      onChange={(e) => setBookingQty(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm placeholder:text-slate-300"
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Lease Start Date</label>
                      <input 
                        type="date" required value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm text-slate-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Lease End Date</label>
                      <input 
                        type="date" required value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm text-slate-500"
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="w-full bg-indigo-950 hover:bg-indigo-900 text-white py-3.5 rounded-xl font-bold transition-all shadow-md shadow-slate-100"
                  >
                    Submit Booking Space Request
                  </button>
                </form>
              </div>

              {/* Farmer's past bookings */}
              <div className="bg-white rounded-3xl sm:rounded-[32px] border border-slate-100 p-4 sm:p-6 lg:p-8 shadow-premium text-left">
                <h3 className="text-xl font-bold text-slate-800 mb-6">Your Storage Bookings</h3>

                {bookings.length === 0 ? (
                  <p className="text-sm text-slate-400">No active storage rooms booked yet.</p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
                    {bookings.map((book) => (
                      <div key={book.id} className="p-4 sm:p-6 border border-slate-100 rounded-2xl sm:rounded-3xl flex flex-col justify-between space-y-4 hover:shadow-sm transition-shadow">
                        <div className="space-y-1">
                          <div className="flex justify-between items-center">
                            <span className="font-extrabold text-sm text-slate-800">{book.storageName}</span>
                            <span className={`px-2.5 py-1 text-[9px] font-bold rounded-lg ${
                              book.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-600' :
                              book.status === 'PENDING' ? 'bg-amber-50 text-amber-600' : 'bg-rose-50 text-rose-600'
                            }`}>{book.status}</span>
                          </div>
                          <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider block">{book.storageLocation}</span>
                        </div>

                        <div className="grid grid-cols-2 gap-4 border-y border-slate-50 py-3 text-xs text-slate-500">
                          <div>
                            <span className="block text-[9px] text-slate-400 uppercase tracking-wider font-semibold">Duration</span>
                            <span className="block font-bold mt-0.5">{book.startDate} to {book.endDate}</span>
                          </div>
                          <div>
                            <span className="block text-[9px] text-slate-400 uppercase tracking-wider font-semibold">Capacity Booked</span>
                            <span className="block font-bold mt-0.5">{book.quantityTons} Tons</span>
                          </div>
                        </div>

                        <div className="flex justify-between items-center pt-2">
                          <span className="text-xs text-slate-400 font-medium">Estimated Booking Fee:</span>
                          <span className="font-extrabold text-slate-800">₹{book.totalPrice.toLocaleString('en-IN')}</span>
                        </div>

                        {book.status === 'REJECTED' && book.rejectionReason && (
                          <div className="bg-rose-50 border border-rose-100/50 p-3 rounded-2xl text-[11px] text-rose-700 font-medium text-left mt-2">
                            <strong>Reason:</strong> {book.rejectionReason}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

            </div>
          )}

          {/* TAB 6: COMPLAINTS */}
          {tab === 'complaints' && (
            <ComplaintsDesk />
          )}

        </main>
      </div>
      {/* Rejection Confirmation Modal */}
      {rejectConfirmOrderId && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 z-50 text-left">
          <div className="bg-white rounded-3xl sm:rounded-[32px] max-w-md w-full p-5 sm:p-8 border border-slate-100 shadow-2xl relative">
            <h3 className="text-xl font-black text-slate-800 tracking-tight mb-2">Reject Order</h3>
            <p className="text-sm text-slate-500 mb-6">Are you sure you want to reject this incoming order? This action will return the reserved crop volume to your active listing inventory and notify the buyer.</p>

            <div className="grid grid-cols-2 gap-4">
              <button
                type="button"
                onClick={() => setRejectConfirmOrderId(null)}
                className="border border-slate-200 hover:bg-slate-50 py-3 rounded-xl font-bold text-xs text-center text-slate-650 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleRejectConfirmSubmit}
                className="bg-rose-600 hover:bg-rose-700 text-white font-bold py-3 rounded-xl text-xs transition-colors flex items-center justify-center"
              >
                Confirm Rejection
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Approval Confirmation Modal */}
      {approveOrderId && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-4 z-50 text-left">
          <div className="bg-white rounded-3xl sm:rounded-[32px] max-w-md w-full p-5 sm:p-8 border border-slate-100 shadow-2xl relative">
            <h3 className="text-xl font-black text-slate-800 tracking-tight mb-2">Approve Order</h3>
            <p className="text-xs text-slate-400 mb-6">Specify the estimated delivery date to update the buyer with your transit roadmap.</p>
            
            <form onSubmit={handleApproveOrderSubmit} className="space-y-4">
              <div>
                <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-2">Estimated Delivery Date</label>
                <input 
                  type="date" 
                  required 
                  min={new Date().toISOString().split('T')[0]}
                  value={estDeliveryDateInput}
                  onChange={(e) => setEstDeliveryDateInput(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-xs text-slate-600 focus:border-agrogreen-650 transition-colors"
                />
              </div>

              <div className="grid grid-cols-2 gap-4 pt-2">
                <button
                  type="button"
                  onClick={() => setApproveOrderId(null)}
                  className="border border-slate-200 hover:bg-slate-50 py-3 rounded-xl font-bold text-xs text-center text-slate-650 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-3 rounded-xl text-xs transition-colors flex items-center justify-center"
                >
                  Confirm & Approve
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
