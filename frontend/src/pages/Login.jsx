import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLanguage } from '../context/LanguageContext';
import Navbar from '../components/Navbar';
import api from '../services/api';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';
import { auth } from '../firebase/firebaseConfig';
import {
  ArrowLeft,
  Eye,
  EyeOff,
  Lock,
  Mail,
  Users,
  Building,
  Warehouse,
  ShieldAlert,
  Bike,
  Sparkles,
  Smartphone,
  CheckCircle2,
  RotateCw,
  KeyRound,
  ShieldCheck
} from 'lucide-react';

export default function Login() {
  const { t } = useLanguage();
  const [searchParams] = useSearchParams();
  const initialRole = searchParams.get('role');

  const [role, setRole] = useState(initialRole);
  const [authMode, setAuthMode] = useState('otp'); // 'otp' | 'password'

  // Password Login State
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  // Real Mobile OTP Login State (Firebase Phone Auth)
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [maskedPhone, setMaskedPhone] = useState('');
  const [cooldown, setCooldown] = useState(0);
  const [sendingOtp, setSendingOtp] = useState(false);
  const [verifyingOtp, setVerifyingOtp] = useState(false);
  const [otpInfoMsg, setOtpInfoMsg] = useState('');
  const [confirmationResult, setConfirmationResult] = useState(null);

  const [error, setError] = useState('');
  const { login, loginWithFirebase, loginWithOtp } = useAuth();
  const navigate = useNavigate();

  // Cleanup reCAPTCHA verifier on unmount
  useEffect(() => {
    return () => {
      if (window.recaptchaVerifier) {
        try {
          window.recaptchaVerifier.clear();
        } catch (_) {}
        window.recaptchaVerifier = null;
      }
    };
  }, []);

  // Cooldown countdown timer
  useEffect(() => {
    let timer;
    if (cooldown > 0) {
      timer = setInterval(() => {
        setCooldown((prev) => (prev > 0 ? prev - 1 : 0));
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [cooldown]);

  // Clear credentials whenever role changes to ensure clean manual entry
  useEffect(() => {
    setEmail('');
    setPassword('');
    setPhone('');
    setOtp('');
    setOtpSent(false);
    setError('');
    if (role === 'ADMIN') {
      setAuthMode('password');
    }
  }, [role]);

  const routeUserAfterAuth = (userObj) => {
    // TC-PROFILE-001 & TC-PROFILE-006:
    // If mandatory profile setup is incomplete, redirect directly to /setup-profile
    if (userObj.isProfileComplete === false || userObj.profileSetupRequired === true) {
      navigate('/setup-profile', { replace: true });
      return;
    }

    if (role === 'FARMER' || userObj.role === 'FARMER') navigate('/farmer', { replace: true });
    else if (role === 'BUYER' || userObj.role === 'BUYER') navigate('/buyer', { replace: true });
    else if (role === 'COLD_STORAGE_MANAGER' || userObj.role === 'COLD_STORAGE_MANAGER') navigate('/storage', { replace: true });
    else if (role === 'ADMIN' || userObj.role === 'ADMIN') navigate('/admin', { replace: true });
    else if (role === 'DELIVERY_PARTNER' || userObj.role === 'DELIVERY_PARTNER') navigate('/delivery', { replace: true });
    else navigate('/', { replace: true });
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const trimmedEmail = email ? email.trim().toLowerCase() : '';
      if (role === 'ADMIN') {
        if (trimmedEmail !== 'admin@gmail.com' || password !== 'admin@gmail.com') {
          setError('Invalid administrator email or password.');
          setLoading(false);
          return;
        }
      }
      const userObj = await login(trimmedEmail, password);
      routeUserAfterAuth(userObj);
    } catch (err) {
      setError(err.message || 'Incorrect email or password.');
    } finally {
      setLoading(false);
    }
  };

  const initRecaptcha = () => {
    if (window.recaptchaVerifier) {
      try {
        window.recaptchaVerifier.clear();
      } catch (e) {
        console.warn('recaptchaVerifier clear error:', e);
      }
      window.recaptchaVerifier = null;
    }
    window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
      size: 'invisible',
      callback: () => {
        // reCAPTCHA solved - ready to dispatch
      },
      'expired-callback': () => {
        setError('reCAPTCHA verification expired. Please try again.');
      }
    });
    return window.recaptchaVerifier;
  };

  const handleSendOtp = async (e) => {
    if (e) e.preventDefault();
    setError('');
    const cleanPhone = phone.trim().replace(/\D/g, '');

    if (cleanPhone.length !== 10 || !/^[6-9]\d{9}$/.test(cleanPhone)) {
      setError('Please enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9.');
      return;
    }

    setSendingOtp(true);
    try {
      const appVerifier = initRecaptcha();
      const formattedPhone = `+91${cleanPhone}`;
      const confirmation = await signInWithPhoneNumber(auth, formattedPhone, appVerifier);
      setConfirmationResult(confirmation);
      setOtpSent(true);
      setMaskedPhone(`+91 ******${cleanPhone.slice(-4)}`);
      setCooldown(60);
      setOtpInfoMsg('Firebase OTP dispatched via SMS to your mobile phone.');
      setOtp('');
    } catch (err) {
      console.error('Firebase signInWithPhoneNumber error:', err);
      if (window.recaptchaVerifier) {
        try { window.recaptchaVerifier.clear(); } catch (_) {}
        window.recaptchaVerifier = null;
      }
      let userMsg = 'SMS delivery failed. Please verify the mobile number or try again.';
      if (err.code === 'auth/invalid-phone-number') {
        userMsg = 'Invalid phone number format.';
      } else if (err.code === 'auth/too-many-requests') {
        userMsg = 'Too many requests. Please wait a few minutes before requesting another OTP.';
      } else if (err.code === 'auth/quota-exceeded') {
        userMsg = 'SMS quota exceeded for Firebase project. Please contact administrator.';
      } else if (err.code === 'auth/unauthorized-domain') {
        userMsg = 'This domain is not authorized in Firebase Console -> Authentication -> Settings -> Authorized domains.';
      } else if (err.code === 'auth/captcha-check-failed') {
        userMsg = 'reCAPTCHA verification failed. Please try again.';
      } else if (err.message) {
        userMsg = err.message;
      }
      setError(userMsg);
    } finally {
      setSendingOtp(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    const cleanOtp = otp.trim();

    if (!cleanOtp || cleanOtp.length !== 6) {
      setError('Please enter the 6-digit verification code received on your phone.');
      return;
    }

    if (!confirmationResult) {
      setError('No active verification session. Please request an OTP first.');
      return;
    }

    setVerifyingOtp(true);
    try {
      // Step 5: Firebase verifies the OTP
      const userCredential = await confirmationResult.confirm(cleanOtp);
      // Step 6: Frontend obtains a Firebase ID token
      const idToken = await userCredential.user.getIdToken();
      // Step 7: Frontend securely submits that token to Spring Boot authentication endpoint
      const userObj = await loginWithFirebase(idToken, role || 'BUYER');
      routeUserAfterAuth(userObj);
    } catch (err) {
      console.error('Firebase verify OTP error:', err);
      let userMsg = 'Invalid or expired OTP code.';
      if (err.code === 'auth/invalid-verification-code') {
        userMsg = 'Incorrect verification code. Please check your SMS inbox and re-enter.';
      } else if (err.code === 'auth/code-expired') {
        userMsg = 'This verification code has expired. Please request a new OTP.';
      } else if (err.message) {
        userMsg = err.message;
      }
      setError(userMsg);
    } finally {
      setVerifyingOtp(false);
    }
  };

  if (!role) {
    return (
      <div className="min-h-screen bg-[#fafbfb] flex flex-col font-sans">
        <Navbar />
        <main className="flex-1 flex items-center justify-center py-16 px-4">
          <div className="w-full max-w-2xl bg-white rounded-3xl border border-slate-100 p-10 shadow-premium text-center">
            <h2 className="text-3xl font-black text-slate-800 tracking-tight mb-2">{t('selectRole')}</h2>
            <p className="text-slate-500 mb-10">Choose how you want to log in to Mandi+.</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              <button onClick={() => { setRole('FARMER'); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); }} className="p-6 rounded-2xl border border-slate-200 hover:border-agrogreen-600 hover:bg-agrogreen-50 transition-all flex flex-col items-center group">
                <Users className="h-10 w-10 text-agrogreen-600 mb-3 group-hover:scale-110 transition-transform" />
                <span className="font-bold text-slate-800">{t('farmer')}</span>
                <span className="text-[11px] text-slate-400 mt-1">Produce Listings & APMC Rates</span>
              </button>
              <button onClick={() => { setRole('BUYER'); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); }} className="p-6 rounded-2xl border border-slate-200 hover:border-agrogreen-600 hover:bg-agrogreen-50 transition-all flex flex-col items-center group">
                <Building className="h-10 w-10 text-agrogreen-600 mb-3 group-hover:scale-110 transition-transform" />
                <span className="font-bold text-slate-800">{t('buyer')}</span>
                <span className="text-[11px] text-slate-400 mt-1">Bulk Sourcing & Escrow</span>
              </button>
              <button onClick={() => { setRole('COLD_STORAGE_MANAGER'); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); }} className="p-6 rounded-2xl border border-slate-200 hover:border-agrogreen-600 hover:bg-agrogreen-50 transition-all flex flex-col items-center group">
                <Warehouse className="h-10 w-10 text-agrogreen-600 mb-3 group-hover:scale-110 transition-transform" />
                <span className="font-bold text-slate-800">{t('coldStorageManager')}</span>
                <span className="text-[11px] text-slate-400 mt-1">IoT Storage & Warehouse Leases</span>
              </button>
              <button onClick={() => { setRole('DELIVERY_PARTNER'); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); }} className="p-6 rounded-2xl border-2 border-emerald-500 bg-emerald-50/50 hover:bg-emerald-100/60 transition-all flex flex-col items-center group relative">
                <span className="absolute top-2 right-2 bg-emerald-600 text-white text-[9px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider">NEW</span>
                <Bike className="h-10 w-10 text-emerald-700 mb-3 group-hover:scale-110 transition-transform" />
                <span className="font-bold text-emerald-950">{t('deliveryPartner')}</span>
                <span className="text-[11px] text-emerald-700 mt-1">Daily Payouts & Transit Orders</span>
              </button>
              <button onClick={() => { setRole('ADMIN'); setAuthMode('password'); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); }} className="p-6 rounded-2xl border border-slate-200 hover:border-agrogreen-600 hover:bg-agrogreen-50 transition-all flex flex-col items-center group col-span-1 sm:col-span-2 lg:col-span-1">
                <ShieldAlert className="h-10 w-10 text-agrogreen-600 mb-3 group-hover:scale-110 transition-transform" />
                <span className="font-bold text-slate-800">{t('systemAdmin')}</span>
                <span className="text-[11px] text-slate-400 mt-1">Disputes, Rates & Oversight</span>
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  const roleConfig = {
    FARMER: {
      title: t('farmer'),
      subtitle: t('loginSubtitle'),
      bgImage: '/images/login_farmer_bg.png',
      leftTitle: 'Mandi+',
      leftDesc: t('heroSubtitle'),
      boxNumber: '1.2M+',
      boxLabel: 'Farmers Trusted Us',
      boxText: '"Mandi+ helped me secure my harvest and get the best market price directly from the mandis."',
      inputLabel: t('email'),
      btnText: t('signIn')
    },
    BUYER: {
      title: t('buyer'),
      subtitle: t('loginSubtitle'),
      bgImage: '/images/login_buyer_bg.png',
      leftTitle: 'Streamline Your Bulk Procurement',
      leftDesc: t('heroSubtitle'),
      boxNumber: 'WHEAT',
      boxLabel: '₹2,450/q',
      boxText: '',
      inputLabel: t('email'),
      btnText: t('signIn')
    },
    COLD_STORAGE_MANAGER: {
      title: t('coldStorageManager'),
      subtitle: t('loginSubtitle'),
      bgImage: '/images/login_storage_bg.png',
      leftTitle: 'Logistics Management Portal',
      leftDesc: t('heroSubtitle'),
      boxNumber: '98%',
      boxLabel: 'Efficiency Rate',
      boxText: '',
      inputLabel: t('email'),
      btnText: t('signIn')
    },
    ADMIN: {
      title: t('systemAdmin'),
      subtitle: 'Configured Administrator Access Portal',
      bgImage: '/images/logistics.png',
      leftTitle: 'Mandi+ Central Control',
      leftDesc: 'Platform oversight, dispute resolution, APMC mandi rates synchronizer, and system management.',
      boxNumber: '100%',
      boxLabel: 'System Uptime',
      boxText: '',
      inputLabel: t('email'),
      btnText: t('signIn')
    },
    DELIVERY_PARTNER: {
      title: 'Delivery Partner Login',
      subtitle: 'Mandi+ Quick Commerce Agri Fleet Portal',
      bgImage: '/images/logistics.png',
      leftTitle: 'Deliver Farm Produce. Earn Daily.',
      leftDesc: 'Direct farm-to-store express delivery network. Instant UPI payouts, high-density order batches, and real-time grievance desk.',
      boxNumber: '₹1,420/day',
      boxLabel: 'Avg Partner Earnings',
      boxText: '"I transport fresh onion & grape consignments directly from local farmers to wholesale buyers and receive daily UPI payments."',
      inputLabel: t('email'),
      btnText: 'Sign In to Partner Fleet'
    }
  };

  const currentConfig = roleConfig[role] || roleConfig.FARMER;

  return (
    <div className="min-h-screen bg-white flex flex-col md:flex-row font-sans">
      {/* Left Pane - Branding */}
      <div className="hidden md:flex md:w-1/2 relative bg-agrogreen-950 text-white overflow-hidden flex-col justify-between min-h-screen">
        <img src={currentConfig.bgImage} alt="Background" className="absolute inset-0 w-full h-full object-cover z-0 opacity-40 mix-blend-overlay" />
        <div className="absolute inset-0 bg-gradient-to-t from-agrogreen-950 via-agrogreen-950/80 to-transparent z-10"></div>

        <div className="relative z-20 p-12">
          <Link to="/" className="inline-block mb-10 group" title="Return to Mandi+ Home">
            <div className="bg-white rounded-2xl p-2.5 shadow-lg inline-flex items-center justify-center hover:scale-105 transition-all">
              <img
                src="/images/logo.png"
                alt="Mandi+ Market Solutions"
                className="h-12 w-auto object-contain"
              />
            </div>
          </Link>
          <h1 className="text-4xl lg:text-5xl font-extrabold mb-4">{currentConfig.leftTitle}</h1>
          <p className="text-agrogreen-100 text-lg max-w-md leading-relaxed">{currentConfig.leftDesc}</p>
        </div>

        <div className="relative z-20 p-12 pt-0">
          <div className="bg-white/90 backdrop-blur-md rounded-2xl p-6 text-slate-800 shadow-xl max-w-sm">
            {role === 'FARMER' && (
              <div className="flex flex-col space-y-4">
                <div className="flex items-center space-x-3">
                  <div className="bg-agrogreen-100 p-2.5 rounded-full text-agrogreen-600"><Users className="h-6 w-6" /></div>
                  <div>
                    <h4 className="font-black text-xl">{currentConfig.boxNumber}</h4>
                    <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">{currentConfig.boxLabel}</p>
                  </div>
                </div>
                <p className="text-sm italic text-slate-600">{currentConfig.boxText}</p>
              </div>
            )}
            {role === 'BUYER' && (
              <div className="flex items-center justify-between font-bold text-sm">
                <span className="text-slate-600">WHEAT <span className="text-agrogreen-600">₹2,450/q</span></span>
                <span className="text-slate-600">POTATO <span className="text-agrogreen-600">₹1,120/q</span></span>
              </div>
            )}
            {role === 'COLD_STORAGE_MANAGER' && (
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-black text-2xl text-agrogreen-700">98%</h4>
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">Efficiency Rate</p>
                </div>
                <div>
                  <h4 className="font-black text-2xl text-agrogreen-700">24/7</h4>
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">Live Monitoring</p>
                </div>
              </div>
            )}
            {role === 'ADMIN' && (
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-black text-xl text-slate-900">Rohan Gaikwad</h4>
                  <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">Configured Admin (9012345678)</p>
                </div>
                <ShieldCheck className="h-8 w-8 text-emerald-600" />
              </div>
            )}
            {role === 'DELIVERY_PARTNER' && (
              <div>
                <h4 className="font-black text-2xl text-emerald-700">{currentConfig.boxNumber}</h4>
                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wide">{currentConfig.boxLabel}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Right Pane - Form */}
      <div className="w-full md:w-1/2 flex flex-col justify-between overflow-y-auto bg-white min-h-screen">
        <div className="p-8 flex justify-end"></div>

        <div className="max-w-md w-full mx-auto px-8 pb-12">
          <button onClick={() => { setRole(null); setEmail(''); setPassword(''); setPhone(''); setOtpSent(false); setError(''); }} className="flex items-center text-xs font-bold text-slate-500 hover:text-slate-800 mb-8 transition-colors">
            <ArrowLeft className="h-4 w-4 mr-1.5" /> Back to Role Selection
          </button>

          <h2 className="text-3xl font-black text-slate-900 mb-2">{currentConfig.title}</h2>
          <p className="text-slate-500 text-sm mb-6">{currentConfig.subtitle}</p>

          {/* Authentication Mode Tabs */}
          {role !== 'ADMIN' ? (
            <div className="flex bg-slate-100 p-1 rounded-xl mb-6 text-xs font-bold">
              <button
                type="button"
                onClick={() => { setAuthMode('otp'); setError(''); }}
                className={`flex-1 py-2.5 rounded-lg flex items-center justify-center space-x-1.5 transition-all ${
                  authMode === 'otp'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                <Smartphone className="h-4 w-4" />
                <span>Mobile SMS OTP</span>
              </button>
              <button
                type="button"
                onClick={() => { setAuthMode('password'); setError(''); }}
                className={`flex-1 py-2.5 rounded-lg flex items-center justify-center space-x-1.5 transition-all ${
                  authMode === 'password'
                    ? 'bg-white text-slate-900 shadow-sm'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                <KeyRound className="h-4 w-4" />
                <span>Email & Password</span>
              </button>
            </div>
          ) : (
            <div className="bg-slate-900 text-white px-4 py-3 rounded-xl mb-6 text-xs flex items-center justify-between shadow-sm">
              <div className="flex items-center space-x-2">
                <ShieldAlert className="h-4 w-4 text-emerald-400 shrink-0" />
                <span className="font-semibold">Administrator Authentication</span>
              </div>
              <span className="text-[11px] text-slate-400">Restricted Access</span>
            </div>
          )}

          {error && (
            <div className="bg-rose-50 text-rose-700 p-4 rounded-xl text-xs font-bold mb-6 border border-rose-200">
              {error}
            </div>
          )}

          {/* ========================================================================= */}
          {/* TAB 1: REAL MOBILE SMS OTP AUTHENTICATION                                   */}
          {/* ========================================================================= */}
          {authMode === 'otp' && (
            <div>
              {!otpSent ? (
                // Step 1: Request OTP
                <form onSubmit={handleSendOtp} className="space-y-5">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-2">
                      Indian Mobile Number <span className="text-rose-500">*</span>
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500 font-bold text-xs">
                        <span>+91</span>
                      </div>
                      <input
                        type="tel"
                        required
                        maxLength={10}
                        value={phone}
                        onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
                        placeholder={role === 'ADMIN' ? 'e.g. 9012345678' : 'e.g. 9876543210'}
                        className="w-full pl-14 pr-4 py-3.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all text-sm font-semibold tracking-wider text-slate-800"
                      />
                    </div>
                    <p className="text-[11px] text-slate-400 mt-1.5">
                      A real 6-digit cryptographic OTP will be dispatched via SMS.
                    </p>
                  </div>

                  {role === 'ADMIN' && (
                    <div className="bg-agrogreen-50 border border-agrogreen-200 p-3.5 rounded-xl text-center space-y-2">
                      <span className="text-xs font-bold text-agrogreen-950 block">Designated Admin Profile</span>
                      <button
                        type="button"
                        onClick={() => setPhone('9012345678')}
                        className="bg-agrogreen-700 hover:bg-agrogreen-800 text-white font-extrabold px-3 py-1.5 rounded-lg text-xs transition-colors shadow-sm inline-flex items-center space-x-1"
                      >
                        <span>⚡ Fill Admin Mobile (9012345678)</span>
                      </button>
                    </div>
                  )}

                  {/* Firebase reCAPTCHA Container */}
                  <div id="recaptcha-container" className="flex justify-center my-2"></div>

                  <button
                    type="submit"
                    disabled={sendingOtp || phone.trim().length !== 10}
                    className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all disabled:opacity-50 shadow-md"
                  >
                    <span>{sendingOtp ? 'Dispatching Firebase SMS...' : 'Send OTP via Firebase'}</span>
                    <ArrowLeft className="h-4 w-4 rotate-180" />
                  </button>
                </form>
              ) : (
                // Step 2: Enter & Verify OTP (shown ONLY after provider acceptance)
                <form onSubmit={handleVerifyOtp} className="space-y-5">
                  <div className="bg-emerald-50 border border-emerald-200 p-4 rounded-2xl flex items-start space-x-3">
                    <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0 mt-0.5" />
                    <div className="text-xs">
                      <span className="font-bold text-emerald-950 block">OTP Sent Successfully!</span>
                      <span className="text-emerald-800">
                        Dispatched to <strong className="font-mono">{maskedPhone}</strong>. Valid for 5 minutes.
                      </span>
                      <button
                        type="button"
                        onClick={() => { setOtpSent(false); setOtp(''); setError(''); }}
                        className="block text-emerald-700 font-bold underline mt-1 hover:text-emerald-900"
                      >
                        Change mobile number
                      </button>
                    </div>
                  </div>

                  {otpInfoMsg && (
                    <div className="bg-amber-50 border border-amber-200 text-amber-900 p-3.5 rounded-xl text-xs space-y-1">
                      <div className="flex items-center space-x-1.5 font-bold text-amber-950">
                        <Sparkles className="h-3.5 w-3.5 text-amber-600" />
                        <span>SMS Gateway Status</span>
                      </div>
                      <p className="leading-relaxed">{otpInfoMsg}</p>
                    </div>
                  )}

                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-2">
                      Enter 6-Digit OTP Code <span className="text-rose-500">*</span>
                    </label>
                    <input
                      type="text"
                      required
                      maxLength={6}
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                      placeholder="••••••"
                      className="w-full px-4 py-3.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all text-center text-2xl font-mono font-black tracking-[0.5em] text-slate-900"
                      autoFocus
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs pt-1">
                    <span className="text-slate-500">Didn't receive SMS?</span>
                    {cooldown > 0 ? (
                      <span className="font-mono text-slate-400 font-bold">
                        Resend in {cooldown}s
                      </span>
                    ) : (
                      <button
                        type="button"
                        onClick={handleSendOtp}
                        disabled={sendingOtp}
                        className="text-agrogreen-700 font-bold hover:underline flex items-center space-x-1"
                      >
                        <RotateCw className="h-3 w-3" />
                        <span>Resend OTP</span>
                      </button>
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={verifyingOtp || otp.trim().length !== 6}
                    className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all disabled:opacity-50 shadow-md"
                  >
                    <span>{verifyingOtp ? 'Verifying OTP...' : 'Verify & Continue'}</span>
                    <ArrowLeft className="h-4 w-4 rotate-180" />
                  </button>
                </form>
              )}
            </div>
          )}

          {/* TAB 2: EMAIL & PASSWORD AUTHENTICATION */}
          {authMode === 'password' && (
            <form onSubmit={handlePasswordSubmit} autoComplete="off" className="space-y-6">
              {/* Dummy inputs to absorb browser autofill */}
              <input type="text" style={{ display: 'none' }} aria-hidden="true" tabIndex={-1} />
              <input type="password" style={{ display: 'none' }} aria-hidden="true" tabIndex={-1} />

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-2">{currentConfig.inputLabel}</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
                    <Mail className="h-4 w-4" />
                  </div>
                  <input
                    type="text"
                    id="mandiplus-auth-email"
                    name="mandiplus_auth_user"
                    autoComplete="off"
                    autoCapitalize="off"
                    autoCorrect="off"
                    spellCheck="false"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="e.g. name@domain.com"
                    className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all text-sm bg-white"
                  />
                </div>
              </div>

              <div>
                <div className="flex justify-between mb-2">
                  <label className="block text-xs font-bold text-slate-700">{t('password')}</label>
                </div>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
                    <Lock className="h-4 w-4" />
                  </div>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    id="mandiplus-auth-password"
                    name="mandiplus_auth_secret"
                    autoComplete="new-password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-11 pr-10 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-agrogreen-500/20 focus:border-agrogreen-600 transition-all text-sm bg-white"
                  />
                  <div
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-4 flex items-center cursor-pointer text-slate-400 hover:text-slate-600"
                  >
                    {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-[#143d2c] hover:bg-agrogreen-950 text-white py-4 rounded-xl font-bold flex items-center justify-center space-x-2 transition-all disabled:opacity-50"
              >
                <span>{loading ? t('loading') : currentConfig.btnText}</span>
                <ArrowLeft className="h-4 w-4 rotate-180" />
              </button>
            </form>
          )}

          <div className="mt-8 text-center space-y-2">
            {role === 'ADMIN' ? (
              <p className="text-xs text-slate-400">
                Administrator accounts are strictly provisioned by system security policy.
              </p>
            ) : role === 'DELIVERY_PARTNER' ? (
              <p className="text-sm text-slate-500">
                Want to become a partner?{' '}
                <Link to="/apply-delivery" className="text-emerald-700 font-bold hover:underline">
                  Apply for Delivery Partner Position →
                </Link>
              </p>
            ) : (
              <p className="text-sm text-slate-500">
                New to Mandi+?{' '}
                <Link to={`/register?role=${role}`} className="text-agrogreen-700 font-bold hover:underline">
                  Register as {role.replace(/_/g, ' ').toLowerCase()}
                </Link>
              </p>
            )}
          </div>
        </div>

        <div className="p-8 text-center text-xs font-medium text-slate-400 flex flex-col md:flex-row justify-between items-center bg-slate-50 border-t border-slate-100 mt-auto">
          <span>© 2026 Mandi+. Empowering Indian Agriculture.</span>
          <div className="space-x-4 mt-4 md:mt-0">
            <Link to="#" className="hover:text-slate-600">Privacy Policy</Link>
            <Link to="#" className="hover:text-slate-600">Terms of Service</Link>
            <Link to="#" className="hover:text-slate-600">Support</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
