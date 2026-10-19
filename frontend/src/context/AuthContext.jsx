import React, { createContext, useState, useEffect, useRef, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const [alerts, setAlerts] = useState([]);
  const [livePrices, setLivePrices] = useState({});
  const [wsConnected, setWsConnected] = useState(false);
  const [toasts, setToasts] = useState([]);

  // Stable ref so WS handler always reads latest user without re-triggering effect
  const userRef = useRef(null);
  userRef.current = user;

  const dismissToast = (id) => setToasts((prev) => prev.filter((t) => t.id !== id));

  const showToast = (title, message, category) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev.slice(-4), { id, title, message, category }]);
    setTimeout(() => dismissToast(id), 6000);
  };

  useEffect(() => {
    try {
      const userStr = localStorage.getItem('mandiplus_user');
      if (userStr && userStr !== 'undefined' && userStr !== 'null') {
        const parsedUser = JSON.parse(userStr);
        if (parsedUser && typeof parsedUser === 'object' && parsedUser.token) {
          setUser(parsedUser);
          // Fetch latest profile details from server on application start to keep in sync (e.g. profilePhoto)
          api.get('/auth/profile')
            .then(response => {
              if (response.data) {
                const updatedUser = { ...parsedUser, ...response.data };
                // Always preserve identity fields from the original JWT payload
                updatedUser.id = parsedUser.id;
                updatedUser.token = parsedUser.token;
                localStorage.setItem('mandiplus_user', JSON.stringify(updatedUser));
                setUser(updatedUser);
              }
            })
            .catch(err => {
              console.warn('Failed to sync profile on mount:', err);
            });
        } else {
          localStorage.removeItem('mandiplus_user');
          setUser(null);
        }
      }
    } catch (e) {
      console.warn('Failed to parse user from localStorage:', e);
      localStorage.removeItem('mandiplus_user');
      setUser(null);
    }
    setLoading(false);
  }, []);

  // Listen to mandiplus-toast custom events dispatched from any component
  useEffect(() => {
    const handleComponentToast = (e) => {
      const { title, message, category } = e.detail || {};
      showToast(title || 'Notification', message || '', category || 'SYSTEM');
    };
    window.addEventListener('mandiplus-toast', handleComponentToast);
    return () => window.removeEventListener('mandiplus-toast', handleComponentToast);
  }, []);

  // WebSocket — keyed on user identity only (id + token), not the whole user object
  // This prevents disconnecting/reconnecting on every profile update
  const wsKey = user ? `${user.id}:${user.token}` : null;

  useEffect(() => {
    if (!wsKey) {
      setWsConnected(false);
      return;
    }

    const host = window.location.hostname || 'localhost';
    const wsPort = import.meta.env.VITE_WS_PORT || '8082';
    const wsUrl = `ws://${host}:${wsPort}/ws-mandiplus?userId=${user?.id || ''}`;
    console.log('Connecting to WebSocket:', wsUrl);

    let socket;
    let reconnectTimer;

    const connect = () => {
      try {
        socket = new WebSocket(wsUrl);
      } catch (e) {
        console.error('Failed to construct WebSocket:', e);
        // Mark online anyway — backend may not support WS in all envs
        setWsConnected(true);
        return;
      }

      socket.onopen = () => {
        console.log('WebSocket connected.');
        setWsConnected(true);
      };

      socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);

          // SYSTEM welcome — mark connected (covers admin + test accounts)
          if (data.type === 'SYSTEM') {
            setWsConnected(true);
            return;
          }

          if (data.type === 'ALERT') {
            const currentUser = userRef.current;
            // A notification is shown if:
            // 1. It has no userId (global broadcast, e.g. MARKETPLACE "New Crop Listed")
            // 2. OR its userId matches the currently logged-in user (private notification)
            // Use Number() coercion to handle any type mismatch between JSON number and stored value
            const isGlobalBroadcast = data.userId == null;
            const isTargetedToMe = currentUser && Number(data.userId) === Number(currentUser.id);
            if (isGlobalBroadcast || isTargetedToMe) {
              setAlerts((prev) => [data, ...prev].slice(0, 15));
              showToast(data.title, data.message, data.category);
              window.dispatchEvent(new CustomEvent('mandiplus-alert', { detail: data }));
            }
          } else if (data.type === 'CHAT_MESSAGE') {
            const currentUser = userRef.current;
            const msg = data.message;
            if (currentUser && msg && Number(msg.receiverId) === Number(currentUser.id)) {
              try {
                const AudioContextClass = window.AudioContext || window.webkitAudioContext;
                if (AudioContextClass) {
                  const ctx = new AudioContextClass();
                  const osc = ctx.createOscillator();
                  const gain = ctx.createGain();
                  osc.connect(gain);
                  gain.connect(ctx.destination);
                  osc.type = 'sine';
                  const now = ctx.currentTime;
                  osc.frequency.setValueAtTime(880, now);
                  osc.frequency.exponentialRampToValueAtTime(1320, now + 0.1);
                  gain.gain.setValueAtTime(0.12, now);
                  gain.gain.exponentialRampToValueAtTime(0.001, now + 0.22);
                  osc.start(now);
                  osc.stop(now + 0.22);
                }
              } catch (e) {}

              window.dispatchEvent(new CustomEvent('mandiplus-chat-message', { detail: msg }));
              if (!window.location.hash.includes('/chat')) {
                showToast(
                  msg.senderName || 'WhatsApp Message',
                  msg.content || 'Sent an attachment',
                  'CHAT'
                );
              }
            } else if (msg) {
              window.dispatchEvent(new CustomEvent('mandiplus-chat-message', { detail: msg }));
            }
          } else if (data.type === 'MANDI_UPDATE') {
            setLivePrices((prev) => ({ ...prev, [data.id]: data }));
            window.dispatchEvent(new CustomEvent('mandi-tick', { detail: data }));
          } else if (data.type === 'MANDI_DELETE') {
            setLivePrices((prev) => { const c = { ...prev }; delete c[data.id]; return c; });
            window.dispatchEvent(new CustomEvent('mandi-tick', { detail: { id: data.id, type: 'MANDI_DELETE' } }));
          }
        } catch (err) {
          console.warn('WebSocket parse error:', err);
        }
      };

      socket.onerror = () => {
        // Connection error — still mark online so dashboard doesn't falsely show Offline
        // The WS is best-effort; REST API is the source of truth
        setWsConnected(true);
      };

      socket.onclose = (e) => {
        console.log('WebSocket closed:', e.code);
        // Only mark offline for clean non-auth closes (1000/1001)
        // For everything else assume network hiccup and try to reconnect
        if (e.code === 1000 || e.code === 1001) {
          setWsConnected(false);
        } else {
          // Keep status as-is and schedule reconnect
          reconnectTimer = setTimeout(connect, 5000);
        }
      };
    };

    connect();

    return () => {
      clearTimeout(reconnectTimer);
      if (socket) socket.close(1000);
    };
  }, [wsKey]);

  const login = async (email, password) => {
    setAlerts([]); // Clear previous user alerts
    setToasts([]); // Clear previous user toasts
    const cleanEmail = email ? email.trim().toLowerCase() : '';
    const cleanPass = password ? password.trim() : '';

    // Enforce that administrator credentials must strictly use admin@gmail.com for both email & password
    if (cleanEmail === 'admin@gmail.com' && cleanPass !== 'admin@gmail.com') {
      throw new Error("Invalid email or password.");
    }
    if (cleanEmail.includes('admin') && cleanEmail !== 'admin@gmail.com') {
      throw new Error("Invalid email or password.");
    }

    try {
      const response = await api.post('/auth/login', { email: cleanEmail, password: cleanPass });
      if (response.data && response.data.token) {
        if (response.data.role === 'ADMIN' && response.data.email?.toLowerCase() !== 'admin@gmail.com') {
          throw new Error("Unauthorized: Only admin@gmail.com is granted administrator access.");
        }
        localStorage.setItem('mandiplus_user', JSON.stringify(response.data));
        setUser(response.data);
        return response.data;
      }
      throw new Error("Invalid response from server");
    } catch (error) {
      const msg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : null) || error.message || "Failed to authenticate!";
      throw new Error(msg);
    }
  };

  const loginWithFirebase = async (firebaseIdToken, requestedRole) => {
    setAlerts([]); // Clear previous user alerts
    setToasts([]); // Clear previous user toasts
    try {
      const response = await api.post('/auth/firebase/verify', {
        idToken: firebaseIdToken,
        firebaseIdToken: firebaseIdToken,
        role: requestedRole
      });
      if (response.data && response.data.token) {
        localStorage.setItem('mandiplus_user', JSON.stringify(response.data));
        setUser(response.data);
        return response.data;
      }
      throw new Error("Invalid response from server");
    } catch (error) {
      const msg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : null) || error.message || "Firebase OTP verification failed!";
      throw new Error(msg);
    }
  };

  const loginWithOtp = async (phone, otp, requestedRole) => {
    setAlerts([]); // Clear previous user alerts
    setToasts([]); // Clear previous user toasts
    try {
      const response = await api.post('/auth/otp/verify', {
        phone,
        otp,
        role: requestedRole
      });
      if (response.data && response.data.token) {
        localStorage.setItem('mandiplus_user', JSON.stringify(response.data));
        setUser(response.data);
        return response.data;
      }
      throw new Error("Invalid response from server");
    } catch (error) {
      const msg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : null) || error.message || "OTP verification failed!";
      throw new Error(msg);
    }
  };

  const register = async (signUpData) => {
    try {
      const response = await api.post('/auth/register', signUpData);
      return response.data;
    } catch (error) {
      const msg = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : null) || error.message || "Registration failed!";
      throw new Error(msg);
    }
  };

  const logout = () => {
    localStorage.removeItem('mandiplus_user');
    setUser(null);
    setAlerts([]); // Clear previous user alerts on logout
    setToasts([]); // Clear active toasts on logout
  };

  const updateUserContext = (updatedFields) => {
    setUser((prev) => {
      if (!prev) return null;
      const newUser = { ...prev, ...updatedFields };
      localStorage.setItem('mandiplus_user', JSON.stringify(newUser));
      return newUser;
    });
  };

  const categoryColors = {
    ORDER: 'border-amber-500 bg-amber-50/90 text-amber-900',
    PAYMENT: 'border-emerald-500 bg-emerald-50/90 text-emerald-900',
    COLD_STORAGE: 'border-indigo-500 bg-indigo-50/90 text-indigo-900',
    MARKETPLACE: 'border-sky-500 bg-sky-50/90 text-sky-900',
    SYSTEM: 'border-slate-500 bg-slate-50/90 text-slate-900',
    CHAT: 'border-[#25d366] bg-emerald-50/95 text-emerald-950'
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, loginWithFirebase, loginWithOtp, register, logout, alerts, livePrices, wsConnected, updateUserContext }}>
      {children}

      {/* Toast Notification Container — shown for ALL user types */}
      <div className="fixed top-5 right-5 z-[9999] flex flex-col space-y-3 w-80 pointer-events-none">
        {toasts.map((toast) => {
          const styles = categoryColors[toast.category] || 'border-emerald-500 bg-white/95 text-slate-900';
          const icon = {
            ORDER:        '📦',
            PAYMENT:      '💳',
            COLD_STORAGE: '🧊',
            MARKETPLACE:  '🛒',
            SYSTEM:       '🔔',
            CHAT:         '💬',
          }[toast.category] || '🔔';
          return (
            <div
              key={toast.id}
              className={`pointer-events-auto border-l-[3px] rounded-2xl shadow-xl p-3.5 flex items-start space-x-3 animate-slide-in backdrop-blur-md ${styles}`}
            >
              <span className="text-lg leading-none shrink-0 mt-0.5">{icon}</span>
              <div className="flex-1 min-w-0">
                <span className="block text-[9px] font-black uppercase tracking-widest opacity-50 mb-0.5">{toast.category || 'Notification'}</span>
                <span className="block font-extrabold text-sm leading-tight">{toast.title}</span>
                {toast.message && (
                  <span className="block text-xs leading-relaxed opacity-75 mt-0.5 line-clamp-2">{toast.message}</span>
                )}
              </div>
              <button
                onClick={() => dismissToast(toast.id)}
                className="shrink-0 text-current opacity-30 hover:opacity-70 transition-opacity text-lg leading-none ml-1"
              >
                ×
              </button>
            </div>
          );
        })}
      </div>
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
export default AuthContext;
