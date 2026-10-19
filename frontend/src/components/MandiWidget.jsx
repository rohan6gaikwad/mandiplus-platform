import React, { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, RefreshCw, ShoppingBasket } from 'lucide-react';
import api from '../services/api';

export default function MandiWidget({ limit = 2 }) {
  const [mandiRates, setMandiRates] = useState([
    {
      id: 1,
      cropName: 'Onion',
      variety: 'Nasik Red',
      marketName: 'Lasalgaon Mandi',
      state: 'Maharashtra',
      pricePerQuintal: 1850.0,
      priceChangeYesterday: 45.0
    },
    {
      id: 2,
      cropName: 'Grapes',
      variety: 'Thomson Seedless',
      marketName: 'Nashik Mandi',
      state: 'Maharashtra',
      pricePerQuintal: 7500.0,
      priceChangeYesterday: -120.0
    }
  ]);
  const [loading, setLoading] = useState(false);

  const fetchRates = async (isManual = false) => {
    setLoading(true);
    try {
      const response = isManual 
        ? await api.post('/public/mandi-rates/refresh')
        : await api.get('/public/mandi-rates');
      if (response.data && response.data.length > 0) {
        const shuffled = [...response.data];
        // Fisher-Yates shuffle algorithm to randomize crop positions
        for (let i = shuffled.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
        }
        setMandiRates(shuffled);
      }
    } catch (error) {
      console.warn("Could not fetch real-time mandi prices, utilizing default mockup values.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRates();

    const handleTick = (e) => {
      const data = e.detail;
      if (data.type === 'MANDI_DELETE') {
        setMandiRates((prev) => prev.filter((rate) => rate.id !== data.id));
      } else {
        setMandiRates((prev) => {
          const exists = prev.some((rate) => rate.id === data.id);
          if (exists) {
            return prev.map((rate) => 
              rate.id === data.id 
                ? {
                    ...rate,
                    cropName: data.cropName,
                    variety: data.variety,
                    marketName: data.marketName,
                    pricePerQuintal: data.pricePerQuintal,
                    priceChangeYesterday: data.priceChangeYesterday
                  }
                : rate
            );
          } else {
            return [
              ...prev,
              {
                id: data.id,
                cropName: data.cropName,
                variety: data.variety,
                marketName: data.marketName,
                state: data.state || 'Maharashtra',
                pricePerQuintal: data.pricePerQuintal,
                priceChangeYesterday: data.priceChangeYesterday
              }
            ];
          }
        });
      }
    };

    window.addEventListener('mandi-tick', handleTick);
    return () => window.removeEventListener('mandi-tick', handleTick);
  }, []);

  return (
    <div className="font-sans">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h3 className="text-lg font-black text-slate-800 tracking-tight">Live Market Rates (APMC)</h3>
          <p className="text-xs text-slate-400 mt-0.5">Real-time daily updates from Maharashtra mandis</p>
        </div>
        <button 
          onClick={() => fetchRates(true)} 
          disabled={loading}
          className="text-slate-400 hover:text-slate-600 transition-colors p-1.5 rounded-lg hover:bg-slate-100"
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
        {mandiRates.slice(0, limit).map((rate) => {
          const isPositive = rate.priceChangeYesterday >= 0;
          
          // Generate beautiful custom interactive SVG sparkline trend details
          const prices = [
            rate.pricePerQuintal - (rate.priceChangeYesterday * 1.6),
            rate.pricePerQuintal - (rate.priceChangeYesterday * 1.1),
            rate.pricePerQuintal - (rate.priceChangeYesterday * 0.7),
            rate.pricePerQuintal - (rate.priceChangeYesterday * 0.3),
            rate.pricePerQuintal
          ];
          const minVal = Math.min(...prices) * 0.99;
          const maxVal = Math.max(...prices) * 1.01;
          const height = 32;
          const width = 80;
          const points = prices.map((p, index) => {
            const x = (index / (prices.length - 1)) * width;
            const y = height - ((p - minVal) / (maxVal - minVal || 1)) * height;
            return `${x},${y}`;
          }).join(' ');

          return (
            <div 
              key={rate.id}
              className="bg-white/80 hover:bg-white rounded-[28px] p-5 border border-slate-150/60 shadow-sm hover:shadow-premium hover:-translate-y-1 transition-all duration-500 flex flex-col justify-between backdrop-blur-md"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center space-x-2.5">
                  <div className="bg-agrogreen-50 p-2.5 rounded-xl text-agrogreen-700">
                    <ShoppingBasket className="h-5 w-5" />
                  </div>
                  <div className="text-left">
                    <h4 className="font-extrabold text-slate-800 tracking-tight leading-none mb-1">
                      {rate.cropName} <span className="text-[10px] text-slate-400 font-normal">({rate.variety})</span>
                    </h4>
                    <span className="text-[9px] text-slate-400 font-bold uppercase tracking-wider block">{rate.marketName}</span>
                  </div>
                </div>
              </div>

              <div className="flex items-end justify-between pt-2 border-t border-slate-50/50">
                <div className="text-left">
                  <span className="text-xl font-black text-slate-800">₹{rate.pricePerQuintal.toLocaleString('en-IN')}</span>
                  <span className="text-[10px] text-slate-400 ml-1">/ qtl</span>
                </div>
                
                {/* SVG Area Sparkline */}
                <div className="w-20 h-8 shrink-0 flex items-center justify-center">
                  <svg className="w-20 h-8 overflow-visible" viewBox="0 0 80 32">
                    <defs>
                      <linearGradient id={`grad-${rate.id}`} x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={isPositive ? '#10b981' : '#ef4444'} stopOpacity="0.25"/>
                        <stop offset="100%" stopColor={isPositive ? '#10b981' : '#ef4444'} stopOpacity="0"/>
                      </linearGradient>
                    </defs>
                    <path
                      d={`M 0,32 L ${points} L 80,32 Z`}
                      fill={`url(#grad-${rate.id})`}
                    />
                    <polyline
                      fill="none"
                      stroke={isPositive ? '#10b981' : '#ef4444'}
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      points={points}
                    />
                  </svg>
                </div>
                
                <span className={`text-[10px] font-extrabold px-2.5 py-1 rounded-lg flex items-center space-x-1 border ${
                  isPositive 
                    ? 'bg-emerald-50/80 text-emerald-700 border-emerald-100/50' 
                    : 'bg-rose-50/80 text-rose-700 border-rose-100/50'
                }`}>
                  {isPositive ? <TrendingUp className="h-3 w-3 stroke-[2.5]" /> : <TrendingDown className="h-3 w-3 stroke-[2.5]" />}
                  <span>{isPositive ? '+' : ''}{Math.round(rate.priceChangeYesterday)}</span>
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
