import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { 
  MessageSquare, X, Maximize2, Shield, 
  Send, User, ArrowRight, Sparkles 
} from 'lucide-react';

export default function WhatsAppChatWidget() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(false);

  // Don't render floating widget if user is already on the dedicated /chat route
  const isChatRoute = location.pathname === '/chat';

  // Fetch unread count & recent conversations
  const fetchUnreadAndRecent = async () => {
    if (!user) return;
    try {
      const [unreadRes, convRes] = await Promise.allSettled([
        api.get('/chat/unread-count'),
        api.get('/chat/conversations')
      ]);

      if (unreadRes.status === 'fulfilled' && unreadRes.value.data) {
        setUnreadCount(unreadRes.value.data.unreadCount || 0);
      }
      if (convRes.status === 'fulfilled' && convRes.value.data) {
        setConversations(convRes.value.data.slice(0, 4) || []);
      }
    } catch (e) {
      console.warn('Failed to fetch chat widget data:', e);
    }
  };

  useEffect(() => {
    if (user && !isChatRoute) {
      fetchUnreadAndRecent();
    }
  }, [user, location.pathname]);

  // Real-time WebSocket listener to increment unread count & bump conversations
  useEffect(() => {
    const handleWsChat = (e) => {
      const msg = e.detail;
      if (!msg) return;

      // Only increment if incoming message is from someone else
      if (msg.senderId !== user?.id) {
        setUnreadCount((prev) => prev + 1);
        fetchUnreadAndRecent();
      }
    };

    window.addEventListener('mandiplus-chat-message', handleWsChat);
    return () => window.removeEventListener('mandiplus-chat-message', handleWsChat);
  }, [user?.id]);

  if (!user || isChatRoute) return null;

  const handleOpenChatWithPartner = (partnerId) => {
    setIsOpen(false);
    navigate(`/chat?partnerId=${partnerId}`);
  };

  const handleOpenFullChat = () => {
    setIsOpen(false);
    navigate('/chat');
  };

  return (
    <div className="fixed bottom-4 right-4 sm:bottom-6 sm:right-6 z-40 font-sans select-none">
      {/* Expanded WhatsApp Mini Modal */}
      {isOpen && (
        <div className="mb-3 w-[calc(100vw-2rem)] max-w-sm sm:w-96 bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden animate-slide-in flex flex-col max-h-[80vh]">
          {/* Header */}
          <div className="bg-[#00a884] p-4 text-white flex items-center justify-between shadow-md">
            <div className="flex items-center space-x-3 text-left">
              <img 
                src="/images/logo-shield.png" 
                alt="Mandi+" 
                className="h-10 w-10 rounded-full object-contain bg-white p-1 ring-2 ring-white/40 shadow-sm" 
              />
              <div>
                <h4 className="font-bold text-sm leading-tight flex items-center space-x-1.5">
                  <span>Mandi+ WhatsApp</span>
                  <span className="bg-[#25d366] text-white text-[9px] px-1.5 py-0.5 rounded-full font-black uppercase">Live</span>
                </h4>
                <p className="text-[11px] text-emerald-100 opacity-90 leading-none mt-1">Real-time marketplace messaging</p>
              </div>
            </div>

            <div className="flex items-center space-x-1">
              <button 
                onClick={handleOpenFullChat}
                className="p-1.5 text-white/80 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
                title="Open WhatsApp Web Fullscreen"
              >
                <Maximize2 className="h-4 w-4" />
              </button>
              <button 
                onClick={() => setIsOpen(false)}
                className="p-1.5 text-white/80 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
                title="Close"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>

          {/* Quick Notice */}
          <div className="bg-[#f0f2f5] px-4 py-2 border-b border-slate-100 flex items-center justify-between text-[11px] text-[#54656f]">
            <span className="flex items-center space-x-1">
              <Shield className="h-3 w-3 text-[#00a884]" />
              <span>Direct communication • E2E Encrypted</span>
            </span>
            {unreadCount > 0 && (
              <span className="bg-[#25d366] text-white font-black text-[10px] px-1.5 py-0.2 rounded-full">
                {unreadCount} unread
              </span>
            )}
          </div>

          {/* Content Body */}
          <div className="p-3 max-h-72 overflow-y-auto space-y-2 scrollbar-thin">
            {/* Quick Action: Admin Support */}
            <div 
              onClick={() => {
                setIsOpen(false);
                navigate('/chat');
              }}
              className="p-3 bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-100 hover:border-emerald-300 rounded-2xl cursor-pointer transition-all flex items-center justify-between group"
            >
              <div className="flex items-center space-x-3 text-left">
                <div className="h-9 w-9 rounded-full bg-rose-500 text-white font-black flex items-center justify-center text-xs shadow-sm">
                  💬
                </div>
                <div>
                  <h5 className="text-xs font-bold text-slate-800 group-hover:text-emerald-700 transition-colors">
                    Official Admin Helpdesk
                  </h5>
                  <p className="text-[10px] text-slate-500">Escrow verification & trade dispute desk</p>
                </div>
              </div>
              <ArrowRight className="h-4 w-4 text-emerald-600 transform group-hover:translate-x-1 transition-transform" />
            </div>

            {/* Recent Conversations */}
            {conversations.length > 0 ? (
              <div className="space-y-1 pt-1">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block px-1 text-left">
                  Recent WhatsApp Chats
                </span>
                {conversations.map((conv) => (
                  <div
                    key={conv.partnerId}
                    onClick={() => handleOpenChatWithPartner(conv.partnerId)}
                    className="p-2.5 hover:bg-slate-50 rounded-xl cursor-pointer transition-colors flex items-center space-x-3 text-left border border-transparent hover:border-slate-100"
                  >
                    <div className="relative shrink-0">
                      {conv.partnerProfilePhoto ? (
                        <img src={conv.partnerProfilePhoto} alt="Profile" className="h-9 w-9 rounded-full object-cover" />
                      ) : (
                        <div className="h-9 w-9 rounded-full bg-[#00a884] text-white font-bold flex items-center justify-center text-xs">
                          {(conv.partnerName || 'User').charAt(0).toUpperCase()}
                        </div>
                      )}
                      <span className="absolute bottom-0 right-0 h-2 w-2 rounded-full bg-[#25d366] ring-1 ring-white"></span>
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-slate-800 truncate">{conv.partnerName}</span>
                        {conv.unreadCount > 0 && (
                          <span className="bg-[#25d366] text-white text-[9px] font-black px-1.5 rounded-full">
                            {conv.unreadCount}
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] text-slate-500 truncate mt-0.5">{conv.lastMessage || 'Open chat'}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-6 text-center text-slate-400 space-y-1">
                <Sparkles className="h-5 w-5 text-slate-300 mx-auto" />
                <p className="text-xs">No active chats yet</p>
                <p className="text-[10px]">Click below to open WhatsApp Web and start messaging!</p>
              </div>
            )}
          </div>

          {/* Bottom Action Footer */}
          <div className="p-3 bg-slate-50 border-t border-slate-100">
            <button
              onClick={handleOpenFullChat}
              className="w-full bg-[#00a884] hover:bg-[#008069] text-white text-xs font-bold py-2.5 rounded-xl transition-all shadow-md shadow-emerald-700/20 flex items-center justify-center space-x-2"
            >
              <span>Open WhatsApp Web</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      )}

      {/* Floating Action WhatsApp Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative group h-12 w-12 sm:h-14 sm:w-14 rounded-full bg-[#25d366] hover:bg-[#20ba5a] text-white flex items-center justify-center shadow-2xl transition-all duration-300 transform hover:scale-105 active:scale-95 ring-4 ring-emerald-500/20"
        title="Open WhatsApp Chat"
        aria-label="Open WhatsApp live chat"
      >
        {/* Animated pulse ring if unread */}
        {unreadCount > 0 && (
          <span className="absolute -inset-1 rounded-full bg-[#25d366] opacity-75 animate-ping"></span>
        )}

        {/* WhatsApp Icon */}
        <div className="relative">
          {isOpen ? (
            <X className="h-6 w-6 sm:h-7 sm:w-7" />
          ) : (
            <MessageSquare className="h-6 w-6 sm:h-7 sm:w-7 fill-white stroke-[#25d366]" />
          )}
        </div>

        {/* Unread Counter Badge */}
        {unreadCount > 0 && (
          <span className="absolute -top-1.5 -right-1.5 h-6 min-w-6 px-1.5 rounded-full bg-rose-500 text-white text-xs font-black flex items-center justify-center ring-2 ring-white shadow-md animate-bounce">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>
    </div>
  );
}
