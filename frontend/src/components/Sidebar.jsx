import React from 'react';
import { NavLink, useNavigate, Link } from 'react-router-dom';
import { 
  LayoutDashboard, PlusCircle, ShoppingBag, 
  IndianRupee, Warehouse, UserCheck, 
  Settings, Users, ClipboardList, LogOut, Leaf, ShoppingCart,
  ShieldAlert, MessageSquare, Bike, CreditCard, TrendingUp, Database
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';

export default function Sidebar() {
  const { user, logout } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  if (!user) return null;

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Sidebar Links based on logged-in role
  const getSidebarLinks = () => {
    switch (user.role) {
      case 'FARMER':
        return [
          { path: '/farmer', label: t('dashboard'), icon: LayoutDashboard },
          { path: '/farmer/mandi-rates', label: 'Govt Mandi Rates', icon: TrendingUp },
          { path: '/profile', label: 'My Account & Profile', icon: UserCheck },
          { path: '/chat', label: 'WhatsApp Chat', icon: MessageSquare, isWhatsApp: true },
          { path: '/farmer/add-product', label: t('listCrop'), icon: PlusCircle },
          { path: '/farmer/orders', label: t('incomingOrders'), icon: ShoppingBag },
          { path: '/farmer/earnings', label: t('earningsRegistry'), icon: IndianRupee },
          { path: '/farmer/cold-storage', label: t('coldStorageLeases'), icon: Warehouse },
          { path: '/farmer/complaints', label: t('complaintsDesk'), icon: ShieldAlert },
          { path: '/schemes', label: t('govtSchemes'), icon: ClipboardList },
        ];
      case 'BUYER':
        return [
          { path: '/buyer', label: t('dashboard') + ' / Shop', icon: LayoutDashboard },
          { path: '/profile', label: 'My Account & Profile', icon: UserCheck },
          { path: '/chat', label: 'WhatsApp Chat', icon: MessageSquare, isWhatsApp: true },
          { path: '/buyer/cart', label: t('shoppingCart'), icon: ShoppingCart },
          { path: '/buyer/orders', label: t('purchasedOrders'), icon: ShoppingBag },
          { path: '/buyer/payments', label: 'Razorpay & Escrow', icon: CreditCard },
          { path: '/buyer/favorites', label: t('favoritesListings'), icon: PlusCircle },
          { path: '/buyer/complaints', label: t('complaintsDesk'), icon: ShieldAlert },
        ];
      case 'COLD_STORAGE_MANAGER':
        return [
          { path: '/storage', label: t('dashboard'), icon: LayoutDashboard },
          { path: '/profile', label: 'My Account & Profile', icon: UserCheck },
          { path: '/chat', label: 'WhatsApp Chat', icon: MessageSquare, isWhatsApp: true },
          { path: '/storage/requests', label: t('bookingsRequests'), icon: ClipboardList },
          { path: '/storage/records', label: t('activeWarehousingRecords'), icon: Warehouse },
          { path: '/storage/complaints', label: t('complaintsDesk'), icon: ShieldAlert },
        ];
      case 'ADMIN':
        return [
          { path: '/admin', label: t('dashboard'), icon: LayoutDashboard },
          { path: '/farmer/mandi-rates', label: 'Govt Mandi Rates', icon: TrendingUp },
          { path: '/admin/mandi-management', label: 'Mandi Ops & Sync', icon: Database },
          { path: '/profile', label: 'My Account & Profile', icon: UserCheck },
          { path: '/chat', label: 'WhatsApp Chat', icon: MessageSquare, isWhatsApp: true },
          { path: '/admin/users', label: t('usersControl'), icon: Users },
          { path: '/admin/products', label: t('productsControl'), icon: Leaf },
          { path: '/admin/deliveries', label: 'Delivery Logistics Desk', icon: Bike },
          { path: '/admin/mandi', label: t('mandiControl'), icon: IndianRupee },
          { path: '/admin/complaints', label: t('complaints'), icon: ClipboardList },
        ];
      case 'DELIVERY_PARTNER':
        return [
          { path: '/delivery', label: 'Command Center', icon: LayoutDashboard },
          { path: '/profile', label: 'My Account & Profile', icon: UserCheck },
          { path: '/chat', label: 'WhatsApp Support', icon: MessageSquare, isWhatsApp: true },
          { path: '/delivery/tasks', label: 'Trip Ledger', icon: Bike },
          { path: '/delivery/earnings', label: 'Earnings & Payouts', icon: IndianRupee },
          { path: '/delivery/complaints', label: 'Grievance Desk', icon: ShieldAlert },
          { path: '/delivery/profile', label: 'Fleet Profile', icon: UserCheck },
        ];
      default:
        return [];
    }
  };

  const links = getSidebarLinks();

  return (
    <aside className="w-64 bg-slate-900 text-slate-200 min-h-screen flex flex-col justify-between border-r border-slate-800 font-sans">
      {/* Brand Header */}
      <div className="p-6">
        <Link to="/" className="block group mb-6" title="Return to Mandi+ Home">
          <div className="bg-white rounded-2xl p-2.5 shadow-md flex items-center justify-center hover:shadow-lg transition-all duration-300 group-hover:scale-[1.02]">
            <img 
              src="/images/logo.png" 
              alt="Mandi+ Market Solutions" 
              className="h-12 w-auto object-contain" 
            />
          </div>
        </Link>
        
        {/* User Card */}
        <Link 
          to="/profile"
          className="mt-4 bg-slate-800/50 hover:bg-slate-800 border border-slate-800 hover:border-slate-700 rounded-xl p-4 flex items-center space-x-3 transition-all duration-200 group block text-left"
        >
          <div className="relative shrink-0">
            {user.profilePhoto ? (
              <img 
                src={user.profilePhoto} 
                alt="Profile" 
                className="h-10 w-10 rounded-full object-cover border border-slate-700 group-hover:border-agrogreen-500 transition-colors" 
              />
            ) : (
              <div className="h-10 w-10 rounded-full bg-agrogreen-500 flex items-center justify-center font-bold text-white text-base">
                {(user.name || user.email || user.phone || 'U').charAt(0).toUpperCase()}
              </div>
            )}
            <span className="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full bg-emerald-500 ring-2 ring-slate-900"></span>
          </div>
          <div className="overflow-hidden flex-1">
            <h5 className="font-semibold text-sm text-slate-100 truncate group-hover:text-agrogreen-400 transition-colors">
              {user.name || (user.email ? user.email.split('@')[0] : (user.phone || 'User'))}
            </h5>
            <span className="text-[10px] text-slate-400 font-bold tracking-wider uppercase block">
              {(user.role || '').replace(/_/g, ' ')}
            </span>
          </div>
        </Link>
      </div>

      {/* Navigation list */}
      <nav className="flex-1 px-4 py-6 space-y-1.5">
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <NavLink
              key={link.path}
              to={link.path}
              end
              className={({ isActive }) => 
                `flex items-center space-x-3.5 px-4 py-3 rounded-xl text-sm font-semibold transition-all duration-200 ${
                  isActive 
                    ? 'bg-agrogreen-600 text-white shadow-lg shadow-agrogreen-800/30' 
                    : 'text-slate-400 hover:bg-slate-800/70 hover:text-slate-100'
                }`
              }
            >
              <Icon className="h-5 w-5 shrink-0" />
              <span>{link.label}</span>
              {link.isWhatsApp && (
                <span className="ml-auto text-[9px] font-black bg-[#25d366] text-white px-2 py-0.5 rounded-full uppercase tracking-wider shadow-sm">
                  LIVE
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Logout button */}
      <div className="p-4 border-t border-slate-800/80">
        <button
          onClick={handleLogout}
          className="w-full flex items-center space-x-3.5 px-4 py-3 rounded-xl text-sm font-semibold text-rose-400 hover:bg-rose-950/20 transition-all duration-200"
        >
          <LogOut className="h-5 w-5 shrink-0" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
}
