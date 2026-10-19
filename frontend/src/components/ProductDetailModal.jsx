import React, { useEffect, useRef } from 'react';
import {
  X, Heart, ShoppingCart, MapPin, User, Package,
  Leaf, TrendingUp, Clock, ChevronRight, ShoppingBag, Zap,
  Plus, Minus
} from 'lucide-react';

const CATEGORY_COLORS = {
  VEGETABLES: { bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-100', dot: 'bg-emerald-500' },
  FRUITS:     { bg: 'bg-orange-50',  text: 'text-orange-700',  border: 'border-orange-100',  dot: 'bg-orange-500' },
  GRAINS:     { bg: 'bg-amber-50',   text: 'text-amber-700',   border: 'border-amber-100',   dot: 'bg-amber-500' },
};

function getCategoryFromName(name) {
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
}

export default function ProductDetailModal({
  product,
  allProducts,
  onClose,
  onAddToCart,
  onBuyNow,
  favorites,
  onToggleFavorite,
  onSelectProduct,
  cart = [],
  onIncrementCart,
  onDecrementCart
}) {
  const overlayRef = useRef(null);

  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handleKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', handleKey);
      document.body.style.overflow = '';
    };
  }, [onClose]);

  if (!product) return null;

  console.log('ProductDetailModal received product:', {
    id: product.id,
    cropName: product.cropName,
    farmerName: product.farmerName,
    farmerProfilePhoto: product.farmerProfilePhoto ? `${product.farmerProfilePhoto.substring(0, 100)}...` : null
  });

  const isFav = favorites.some(f => f.id === product.id);
  const cartItem = cart.find(c => c.id === product.id);
  const category = getCategoryFromName(product.cropName);
  const catColors = CATEGORY_COLORS[category] || CATEGORY_COLORS.GRAINS;
  const pricePerKg = (product.pricePerQuintal / 100).toFixed(2);
  const stockKg = product.quantityQuintals * 100;

  // Related: same category, different product
  const related = allProducts
    .filter(p => p.id !== product.id && getCategoryFromName(p.cropName) === category)
    .slice(0, 4);

  // Recently added: last 4 by id
  const recentlyAdded = [...allProducts]
    .filter(p => p.id !== product.id)
    .sort((a, b) => b.id - a.id)
    .slice(0, 4);

  return (
    <div
      ref={overlayRef}
      className="fixed inset-0 z-[200] bg-slate-900/70 backdrop-blur-md flex items-center justify-center p-2 sm:p-4 animate-fade-in"
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
    >
      <div className="bg-white rounded-[28px] w-full max-w-5xl max-h-[92vh] overflow-hidden flex flex-col shadow-2xl border border-slate-100 animate-step-enter">
        
        {/* Header */}
        <div className="flex items-center justify-between px-4 sm:px-8 py-4 sm:py-5 border-b border-slate-100 bg-white shrink-0">
          <div className="flex items-center space-x-3">
            <span className={`inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-wider border ${catColors.bg} ${catColors.text} ${catColors.border}`}>
              <span className={`h-1.5 w-1.5 rounded-full ${catColors.dot}`}></span>
              <span>{category}</span>
            </span>
            <span className="text-xs text-slate-400 font-medium">Product Details</span>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto">
          <div className="grid grid-cols-1 lg:grid-cols-5 min-h-full">
            
            {/* LEFT: Image + Actions */}
            <div className="lg:col-span-2 p-4 sm:p-8 flex flex-col space-y-6 border-b lg:border-b-0 lg:border-r border-slate-100">
              
              {/* Product Title (Mobile Only) */}
              <div className="lg:hidden text-left space-y-0.5">
                <h2 className="text-2xl font-black text-slate-800 tracking-tight leading-tight">{product.cropName}</h2>
                <p className="text-xs text-slate-500 font-semibold">{product.variety}</p>
              </div>

              {/* Product Image */}
              <div className="w-full aspect-[4/3] sm:aspect-square rounded-3xl overflow-hidden bg-slate-50 border border-slate-100 relative flex items-center justify-center">
                {product.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product.cropName}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="flex flex-col items-center space-y-3 text-slate-300">
                    <ShoppingBag className="h-16 w-16 stroke-[1.5]" />
                    <span className="text-xs font-bold uppercase tracking-wider">No Photo</span>
                  </div>
                )}
                <button
                  onClick={() => onToggleFavorite(product)}
                  className={`absolute top-4 right-4 p-2.5 rounded-2xl shadow-sm backdrop-blur-sm transition-all ${
                    isFav
                      ? 'bg-rose-50/90 text-rose-500 border border-rose-100'
                      : 'bg-white/80 text-slate-300 hover:text-rose-400 border border-white/60'
                  }`}
                >
                  <Heart className={`h-5 w-5 ${isFav ? 'fill-current' : ''}`} />
                </button>
              </div>

              {/* Pricing Block */}
              <div className="bg-gradient-to-br from-[#f0fdf4] to-[#ecfdf5] rounded-3xl p-5 border border-emerald-100">
                <span className="text-[10px] font-black text-emerald-700 uppercase tracking-widest block mb-3">Live Market Rate</span>
                <div className="flex items-end justify-between">
                  <div>
                    <span className="text-3xl font-black text-slate-800">₹{pricePerKg}</span>
                    <span className="text-sm text-emerald-700 font-bold ml-2">/ kg</span>
                  </div>
                  <div className="text-right">
                    <span className="block text-sm font-extrabold text-slate-600">₹{product.pricePerQuintal.toLocaleString('en-IN')}</span>
                    <span className="text-[10px] text-slate-400 font-bold">per quintal (100 kg)</span>
                  </div>
                </div>
              </div>

              {/* Action Buttons (Desktop Only) */}
              <div className="hidden lg:block space-y-3">
                <button
                  onClick={() => { onBuyNow(product); onClose(); }}
                  className="w-full bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-black py-4 rounded-2xl text-sm transition-all flex items-center justify-center space-x-2 shadow-lg shadow-emerald-900/20 group"
                >
                  <Zap className="h-4 w-4 group-hover:scale-110 transition-transform" />
                  <span>Buy Now</span>
                </button>
                {cartItem ? (
                  <div className="w-full bg-rose-50 border-2 border-rose-200 rounded-2xl p-2 flex items-center justify-between">
                    <button
                      onClick={() => onDecrementCart && onDecrementCart(product.id)}
                      className="h-10 w-10 rounded-xl bg-white hover:bg-rose-100 text-rose-600 font-black flex items-center justify-center active:scale-90 transition-transform shadow-sm"
                      title="Decrease 1 kg"
                    >
                      <Minus className="h-5 w-5 stroke-[3]" />
                    </button>
                    <div className="text-center px-2">
                      <span className="block text-xs font-black text-rose-600">{cartItem.cartQty} {cartItem.cartUnit || 'kg'} in Cart</span>
                      <span className="text-[10px] text-slate-400 font-bold">₹{((cartItem.cartUnit === 'kg' ? product.pricePerQuintal / 100 : product.pricePerQuintal) * cartItem.cartQty).toLocaleString('en-IN', { maximumFractionDigits: 0 })}</span>
                    </div>
                    <button
                      onClick={() => onIncrementCart && onIncrementCart(product)}
                      className="h-10 w-10 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-black flex items-center justify-center active:scale-90 transition-transform shadow-sm"
                      title="Increase 1 kg"
                    >
                      <Plus className="h-5 w-5 stroke-[3]" />
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => onAddToCart(product)}
                    className="w-full bg-rose-50 hover:bg-rose-100 text-rose-600 border border-rose-200 font-black py-4 rounded-2xl text-sm transition-all flex items-center justify-center space-x-2"
                  >
                    <Plus className="h-4 w-4 stroke-[3]" />
                    <span>Add to Cart (1 kg)</span>
                  </button>
                )}
              </div>
            </div>

            {/* RIGHT: Details */}
            <div className="lg:col-span-3 p-4 sm:p-8 space-y-6 sm:space-y-8">

              {/* Product Title (Desktop Only) */}
              <div className="hidden lg:block">
                <h2 className="text-3xl font-black text-slate-800 tracking-tight leading-tight">{product.cropName}</h2>
                <p className="text-sm text-slate-500 font-semibold mt-1">{product.variety}</p>
              </div>

              {/* Key Stats */}
              <div className="grid grid-cols-3 gap-2 sm:gap-4">
                <div className="bg-slate-50 rounded-2xl p-2.5 sm:p-4 text-center border border-slate-100">
                  <Package className="h-5 w-5 mx-auto text-slate-400 mb-2" />
                  <span className="block text-[10px] text-slate-400 font-black uppercase tracking-wider mb-1">Stock</span>
                  <span className="text-lg font-black text-slate-800">{stockKg.toLocaleString('en-IN')}</span>
                  <span className="block text-[10px] text-slate-500 font-bold">kg available</span>
                </div>
                <div className="bg-slate-50 rounded-2xl p-2.5 sm:p-4 text-center border border-slate-100">
                  <Leaf className="h-5 w-5 mx-auto text-emerald-500 mb-2" />
                  <span className="block text-[10px] text-slate-400 font-black uppercase tracking-wider mb-1">Grade</span>
                  <span className="text-lg font-black text-slate-800">A+</span>
                  <span className="block text-[10px] text-slate-500 font-bold">Farm Fresh</span>
                </div>
                <div className="bg-emerald-50/60 rounded-2xl p-2.5 sm:p-4 text-center border border-emerald-100">
                  <Zap className="h-5 w-5 mx-auto text-amber-500 fill-amber-400 mb-2" />
                  <span className="block text-[10px] text-emerald-800 font-black uppercase tracking-wider mb-1">Delivery</span>
                  <span className="text-lg font-black text-slate-800">10-15</span>
                  <span className="block text-[10px] text-emerald-700 font-bold">Mins (Farmhouse)</span>
                </div>
              </div>

              {/* Farmer Info */}
              <div className="bg-white border border-slate-100 rounded-2xl p-5 space-y-3">
                <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Seller Information</span>
                <div className="flex items-center space-x-3">
                  {product.farmerProfilePhoto ? (
                    <img 
                      src={product.farmerProfilePhoto} 
                      alt={product.farmerName} 
                      className="h-10 w-10 rounded-xl object-cover border border-slate-150 shadow-sm" 
                    />
                  ) : (
                    <div className="h-10 w-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center">
                      <User className="h-5 w-5 text-emerald-600" />
                    </div>
                  )}
                  <div>
                    <span className="block font-extrabold text-slate-800 text-sm">{product.farmerName}</span>
                    <span className="block text-xs text-slate-400 font-medium">{product.farmerPhone}</span>
                  </div>
                </div>
                <div className="flex items-start space-x-2 pt-1">
                  <MapPin className="h-4 w-4 text-slate-400 shrink-0 mt-0.5" />
                  <span className="text-sm text-slate-600 font-medium">{product.location}</span>
                </div>
              </div>

              {/* Description */}
              {product.description && (
                <div className="bg-amber-50/60 border border-amber-100 rounded-2xl p-5">
                  <span className="text-[10px] font-black text-amber-700 uppercase tracking-widest block mb-2">About this Produce</span>
                  <p className="text-sm text-slate-600 leading-relaxed">{product.description}</p>
                </div>
              )}

              {/* Related Products */}
              {related.length > 0 && (
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-sm font-black text-slate-700 uppercase tracking-wider">Related Products</span>
                    <ChevronRight className="h-4 w-4 text-slate-300" />
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {related.map(rel => (
                      <MiniProductCard key={rel.id} product={rel} onSelect={() => onSelectProduct(rel)} />
                    ))}
                  </div>
                </div>
              )}

              {/* Recently Added */}
              {recentlyAdded.length > 0 && (
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-sm font-black text-slate-700 uppercase tracking-wider flex items-center space-x-1.5">
                      <Clock className="h-3.5 w-3.5 text-slate-400" />
                      <span>Recently Added</span>
                    </span>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {recentlyAdded.map(rel => (
                      <MiniProductCard key={rel.id} product={rel} onSelect={() => onSelectProduct(rel)} />
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Sticky Action Bar (Mobile Only) */}
        <div className="lg:hidden bg-white border-t border-slate-100 p-4 flex items-center space-x-3 shrink-0">
          {cartItem ? (
            <div className="flex-1 bg-rose-50 border-2 border-rose-200 rounded-2xl p-1.5 flex items-center justify-between">
              <button
                onClick={() => onDecrementCart && onDecrementCart(product.id)}
                className="h-8 w-8 rounded-xl bg-white text-rose-600 font-black flex items-center justify-center active:scale-90 transition-transform shadow-sm"
                title="Decrease 1 kg"
              >
                <Minus className="h-4 w-4 stroke-[3]" />
              </button>
              <span className="text-xs font-black text-rose-600 px-1">{cartItem.cartQty} {cartItem.cartUnit || 'kg'}</span>
              <button
                onClick={() => onIncrementCart && onIncrementCart(product)}
                className="h-8 w-8 rounded-xl bg-rose-600 text-white font-black flex items-center justify-center active:scale-90 transition-transform shadow-sm"
                title="Increase 1 kg"
              >
                <Plus className="h-4 w-4 stroke-[3]" />
              </button>
            </div>
          ) : (
            <button
              onClick={() => onAddToCart(product)}
              className="flex-1 bg-rose-50 hover:bg-rose-100 text-rose-600 border border-rose-200 font-black py-3.5 rounded-2xl text-xs transition-all flex items-center justify-center space-x-2"
            >
              <Plus className="h-4 w-4 stroke-[3]" />
              <span>Add to Cart (1 kg)</span>
            </button>
          )}
          <button
            onClick={() => { onBuyNow(product); onClose(); }}
            className="flex-1 bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-black py-3.5 rounded-2xl text-xs transition-all flex items-center justify-center space-x-2 shadow-md shadow-emerald-900/10"
          >
            <Zap className="h-4 w-4" />
            <span>Buy Now</span>
          </button>
        </div>
      </div>
    </div>
  );
}

function MiniProductCard({ product, onSelect }) {
  const pricePerKg = (product.pricePerQuintal / 100).toFixed(2);
  return (
    <div
      onClick={onSelect}
      className="bg-slate-50/80 border border-slate-100 rounded-2xl p-3.5 flex space-x-3 items-center hover:bg-slate-100/60 transition-colors cursor-pointer group"
    >
      <div className="h-12 w-12 rounded-xl bg-white border border-slate-100 overflow-hidden shrink-0 flex items-center justify-center">
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.cropName} className="h-full w-full object-cover" />
          : <ShoppingBag className="h-5 w-5 text-slate-300" />
        }
      </div>
      <div className="min-w-0">
        <span className="block font-extrabold text-slate-800 text-xs truncate">{product.cropName}</span>
        <span className="block text-[10px] text-slate-400 font-medium truncate">{product.variety}</span>
        <span className="block text-xs font-black text-emerald-700 mt-0.5">₹{product.pricePerQuintal}/qtl</span>
      </div>
    </div>
  );
}
