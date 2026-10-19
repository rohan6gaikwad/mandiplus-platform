import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import DashboardHeader from '../components/DashboardHeader';
import api from '../services/api';
import { useLanguage } from '../context/LanguageContext';
import ComplaintsDesk from '../components/ComplaintsDesk';
import ProductDetailModal from '../components/ProductDetailModal';
import CheckoutFlowModal from '../components/CheckoutFlowModal';
import LiveDeliveryMapModal from '../components/LiveDeliveryMapModal';
import {
  Search, MapPin, ShoppingBag, Heart,
  CheckCircle2, ChevronRight, MessageCircle, ShoppingCart, Trash2, Eye, Package, User,
  CreditCard, ShieldCheck, Shield, Plus, Minus, Bike, Sparkles, Tag, ArrowRight, Clock, Zap
} from 'lucide-react';

const getCategoryFromName = (name) => {
  const n = (name || '').toLowerCase();
  const vegList = [
    'tomato', 'potato', 'onion', 'cabbage', 'cauliflower', 'carrot', 'garlic', 'ginger',
    'spinach', 'palak', 'okra', 'bhindi', 'lady finger', 'brinjal', 'baingan', 'eggplant',
    'chilli', 'chili', 'capsicum', 'pepper', 'peas', 'matar', 'bitter gourd', 'karela',
    'bottle gourd', 'lauki', 'cucumber', 'kheera', 'beetroot', 'radish', 'mooli',
    'drumstick', 'moringa', 'shevga', 'sweet potato', 'methi', 'fenugreek', 'veg'
  ];
  if (vegList.some(v => n.includes(v))) return 'VEGETABLES';

  const fruitList = [
    'mango', 'banana', 'grapes', 'grape', 'pomegranate', 'papaya', 'orange', 'santra',
    'mosambi', 'sweet lime', 'guava', 'amrud', 'watermelon', 'muskmelon', 'kharbuj', 'cantaloupe',
    'apple', 'sitaphal', 'custard apple', 'strawberry', 'chikoo', 'sapota', 'lemon', 'lime',
    'nimbu', 'pineapple', 'coconut', 'fruit'
  ];
  if (fruitList.some(v => n.includes(v))) return 'FRUITS';

  return 'GRAINS';
};

const CATEGORY_CHIPS = {
  VEGETABLES: { emoji: '🥦', bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-100' },
  FRUITS:     { emoji: '🍊', bg: 'bg-orange-50',  text: 'text-orange-700',  border: 'border-orange-100' },
  GRAINS:     { emoji: '🌾', bg: 'bg-amber-50',   text: 'text-amber-700',   border: 'border-amber-100' },
};

export default function BuyerDashboard({ tab = 'shop' }) {
  const navigate = useNavigate();
  const { t } = useLanguage();
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [payments, setPayments] = useState([]);

  // Search & Filter
  const [cropSearch, setCropSearch] = useState('');
  const [locationSearch, setLocationSearch] = useState('');
  const [debouncedCropSearch, setDebouncedCropSearch] = useState('');
  const [debouncedLocationSearch, setDebouncedLocationSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  // Cold Storage States
  const [bookings, setBookings] = useState([]);
  const [facilities, setFacilities] = useState([]);
  const [selectedFacility, setSelectedFacility] = useState('');
  const [bookingQty, setBookingQty] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [bookingMessage, setBookingMessage] = useState('');

  // Shopping Cart
  const [cart, setCart] = useState(() => {
    try {
      const saved = localStorage.getItem('mandiplus_cart');
      if (!saved || saved === 'undefined' || saved === 'null') return [];
      const parsed = JSON.parse(saved);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  });

  // Product Detail Modal
  const [detailProduct, setDetailProduct] = useState(null);

  // Checkout Flow Modal
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [checkoutProduct, setCheckoutProduct] = useState(null); // for Buy Now
  const [checkoutMode, setCheckoutMode] = useState('buynow'); // 'buynow' | 'cart'

  // Live 15-Minute Uber Map Tracking
  const [showTrackingModal, setShowTrackingModal] = useState(false);
  const [selectedTrackingOrder, setSelectedTrackingOrder] = useState(null);

  // Cart address (pre-fill)
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [cartMessage, setCartMessage] = useState('');

  useEffect(() => {
    if (Array.isArray(cart)) {
      localStorage.setItem('mandiplus_cart', JSON.stringify(cart));
    }
  }, [cart]);

  useEffect(() => {
    if (tab === 'cart') {
      api.get('/auth/profile').then(r => {
        if (r.data?.address) setDeliveryAddress(r.data.address);
      }).catch(() => {});
    }
  }, [tab]);

  const handleAddToCart = (product, initialQty = 1) => {
    const existing = cart.find(i => i.id === product.id);
    if (existing) {
      handleIncrementCart(product);
      return;
    }
    const defaultQty = initialQty || 1;
    setCart([...cart, { ...product, cartQty: defaultQty, cartUnit: 'kg' }]);
    window.dispatchEvent(new CustomEvent('mandiplus-toast', {
      detail: { title: 'Added to Cart', message: `${product.cropName} (${defaultQty} kg) added to cart.`, category: 'MARKETPLACE', timestamp: new Date() }
    }));
  };

  const handleIncrementCart = (product) => {
    const existing = cart.find(i => i.id === product.id);
    if (!existing) {
      handleAddToCart(product);
      return;
    }
    const u = existing.cartUnit || 'kg';
    const step = 1;
    const max = u === 'kg' ? product.quantityQuintals * 100 : product.quantityQuintals;
    if (existing.cartQty + step > max) {
      window.dispatchEvent(new CustomEvent('mandiplus-toast', {
        detail: { title: 'Max Stock Reached', message: `Only ${max} ${u} available for ${product.cropName}.`, category: 'SYSTEM', timestamp: new Date() }
      }));
      return;
    }
    setCart(cart.map(i => i.id === product.id ? { ...i, cartQty: i.cartQty + step } : i));
  };

  const handleDecrementCart = (productId) => {
    const existing = cart.find(i => i.id === productId);
    if (!existing) return;
    const u = existing.cartUnit || 'kg';
    const step = 1;
    if (existing.cartQty <= step) {
      setCart(cart.filter(i => i.id !== productId));
      window.dispatchEvent(new CustomEvent('mandiplus-toast', {
        detail: { title: 'Removed from Cart', message: `${existing.cropName} removed from cart.`, category: 'MARKETPLACE', timestamp: new Date() }
      }));
    } else {
      setCart(cart.map(i => i.id === productId ? { ...i, cartQty: i.cartQty - step } : i));
    }
  };

  const handleCartQtyChange = (id, val, max, unit) => {
    const q = parseFloat(val);
    const lim = unit === 'kg' ? max * 100 : max;
    if (isNaN(q) || q <= 0 || q > lim) return;
    setCart(cart.map(i => i.id === id ? { ...i, cartQty: q } : i));
  };

  const handleCartUnitChange = (id, newUnit) => {
    setCart(cart.map(item => {
      if (item.id !== id) return item;
      const old = item.cartUnit || 'qtl';
      if (old === newUnit) return item;
      let q = parseFloat(item.cartQty) || 0;
      q = newUnit === 'kg' ? Math.round(q * 100) : parseFloat((q / 100).toFixed(2));
      return { ...item, cartUnit: newUnit, cartQty: q };
    }));
  };

  const handleRemoveFromCart = (id) => setCart(cart.filter(i => i.id !== id));

  const handleBuyNow = (product) => {
    setCheckoutProduct(product);
    setCheckoutMode('buynow');
    setCheckoutOpen(true);
  };

  const handleCartCheckout = () => {
    if (cart.length === 0) return;
    setCheckoutMode('cart');
    setCheckoutProduct(null);
    setCheckoutOpen(true);
  };

  const handleToggleFavorite = (product) => {
    const isFav = favorites.some(f => f.id === product.id);
    setFavorites(isFav ? favorites.filter(f => f.id !== product.id) : [...favorites, product]);
  };

  const handleConfirmDelivery = async (orderId) => {
    try {
      await api.put(`/orders/${orderId}/status?status=DELIVERED`);
      fetchBuyerData();
    } catch { alert('Verification failed!'); }
  };

  // Debounce
  useEffect(() => {
    const h = setTimeout(() => setDebouncedCropSearch(cropSearch), 400);
    return () => clearTimeout(h);
  }, [cropSearch]);

  useEffect(() => {
    const h = setTimeout(() => setDebouncedLocationSearch(locationSearch), 400);
    return () => clearTimeout(h);
  }, [locationSearch]);

  const fetchBuyerData = async () => {
    try {
      const prodRes = await api.get(`/public/products?crop=${encodeURIComponent(debouncedCropSearch)}&location=${encodeURIComponent(debouncedLocationSearch)}`);
      setProducts(Array.isArray(prodRes.data) ? prodRes.data : []);
      const ordRes = await api.get('/buyer/orders');
      setOrders(Array.isArray(ordRes.data) ? ordRes.data : []);
      if (tab === 'payments') {
        const payRes = await api.get('/buyer/payment/history');
        setPayments(Array.isArray(payRes.data) ? payRes.data : []);
      }
      if (tab === 'cold-storage') {
        const bookRes = await api.get('/buyer/cold-storage/bookings');
        setBookings(Array.isArray(bookRes.data) ? bookRes.data : []);
        const facRes = await api.get('/public/cold-storage');
        setFacilities(Array.isArray(facRes.data) ? facRes.data : []);
      }
    } catch (e) {
      console.error('Error loading buyer data', e);
    }
  };

  useEffect(() => {
    fetchBuyerData();
    const handler = (e) => {
      if (['ORDER','MARKETPLACE','COLD_STORAGE','PAYMENT'].includes(e.detail?.category)) fetchBuyerData();
    };
    window.addEventListener('mandiplus-alert', handler);
    return () => window.removeEventListener('mandiplus-alert', handler);
  }, [tab, debouncedCropSearch, debouncedLocationSearch]);

  const handleBookStorageSubmit = async (e) => {
    e.preventDefault();
    setBookingMessage('');
    try {
      await api.post('/buyer/cold-storage/bookings', {
        coldStorageId: parseInt(selectedFacility),
        quantityTons: parseFloat(bookingQty),
        startDate,
        endDate
      });
      setBookingMessage('Reservation submitted!');
      setSelectedFacility(''); setBookingQty(''); setStartDate(''); setEndDate('');
      fetchBuyerData();
    } catch (err) {
      setBookingMessage('Failed: ' + (err.response?.data || err.message));
    }
  };

  const safeCart = Array.isArray(cart) ? cart : [];
  const safeProducts = Array.isArray(products) ? products : [];
  const safeOrders = Array.isArray(orders) ? orders : [];
  const safePayments = Array.isArray(payments) ? payments : [];
  const safeFavorites = Array.isArray(favorites) ? favorites : [];

  const filteredProducts = safeProducts.filter(p => {
    if (selectedCategory !== 'ALL' && getCategoryFromName(p.cropName) !== selectedCategory) return false;
    if (minPrice && p.pricePerQuintal < parseFloat(minPrice)) return false;
    if (maxPrice && p.pricePerQuintal > parseFloat(maxPrice)) return false;
    return true;
  });

  const cartTotal = safeCart.reduce((s, i) => {
    const r = i.cartUnit === 'kg' ? i.pricePerQuintal / 100 : i.pricePerQuintal;
    return s + r * (i.cartQty || 0);
  }, 0);

  return (
    <div className="flex bg-[#fafbfc] min-h-screen font-sans">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-y-auto">
        <DashboardHeader title={
          tab === 'shop'         ? t('produceMarketplace') :
          tab === 'orders'       ? t('purchasedOrders') :
          tab === 'cart'         ? t('shoppingCart') :
          tab === 'payments'     ? 'Razorpay & Escrow Ledger' :
          tab === 'favorites'    ? t('favoritesListings') :
          tab === 'cold-storage' ? t('reserveColdStorage') :
          t('complaintsDesk')
        } />

        <main className="p-6 max-w-7xl w-full mx-auto space-y-6">

          {/* ══════════════════ TAB: SHOP ══════════════════ */}
          {tab === 'shop' && (
            <div className="space-y-5">

              {/* Search & Filters */}
              <div className="bg-white rounded-2xl border border-slate-100 p-5 shadow-sm space-y-4">
                <div className="flex flex-col md:flex-row gap-3">
                  <div className="relative flex-1">
                    <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                    <input
                      type="text" placeholder="Search crops..." value={cropSearch}
                      onChange={e => setCropSearch(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-sm bg-slate-50/60 placeholder:text-slate-300 transition-all"
                    />
                  </div>
                  <div className="relative flex-1">
                    <MapPin className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-300" />
                    <input
                      type="text" placeholder="Filter by location..." value={locationSearch}
                      onChange={e => setLocationSearch(e.target.value)}
                      className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-sm bg-slate-50/60 placeholder:text-slate-300 transition-all"
                    />
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 items-center pt-1 border-t border-slate-50">
                  {/* Category pills */}
                  {['ALL','VEGETABLES','FRUITS','GRAINS'].map(cat => (
                    <button
                      key={cat}
                      onClick={() => setSelectedCategory(cat)}
                      className={`px-3.5 py-1.5 rounded-full text-[11px] font-black uppercase tracking-wider transition-all border ${
                        selectedCategory === cat
                          ? 'bg-[#0a2d1e] text-white border-transparent shadow-sm'
                          : 'bg-slate-50 text-slate-500 border-slate-100 hover:border-slate-200 hover:text-slate-700'
                      }`}
                    >
                      {cat === 'ALL' ? 'All' : `${CATEGORY_CHIPS[cat].emoji} ${cat.charAt(0) + cat.slice(1).toLowerCase()}`}
                    </button>
                  ))}
                  <div className="flex items-center space-x-2 ml-auto">
                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider whitespace-nowrap">Price ₹:</span>
                    <input type="number" placeholder="Min" value={minPrice} onChange={e => setMinPrice(e.target.value)}
                      className="w-20 px-2.5 py-1.5 rounded-lg border border-slate-100 text-xs bg-slate-50/60 focus:outline-none" />
                    <span className="text-slate-300 text-xs">—</span>
                    <input type="number" placeholder="Max" value={maxPrice} onChange={e => setMaxPrice(e.target.value)}
                      className="w-20 px-2.5 py-1.5 rounded-lg border border-slate-100 text-xs bg-slate-50/60 focus:outline-none" />
                    {(minPrice || maxPrice) && (
                      <button onClick={() => { setMinPrice(''); setMaxPrice(''); }} className="text-[10px] font-black text-rose-400 hover:text-rose-600">Reset</button>
                    )}
                  </div>
                </div>
              </div>

              {/* Results count */}
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-medium">{filteredProducts.length} listings available</span>
                {cart.length > 0 && (
                  <button
                    onClick={handleCartCheckout}
                    className="flex items-center space-x-2 bg-[#0a2d1e] hover:bg-[#0d3825] text-white px-4 py-2 rounded-xl text-xs font-black transition-all shadow-md shadow-emerald-900/15"
                  >
                    <ShoppingCart className="h-3.5 w-3.5" />
                    <span>Cart ({cart.length}) · ₹{cartTotal.toLocaleString('en-IN', { maximumFractionDigits: 0 })}</span>
                  </button>
                )}
              </div>

              {/* Product Grid - Small Cards (4 columns) */}
              {filteredProducts.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 space-y-3 text-slate-300">
                  <ShoppingBag className="h-12 w-12 stroke-[1.5]" />
                  <p className="text-sm font-medium">No produce matches your filters.</p>
                </div>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
                  {filteredProducts.map(product => {
                    const isFav = favorites.some(f => f.id === product.id);
                    const cat = getCategoryFromName(product.cropName);
                    const chip = CATEGORY_CHIPS[cat];
                    const cartItem = cart.find(i => i.id === product.id);
                    const isInCart = Boolean(cartItem);

                    return (
                      <div
                        key={product.id}
                        className="bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col overflow-hidden hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 group shimmer-hover relative"
                      >
                        {/* Image */}
                        <div
                          className="relative aspect-[4/3] bg-slate-50 overflow-hidden cursor-pointer"
                          onClick={() => setDetailProduct(product)}
                        >
                          {product.imageUrl ? (
                            <img src={product.imageUrl} alt={product.cropName} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-slate-200">
                              <ShoppingBag className="h-8 w-8 stroke-[1.5]" />
                            </div>
                          )}

                          {/* Category chip */}
                          <span className={`absolute top-2 left-2 text-[9px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full border ${chip.bg} ${chip.text} ${chip.border}`}>
                            {chip.emoji} {cat.charAt(0) + cat.slice(1).toLowerCase()}
                          </span>

                          {/* Fav button */}
                          <button
                            onClick={e => { e.stopPropagation(); handleToggleFavorite(product); }}
                            className={`absolute top-2 right-2 h-6 w-6 flex items-center justify-center rounded-full shadow-sm transition-all ${
                              isFav ? 'bg-rose-500 text-white' : 'bg-white/80 text-slate-300 hover:text-rose-400 backdrop-blur-sm'
                            }`}
                          >
                            <Heart className="h-3 w-3 fill-current" />
                          </button>

                          {/* Quick Commerce Add / Stepper Button (Zepto / Blinkit style overlay) */}
                          <div className="absolute bottom-2 right-2 z-20">
                            {!cartItem ? (
                              <button
                                onClick={(e) => { e.stopPropagation(); handleAddToCart(product); }}
                                className="bg-white/95 hover:bg-rose-50 text-rose-600 border-2 border-rose-500 hover:border-rose-600 font-black h-7.5 px-2.5 rounded-xl text-xs flex items-center justify-center space-x-1 shadow-md hover:scale-105 active:scale-95 transition-all"
                                title="Add 1 kg to Cart"
                              >
                                <Plus className="h-3.5 w-3.5 stroke-[3]" />
                                <span className="text-[11px] font-black">ADD</span>
                              </button>
                            ) : (
                              <div
                                onClick={(e) => e.stopPropagation()}
                                className="bg-rose-600 text-white font-black h-7.5 px-1.5 rounded-xl text-xs flex items-center space-x-1 shadow-lg shadow-rose-300/50 transition-all"
                              >
                                <button
                                  onClick={() => handleDecrementCart(product.id)}
                                  className="h-5.5 w-5.5 rounded-lg bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center active:scale-90 transition-transform"
                                  title="Decrease 1 kg"
                                >
                                  <Minus className="h-3 w-3 stroke-[3]" />
                                </button>
                                <span className="font-black text-[11px] px-1 min-w-[20px] text-center select-none">
                                  {cartItem.cartQty} kg
                                </span>
                                <button
                                  onClick={() => handleIncrementCart(product)}
                                  className="h-5.5 w-5.5 rounded-lg bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center active:scale-90 transition-transform"
                                  title="Increase 1 kg"
                                >
                                  <Plus className="h-3 w-3 stroke-[3]" />
                                </button>
                              </div>
                            )}
                          </div>

                          {/* View details hover overlay */}
                          <div className="absolute inset-0 bg-slate-900/0 group-hover:bg-slate-900/20 transition-all duration-200 flex items-center justify-center opacity-0 group-hover:opacity-100 pointer-events-none">
                            <span className="bg-white/95 text-slate-800 text-[10px] font-black px-3 py-1.5 rounded-full flex items-center space-x-1 shadow-sm">
                              <Eye className="h-3 w-3" />
                              <span>View Details</span>
                            </span>
                          </div>
                        </div>

                        {/* Card body */}
                        <div className="p-3 flex flex-col flex-1 space-y-2">
                          <div>
                            <div className="flex items-center justify-between gap-1">
                              <h4 className="font-extrabold text-slate-800 text-sm leading-tight truncate">{product.cropName}</h4>
                              <span className="shrink-0 inline-flex items-center space-x-0.5 text-[9px] font-black text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-100">
                                <Zap className="h-2.5 w-2.5 fill-amber-400 text-amber-500" />
                                <span>15 MINS</span>
                              </span>
                            </div>
                            <span className="text-[10px] text-slate-400 font-semibold truncate block">{product.variety}</span>
                          </div>

                          <div className="flex items-end justify-between">
                            <div>
                              <span className="block text-base font-black text-slate-800 leading-none">₹{(product.pricePerQuintal / 100).toFixed(2)}</span>
                              <span className="text-[10px] text-slate-400 font-medium">per kg</span>
                            </div>
                            <div className="text-right">
                              <span className="block text-xs font-bold text-slate-500">{(product.quantityQuintals * 100).toLocaleString('en-IN')} kg</span>
                              <span className="text-[10px] text-slate-400">stock</span>
                            </div>
                          </div>

                          {/* Actions Row */}
                          <div className="pt-1 flex items-center space-x-1.5">
                            {!cartItem ? (
                              <div className="flex-1 bg-rose-50 border border-rose-200 rounded-xl p-0.5 flex items-center justify-between shadow-xs">
                                <button
                                  type="button"
                                  disabled
                                  className="h-6 w-6 rounded-lg text-rose-300 flex items-center justify-center cursor-not-allowed opacity-40"
                                  title="Add to cart first"
                                >
                                  <Minus className="h-3 w-3 stroke-[3]" />
                                </button>
                                <button
                                  type="button"
                                  onClick={() => handleAddToCart(product)}
                                  className="flex-1 py-1 px-1 text-center font-black text-rose-700 hover:text-rose-800 text-[11px] transition-all active:scale-95"
                                  title="Add 1 kg to cart"
                                >
                                  ADD 1 kg
                                </button>
                                <button
                                  type="button"
                                  onClick={() => handleAddToCart(product)}
                                  className="h-6 w-6 rounded-lg bg-rose-100 hover:bg-rose-200 text-rose-700 flex items-center justify-center active:scale-90 transition-transform"
                                  title="Add 1 kg"
                                >
                                  <Plus className="h-3 w-3 stroke-[3]" />
                                </button>
                              </div>
                            ) : (
                              <div className="flex-1 bg-rose-600 text-white font-black py-1 px-1.5 rounded-xl text-xs flex items-center justify-between shadow-sm">
                                <button
                                  type="button"
                                  onClick={() => handleDecrementCart(product.id)}
                                  className="h-6 w-6 rounded-lg bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center active:scale-90 transition-transform"
                                  title="Decrease 1 kg"
                                >
                                  <Minus className="h-3 w-3 stroke-[3]" />
                                </button>
                                <span className="text-[11px] font-black truncate px-1">{cartItem.cartQty} {cartItem.cartUnit || 'kg'}</span>
                                <button
                                  type="button"
                                  onClick={() => handleIncrementCart(product)}
                                  className="h-6 w-6 rounded-lg bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center active:scale-90 transition-transform"
                                  title="Increase 1 kg"
                                >
                                  <Plus className="h-3 w-3 stroke-[3]" />
                                </button>
                              </div>
                            )}
                            <button
                              onClick={() => handleBuyNow(product)}
                              className="bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-bold py-2 px-3 rounded-xl text-[11px] transition-colors shrink-0"
                            >
                              Buy Now
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Floating Bottom Quick-Commerce Cart Bar (Zepto Style) */}
              {cart.length > 0 && (
                <div className="fixed bottom-5 left-4 right-4 sm:left-auto sm:right-8 sm:w-[420px] z-40 bg-slate-900/95 backdrop-blur-md text-white rounded-2xl p-2.5 sm:p-3 border border-slate-700/60 shadow-2xl flex items-center justify-between gap-3 animate-fade-in">
                  <div className="flex items-center space-x-3 min-w-0">
                    <div className="h-10 w-10 rounded-xl bg-white/10 flex items-center justify-center shrink-0 border border-white/10 text-emerald-400 shadow-inner">
                      <Bike className="h-5 w-5" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex items-center space-x-1.5 mb-0.5">
                        <span className="text-[9px] font-black uppercase tracking-wider text-rose-300 bg-rose-500/20 px-1.5 py-0.5 rounded border border-rose-500/30">Offers</span>
                        <span className="text-xs font-black text-white truncate">Free Delivery Unlocked</span>
                      </div>
                      <span className="block text-[11px] text-emerald-400 font-extrabold truncate">
                        ₹{cartTotal.toLocaleString('en-IN', { maximumFractionDigits: 0 })} · ⚡ 15-Min Farmhouse Delivery
                      </span>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate('/buyer/cart')}
                    className="bg-rose-600 hover:bg-rose-700 active:scale-95 text-white font-black px-4 py-2.5 rounded-xl text-xs flex items-center space-x-1.5 shadow-lg shadow-rose-900/40 shrink-0 transition-all"
                  >
                    <ShoppingCart className="h-4 w-4" />
                    <span>Cart {cart.length} {cart.length === 1 ? 'item' : 'items'}</span>
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              )}
            </div>
          )}

          {/* ══════════════════ TAB: ORDERS ══════════════════ */}
          {tab === 'orders' && (
            <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm text-left">
              <h3 className="text-lg font-black text-slate-800 mb-6 tracking-tight">Purchase History</h3>
              {orders.length === 0 ? (
                <div className="py-12 text-center">
                  <Package className="h-10 w-10 mx-auto text-slate-200 mb-3 stroke-[1.5]" />
                  <p className="text-sm text-slate-400">No orders placed yet.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {orders.map(order => (
                    <div key={order.id} className="p-5 border border-slate-100 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-5 hover:shadow-sm transition-all">
                      <div className="space-y-1.5">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-black text-slate-800">#AGR-{order.id}</span>
                          <span className={`px-2 py-0.5 text-[9px] font-black uppercase rounded-lg ${
                            order.status === 'DELIVERED' ? 'bg-emerald-50 text-emerald-600' :
                            order.status === 'PENDING'   ? 'bg-amber-50 text-amber-600' :
                                                           'bg-blue-50 text-blue-600'
                          }`}>{order.status}</span>
                          {order.paymentMethod && (
                            <span className={`px-2 py-0.5 text-[9px] font-black uppercase rounded-lg border ${
                              order.paymentMethod === 'COD'
                                ? 'bg-amber-50 text-amber-700 border-amber-200'
                                : order.paymentMethod === 'UPI'
                                ? 'bg-purple-50 text-purple-700 border-purple-200'
                                : order.paymentMethod === 'CARD'
                                ? 'bg-blue-50 text-blue-700 border-blue-200'
                                : order.paymentMethod === 'NETBANKING'
                                ? 'bg-cyan-50 text-cyan-700 border-cyan-200'
                                : 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            }`}>
                              {order.paymentMethod === 'COD' ? '💵 Cash on Delivery' :
                               order.paymentMethod === 'UPI' ? '⚡ UPI Paid' :
                               order.paymentMethod === 'CARD' ? '💳 Card Paid' :
                               order.paymentMethod === 'NETBANKING' ? '🏦 Net Banking' :
                               '🛡️ Razorpay Escrow'}
                            </span>
                          )}
                          {order.razorpayPaymentId && (
                            <span className="text-[10px] font-mono text-slate-400 font-bold">
                              Ref: {order.razorpayPaymentId}
                            </span>
                          )}
                        </div>
                        <h4 className="font-bold text-slate-700 text-sm">{order.cropName} ({order.variety})</h4>
                        <div className="flex items-center space-x-2 text-xs text-slate-400 py-1">
                          {order.farmerProfilePhoto ? (
                            <img 
                              src={order.farmerProfilePhoto} 
                              alt="Farmer Avatar" 
                              className="h-6 w-6 rounded-full object-cover border border-slate-200 shadow-sm" 
                            />
                          ) : (
                            <div className="h-6 w-6 rounded-full bg-slate-50 border border-slate-150 flex items-center justify-center text-slate-400">
                              <User className="h-3 w-3" />
                            </div>
                          )}
                          <span>Farmer: <strong>{order.farmerName}</strong> ({order.farmerPhone})</span>
                        </div>
                        <p className="text-xs text-slate-400">Volume: {((Number(order.quantityQuintals) || 0) * 100).toLocaleString('en-IN')} kg · Price: ₹{((Number(order.pricePerQuintal) || 0) / 100).toFixed(2)}/kg</p>
                        {order.estimatedDeliveryDate && (
                          <div className="inline-flex items-center space-x-1.5 bg-emerald-50/50 border border-emerald-100 text-emerald-850 px-2.5 py-1 rounded-xl text-[10px] font-black mt-2">
                            <span>📅 Est. Delivery: {new Date(order.estimatedDeliveryDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</span>
                          </div>
                        )}

                        {order.status === 'SHIPPED' && (
                          <div className="bg-slate-50 border border-slate-100 rounded-xl p-3 mt-2 max-w-md">
                            <div className="flex justify-between text-[9px] font-black uppercase tracking-wider text-slate-400 mb-2">
                              <span>Origin</span>
                              <span className="text-blue-500 animate-pulse">In Transit</span>
                              <span>Destination</span>
                            </div>
                            <div className="relative h-5 flex items-center">
                              <div className="w-full h-1 bg-slate-100 rounded-full overflow-hidden">
                                <div className="h-1 bg-blue-500 rounded-full" style={{ width: '62%' }} />
                              </div>
                              <div className="absolute left-[62%] -translate-x-1/2 -top-0.5">
                                <span className="h-5 w-5 flex items-center justify-center rounded-full bg-blue-600 text-white text-[10px] animate-bounce shadow-sm">🚚</span>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                      <div className="min-w-[160px] space-y-2.5">
                        <div>
                          <span className="block text-[10px] text-slate-400 font-black uppercase tracking-wider">Total Paid</span>
                          <span className="block text-lg font-black text-slate-800">₹{Number(order.totalPrice || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                        </div>
                        {order.status === 'SHIPPED' && (
                          <span className="text-xs text-blue-600 font-bold block">🚚 Dispatch In Transit</span>
                        )}
                        {order.status === 'PENDING'   && <span className="text-xs text-slate-400 italic">Awaiting farmer approval...</span>}
                        {order.status === 'APPROVED'  && <span className="text-xs text-slate-400 italic">Awaiting shipment...</span>}
                        {order.status === 'DELIVERED' && (
                          <div className="text-emerald-600 text-xs font-bold flex items-center space-x-1.5">
                            <CheckCircle2 className="h-3.5 w-3.5" /><span>Completed</span>
                          </div>
                        )}

                        {/* Live Delivery Tracking Trigger */}
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedTrackingOrder(order);
                            setShowTrackingModal(true);
                          }}
                          className="w-full bg-slate-900 hover:bg-slate-800 active:scale-95 text-white font-black py-2 px-3 rounded-xl text-xs flex items-center justify-center space-x-1.5 border border-slate-700 shadow-sm transition-all"
                        >
                          <Bike className="h-3.5 w-3.5 text-emerald-400" />
                          <span>Track Live Delivery</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ══════════════════ TAB: FAVORITES ══════════════════ */}
          {tab === 'favorites' && (
            <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm text-left">
              <h3 className="text-lg font-black text-slate-800 mb-5 tracking-tight">Saved Favorites</h3>
              {favorites.length === 0 ? (
                <div className="py-12 text-center">
                  <Heart className="h-10 w-10 mx-auto text-slate-200 mb-3" />
                  <p className="text-sm text-slate-400">No favorites saved yet.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {favorites.map(product => {
                    const cartItem = cart.find(i => i.id === product.id);
                    return (
                      <div key={product.id} className="p-4 border border-slate-100 rounded-2xl flex items-center justify-between hover:shadow-sm transition-all">
                        <div className="min-w-0 space-y-0.5">
                          <h4 className="font-extrabold text-slate-800 text-sm truncate">{product.cropName}</h4>
                          <span className="text-[10px] text-slate-400 font-semibold block">{product.variety} · {product.location}</span>
                          <span className="text-sm font-black text-slate-800">₹{(product.pricePerQuintal / 100).toFixed(2)}/kg</span>
                        </div>
                        <div className="flex flex-col space-y-1.5 ml-4 shrink-0">
                          {!cartItem ? (
                            <button
                              onClick={() => handleAddToCart(product)}
                              className="bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 font-bold py-1.5 px-3 rounded-xl text-[10px] transition-colors flex items-center justify-center space-x-1"
                              title="Add 1 kg to cart"
                            >
                              <Plus className="h-3 w-3 stroke-[3]" />
                              <span>Add 1 kg</span>
                            </button>
                          ) : (
                            <div className="bg-rose-600 text-white font-black py-1 px-1.5 rounded-xl text-[10px] flex items-center justify-between space-x-1 shadow-xs">
                              <button
                                onClick={() => handleDecrementCart(product.id)}
                                className="h-5 w-5 rounded bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center"
                                title="Decrease 1 kg"
                              >
                                <Minus className="h-2.5 w-2.5 stroke-[3]" />
                              </button>
                              <span className="font-black px-1">{cartItem.cartQty} kg</span>
                              <button
                                onClick={() => handleIncrementCart(product)}
                                className="h-5 w-5 rounded bg-rose-700/70 hover:bg-rose-800 flex items-center justify-center"
                                title="Increase 1 kg"
                              >
                                <Plus className="h-2.5 w-2.5 stroke-[3]" />
                              </button>
                            </div>
                          )}
                          <div className="flex space-x-1">
                            <button onClick={() => setDetailProduct(product)} className="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-1 px-2 rounded-lg text-[10px] transition-colors flex items-center space-x-0.5">
                              <Eye className="h-3 w-3" /><span>View</span>
                            </button>
                            <button onClick={() => handleBuyNow(product)} className="bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-bold py-1 px-2.5 rounded-lg text-[10px] transition-colors">
                              Buy
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* ══════════════════ TAB: CART ══════════════════ */}
          {tab === 'cart' && (
            <div className="space-y-4 text-left">
              
              {/* Zepto Savings Banner */}
              {cart.length > 0 && (
                <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 px-4 py-3 rounded-2xl flex items-center justify-between shadow-xs">
                  <div className="flex items-center space-x-2.5">
                    <Sparkles className="h-4 w-4 text-emerald-600 fill-emerald-600 shrink-0" />
                    <span className="text-xs font-black">
                      Yay! You saved ₹{Math.round(cartTotal * 0.12).toLocaleString('en-IN')} on direct farmer mandi rates
                    </span>
                  </div>
                  <span className="text-[10px] font-black uppercase tracking-wider bg-emerald-200/60 text-emerald-900 px-2.5 py-1 rounded-full">
                    Best Value
                  </span>
                </div>
              )}

              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

                {/* Left Column: Cart Items & Delivery Info */}
                <div className="lg:col-span-8 space-y-4">

                  {/* 15-Minute Quick-Commerce Delivery Card (Blinkit / Zepto Style) */}
                  <div className="bg-gradient-to-r from-emerald-50/90 via-teal-50/50 to-white rounded-2xl border border-emerald-100 p-4 shadow-sm flex items-center justify-between gap-3">
                    <div className="flex items-center space-x-3.5 min-w-0">
                      <div className="h-11 w-11 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center shadow-md shadow-emerald-200 shrink-0">
                        <Zap className="h-5 w-5 fill-amber-300 text-amber-300 animate-pulse" />
                      </div>
                      <div className="min-w-0">
                        <div className="flex items-center space-x-2">
                          <span className="font-black text-slate-800 text-sm sm:text-base tracking-tight">
                            Delivering in 10 – 15 Mins
                          </span>
                          <span className="inline-flex items-center px-1.5 py-0.5 rounded-full text-[9px] font-black uppercase bg-emerald-100 text-emerald-800 border border-emerald-200/60">
                            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 mr-1 animate-ping" />
                            Live
                          </span>
                        </div>
                        <p className="text-xs text-slate-500 font-medium mt-0.5 truncate">
                          Direct farmhouse pickup · Delivery partner reaches your doorstep in 15 mins max
                        </p>
                      </div>
                    </div>
                    <div className="flex flex-col items-end shrink-0 space-y-1">
                      <span className="text-[10px] font-black uppercase tracking-wider text-emerald-800 bg-emerald-100/90 px-3 py-1 rounded-xl border border-emerald-200/80 shadow-xs flex items-center space-x-1">
                        <Bike className="h-3.5 w-3.5 text-emerald-700" />
                        <span>15 MIN EXPRESS</span>
                      </span>
                      <span className="text-[9px] text-slate-400 font-bold uppercase tracking-wider hidden sm:block">
                        Farmhouse to Doorstep
                      </span>
                    </div>
                  </div>

                  {/* Cart Items List */}
                  <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm space-y-4">
                    <div className="flex justify-between items-center border-b border-slate-100 pb-4">
                      <div>
                        <h3 className="text-lg font-black text-slate-800 tracking-tight">{t('shoppingCart')}</h3>
                        <p className="text-xs text-slate-400 font-medium mt-0.5">Direct crop lots straight from verified farms</p>
                      </div>
                      <span className="text-xs bg-rose-50 text-rose-700 font-black px-3 py-1.5 rounded-full border border-rose-100">
                        {cart.length} {cart.length === 1 ? 'item' : 'items'}
                      </span>
                    </div>

                    {cart.length === 0 ? (
                      <div className="py-14 text-center space-y-3">
                        <ShoppingCart className="h-14 w-14 mx-auto text-slate-200 stroke-[1.5]" />
                        <h4 className="font-extrabold text-slate-700 text-sm">Your cart is empty</h4>
                        <p className="text-xs text-slate-400">Add fresh crops and produce from the marketplace.</p>
                        <button
                          onClick={() => navigate('/buyer')}
                          className="mt-3 bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-black px-5 py-2.5 rounded-xl text-xs transition-all inline-flex items-center space-x-1.5"
                        >
                          <Search className="h-3.5 w-3.5" />
                          <span>Explore Marketplace</span>
                        </button>
                      </div>
                    ) : (
                      <div className="divide-y divide-slate-100">
                        {cart.map(item => {
                          const u = item.cartUnit || 'kg';
                          const rate = u === 'kg' ? item.pricePerQuintal / 100 : item.pricePerQuintal;
                          const total = rate * item.cartQty;
                          const mrpRate = rate * 1.15;
                          return (
                            <div key={item.id} className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 group">
                              
                              {/* Left: Thumbnail + Title */}
                              <div className="flex items-center space-x-3.5 min-w-0">
                                <div className="h-16 w-16 sm:h-20 sm:w-20 rounded-2xl bg-slate-50 border border-slate-100 overflow-hidden shrink-0 flex items-center justify-center relative">
                                  {item.imageUrl ? (
                                    <img src={item.imageUrl} alt={item.cropName} className="h-full w-full object-cover group-hover:scale-105 transition-transform" />
                                  ) : (
                                    <ShoppingBag className="h-7 w-7 text-slate-300 stroke-[1.5]" />
                                  )}
                                  <span className="absolute bottom-1 right-1 text-[8px] font-black uppercase bg-white/90 text-slate-700 px-1 rounded shadow-xs">
                                    {u}
                                  </span>
                                </div>
                                <div className="space-y-0.5 min-w-0">
                                  <h4 className="font-extrabold text-slate-800 text-sm leading-snug truncate">{item.cropName}</h4>
                                  <span className="text-[11px] text-slate-400 font-semibold uppercase block">{item.variety}</span>
                                  <span className="text-xs text-slate-500 font-medium block truncate">{item.farmerName} · {item.location}</span>
                                  <div className="flex items-baseline space-x-2 pt-0.5">
                                    <span className="text-xs font-black text-slate-800">₹{rate.toLocaleString('en-IN', { maximumFractionDigits: 2 })}/{u}</span>
                                    <span className="text-[10px] text-slate-400 line-through">₹{mrpRate.toFixed(0)}/{u}</span>
                                    <span className="text-[10px] font-black text-emerald-600">15% OFF</span>
                                  </div>
                                </div>
                              </div>

                              {/* Right: Zepto Stepper + Unit Toggle + Item Total */}
                              <div className="flex items-center justify-between sm:justify-end gap-3 shrink-0">
                                
                                {/* Unit selector pill */}
                                <div className="flex items-center bg-slate-100 p-0.5 rounded-xl border border-slate-200">
                                  <button
                                    type="button"
                                    onClick={() => handleCartUnitChange(item.id, 'kg')}
                                    className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase transition-all ${
                                      u === 'kg' ? 'bg-white text-slate-800 shadow-xs' : 'text-slate-400 hover:text-slate-700'
                                    }`}
                                  >
                                    kg
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => handleCartUnitChange(item.id, 'qtl')}
                                    className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase transition-all ${
                                      u === 'qtl' ? 'bg-white text-slate-800 shadow-xs' : 'text-slate-400 hover:text-slate-700'
                                    }`}
                                  >
                                    qtl
                                  </button>
                                </div>

                                {/* Zepto-Style Pill Stepper: [ - ] [ qty ] [ + ] */}
                                <div className="flex items-center bg-rose-50 border border-rose-200 rounded-xl p-1 shadow-xs">
                                  <button
                                    type="button"
                                    onClick={() => handleDecrementCart(item.id)}
                                    className="h-7 w-7 rounded-lg bg-white hover:bg-rose-100 text-rose-600 font-black flex items-center justify-center active:scale-90 transition-transform shadow-xs"
                                    title="Decrease 1 kg"
                                  >
                                    <Minus className="h-3.5 w-3.5 stroke-[3]" />
                                  </button>
                                  <span className="px-3 text-xs font-black text-rose-600 min-w-[48px] text-center select-none">
                                    {item.cartQty} {u}
                                  </span>
                                  <button
                                    type="button"
                                    onClick={() => handleIncrementCart(item)}
                                    className="h-7 w-7 rounded-lg bg-white hover:bg-rose-100 text-rose-600 font-black flex items-center justify-center active:scale-90 transition-transform shadow-xs"
                                    title="Increase 1 kg"
                                  >
                                    <Plus className="h-3.5 w-3.5 stroke-[3]" />
                                  </button>
                                </div>

                                {/* Subtotal */}
                                <div className="text-right min-w-[70px]">
                                  <span className="block text-[9px] text-slate-400 font-black uppercase tracking-wider">Total</span>
                                  <span className="text-sm font-black text-emerald-700">₹{total.toLocaleString('en-IN', { maximumFractionDigits: 0 })}</span>
                                </div>

                                {/* Remove */}
                                <button
                                  type="button"
                                  onClick={() => handleRemoveFromCart(item.id)}
                                  className="bg-slate-100 hover:bg-rose-50 text-slate-400 hover:text-rose-500 p-2 rounded-xl transition-colors"
                                  title="Remove item"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </button>
                              </div>
                            </div>
                          );
                        })}

                        {/* Zepto "Forgot something? Add More Items" Link */}
                        <div className="pt-4">
                          <button
                            type="button"
                            onClick={() => navigate('/buyer')}
                            className="w-full py-3 px-4 border-2 border-dashed border-rose-200 hover:border-rose-400 rounded-2xl bg-rose-50/40 hover:bg-rose-50/80 text-rose-600 transition-all flex items-center justify-center space-x-2 group"
                          >
                            <span className="text-xs font-medium text-slate-600">Forgot something?</span>
                            <span className="text-xs font-black text-rose-600 group-hover:underline">Add More Items</span>
                            <ArrowRight className="h-3.5 w-3.5 text-rose-500 group-hover:translate-x-0.5 transition-transform" />
                          </button>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Delivery milestone bar */}
                  {cart.length > 0 && (
                    <div className="bg-white rounded-2xl border border-slate-100 p-4 shadow-sm space-y-2">
                      <div className="flex justify-between items-center text-xs">
                        <span className="font-black text-slate-800">Direct Farmer Logistics Milestone</span>
                        <span className="text-emerald-700 font-extrabold">100% Free Farm Delivery Unlocked</span>
                      </div>
                      <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                        <div className="bg-gradient-to-r from-emerald-400 to-emerald-600 h-full w-full rounded-full" />
                      </div>
                      <div className="flex justify-between text-[10px] text-slate-400 font-bold pt-1">
                        <span className="text-emerald-600 font-black">✓ Free Logistics</span>
                        <span>✓ Mandi Quality Checked</span>
                        <span>✓ Escrow Protected</span>
                      </div>
                    </div>
                  )}
                </div>

                {/* Right Column: Checkout Summary Panel */}
                <div className="lg:col-span-4 bg-white rounded-2xl border border-slate-100 p-6 shadow-sm space-y-5">
                  <h3 className="text-base font-black text-slate-800 tracking-tight">Order Summary</h3>

                  <div className="space-y-2.5 text-sm">
                    <div className="flex justify-between text-slate-500 font-medium">
                      <span>Item Total</span>
                      <span className="font-bold text-slate-800">₹{cartTotal.toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                    </div>
                    <div className="flex justify-between text-slate-500 font-medium">
                      <span>Logistics & Delivery</span>
                      <span className="font-bold text-emerald-600">FREE</span>
                    </div>
                    <div className="flex justify-between text-slate-500 font-medium">
                      <span>Escrow Protection Fee</span>
                      <span className="font-bold text-emerald-600">FREE</span>
                    </div>
                    <div className="border-t border-slate-100 pt-3 flex justify-between items-center font-black text-slate-800">
                      <span className="text-base">To Pay</span>
                      <span className="text-xl text-emerald-700">₹{cartTotal.toLocaleString('en-IN', { maximumFractionDigits: 2 })}</span>
                    </div>
                  </div>

                  {/* Primary Action Button (Proceed to Checkout) */}
                  <button
                    onClick={handleCartCheckout}
                    disabled={cart.length === 0}
                    className="w-full bg-rose-600 hover:bg-rose-700 active:scale-98 text-white font-black py-4 rounded-2xl text-sm transition-all flex items-center justify-center space-x-2 shadow-lg shadow-rose-900/20 disabled:opacity-50 group"
                  >
                    <span>Proceed to Checkout</span>
                    <ChevronRight className="h-4 w-4 group-hover:translate-x-0.5 transition-transform" />
                  </button>

                  {/* Multiple Payment Modes Supported Badge */}
                  <div className="pt-2 text-center space-y-1.5 border-t border-slate-100">
                    <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block">
                      Accepted Payment Options
                    </span>
                    <div className="flex flex-wrap items-center justify-center gap-1.5 text-[10px] font-bold text-slate-600">
                      <span className="bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-md">💵 Cash on Delivery</span>
                      <span className="bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-md">⚡ UPI</span>
                      <span className="bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-md">💳 Card</span>
                      <span className="bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-md">🏦 Net Banking</span>
                      <span className="bg-slate-50 border border-slate-200 px-2 py-0.5 rounded-md">🛡️ Escrow</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ══════════════════ TAB: COLD STORAGE ══════════════════ */}
          {tab === 'cold-storage' && (
            <div className="space-y-6">

              <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm text-left max-w-2xl">
                <h3 className="text-lg font-black text-slate-800 mb-5 tracking-tight">Reserve Cold Storage</h3>

                {bookingMessage && (
                  <div className="bg-indigo-50 border border-indigo-100 text-indigo-700 p-4 rounded-xl text-sm font-semibold mb-5">
                    {bookingMessage}
                  </div>
                )}

                <form onSubmit={handleBookStorageSubmit} className="space-y-4">
                  <div>
                    <label className="block text-xs font-black text-slate-500 uppercase tracking-wider mb-2">Storage Facility</label>
                    <select required value={selectedFacility} onChange={e => setSelectedFacility(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm">
                      <option value="">-- Choose Facility --</option>
                      {facilities.map(f => {
                        const isFull = f.availableCapacityTons <= 0;
                        return (
                          <option key={f.id} value={f.id} disabled={isFull}>
                            {f.name} - {f.location} {isFull ? '(No Space - Check for another)' : `(₹${f.pricePerTonDay}/ton/day, Avail: ${f.availableCapacityTons}t)`}
                          </option>
                        );
                      })}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-black text-slate-500 uppercase tracking-wider mb-2">Capacity (Tons)</label>
                    <input type="number" required placeholder="e.g. 10" value={bookingQty} onChange={e => setBookingQty(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm" />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-black text-slate-500 uppercase tracking-wider mb-2">Start Date</label>
                      <input type="date" required value={startDate} onChange={e => setStartDate(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm text-slate-500" />
                    </div>
                    <div>
                      <label className="block text-xs font-black text-slate-500 uppercase tracking-wider mb-2">End Date</label>
                      <input type="date" required value={endDate} onChange={e => setEndDate(e.target.value)}
                        className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none text-sm text-slate-500" />
                    </div>
                  </div>
                  <button type="submit" className="w-full bg-indigo-900 hover:bg-indigo-800 text-white py-3.5 rounded-xl font-black text-sm transition-all shadow-sm">
                    Submit Booking Request
                  </button>
                </form>
              </div>

              <div className="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm text-left">
                <h3 className="text-lg font-black text-slate-800 mb-5 tracking-tight">Your Bookings</h3>
                {bookings.length === 0 ? (
                  <p className="text-sm text-slate-400">No bookings yet.</p>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {bookings.map(b => (
                      <div key={b.id} className="p-5 border border-slate-100 rounded-2xl space-y-3 hover:shadow-sm transition-shadow">
                        <div className="flex justify-between items-center">
                          <span className="font-black text-sm text-slate-800">{b.storageName}</span>
                          <span className={`px-2.5 py-1 text-[9px] font-black rounded-lg ${
                            b.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-600' :
                            b.status === 'PENDING'  ? 'bg-amber-50 text-amber-600' : 'bg-rose-50 text-rose-600'
                          }`}>{b.status}</span>
                        </div>
                        <span className="text-[10px] text-slate-400 font-semibold uppercase block">{b.storageLocation}</span>
                        <div className="grid grid-cols-2 gap-3 text-xs text-slate-500 border-y border-slate-50 py-3">
                          <div>
                            <span className="block text-[9px] font-black uppercase tracking-wider text-slate-400">Duration</span>
                            <span className="font-bold">{b.startDate} → {b.endDate}</span>
                          </div>
                          <div>
                            <span className="block text-[9px] font-black uppercase tracking-wider text-slate-400">Capacity</span>
                            <span className="font-bold">{b.quantityTons} Tons</span>
                          </div>
                        </div>
                        <div className="flex justify-between items-center">
                          <span className="text-xs text-slate-400 font-medium">Booking Fee</span>
                          <span className="font-black text-slate-800">₹{b.totalPrice.toLocaleString('en-IN')}</span>
                        </div>
                        {b.status === 'REJECTED' && b.rejectionReason && (
                          <div className="bg-rose-50 border border-rose-100/50 p-3 rounded-2xl text-[11px] text-rose-700 font-medium text-left mt-2">
                            <strong>Reason:</strong> {b.rejectionReason}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ══════════════════ TAB: RAZORPAY PAYMENTS & ESCROW ══════════════════ */}
          {tab === 'payments' && (
            <div className="space-y-6 text-left">
              {/* Header stats row */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-white rounded-2xl border border-slate-100 p-5 shadow-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Total Volume Paid</span>
                    <CreditCard className="h-4 w-4 text-emerald-600" />
                  </div>
                  <div className="text-2xl font-black text-slate-800">
                    ₹{safePayments
                      .filter(p => p.status === 'CAPTURED')
                      .reduce((s, p) => s + (p.amount || 0), 0)
                      .toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                  </div>
                  <span className="text-[10px] text-emerald-600 font-bold flex items-center space-x-1">
                    <span>✓</span>
                    <span>100% Cryptographically Verified</span>
                  </span>
                </div>

                <div className="bg-gradient-to-br from-[#f0fdf4] to-emerald-50/50 rounded-2xl border border-emerald-100 p-5 shadow-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-black text-emerald-800 uppercase tracking-wider">Active Escrow Reserves</span>
                    <ShieldCheck className="h-4 w-4 text-emerald-600" />
                  </div>
                  <div className="text-2xl font-black text-emerald-900">
                    ₹{safePayments
                      .filter(p => p.escrowStatus === 'HELD_IN_ESCROW')
                      .reduce((s, p) => s + (p.amount || 0), 0)
                      .toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                  </div>
                  <span className="text-[10px] text-emerald-700 font-bold flex items-center space-x-1">
                    <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    <span>Safely Locked Until Delivery Inspection</span>
                  </span>
                </div>

                <div className="bg-white rounded-2xl border border-slate-100 p-5 shadow-sm space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Transactions Count</span>
                    <Package className="h-4 w-4 text-slate-400" />
                  </div>
                  <div className="text-2xl font-black text-slate-800">
                    {payments.length} <span className="text-sm font-semibold text-slate-400">Ledger records</span>
                  </div>
                  <span className="text-[10px] text-slate-500 font-medium">
                    Razorpay Gateway + COD Ledger
                  </span>
                </div>
              </div>

              {/* Escrow assurance note */}
              <div className="bg-emerald-950 text-white rounded-2xl p-5 flex items-start space-x-3.5 shadow-sm">
                <ShieldCheck className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
                <div className="space-y-1 text-xs">
                  <span className="font-extrabold text-white text-sm block">Razorpay Real-Time Escrow Guarantee</span>
                  <p className="text-emerald-200/80 leading-relaxed text-[11px]">
                    All online payments are processed through Razorpay's 256-bit encrypted gateway. Your funds remain in a protected escrow account and are released to the farmer only after you inspect and accept the produce upon delivery.
                  </p>
                </div>
              </div>

              {/* Payments table / cards */}
              <div className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-slate-100 flex justify-between items-center">
                  <div>
                    <h3 className="text-lg font-black text-slate-800 tracking-tight">Payment Transactions</h3>
                    <p className="text-xs text-slate-400 font-medium">Real-time ledger synchronized with Razorpay webhooks</p>
                  </div>
                  <span className="text-xs bg-slate-50 border border-slate-150 text-slate-600 font-black px-3 py-1.5 rounded-xl">
                    {payments.length} Records
                  </span>
                </div>

                {payments.length === 0 ? (
                  <div className="py-16 text-center space-y-3">
                    <CreditCard className="h-12 w-12 mx-auto text-slate-200 stroke-[1.5]" />
                    <p className="text-sm font-bold text-slate-600">No payment transactions recorded yet</p>
                    <p className="text-xs text-slate-400 max-w-sm mx-auto">When you buy crops or reserve cold storage using Razorpay, your payments and escrow hold statements will appear here in real-time.</p>
                  </div>
                ) : (
                  <div className="divide-y divide-slate-100">
                    {payments.map(payment => (
                      <div key={payment.id} className="p-6 hover:bg-slate-50/50 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="space-y-1.5 min-w-0">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="font-mono text-xs font-black text-slate-850">
                              {payment.razorpayPaymentId || payment.razorpayOrderId}
                            </span>
                            <span className={`px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider ${
                              payment.status === 'CAPTURED' ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' :
                              payment.status === 'CREATED' ? 'bg-amber-50 text-amber-700 border border-amber-100' :
                              'bg-rose-50 text-rose-700 border border-rose-100'
                            }`}>
                              {payment.status}
                            </span>
                            <span className={`px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider flex items-center space-x-1 ${
                              payment.escrowStatus === 'HELD_IN_ESCROW' ? 'bg-indigo-50 text-indigo-700 border border-indigo-100' :
                              payment.escrowStatus === 'RELEASED_TO_FARMER' ? 'bg-blue-50 text-blue-700 border border-blue-100' :
                              'bg-slate-100 text-slate-600'
                            }`}>
                              <span>🛡️</span>
                              <span>{payment.escrowStatus.replace(/_/g, ' ')}</span>
                            </span>
                          </div>

                          <div className="text-xs text-slate-500 font-medium flex flex-wrap items-center gap-x-4 gap-y-1">
                            {payment.cropName && (
                              <span>Produce: <strong>{payment.cropName}</strong></span>
                            )}
                            {payment.orderId && (
                              <span>Order: <strong>#AGR-{payment.orderId}</strong></span>
                            )}
                            <span>Method: <strong className="uppercase">{payment.paymentMethod}</strong></span>
                            <span>Date: {payment.createdAt ? new Date(payment.createdAt).toLocaleString('en-IN') : 'Recent'}</span>
                          </div>

                          {payment.failureReason && (
                            <p className="text-[11px] text-rose-600 font-semibold bg-rose-50 px-2 py-1 rounded-lg inline-block">
                              Reason: {payment.failureReason}
                            </p>
                          )}
                        </div>

                        <div className="text-right shrink-0">
                          <span className="block text-xl font-black text-slate-850">
                            ₹{(payment.amount || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                          </span>
                          <span className="text-[10px] text-slate-400 font-medium block">Razorpay 256-Bit Node</span>
                        </div>
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

      {/* Product Detail Modal */}
      {detailProduct && (
        <ProductDetailModal
          product={detailProduct}
          allProducts={products}
          onClose={() => setDetailProduct(null)}
          onAddToCart={handleAddToCart}
          onBuyNow={handleBuyNow}
          favorites={favorites}
          onToggleFavorite={handleToggleFavorite}
          onSelectProduct={setDetailProduct}
          cart={cart}
          onIncrementCart={handleIncrementCart}
          onDecrementCart={handleDecrementCart}
        />
      )}

      {/* Unified Checkout Flow Modal */}
      <CheckoutFlowModal
        isOpen={checkoutOpen}
        onClose={() => setCheckoutOpen(false)}
        product={checkoutProduct}
        cart={cart}
        mode={checkoutMode}
        onSuccess={(placedOrder) => {
          if (checkoutMode === 'cart') {
            setCart([]);
            localStorage.removeItem('mandiplus_cart');
          }
          fetchBuyerData();
          navigate('/buyer/orders');
          window.dispatchEvent(new CustomEvent('mandiplus-toast', {
            detail: {
              title: 'Order Placed Successfully! 🎉',
              message: placedOrder?.cropName 
                ? `${placedOrder.cropName} confirmed! 15-Min Express Farm Dispatch active.`
                : 'Your produce order has been confirmed! Live 15-minute express delivery is in transit.',
              category: 'ORDER',
              timestamp: new Date()
            }
          }));
        }}
      />

      {/* Real-time Delivery Tracking Modal */}
      <LiveDeliveryMapModal
        isOpen={showTrackingModal}
        onClose={() => setShowTrackingModal(false)}
        mode="tracking"
        orderId={selectedTrackingOrder?.id}
        initialAddress={selectedTrackingOrder?.deliveryAddress || 'Sant Savata Mali Nagar, Nashik, Maharashtra'}
      />
    </div>
  );
}
