import React, { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';
import api from '../services/api';

export default function MandiTicker() {
  const [rates, setRates] = useState([]);

  useEffect(() => {
    const fetchRates = async () => {
      try {
        const response = await api.get('/public/mandi-rates');
        setRates(response.data.map(item => ({
          id: item.id,
          crop: `${item.cropName} (${item.marketName})`,
          price: `₹${Math.round(item.pricePerQuintal)}`,
          trend: item.priceChangeYesterday >= 0 ? 'up' : 'down',
          percentage: `${Math.abs(Math.round((item.priceChangeYesterday / Math.max(1, item.pricePerQuintal - item.priceChangeYesterday)) * 1000) / 10)}%`
        })));
      } catch (err) {
        console.error("Mandi ticker load failed:", err);
      }
    };
    fetchRates();

    const handleTick = (e) => {
      const data = e.detail;
      if (data.type === 'MANDI_DELETE') {
        setRates((prev) => prev.filter((rate) => rate.id !== data.id));
      } else {
        setRates((prev) => {
          const exists = prev.some((rate) => rate.id === data.id);
          if (exists) {
            return prev.map((rate) => 
              rate.id === data.id 
                ? {
                    ...rate,
                    crop: `${data.cropName} (${data.marketName})`,
                    price: `₹${Math.round(data.pricePerQuintal)}`,
                    trend: data.priceChangeYesterday >= 0 ? 'up' : 'down',
                    percentage: `${Math.abs(Math.round((data.priceChangeYesterday / Math.max(1, data.pricePerQuintal - data.priceChangeYesterday)) * 1000) / 10)}%`
                  }
                : rate
            );
          } else {
            return [
              ...prev,
              {
                id: data.id,
                crop: `${data.cropName} (${data.marketName})`,
                price: `₹${Math.round(data.pricePerQuintal)}`,
                trend: data.priceChangeYesterday >= 0 ? 'up' : 'down',
                percentage: `${Math.abs(Math.round((data.priceChangeYesterday / Math.max(1, data.pricePerQuintal - data.priceChangeYesterday)) * 1000) / 10)}%`
              }
            ];
          }
        });
      }
    };

    window.addEventListener('mandi-tick', handleTick);
    return () => window.removeEventListener('mandi-tick', handleTick);
  }, []);

  if (rates.length === 0) return null;

  return (
    <div className="bg-[#9c5333] text-white py-1.5 overflow-hidden flex whitespace-nowrap border-b border-[#7a4128]">
      <div className="animate-marquee flex space-x-12 items-center text-xs font-bold font-sans">
        {[...rates, ...rates, ...rates].map((rate, idx) => (
          <div key={idx} className="flex items-center space-x-2">
            <span className="text-white/90">{rate.crop}:</span>
            <span>{rate.price}/qtl</span>
            {rate.trend === 'up' ? (
              <span className="flex items-center text-[#4ade80] ml-1">
                <TrendingUp className="h-3.5 w-3.5 mr-0.5" />
                {rate.percentage}
              </span>
            ) : (
              <span className="flex items-center text-[#f87171] ml-1">
                <TrendingDown className="h-3.5 w-3.5 mr-0.5" />
                {rate.percentage}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
