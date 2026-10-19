import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import api from '../services/api';
import { 
  Search, Send, Paperclip, Smile, MoreVertical, 
  Phone, Video, Check, CheckCheck, ArrowLeft, 
  MessageSquare, User, Shield, Warehouse, 
  ShoppingBag, Sparkles, X, Image as ImageIcon,
  Mic, MicOff, Play, Pause, RefreshCw, Info,
  Store, HelpCircle, PhoneCall, PhoneOff,
  Camera, Film, Music, FileText, Download,
  Volume2, VolumeX, ShieldCheck
} from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';

export default function ChatPage() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const [searchParams] = useSearchParams();
  const initialPartnerId = searchParams.get('partnerId');

  const [conversations, setConversations] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [selectedPartner, setSelectedPartner] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('chats'); // 'chats' | 'contacts'
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [loadingConversations, setLoadingConversations] = useState(true);
  
  // Popovers & Drawers
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [showAttachMenu, setShowAttachMenu] = useState(false);
  const [showProfileDrawer, setShowProfileDrawer] = useState(false);
  
  // Media Preview Before Sending
  const [pendingMedia, setPendingMedia] = useState(null); // { type: 'IMAGE'|'VIDEO'|'AUDIO', url: '', file: null, caption: '' }
  const [isUploadingMedia, setIsUploadingMedia] = useState(false);
  
  // Lightbox for Fullscreen Image View
  const [lightboxImage, setLightboxImage] = useState(null);

  // Real Audio Voice Recording
  const [isRecordingVoice, setIsRecordingVoice] = useState(false);
  const [recordingSeconds, setRecordingSeconds] = useState(0);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  // Active Audio Playback state
  const [currentPlayingId, setCurrentPlayingId] = useState(null);
  const audioElementsRef = useRef({});

  // Simulated Voice/Video Call
  const [activeCall, setActiveCall] = useState(null); // null | { type: 'voice' | 'video', status: 'ringing' | 'connected', duration: 0 }

  const messagesEndRef = useRef(null);
  const recordingTimerRef = useRef(null);
  const callTimerRef = useRef(null);
  const inputRef = useRef(null);
  const imageInputRef = useRef(null);
  const videoInputRef = useRef(null);
  const audioInputRef = useRef(null);

  // Scroll messages to bottom smoothly
  const scrollToBottom = (behavior = 'smooth') => {
    messagesEndRef.current?.scrollIntoView({ behavior });
  };

  // Fetch conversations list
  const fetchConversations = async () => {
    try {
      const res = await api.get('/chat/conversations');
      setConversations(res.data || []);
      return res.data || [];
    } catch (err) {
      console.warn('Failed to load conversations:', err);
      return [];
    } finally {
      setLoadingConversations(false);
    }
  };

  // Fetch available contacts
  const fetchContacts = async () => {
    try {
      const res = await api.get('/chat/contacts');
      setContacts(res.data || []);
      return res.data || [];
    } catch (err) {
      console.warn('Failed to load contacts:', err);
      return [];
    }
  };

  // Initial load
  useEffect(() => {
    fetchConversations().then((convs) => {
      fetchContacts().then((allContacts) => {
        // If partnerId param is in URL, auto-select partner
        if (initialPartnerId) {
          const pId = Number(initialPartnerId);
          const foundInConv = convs.find(c => c.partnerId === pId);
          if (foundInConv) {
            selectPartnerFromConversation(foundInConv);
          } else {
            const foundInContact = allContacts.find(c => c.id === pId);
            if (foundInContact) {
              selectPartnerFromContact(foundInContact);
            }
          }
        } else if (convs.length > 0 && window.innerWidth >= 768) {
          // Auto select first conversation on desktop
          selectPartnerFromConversation(convs[0]);
        } else if (allContacts.length > 0 && window.innerWidth >= 768) {
          // Auto select primary contact (Admin Helpdesk for regular users)
          selectPartnerFromContact(allContacts[0]);
        }
      });
    });
  }, [initialPartnerId]);

  // Load message history when partner changes
  useEffect(() => {
    if (!selectedPartner) return;
    
    setLoadingHistory(true);
    api.get(`/chat/history/${selectedPartner.id}`)
      .then(res => {
        setMessages(res.data || []);
        setTimeout(() => scrollToBottom('auto'), 100);
        // Mark as read on backend
        api.post(`/chat/read/${selectedPartner.id}`).catch(() => {});
        // Update local conversation unread count
        setConversations(prev => prev.map(c => 
          c.partnerId === selectedPartner.id ? { ...c, unreadCount: 0 } : c
        ));
      })
      .catch(err => console.error('Failed to load chat history:', err))
      .finally(() => setLoadingHistory(false));
  }, [selectedPartner?.id]);

  // Real-time WebSocket listener
  useEffect(() => {
    const handleIncomingMessage = (e) => {
      const msg = e.detail;
      if (!msg) return;

      // If message is from or to the currently open chat partner
      if (selectedPartner && (msg.senderId === selectedPartner.id || msg.receiverId === selectedPartner.id)) {
        setMessages(prev => {
          if (prev.some(m => m.id === msg.id)) return prev;
          return [...prev, msg];
        });
        setTimeout(() => scrollToBottom('smooth'), 50);

        // Mark as read immediately if it's from the active partner
        if (msg.senderId === selectedPartner.id) {
          api.post(`/chat/read/${selectedPartner.id}`).catch(() => {});
        }
      }

      // Update conversations list in real-time
      setConversations(prev => {
        const partnerId = msg.senderId === user?.id ? msg.receiverId : msg.senderId;
        const existingIdx = prev.findIndex(c => c.partnerId === partnerId);
        
        const isCurrentActive = selectedPartner && selectedPartner.id === partnerId;
        const updatedEntry = {
          partnerId,
          partnerName: msg.senderId === user?.id ? msg.receiverName : msg.senderName,
          partnerEmail: msg.senderId === user?.id ? msg.receiverEmail : msg.senderEmail,
          partnerRole: msg.senderId === user?.id ? msg.receiverRole : msg.senderRole,
          lastMessage: msg.messageType === 'IMAGE' ? '📷 Photo' : 
                       msg.messageType === 'VIDEO' ? '🎥 Video' : 
                       msg.messageType === 'AUDIO' ? '🎤 Voice Note' : msg.content,
          lastMessageAt: msg.createdAt,
          lastMessageType: msg.messageType,
          unreadCount: (existingIdx >= 0 ? prev[existingIdx].unreadCount : 0) + (isCurrentActive || msg.senderId === user?.id ? 0 : 1),
          isOnline: true
        };

        if (existingIdx >= 0) {
          const rest = prev.filter((_, idx) => idx !== existingIdx);
          return [updatedEntry, ...rest];
        } else {
          return [updatedEntry, ...prev];
        }
      });
    };

    window.addEventListener('mandiplus-chat-message', handleIncomingMessage);
    return () => window.removeEventListener('mandiplus-chat-message', handleIncomingMessage);
  }, [selectedPartner, user?.id]);

  // Voice recording timer
  useEffect(() => {
    if (isRecordingVoice) {
      setRecordingSeconds(0);
      recordingTimerRef.current = setInterval(() => {
        setRecordingSeconds(s => s + 1);
      }, 1000);
    } else {
      clearInterval(recordingTimerRef.current);
    }
    return () => clearInterval(recordingTimerRef.current);
  }, [isRecordingVoice]);

  // Active call duration timer
  useEffect(() => {
    if (activeCall && activeCall.status === 'connected') {
      callTimerRef.current = setInterval(() => {
        setActiveCall(prev => prev ? { ...prev, duration: prev.duration + 1 } : null);
      }, 1000);
    } else {
      clearInterval(callTimerRef.current);
    }
    return () => clearInterval(callTimerRef.current);
  }, [activeCall?.status]);

  const selectPartnerFromConversation = (conv) => {
    setSelectedPartner({
      id: conv.partnerId,
      name: conv.partnerName,
      email: conv.partnerEmail,
      role: conv.partnerRole,
      profilePhoto: conv.partnerProfilePhoto || conv.partnerPhoto,
      isOnline: conv.isOnline,
      subtitle: getRoleSubtitle(conv.partnerRole)
    });
  };

  const selectPartnerFromContact = (contact) => {
    setSelectedPartner({
      id: contact.id,
      name: contact.name,
      email: contact.email,
      role: contact.role,
      profilePhoto: contact.profilePhoto,
      isOnline: contact.isOnline,
      subtitle: contact.subtitle || getRoleSubtitle(contact.role)
    });
    setActiveTab('chats');
  };

  const getRoleSubtitle = (role) => {
    switch (role) {
      case 'ADMIN': return 'Mandi+ Official Helpdesk & Escrow Support';
      case 'FARMER': return 'Verified Harvest Producer • Maharashtra';
      case 'BUYER': return 'Commercial Wholesale Trader';
      case 'COLD_STORAGE_MANAGER': return 'IoT Cold Storage Facility';
      default: return 'Mandi+ Member';
    }
  };

  const getRoleBadge = (role) => {
    switch (role) {
      case 'ADMIN':
        return <span className="bg-rose-100 text-rose-700 text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider flex items-center space-x-1"><ShieldCheck className="h-3 w-3 inline mr-0.5" />Admin</span>;
      case 'FARMER':
        return <span className="bg-emerald-100 text-emerald-800 text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider">Farmer</span>;
      case 'BUYER':
        return <span className="bg-sky-100 text-sky-800 text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider">Buyer</span>;
      case 'COLD_STORAGE_MANAGER':
        return <span className="bg-amber-100 text-amber-800 text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider">Cold Storage</span>;
      default:
        return null;
    }
  };

  // SEND TEXT OR MEDIA MESSAGE
  const handleSendMessage = async (customContent = null, customType = 'TEXT') => {
    const textToSend = customContent || messageText.trim();
    if (!textToSend || !selectedPartner) return;

    setMessageText('');
    setShowEmojiPicker(false);
    setShowAttachMenu(false);

    // Optimistic UI push
    const optimisticMessage = {
      id: Date.now(),
      senderId: user.id,
      senderEmail: user.email,
      senderName: user.name || user.email.split('@')[0],
      senderRole: user.role,
      receiverId: selectedPartner.id,
      receiverEmail: selectedPartner.email,
      receiverName: selectedPartner.name,
      receiverRole: selectedPartner.role,
      content: textToSend,
      messageType: customType,
      createdAt: new Date().toISOString(),
      isRead: false
    };

    setMessages(prev => [...prev, optimisticMessage]);
    setTimeout(() => scrollToBottom('smooth'), 50);

    try {
      const res = await api.post('/chat/send', {
        receiverId: selectedPartner.id,
        content: textToSend,
        messageType: customType
      });

      if (res.data) {
        setMessages(prev => prev.map(m => m.id === optimisticMessage.id ? res.data : m));
      }
    } catch (err) {
      console.error('Failed to send message:', err);
    }
  };

  // -------------------------------------------------------------
  // REAL AUDIO RECORDING (VOICE NOTES)
  // -------------------------------------------------------------
  const startRecordingAudio = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioChunksRef.current = [];
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        stream.getTracks().forEach(track => track.stop());
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        
        // Try uploading via multipart endpoint or read as base64
        try {
          const formData = new FormData();
          formData.append('file', audioBlob, `voice-note-${Date.now()}.webm`);
          formData.append('fileType', 'AUDIO');
          
          const uploadRes = await api.post('/chat/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          if (uploadRes.data?.url) {
            handleSendMessage(uploadRes.data.url, 'AUDIO');
            return;
          }
        } catch (e) {
          console.warn('Backend upload failed, fallback to base64 audio:', e);
        }

        // Fallback to Base64 data URL
        const reader = new FileReader();
        reader.readAsDataURL(audioBlob);
        reader.onloadend = () => {
          handleSendMessage(reader.result, 'AUDIO');
        };
      };

      mediaRecorder.start();
      setIsRecordingVoice(true);
    } catch (err) {
      console.warn('Microphone permission denied or unavailable:', err);
      // Fallback to simulated audio message
      setIsRecordingVoice(true);
    }
  };

  const stopRecordingAudio = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      mediaRecorderRef.current.stop();
      setIsRecordingVoice(false);
    } else {
      setIsRecordingVoice(false);
      // Fallback simulated voice note
      const simulatedDuration = recordingSeconds > 0 ? recordingSeconds : 4;
      handleSendMessage(`🎤 Simulated Voice Note (${simulatedDuration}s)`, 'AUDIO');
    }
  };

  const cancelRecordingAudio = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === 'recording') {
      mediaRecorderRef.current.stop();
      audioChunksRef.current = [];
    }
    setIsRecordingVoice(false);
  };

  // Play / Pause Audio handler
  const togglePlayAudio = (msgId, audioSrc) => {
    // If clicking same active audio, pause it
    if (currentPlayingId === msgId) {
      const activeEl = audioElementsRef.current[msgId];
      if (activeEl) activeEl.pause();
      setCurrentPlayingId(null);
      return;
    }

    // Stop previously playing audio
    if (currentPlayingId && audioElementsRef.current[currentPlayingId]) {
      audioElementsRef.current[currentPlayingId].pause();
    }

    // Start playing requested audio
    let el = audioElementsRef.current[msgId];
    if (!el) {
      el = new Audio(audioSrc);
      audioElementsRef.current[msgId] = el;
      el.onended = () => setCurrentPlayingId(null);
    }

    el.currentTime = 0;
    el.play().catch(e => console.warn('Audio playback error:', e));
    setCurrentPlayingId(msgId);
  };

  // -------------------------------------------------------------
  // PHOTO / VIDEO FILE SELECTION & UPLOAD
  // -------------------------------------------------------------
  const handleFileSelected = (e, mediaType) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setShowAttachMenu(false);
    const objectUrl = URL.createObjectURL(file);
    setPendingMedia({
      type: mediaType,
      url: objectUrl,
      file,
      caption: ''
    });

    // Reset input
    e.target.value = '';
  };

  // Send pending photo or video
  const handleConfirmSendMedia = async () => {
    if (!pendingMedia) return;
    setIsUploadingMedia(true);

    try {
      let finalMediaUrl = pendingMedia.url;

      // Upload file to server
      try {
        const formData = new FormData();
        formData.append('file', pendingMedia.file);
        formData.append('fileType', pendingMedia.type);
        const res = await api.post('/chat/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        if (res.data?.url) {
          finalMediaUrl = res.data.url;
        }
      } catch (err) {
        console.warn('Backend file upload fallback to base64:', err);
        // Fallback to base64
        finalMediaUrl = await new Promise((resolve) => {
          const reader = new FileReader();
          reader.onloadend = () => resolve(reader.result);
          reader.readAsDataURL(pendingMedia.file);
        });
      }

      // Payload format: payload can be plain URL or JSON containing { url, caption }
      const payload = JSON.stringify({
        mediaUrl: finalMediaUrl,
        caption: pendingMedia.caption || ''
      });

      await handleSendMessage(payload, pendingMedia.type);
    } catch (err) {
      console.error('Failed to send media:', err);
    } finally {
      setIsUploadingMedia(false);
      setPendingMedia(null);
    }
  };

  // Parse media content safely
  const parseMediaContent = (content) => {
    if (!content) return { mediaUrl: '', caption: '' };
    if (content.startsWith('{') && content.endsWith('}')) {
      try {
        return JSON.parse(content);
      } catch (e) {
        return { mediaUrl: content, caption: '' };
      }
    }
    return { mediaUrl: content, caption: '' };
  };

  // -------------------------------------------------------------
  // SIMULATED CALLS
  // -------------------------------------------------------------
  const startCall = (type) => {
    setActiveCall({ type, status: 'ringing', duration: 0 });
    setTimeout(() => {
      setActiveCall(prev => prev ? { ...prev, status: 'connected' } : null);
    }, 2500);
  };

  const endCall = () => {
    setActiveCall(null);
  };

  // Quick prompt chips
  const userQuickInquiries = [
    { label: '🌾 Inquire Harvest Quintals', text: 'Hello! Are quintals available for immediate procurement with certified APMC quality?' },
    { label: '🚚 Confirm Logistics', text: 'Hi! Could you confirm the expected delivery schedule and transportation arrangements for my order?' },
    { label: '🧊 Storage Space Booking', text: 'Hello Manager, I would like to inquire about temperature-controlled warehouse space for my upcoming harvest batch.' },
    { label: '🆘 Mandi+ Helpdesk Support', text: 'Hello Admin Support! I need assistance with an escrow payment or trade dispute verification on Mandi+.' }
  ];

  const adminQuickReplies = [
    { label: '✅ Escrow Verified', text: 'Official Mandi+ Notice: Your escrow payment has been validated and secured. You may proceed with harvest dispatch.' },
    { label: '📋 Inspection Approved', text: 'APMC Quality Inspection Report: Crop quality grade A confirmed by Mandi+ field inspectors.' },
    { label: '🚚 Logistics Cleared', text: 'Logistics Update: Cold-chain transportation vehicle allocated and en route to the designated facility.' },
    { label: '🛡️ Dispute Resolved', text: 'Platform Resolution Desk: Case investigated and funds released according to Mandi+ Fair Trade Policy.' }
  ];

  const emojis = ['🌾', '🚜', '🥔', '🍅', '🧅', '🍎', '🍇', '📦', '🤝', '✅', '👍', '🙏', '👋', '😊', '💰', '🚛', '⭐', '❤️', '📍', '📋'];

  // Format timestamp like WhatsApp
  const formatTime = (isoString) => {
    if (!isoString) return '';
    const d = new Date(isoString);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const formatDateHeader = (isoString) => {
    if (!isoString) return 'TODAY';
    const d = new Date(isoString);
    const now = new Date();
    if (d.toDateString() === now.toDateString()) return 'TODAY';
    return d.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
  };

  // Filter conversations & contacts
  const filteredConversations = conversations.filter(c => 
    (c.partnerName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.lastMessage || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.partnerRole || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredContacts = contacts.filter(c => 
    (c.name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.email || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.role || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (c.subtitle || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-[#d1d7db] flex flex-col font-sans select-none relative overflow-hidden">
      {/* Hidden file inputs for Photo, Video, Audio */}
      <input 
        type="file" 
        ref={imageInputRef} 
        accept="image/*" 
        className="hidden" 
        onChange={(e) => handleFileSelected(e, 'IMAGE')} 
      />
      <input 
        type="file" 
        ref={videoInputRef} 
        accept="video/*" 
        className="hidden" 
        onChange={(e) => handleFileSelected(e, 'VIDEO')} 
      />
      <input 
        type="file" 
        ref={audioInputRef} 
        accept="audio/*" 
        className="hidden" 
        onChange={(e) => handleFileSelected(e, 'AUDIO')} 
      />

      {/* WhatsApp Signature Top Green Header Bar */}
      <div className="h-32 bg-[#00a884] w-full absolute top-0 left-0 z-0"></div>

      {/* Main WhatsApp Window Container */}
      <div className="relative z-10 w-full max-w-[1650px] mx-auto h-[100vh] sm:h-[calc(100vh-32px)] sm:my-4 sm:rounded-2xl shadow-2xl overflow-hidden flex bg-white border border-[#d1d7db]">

        {/* ============================================================== */}
        {/* LEFT PANE: Conversations & Contacts Sidebar                     */}
        {/* ============================================================== */}
        <div className={`w-full md:w-[410px] lg:w-[450px] shrink-0 border-r border-[#e9edef] flex flex-col bg-white h-full ${selectedPartner && 'hidden md:flex'}`}>
          
          {/* Top Left WhatsApp Header */}
          <div className="h-16 bg-[#f0f2f5] px-4 flex items-center justify-between border-b border-[#e9edef] shrink-0">
            {/* User Profile */}
            <Link to="/profile" className="flex items-center space-x-3 group" title="My Profile">
              <div className="relative">
                {user?.profilePhoto ? (
                  <img src={user.profilePhoto} alt="User" className="h-10 w-10 rounded-full object-cover border border-slate-300" />
                ) : (
                  <div className="h-10 w-10 rounded-full bg-[#00a884] flex items-center justify-center text-white font-bold text-base shadow-sm">
                    {(user?.name || user?.email || user?.phone || 'U').charAt(0).toUpperCase()}
                  </div>
                )}
                <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full bg-[#25d366] ring-2 ring-white"></span>
              </div>
              <div className="text-left hidden sm:block">
                <span className="text-xs font-bold text-[#111b21] block truncate max-w-[130px] leading-tight">
                  {user?.name || user?.email?.split('@')[0]}
                </span>
                <span className="text-[10px] font-bold text-[#00a884] uppercase tracking-wider block">
                  {user?.role?.replace(/_/g, ' ')}
                </span>
              </div>
            </Link>

            {/* Header Action Icons */}
            <div className="flex items-center space-x-2 text-[#54656f]">
              {user?.role === 'ADMIN' && (
                <button 
                  onClick={() => setActiveTab(activeTab === 'chats' ? 'contacts' : 'chats')}
                  className={`p-2 rounded-full hover:bg-[#e9edef] transition-colors relative ${activeTab === 'contacts' ? 'text-[#00a884] bg-emerald-50' : ''}`}
                  title={activeTab === 'chats' ? 'All User Directory' : 'Back to Active Chats'}
                >
                  <MessageSquare className="h-5 w-5" />
                </button>
              )}
              
              <Link 
                to={user?.role === 'FARMER' ? '/farmer' : user?.role === 'BUYER' ? '/buyer' : user?.role === 'COLD_STORAGE_MANAGER' ? '/storage' : '/admin'}
                className="p-2 rounded-full hover:bg-[#e9edef] transition-colors" 
                title="Go to Mandi+ Portal"
              >
                <Store className="h-5 w-5" />
              </Link>
            </div>
          </div>

          {/* Quick Platform Support Banner for Non-Admins */}
          {user?.role !== 'ADMIN' ? (
            <div 
              onClick={() => {
                const adminContact = contacts.find(c => c.role === 'ADMIN') || contacts[0];
                if (adminContact) selectPartnerFromContact(adminContact);
                else if (conversations.length > 0) selectPartnerFromConversation(conversations[0]);
              }}
              className="bg-gradient-to-r from-emerald-600 to-[#00a884] text-white px-4 py-3 flex items-center justify-between cursor-pointer hover:opacity-95 transition-all shrink-0 shadow-inner"
            >
              <div className="flex items-center space-x-2.5 text-left">
                <div className="h-8 w-8 rounded-full bg-white/20 flex items-center justify-center text-white ring-1 ring-white/50">
                  <ShieldCheck className="h-4 w-4" />
                </div>
                <div>
                  <h5 className="text-xs font-bold leading-tight flex items-center space-x-1">
                    <span>Mandi+ Admin Helpdesk</span>
                    <span className="bg-[#25d366] text-white text-[8px] font-black px-1.5 rounded-full uppercase">Official</span>
                  </h5>
                  <p className="text-[10px] text-emerald-100 opacity-90 leading-tight">Private direct communication with Mandi+ Admin</p>
                </div>
              </div>
              <span className="text-[11px] font-bold bg-white/20 px-2 py-1 rounded-lg">Chat Now →</span>
            </div>
          ) : (
            <div className="bg-slate-900 text-slate-200 px-4 py-2 flex items-center justify-between text-[11px] shrink-0 border-b border-slate-800">
              <span className="flex items-center space-x-1.5 font-bold">
                <Shield className="h-3.5 w-3.5 text-emerald-400" />
                <span>Admin Moderation Desk • All User Threads</span>
              </span>
              <span className="bg-emerald-500/20 text-emerald-300 font-bold px-2 py-0.5 rounded-full text-[10px]">
                {conversations.length} Active Chats
              </span>
            </div>
          )}

          {/* Search Bar & WhatsApp Web Tabs */}
          <div className="p-2.5 bg-white border-b border-[#e9edef] space-y-2 shrink-0">
            <div className="relative flex items-center bg-[#f0f2f5] rounded-lg px-3 py-1.5 focus-within:bg-white focus-within:ring-1 focus-within:ring-[#00a884] transition-all">
              <Search className="h-4 w-4 text-[#54656f] mr-2 shrink-0" />
              <input 
                type="text" 
                placeholder={user?.role === 'ADMIN' ? (activeTab === 'chats' ? "Search all active user chats..." : "Search user directory (Farmers, Buyers, Cold Storage)...") : "Search platform conversation..."}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-transparent text-sm text-[#111b21] placeholder-[#54656f] focus:outline-none"
              />
              {searchQuery && (
                <button onClick={() => setSearchQuery('')} className="text-[#54656f] hover:text-[#111b21]">
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>

            {/* Chats / Contacts Navigation: Admin sees switcher; Non-admins see privacy assurance badge */}
            {user?.role === 'ADMIN' ? (
              <div className="flex bg-[#f0f2f5] p-1 rounded-xl">
                <button
                  onClick={() => setActiveTab('chats')}
                  className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all flex items-center justify-center space-x-1.5 ${
                    activeTab === 'chats' 
                      ? 'bg-white text-[#111b21] shadow-sm' 
                      : 'text-[#54656f] hover:text-[#111b21]'
                  }`}
                >
                  <span>Active User Chats</span>
                  {conversations.reduce((sum, c) => sum + (c.unreadCount || 0), 0) > 0 && (
                    <span className="bg-[#25d366] text-white text-[10px] font-black px-1.5 py-0.2 rounded-full">
                      {conversations.reduce((sum, c) => sum + (c.unreadCount || 0), 0)}
                    </span>
                  )}
                </button>
                <button
                  onClick={() => setActiveTab('contacts')}
                  className={`flex-1 py-1.5 text-xs font-bold rounded-lg transition-all flex items-center justify-center space-x-1.5 ${
                    activeTab === 'contacts' 
                      ? 'bg-white text-[#111b21] shadow-sm' 
                      : 'text-[#54656f] hover:text-[#111b21]'
                  }`}
                >
                  <span>All Users ({contacts.length})</span>
                </button>
              </div>
            ) : (
              <div className="bg-[#f0f2f5] px-3.5 py-2 rounded-xl flex items-center justify-between text-xs text-[#54656f]">
                <span className="font-bold text-[#111b21] flex items-center space-x-1.5">
                  <ShieldCheck className="h-4 w-4 text-[#00a884]" />
                  <span>Platform Helpdesk Channel</span>
                </span>
                <span className="text-[10px] bg-emerald-100 text-emerald-800 font-bold px-2 py-0.5 rounded-full border border-emerald-200">
                  Private & Direct
                </span>
              </div>
            )}
          </div>

          {/* LIST VIEW: Conversations or Contacts */}
          <div className="flex-1 overflow-y-auto divide-y divide-[#f0f2f5] scrollbar-thin">
            {activeTab === 'chats' ? (
              // TAB 1: CONVERSATIONS
              loadingConversations ? (
                <div className="py-16 text-center text-[#54656f] space-y-2">
                  <RefreshCw className="h-6 w-6 animate-spin mx-auto text-[#00a884]" />
                  <p className="text-xs">Loading messages...</p>
                </div>
              ) : filteredConversations.length === 0 ? (
                <div className="py-16 px-6 text-center text-[#54656f] space-y-3">
                  <div className="h-14 w-14 rounded-full bg-[#f0f2f5] flex items-center justify-center mx-auto text-[#00a884]">
                    <MessageSquare className="h-7 w-7" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-[#111b21]">No chats yet</h4>
                    <p className="text-xs text-[#54656f] mt-1 leading-relaxed">
                      {user?.role === 'ADMIN'
                        ? 'No active user threads recorded yet.'
                        : 'Connect directly with Mandi+ Platform Helpdesk for assistance with produce approval, prices, and orders.'}
                    </p>
                  </div>
                  <button
                    onClick={() => {
                      if (contacts.length > 0) {
                        selectPartnerFromContact(contacts[0]);
                      } else {
                        setActiveTab('contacts');
                      }
                    }}
                    className="bg-[#00a884] hover:bg-[#008069] text-white text-xs font-bold px-4 py-2 rounded-xl transition-all shadow-md shadow-emerald-700/20"
                  >
                    {user?.role === 'ADMIN' ? 'Search Users' : 'Open Support Chat'}
                  </button>
                </div>
              ) : (
                filteredConversations.map((conv) => {
                  const isSelected = selectedPartner?.id === conv.partnerId;
                  return (
                    <div
                      key={conv.partnerId}
                      onClick={() => selectPartnerFromConversation(conv)}
                      className={`px-4 py-3 flex items-center space-x-3 cursor-pointer transition-colors text-left relative ${
                        isSelected ? 'bg-[#f0f2f5]' : 'hover:bg-[#f5f6f6]'
                      }`}
                    >
                      {/* Avatar */}
                      <div className="relative shrink-0">
                        {conv.partnerProfilePhoto || conv.partnerPhoto ? (
                          <img src={conv.partnerProfilePhoto || conv.partnerPhoto} alt="Avatar" className="h-12 w-12 rounded-full object-cover" />
                        ) : (
                          <div className={`h-12 w-12 rounded-full flex items-center justify-center font-bold text-white text-base shadow-sm ${
                            conv.partnerRole === 'ADMIN' ? 'bg-rose-500' :
                            conv.partnerRole === 'FARMER' ? 'bg-emerald-600' :
                            conv.partnerRole === 'COLD_STORAGE_MANAGER' ? 'bg-amber-600' : 'bg-sky-600'
                          }`}>
                            {(conv.partnerName || 'User').charAt(0).toUpperCase()}
                          </div>
                        )}
                        <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full bg-[#25d366] ring-2 ring-white"></span>
                      </div>

                      {/* Info & Last snippet */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <h4 className="text-sm font-semibold text-[#111b21] truncate flex items-center space-x-1.5">
                            <span>{conv.partnerName}</span>
                            {conv.isPinned && <span className="text-[10px] text-[#00a884]">📌</span>}
                          </h4>
                          <span className={`text-[11px] shrink-0 font-medium ${conv.unreadCount > 0 ? 'text-[#25d366] font-bold' : 'text-[#667781]'}`}>
                            {formatTime(conv.lastMessageAt)}
                          </span>
                        </div>

                        <div className="flex items-center justify-between mt-1">
                          <p className="text-xs text-[#667781] truncate flex items-center space-x-1">
                            <span className="truncate">{conv.lastMessage || 'Sent attachment'}</span>
                          </p>
                          <div className="flex items-center space-x-1.5 shrink-0 ml-2">
                            {getRoleBadge(conv.partnerRole)}
                            {conv.unreadCount > 0 && (
                              <span className="bg-[#25d366] text-white text-[11px] font-black h-5 min-w-5 px-1.5 rounded-full flex items-center justify-center">
                                {conv.unreadCount}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })
              )
            ) : (
              // TAB 2: CONTACTS LIST
              filteredContacts.length === 0 ? (
                <div className="py-16 text-center text-[#54656f]">
                  <p className="text-xs">No contacts match "{searchQuery}"</p>
                </div>
              ) : (
                <div className="p-2 space-y-1">
                  {filteredContacts.map((contact) => (
                    <div
                      key={contact.id}
                      onClick={() => selectPartnerFromContact(contact)}
                      className="p-3 hover:bg-[#f0f2f5] rounded-xl flex items-center space-x-3 cursor-pointer transition-colors text-left"
                    >
                      <div className="relative shrink-0">
                        {contact.profilePhoto ? (
                          <img src={contact.profilePhoto} alt="Avatar" className="h-11 w-11 rounded-full object-cover" />
                        ) : (
                          <div className={`h-11 w-11 rounded-full flex items-center justify-center font-bold text-white text-base ${
                            contact.role === 'ADMIN' ? 'bg-rose-500' :
                            contact.role === 'FARMER' ? 'bg-emerald-600' :
                            contact.role === 'COLD_STORAGE_MANAGER' ? 'bg-amber-600' : 'bg-sky-600'
                          }`}>
                            {(contact.name || 'User').charAt(0).toUpperCase()}
                          </div>
                        )}
                        <span className="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full bg-[#25d366] ring-2 ring-white"></span>
                      </div>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <h4 className="text-sm font-semibold text-[#111b21] truncate">{contact.name}</h4>
                          {getRoleBadge(contact.role)}
                        </div>
                        <p className="text-xs text-[#667781] truncate mt-0.5">{contact.subtitle || contact.email}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )
            )}
          </div>
        </div>

        {/* ============================================================== */}
        {/* RIGHT PANE: Active Conversation or WhatsApp Welcome Screen     */}
        {/* ============================================================== */}
        <div className={`flex-1 flex flex-col h-full bg-[#efeae2] relative overflow-hidden ${!selectedPartner && 'hidden md:flex'}`}>
          
          {selectedPartner ? (
            <>
              {/* TOP HEADER: Active Partner details & Calling Icons */}
              <div className="h-16 bg-[#f0f2f5] px-4 flex items-center justify-between border-b border-[#e9edef] shrink-0 z-20">
                <div className="flex items-center space-x-3 text-left">
                  {/* Mobile Back Button */}
                  <button 
                    onClick={() => setSelectedPartner(null)}
                    className="md:hidden p-1.5 text-[#54656f] hover:text-[#111b21] rounded-full hover:bg-slate-200 transition-colors"
                  >
                    <ArrowLeft className="h-5 w-5" />
                  </button>

                  {/* Partner Avatar */}
                  <div 
                    onClick={() => setShowProfileDrawer(!showProfileDrawer)}
                    className="relative cursor-pointer hover:opacity-90 transition-opacity"
                  >
                    {selectedPartner.profilePhoto ? (
                      <img src={selectedPartner.profilePhoto} alt="Avatar" className="h-10 w-10 rounded-full object-cover" />
                    ) : (
                      <div className={`h-10 w-10 rounded-full flex items-center justify-center font-bold text-white text-base ${
                        selectedPartner.role === 'ADMIN' ? 'bg-rose-500' :
                        selectedPartner.role === 'FARMER' ? 'bg-emerald-600' :
                        selectedPartner.role === 'COLD_STORAGE_MANAGER' ? 'bg-amber-600' : 'bg-sky-600'
                      }`}>
                        {(selectedPartner.name || 'User').charAt(0).toUpperCase()}
                      </div>
                    )}
                    <span className="absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full bg-[#25d366] ring-2 ring-white"></span>
                  </div>

                  {/* Partner Name & Live Status */}
                  <div 
                    onClick={() => setShowProfileDrawer(!showProfileDrawer)}
                    className="cursor-pointer"
                  >
                    <h3 className="text-sm font-bold text-[#111b21] leading-tight flex items-center space-x-2">
                      <span className="truncate max-w-[200px] sm:max-w-[320px]">{selectedPartner.name}</span>
                      {getRoleBadge(selectedPartner.role)}
                    </h3>
                    <p className="text-[11px] text-[#00a884] font-medium leading-none mt-1">
                      online
                    </p>
                  </div>
                </div>

                {/* Right Call & Search Actions */}
                <div className="flex items-center space-x-2 sm:space-x-3 text-[#54656f]">
                  <button 
                    onClick={() => startCall('video')}
                    className="p-2 rounded-full hover:bg-[#e9edef] transition-colors" 
                    title="WhatsApp Video Call"
                  >
                    <Video className="h-5 w-5" />
                  </button>
                  <button 
                    onClick={() => startCall('voice')}
                    className="p-2 rounded-full hover:bg-[#e9edef] transition-colors" 
                    title="WhatsApp Voice Call"
                  >
                    <Phone className="h-5 w-5" />
                  </button>
                  <button 
                    onClick={() => setShowProfileDrawer(!showProfileDrawer)}
                    className="p-2 rounded-full hover:bg-[#e9edef] transition-colors" 
                    title="Contact Info"
                  >
                    <Info className="h-5 w-5" />
                  </button>
                </div>
              </div>

              {/* WHATSAPP CHAT CANVAS: Authentic Doodle Background & Bubbles */}
              <div 
                className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-3 relative z-10 scrollbar-thin"
                style={{
                  backgroundColor: '#efeae2',
                  backgroundImage: `radial-gradient(rgba(17, 27, 33, 0.04) 1px, transparent 0)`,
                  backgroundSize: '24px 24px'
                }}
              >
                {/* Security & Privacy Platform Banner */}
                <div className="flex justify-center my-2">
                  <div className="bg-[#ffeecd] border border-[#ffdf9e] text-[#54656f] text-[11px] px-4 py-2 rounded-xl shadow-sm flex items-center space-x-2 max-w-lg text-center leading-relaxed">
                    <Shield className="h-4 w-4 text-[#00a884] shrink-0" />
                    <span>
                      {user?.role === 'ADMIN' 
                        ? 'Admin Global Oversight: You are managing this user thread. Only Mandi+ Admins can read or communicate across user accounts.' 
                        : 'Official Mandi+ Platform Support: Messages and media in this channel are private and shared strictly between you and Mandi+ Administration. Regular members only communicate directly with Platform Helpdesk.'}
                    </span>
                  </div>
                </div>

                {/* Date Divider Pill */}
                <div className="flex justify-center my-3">
                  <span className="bg-white/90 backdrop-blur-sm text-[#54656f] text-[11px] font-bold px-3 py-1 rounded-md shadow-sm uppercase tracking-wider">
                    {formatDateHeader(messages[0]?.createdAt)}
                  </span>
                </div>

                {/* Messages List */}
                {loadingHistory ? (
                  <div className="py-12 text-center text-[#54656f] space-y-2">
                    <RefreshCw className="h-6 w-6 animate-spin mx-auto text-[#00a884]" />
                    <p className="text-xs">Loading messages...</p>
                  </div>
                ) : messages.length === 0 ? (
                  <div className="py-12 text-center text-[#54656f] space-y-3 max-w-sm mx-auto">
                    <div className="h-12 w-12 rounded-full bg-white/80 flex items-center justify-center mx-auto text-[#00a884] shadow-sm">
                      <MessageSquare className="h-6 w-6" />
                    </div>
                    <p className="text-xs font-semibold text-[#111b21]">No messages yet with {selectedPartner.name}.</p>
                    <p className="text-[11px] text-[#667781]">Send a message, photo, audio voice note, or video below!</p>
                  </div>
                ) : (
                  messages.map((msg, index) => {
                    const isMe = msg.senderId === user?.id;
                    const mediaData = parseMediaContent(msg.content);

                    return (
                      <div 
                        key={msg.id || index} 
                        className={`flex ${isMe ? 'justify-end' : 'justify-start'} animate-fade-in`}
                      >
                        <div 
                          className={`relative max-w-[85%] sm:max-w-[70%] p-2 rounded-2xl shadow-sm text-sm leading-relaxed ${
                            isMe 
                              ? 'bg-[#d9fdd3] text-[#111b21] rounded-tr-none' 
                              : 'bg-white text-[#111b21] rounded-tl-none'
                          }`}
                        >
                          {/* 1. PHOTO MESSAGE */}
                          {msg.messageType === 'IMAGE' ? (
                            <div className="space-y-1.5">
                              <div 
                                className="relative rounded-xl overflow-hidden cursor-pointer group bg-slate-100 max-h-72"
                                onClick={() => setLightboxImage(mediaData.mediaUrl)}
                              >
                                <img 
                                  src={mediaData.mediaUrl} 
                                  alt="Photo Attachment" 
                                  className="w-full max-h-72 object-cover rounded-xl group-hover:scale-105 transition-transform duration-300"
                                />
                                <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
                                  <ImageIcon className="h-6 w-6 drop-shadow" />
                                </div>
                              </div>
                              {mediaData.caption && (
                                <p className="px-1 text-xs text-[#111b21] whitespace-pre-wrap">{mediaData.caption}</p>
                              )}
                            </div>
                          ) : msg.messageType === 'VIDEO' ? (
                            /* 2. VIDEO MESSAGE */
                            <div className="space-y-1.5">
                              <div className="rounded-xl overflow-hidden bg-black max-w-sm">
                                <video 
                                  src={mediaData.mediaUrl} 
                                  controls 
                                  playsInline 
                                  className="w-full max-h-64 object-contain rounded-xl"
                                />
                              </div>
                              {mediaData.caption && (
                                <p className="px-1 text-xs text-[#111b21] whitespace-pre-wrap">{mediaData.caption}</p>
                              )}
                            </div>
                          ) : msg.messageType === 'AUDIO' ? (
                            /* 3. AUDIO / VOICE NOTE MESSAGE */
                            <div className="flex items-center space-x-3 py-1 px-1 min-w-[200px] sm:min-w-[240px]">
                              {/* Play / Pause button */}
                              <button 
                                onClick={() => togglePlayAudio(msg.id, mediaData.mediaUrl)}
                                className={`h-11 w-11 rounded-full flex items-center justify-center text-white transition-all shadow-md shrink-0 ${
                                  currentPlayingId === msg.id ? 'bg-[#00a884] scale-105' : 'bg-[#008069] hover:bg-[#00a884]'
                                }`}
                              >
                                {currentPlayingId === msg.id ? <Pause className="h-5 w-5" /> : <Play className="h-5 w-5 ml-0.5" />}
                              </button>

                              {/* WhatsApp Waveform visualization */}
                              <div className="flex-1">
                                <div className="flex items-center space-x-0.5 h-6">
                                  {[40, 70, 30, 90, 60, 100, 50, 80, 45, 95, 35, 75, 55, 85, 65, 90, 40, 80].map((h, i) => (
                                    <span 
                                      key={i} 
                                      className={`w-1 rounded-full transition-all duration-200 ${
                                        currentPlayingId === msg.id 
                                          ? 'bg-[#00a884] animate-pulse' 
                                          : 'bg-slate-400'
                                      }`}
                                      style={{ height: `${h}%` }}
                                    ></span>
                                  ))}
                                </div>
                                <div className="flex justify-between items-center text-[10px] text-[#667781] mt-1 font-mono">
                                  <span>{currentPlayingId === msg.id ? 'Playing...' : 'Voice Note'}</span>
                                  <span>0:14</span>
                                </div>
                              </div>

                              {/* WhatsApp Voice avatar with mic badge */}
                              <div className="relative shrink-0">
                                <div className="h-9 w-9 rounded-full bg-slate-300 flex items-center justify-center font-bold text-slate-700 text-xs">
                                  {isMe ? (user?.name || user?.email || 'U').charAt(0).toUpperCase() : (selectedPartner?.name || 'U').charAt(0).toUpperCase()}
                                </div>
                                <span className="absolute -bottom-1 -right-1 h-4 w-4 rounded-full bg-[#25d366] text-white flex items-center justify-center ring-1 ring-white">
                                  <Mic className="h-2.5 w-2.5" />
                                </span>
                              </div>
                            </div>
                          ) : (
                            /* 4. REGULAR TEXT MESSAGE */
                            <p className="break-words whitespace-pre-wrap px-1.5 pt-0.5">{msg.content}</p>
                          )}

                          {/* Message Footer: Timestamp + Blue Double Checks */}
                          <div className="flex items-center justify-end space-x-1 mt-1 text-[11px] text-[#667781] select-none pr-1">
                            <span>{formatTime(msg.createdAt)}</span>
                            {isMe && (
                              <span className={msg.isRead ? 'text-[#53bdeb]' : 'text-[#8696a0]'}>
                                <CheckCheck className="h-3.5 w-3.5" />
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* QUICK INQUIRY OR ADMIN TEMPLATES CHIPS BAR */}
              <div className="bg-[#f0f2f5] px-3 py-1.5 border-t border-[#e9edef] overflow-x-auto flex items-center space-x-2 scrollbar-none z-10">
                <span className="text-[10px] font-bold text-[#54656f] uppercase tracking-wider shrink-0 flex items-center">
                  <Sparkles className="h-3 w-3 mr-1 text-[#00a884]" /> 
                  {user?.role === 'ADMIN' ? 'Admin Action Templates:' : 'Quick Inquiries:'}
                </span>
                {(user?.role === 'ADMIN' ? adminQuickReplies : userQuickInquiries).map((q, i) => (
                  <button
                    key={i}
                    onClick={() => handleSendMessage(q.text)}
                    className="shrink-0 bg-white hover:bg-emerald-50 hover:text-[#00a884] text-[#111b21] border border-slate-200 text-xs font-semibold px-3 py-1 rounded-full transition-all shadow-sm"
                  >
                    {q.label}
                  </button>
                ))}
              </div>

              {/* BOTTOM INPUT BAR: Emojis, Attachment, Input, Voice/Send Button */}
              <div className="bg-[#f0f2f5] px-3 py-2 border-t border-[#e9edef] flex items-center space-x-2 shrink-0 relative z-20">
                
                {/* Emoji Drawer Toggle */}
                <div className="relative">
                  <button 
                    onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                    className={`p-2 rounded-full hover:bg-slate-200 transition-colors ${showEmojiPicker ? 'text-[#00a884]' : 'text-[#54656f]'}`}
                    title="Emojis"
                  >
                    <Smile className="h-6 w-6" />
                  </button>

                  {/* Popover Emoji Grid */}
                  {showEmojiPicker && (
                    <div className="absolute bottom-12 left-0 w-72 bg-white rounded-2xl shadow-2xl border border-slate-200 p-3 z-50 animate-step-enter">
                      <div className="flex items-center justify-between pb-2 border-b border-slate-100 mb-2">
                        <span className="text-xs font-bold text-[#111b21]">Quick Emojis</span>
                        <button onClick={() => setShowEmojiPicker(false)} className="text-[#54656f] hover:text-[#111b21]">
                          <X className="h-4 w-4" />
                        </button>
                      </div>
                      <div className="grid grid-cols-5 gap-2 text-2xl">
                        {emojis.map((emoji, idx) => (
                          <button
                            key={idx}
                            onClick={() => {
                              setMessageText(prev => prev + emoji);
                              inputRef.current?.focus();
                            }}
                            className="p-1 hover:bg-[#f0f2f5] rounded-lg transition-transform hover:scale-125"
                          >
                            {emoji}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Attachment Menu Toggle (Photos, Videos, Audio, Quotes) */}
                <div className="relative">
                  <button 
                    onClick={() => setShowAttachMenu(!showAttachMenu)}
                    className={`p-2 rounded-full hover:bg-slate-200 transition-colors ${showAttachMenu ? 'text-[#00a884]' : 'text-[#54656f]'}`}
                    title="Attach Photo, Video, or Audio"
                  >
                    <Paperclip className="h-6 w-6" />
                  </button>

                  {/* Attachment Popover */}
                  {showAttachMenu && (
                    <div className="absolute bottom-12 left-0 w-64 bg-white rounded-2xl shadow-2xl border border-slate-200 py-2 z-50 animate-step-enter divide-y divide-slate-100">
                      <div className="py-1">
                        {/* 1. PHOTO PICKER */}
                        <button 
                          onClick={() => imageInputRef.current?.click()}
                          className="w-full px-4 py-2 hover:bg-[#f0f2f5] flex items-center space-x-3 text-xs font-semibold text-[#111b21] text-left transition-colors"
                        >
                          <span className="h-8 w-8 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center">
                            <ImageIcon className="h-4 w-4" />
                          </span>
                          <div>
                            <span className="block font-bold">Photos & Images</span>
                            <span className="block text-[10px] text-[#667781]">Share produce or bill photo</span>
                          </div>
                        </button>

                        {/* 2. VIDEO PICKER */}
                        <button 
                          onClick={() => videoInputRef.current?.click()}
                          className="w-full px-4 py-2 hover:bg-[#f0f2f5] flex items-center space-x-3 text-xs font-semibold text-[#111b21] text-left transition-colors"
                        >
                          <span className="h-8 w-8 rounded-full bg-rose-100 text-rose-700 flex items-center justify-center">
                            <Film className="h-4 w-4" />
                          </span>
                          <div>
                            <span className="block font-bold">Videos</span>
                            <span className="block text-[10px] text-[#667781]">Share crop video / field demo</span>
                          </div>
                        </button>

                        {/* 3. AUDIO FILE PICKER */}
                        <button 
                          onClick={() => audioInputRef.current?.click()}
                          className="w-full px-4 py-2 hover:bg-[#f0f2f5] flex items-center space-x-3 text-xs font-semibold text-[#111b21] text-left transition-colors"
                        >
                          <span className="h-8 w-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center">
                            <Music className="h-4 w-4" />
                          </span>
                          <div>
                            <span className="block font-bold">Audio File</span>
                            <span className="block text-[10px] text-[#667781]">Upload voice recording/audio</span>
                          </div>
                        </button>
                      </div>

                      <div className="py-1">
                        {/* 4. PRESET CROP QUOTE */}
                        <button 
                          onClick={() => {
                            setShowAttachMenu(false);
                            handleSendMessage("🌾 [Certified Crop Harvest Listing] 100 Quintals Grade A Nashik Red Onions - Rate: ₹2,450/qtl. Verified by APMC.");
                          }}
                          className="w-full px-4 py-2 hover:bg-[#f0f2f5] flex items-center space-x-3 text-xs font-semibold text-[#111b21] text-left transition-colors"
                        >
                          <span className="h-8 w-8 rounded-full bg-amber-100 text-amber-700 flex items-center justify-center">🌾</span>
                          <div>
                            <span className="block font-bold">Crop Quote Card</span>
                            <span className="block text-[10px] text-[#667781]">Instant marketplace quote</span>
                          </div>
                        </button>

                        {/* 5. PRESET COLD STORAGE SLOT */}
                        <button 
                          onClick={() => {
                            setShowAttachMenu(false);
                            handleSendMessage("🧊 [Cold Storage Facility Card] Nashik Agropark Unit 4 - Temp: 3.8°C • Capacity: 250 MT. Bookings open.");
                          }}
                          className="w-full px-4 py-2 hover:bg-[#f0f2f5] flex items-center space-x-3 text-xs font-semibold text-[#111b21] text-left transition-colors"
                        >
                          <span className="h-8 w-8 rounded-full bg-sky-100 text-sky-700 flex items-center justify-center">🧊</span>
                          <div>
                            <span className="block font-bold">Cold Storage Card</span>
                            <span className="block text-[10px] text-[#667781]">Warehouse capacity specs</span>
                          </div>
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                {/* Text Input Capsule */}
                <div className="flex-1 bg-white rounded-xl px-4 py-2 flex items-center border border-transparent focus-within:border-[#00a884] shadow-sm">
                  {isRecordingVoice ? (
                    <div className="flex items-center justify-between w-full text-rose-600 font-mono text-sm">
                      <div className="flex items-center space-x-2">
                        <span className="h-3 w-3 rounded-full bg-rose-500 animate-ping"></span>
                        <span>Recording voice note... 0:{recordingSeconds < 10 ? `0${recordingSeconds}` : recordingSeconds}</span>
                      </div>
                      <button 
                        onClick={cancelRecordingAudio}
                        className="text-xs text-[#54656f] hover:text-[#111b21] font-sans underline"
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <input 
                      ref={inputRef}
                      type="text" 
                      placeholder="Type a message" 
                      value={messageText}
                      onChange={(e) => setMessageText(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' && !e.shiftKey) {
                          e.preventDefault();
                          handleSendMessage();
                        }
                      }}
                      className="w-full bg-transparent text-sm text-[#111b21] placeholder-[#54656f] focus:outline-none"
                    />
                  )}
                </div>

                {/* Mic / Send Button Toggle */}
                {messageText.trim().length > 0 ? (
                  <button 
                    onClick={() => handleSendMessage()}
                    className="h-10 w-10 rounded-full bg-[#00a884] hover:bg-[#008069] text-white flex items-center justify-center transition-transform hover:scale-105 shadow-md shadow-emerald-700/20 shrink-0"
                    title="Send Message"
                  >
                    <Send className="h-5 w-5 ml-0.5" />
                  </button>
                ) : isRecordingVoice ? (
                  <button 
                    onClick={stopRecordingAudio}
                    className="h-10 w-10 rounded-full bg-[#25d366] hover:bg-[#00a884] text-white flex items-center justify-center transition-transform hover:scale-105 shadow-md shrink-0"
                    title="Finish & Send Voice Note"
                  >
                    <Check className="h-5 w-5" />
                  </button>
                ) : (
                  <button 
                    onClick={startRecordingAudio}
                    className="p-2 text-[#54656f] hover:text-[#111b21] rounded-full hover:bg-slate-200 transition-colors shrink-0"
                    title="Hold or click to record Voice Note"
                  >
                    <Mic className="h-6 w-6" />
                  </button>
                )}
              </div>
            </>
          ) : (
            // EMPTY STATE: Authentic WhatsApp Web Laptop Illustration
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center bg-[#f0f2f5] border-b-8 border-[#25d366]">
              <div className="max-w-md space-y-6 animate-fade-in">
                <div className="h-28 w-28 rounded-full bg-white shadow-xl mx-auto flex items-center justify-center p-3 ring-8 ring-emerald-50">
                  <img src="/images/logo-shield.png" alt="Mandi+ Emblem" className="h-20 w-20 object-contain" />
                </div>
                <div>
                  <h2 className="text-2xl font-light text-[#41525d]">Mandi+ WhatsApp Web</h2>
                  <p className="text-sm text-[#667781] mt-2 leading-relaxed">
                    Direct real-time communication with Photo, Audio Voice Notes, and Video sharing between Farmers, Wholesalers, Cold Storages, and Mandi+ Admin Support.
                  </p>
                </div>

                <div className="bg-white p-4 rounded-2xl shadow-sm border border-slate-200/80 space-y-2 text-left">
                  <span className="text-[11px] font-bold text-[#00a884] uppercase tracking-wider block">Quick Actions</span>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => {
                        const adminContact = contacts.find(c => c.role === 'ADMIN');
                        if (adminContact) selectPartnerFromContact(adminContact);
                        else setActiveTab('contacts');
                      }}
                      className="p-2.5 rounded-xl border border-rose-100 hover:border-rose-300 bg-rose-50/50 hover:bg-rose-50 text-rose-800 text-xs font-bold transition-all text-left flex items-center space-x-2"
                    >
                      <span>💬</span>
                      <span>Admin Helpdesk</span>
                    </button>
                    <button
                      onClick={() => setActiveTab('contacts')}
                      className="p-2.5 rounded-xl border border-emerald-100 hover:border-emerald-300 bg-emerald-50/50 hover:bg-emerald-50 text-emerald-800 text-xs font-bold transition-all text-left flex items-center space-x-2"
                    >
                      <span>🌾</span>
                      <span>Browse Farmers</span>
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-center space-x-1.5 text-xs text-[#8696a0] pt-4">
                  <Shield className="h-3.5 w-3.5 text-[#00a884]" />
                  <span>End-to-end encrypted across Mandi+ network</span>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* MEDIA PREVIEW MODAL BEFORE SENDING (Photo / Video / Audio)      */}
          {/* ============================================================== */}
          {pendingMedia && (
            <div className="absolute inset-0 bg-slate-900/95 z-50 flex flex-col justify-between p-4 sm:p-6 animate-fade-in text-white">
              {/* Top bar */}
              <div className="flex items-center justify-between">
                <span className="text-sm font-bold text-slate-200">
                  Preview {pendingMedia.type === 'IMAGE' ? 'Photo' : pendingMedia.type === 'VIDEO' ? 'Video' : 'Audio File'}
                </span>
                <button 
                  onClick={() => setPendingMedia(null)}
                  className="p-2 text-slate-400 hover:text-white rounded-full hover:bg-white/10"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>

              {/* Preview Canvas */}
              <div className="flex-1 flex items-center justify-center p-4 overflow-hidden">
                {pendingMedia.type === 'IMAGE' ? (
                  <img 
                    src={pendingMedia.url} 
                    alt="Preview" 
                    className="max-h-[65vh] max-w-full rounded-2xl object-contain shadow-2xl ring-1 ring-white/10" 
                  />
                ) : pendingMedia.type === 'VIDEO' ? (
                  <video 
                    src={pendingMedia.url} 
                    controls 
                    className="max-h-[65vh] max-w-full rounded-2xl shadow-2xl ring-1 ring-white/10"
                  />
                ) : (
                  <div className="p-8 bg-slate-800 rounded-3xl border border-slate-700 text-center space-y-4">
                    <Music className="h-16 w-16 text-[#00a884] mx-auto animate-bounce" />
                    <div>
                      <h4 className="font-bold text-base">{pendingMedia.file?.name}</h4>
                      <p className="text-xs text-slate-400 mt-1">Ready to send audio attachment</p>
                    </div>
                    <audio src={pendingMedia.url} controls className="mx-auto" />
                  </div>
                )}
              </div>

              {/* Caption & Send Footer */}
              <div className="w-full max-w-xl mx-auto flex items-center space-x-3 bg-slate-800/90 backdrop-blur-md p-2 rounded-2xl border border-slate-700">
                <input 
                  type="text" 
                  placeholder="Add a caption..." 
                  value={pendingMedia.caption} 
                  onChange={(e) => setPendingMedia(prev => ({ ...prev, caption: e.target.value }))}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleConfirmSendMedia();
                  }}
                  className="flex-1 bg-transparent px-3 text-sm text-white placeholder-slate-400 focus:outline-none"
                />
                <button
                  disabled={isUploadingMedia}
                  onClick={handleConfirmSendMedia}
                  className="h-11 w-11 rounded-full bg-[#00a884] hover:bg-[#008069] text-white flex items-center justify-center shadow-lg transition-transform hover:scale-105 shrink-0 disabled:opacity-50"
                  title="Send Media"
                >
                  {isUploadingMedia ? <RefreshCw className="h-5 w-5 animate-spin" /> : <Send className="h-5 w-5 ml-0.5" />}
                </button>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* LIGHTBOX FOR FULLSCREEN IMAGE VIEW                             */}
          {/* ============================================================== */}
          {lightboxImage && (
            <div 
              className="absolute inset-0 bg-black/90 z-50 flex items-center justify-center p-4 animate-fade-in"
              onClick={() => setLightboxImage(null)}
            >
              <div className="relative max-w-4xl max-h-[90vh]" onClick={(e) => e.stopPropagation()}>
                <img src={lightboxImage} alt="Fullscreen View" className="max-w-full max-h-[85vh] rounded-2xl object-contain shadow-2xl" />
                <div className="absolute top-4 right-4 flex items-center space-x-2">
                  <a 
                    href={lightboxImage} 
                    download="whatsapp-media" 
                    target="_blank" 
                    rel="noreferrer"
                    className="p-2 rounded-full bg-black/50 text-white hover:bg-black transition-colors"
                    title="Download"
                  >
                    <Download className="h-5 w-5" />
                  </a>
                  <button 
                    onClick={() => setLightboxImage(null)}
                    className="p-2 rounded-full bg-black/50 text-white hover:bg-black transition-colors"
                  >
                    <X className="h-5 w-5" />
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* CONTACT INFO DRAWER (SLIDE-OVER)                               */}
          {/* ============================================================== */}
          {showProfileDrawer && selectedPartner && (
            <div className="absolute inset-y-0 right-0 w-80 bg-white border-l border-[#e9edef] shadow-2xl z-30 flex flex-col animate-slide-in">
              <div className="h-16 bg-[#f0f2f5] px-4 flex items-center space-x-3 border-b border-[#e9edef]">
                <button 
                  onClick={() => setShowProfileDrawer(false)}
                  className="p-1.5 text-[#54656f] hover:text-[#111b21] rounded-full hover:bg-slate-200"
                >
                  <X className="h-5 w-5" />
                </button>
                <span className="text-sm font-bold text-[#111b21]">Contact Info</span>
              </div>

              <div className="flex-1 overflow-y-auto p-6 space-y-6 text-center">
                <div className="space-y-3">
                  {selectedPartner.profilePhoto ? (
                    <img src={selectedPartner.profilePhoto} alt="Profile" className="h-28 w-28 rounded-full object-cover mx-auto ring-4 ring-slate-100" />
                  ) : (
                    <div className="h-28 w-28 rounded-full bg-[#00a884] text-white font-black text-3xl flex items-center justify-center mx-auto shadow-md">
                      {(selectedPartner.name || 'User').charAt(0).toUpperCase()}
                    </div>
                  )}
                  <div>
                    <h3 className="text-lg font-bold text-[#111b21]">{selectedPartner.name}</h3>
                    <p className="text-xs text-[#667781] mt-0.5">{selectedPartner.email}</p>
                    <div className="mt-2">{getRoleBadge(selectedPartner.role)}</div>
                  </div>
                </div>

                <div className="bg-[#f0f2f5] p-4 rounded-2xl text-left space-y-1">
                  <span className="text-[10px] font-bold text-[#54656f] uppercase tracking-wider block">Description / Location</span>
                  <p className="text-xs text-[#111b21] leading-relaxed font-medium">{selectedPartner.subtitle}</p>
                </div>

                <div className="space-y-2">
                  <button 
                    onClick={() => startCall('voice')}
                    className="w-full py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-xs font-bold text-[#111b21] flex items-center justify-center space-x-2 transition-colors"
                  >
                    <Phone className="h-4 w-4 text-[#00a884]" />
                    <span>WhatsApp Voice Call</span>
                  </button>
                  <button 
                    onClick={() => startCall('video')}
                    className="w-full py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-xs font-bold text-[#111b21] flex items-center justify-center space-x-2 transition-colors"
                  >
                    <Video className="h-4 w-4 text-[#00a884]" />
                    <span>WhatsApp Video Call</span>
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* ============================================================== */}
          {/* SIMULATED WHATSAPP CALL MODAL                                  */}
          {/* ============================================================== */}
          {activeCall && (
            <div className="absolute inset-0 bg-slate-900/90 backdrop-blur-md z-50 flex items-center justify-center p-4 animate-fade-in">
              <div className="w-full max-w-sm bg-slate-800 rounded-3xl p-6 text-center text-white space-y-6 shadow-2xl border border-slate-700">
                <div className="relative inline-block">
                  {selectedPartner?.profilePhoto ? (
                    <img src={selectedPartner.profilePhoto} alt="Avatar" className="h-24 w-24 rounded-full object-cover mx-auto ring-4 ring-[#00a884] animate-pulse" />
                  ) : (
                    <div className="h-24 w-24 rounded-full bg-[#00a884] flex items-center justify-center text-3xl font-bold mx-auto">
                      {(selectedPartner?.name || 'User').charAt(0).toUpperCase()}
                    </div>
                  )}
                  <span className="absolute bottom-0 right-0 h-4 w-4 rounded-full bg-[#25d366] ring-2 ring-slate-800"></span>
                </div>

                <div>
                  <h3 className="text-lg font-bold">{selectedPartner?.name}</h3>
                  <p className="text-xs text-emerald-400 mt-1 font-mono tracking-wider">
                    {activeCall.status === 'ringing' 
                      ? `WhatsApp ${activeCall.type === 'video' ? 'Video' : 'Voice'} Call Ringing...` 
                      : `Connected • 0:${activeCall.duration < 10 ? `0${activeCall.duration}` : activeCall.duration}`}
                  </p>
                </div>

                <div className="flex items-center justify-center space-x-6 pt-4">
                  <button 
                    onClick={endCall}
                    className="h-14 w-14 rounded-full bg-rose-600 hover:bg-rose-700 text-white flex items-center justify-center shadow-lg transition-transform hover:scale-110"
                    title="End Call"
                  >
                    <PhoneOff className="h-6 w-6" />
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}
