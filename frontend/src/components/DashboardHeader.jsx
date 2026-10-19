import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import LanguageSelector from './LanguageSelector';
import { LogOut, User, Bell, CheckCircle, Shield, Sparkles, MessageSquare } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import MandiTicker from './MandiTicker';

export default function DashboardHeader({ title }) {
  const { user, logout, alerts, wsConnected } = useAuth();
  const { t } = useLanguage();
  const [showNotifications, setShowNotifications] = useState(false);
  const navigate = useNavigate();
  const dropdownRef = useRef(null);


  const handleLogout = () => {
    logout();
    navigate('/');
  };

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowNotifications(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const categoryColors = {
    ORDER: 'bg-amber-500',
    PAYMENT: 'bg-emerald-500',
    COLD_STORAGE: 'bg-indigo-500',
    MARKETPLACE: 'bg-sky-500',
    SYSTEM: 'bg-slate-500'
  };

  const [lastSeenCount, setSeenCount] = useState(0);

  useEffect(() => {
    if (showNotifications) {
      setSeenCount(alerts.length);
    }
  }, [alerts.length, showNotifications]);

  const unreadCount = Math.max(0, alerts.length - lastSeenCount);

  return (
    <div className="flex flex-col w-full shrink-0">
      {/* Live Mandi Ticker on Top of Dashboard */}
      <MandiTicker />
      
      <header className="bg-white border-b border-slate-100 h-20 px-4 sm:px-8 flex items-center justify-between font-sans relative z-40">
        <div className="min-w-0 flex-1 mr-3 sm:mr-6 text-left">
          <h1 className="text-2xl font-bold text-slate-800 tracking-tight truncate">{title}</h1>
          <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5 mt-0.5">
            <p className="text-xs text-slate-400 whitespace-nowrap hidden md:inline-block">Mandi+ Portal • Smart Farming Marketplace</p>
            <span className="h-1.5 w-1.5 rounded-full bg-slate-200 hidden md:inline-block"></span>
            <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider flex items-center whitespace-nowrap">
              Region: Maharashtra
            </span>
          </div>
        </div>

        <div className="flex items-center space-x-3 sm:space-x-4">
          <LanguageSelector />

          {/* Quick WhatsApp Chat Icon */}
          <Link
            to="/chat"
            className="text-slate-400 hover:text-[#00a884] transition-colors p-2 rounded-xl hover:bg-emerald-50 relative"
            title="Open WhatsApp Web"
          >
            <MessageSquare className="h-5 w-5" />
          </Link>

          {/* Quick notification bell with dropdown */}
          <div className="relative" ref={dropdownRef}>
            <button 
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative text-slate-400 hover:text-slate-600 transition-colors p-2 rounded-xl hover:bg-slate-50"
            >
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 h-4 min-w-4 px-1 rounded-full bg-rose-500 text-white text-[9px] font-black flex items-center justify-center ring-2 ring-white">
                  {unreadCount}
                </span>
              )}
              <Bell className="h-5 w-5" />
            </button>

            {/* Premium Glassmorphic Notification Dropdown */}
            {showNotifications && (
              <div className="absolute right-0 mt-3 w-80 bg-white/95 backdrop-blur-md rounded-3xl border border-slate-100 shadow-xl py-3 z-50 text-left">
                <div className="px-4 py-2 border-b border-slate-100 flex items-center justify-between">
                  <span className="font-extrabold text-sm text-slate-800">Platform Alerts</span>
                  
                  {/* WebSocket Live Connection Pulse */}
                  <div className="flex items-center space-x-1.5 bg-slate-50 px-2.5 py-1 rounded-full border border-slate-100">
                    <span className={`h-2 w-2 rounded-full ${wsConnected ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`}></span>
                    <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider">
                      {wsConnected ? 'Connected' : 'Offline'}
                    </span>
                  </div>
                </div>

                <div className="max-h-64 overflow-y-auto px-2 py-1 space-y-1.5 scrollbar-thin">
                  {alerts.length === 0 ? (
                    <div className="py-8 text-center text-slate-400 space-y-2">
                      <Sparkles className="h-6 w-6 text-slate-300 mx-auto" />
                      <p className="text-xs">All caught up! No alerts.</p>
                    </div>
                  ) : (
                    alerts.map((alert, idx) => (
                      <div 
                        key={idx} 
                        className="p-3 hover:bg-slate-50/80 rounded-2xl border border-transparent hover:border-slate-100/50 transition-all flex items-start space-x-3 text-xs"
                      >
                        <span className={`h-2 w-2 rounded-full mt-1.5 shrink-0 ${categoryColors[alert.category] || 'bg-agrogreen-500'}`}></span>
                        <div className="flex-1 space-y-0.5">
                          <p className="font-bold text-slate-700">{alert.title}</p>
                          <p className="text-slate-500 text-[11px] leading-relaxed">{alert.message}</p>
                          <span className="text-[9px] text-slate-400 block pt-0.5">
                            {new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                          </span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Profile details */}
          <Link to="/profile" className="flex items-center space-x-3.5 pl-4 border-l border-slate-100 hover:opacity-90 transition-opacity text-left">
            <div className="text-right hidden lg:block">
              <h4 className="font-semibold text-sm text-slate-800">{user?.email.split('@')[0]}</h4>
              <span className="text-xs text-agrogreen-600 font-semibold uppercase">{user?.role.replace(/_/g, ' ')}</span>
            </div>
            {user?.profilePhoto ? (
              <img src={user.profilePhoto} alt="Profile" className="h-10 w-10 rounded-xl object-cover border border-slate-200" />
            ) : (
              <div className="h-10 w-10 rounded-xl bg-agrogreen-50 border border-agrogreen-100 flex items-center justify-center text-agrogreen-700">
                <User className="h-5 w-5" />
              </div>
            )}
          </Link>
        </div>
      </header>
    </div>
  );
}
