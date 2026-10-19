import React, { useState, useEffect } from 'react';
import { CreditCard, Smartphone, ShieldCheck, CheckCircle2, AlertCircle, Clock, Info } from 'lucide-react';
import { useLanguage } from '../context/LanguageContext';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function PaymentGatewayModal({ isOpen, onClose, onSuccess, amount }) {
  const { t } = useLanguage();
  const { user } = useAuth();
  const [step, setStep] = useState('choose'); // choose, authenticating, verifying, success
  const [txId, setTxId] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (isOpen) {
      setStep('choose');
      setErrorMsg('');
      setTxId('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  // Helper to load Razorpay SDK dynamically
  const loadRazorpayScript = () => {
    return new Promise((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => {
        resolve(true);
      };
      script.onerror = () => {
        resolve(false);
      };
      document.body.appendChild(script);
    });
  };

  // Launch Razorpay Payment Flow
  const handlePaymentSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setStep('authenticating');

    try {
      // 1. Load Razorpay Script
      const isScriptLoaded = await loadRazorpayScript();
      if (!isScriptLoaded) {
        throw new Error('Failed to load Razorpay payment SDK. Please check your internet connection.');
      }

      // 2. Request backend to create Razorpay Order
      const res = await api.post(`/buyer/payment/create-order?amount=${amount}`);
      const { orderId, amount: orderAmount, currency, keyId } = res.data;

      // Intercept simulated checkout test-mode order ID
      if (orderId && (orderId.startsWith('order_test_simulated_') || orderId.startsWith('order_sbx_'))) {
        setTimeout(async () => {
          setStep('verifying');
          try {
            const fakePayId = 'pay_sbx_' + Math.random().toString(36).substring(2, 11);
            // Forward simulated signature verification
            await onSuccess({
              razorpayPaymentId: fakePayId,
              razorpayOrderId: orderId,
              razorpaySignature: 'sig_test_simulated'
            });
            setTxId(fakePayId);
            setStep('success');
          } catch (err) {
            setErrorMsg(err.response?.data || err.message || 'Payment verification failed.');
            setStep('choose');
          }
        }, 1500); // Simulate network processing delay
        return;
      }

      // 3. Configure Razorpay Options
      const options = {
        key: keyId,
        amount: orderAmount, // Amount is in paise from backend
        currency: currency,
        name: "Mandi+ Secure Escrow",
        description: "Direct Harvest Escrow Hold",
        image: "https://cdn-icons-png.flaticon.com/512/1865/1865269.png", // Agro green icon
        order_id: orderId,
        handler: async function (response) {
          // Trigger when payment is completed by user
          setStep('verifying');
          try {
            // Forward signature details to parent onSuccess callback
            await onSuccess({
              razorpayPaymentId: response.razorpay_payment_id,
              razorpayOrderId: response.razorpay_order_id,
              razorpaySignature: response.razorpay_signature
            });
            setTxId(response.razorpay_payment_id);
            setStep('success');
          } catch (err) {
            setErrorMsg(err.response?.data || err.message || 'Payment verification failed.');
            setStep('choose');
          }
        },
        prefill: {
          name: user ? user.name : "",
          email: user ? user.email : "",
          contact: user ? user.phone || "" : ""
        },
        notes: {
          address: "Mandi+ Office, Nashik, Maharashtra, India"
        },
        theme: {
          color: "#043e2b" // Mandi+ theme dark green
        },
        modal: {
          ondismiss: function () {
            setErrorMsg('Payment overlay dismissed by user.');
            setStep('choose');
          }
        }
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (err) {
      setErrorMsg(err.response?.data || err.message || 'Failed to initiate secure checkout.');
      setStep('choose');
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md flex items-center justify-center p-3 sm:p-4 z-[100] animate-fade-in font-sans">
      
      <div className="bg-white rounded-[32px] sm:rounded-[36px] max-w-lg w-full overflow-hidden border border-slate-100 shadow-2xl relative flex flex-col max-h-[90vh]">
        
        {/* Header bar */}
        <div className="bg-gradient-to-r from-agrogreen-950 to-slate-900 text-white px-5 sm:px-8 py-4 sm:py-6 relative text-left">
          <h3 className="text-lg font-black tracking-tight">{t('paymentGateway')}</h3>
          <p className="text-[10px] text-agrogreen-200/80 font-bold uppercase tracking-wider mt-0.5">{t('paymentGatewaySubtitle')}</p>
          <div className="absolute right-4 sm:right-6 top-1/2 -translate-y-1/2 flex items-center space-x-1.5 bg-white/10 px-2.5 sm:px-3 py-1 rounded-full border border-white/10 backdrop-blur-sm">
            <ShieldCheck className="h-4 w-4 text-agrogreen-400" />
            <span className="text-[9px] font-black tracking-wider uppercase">Secure 256-Bit SSL</span>
          </div>
        </div>

        {/* Amount Display */}
        <div className="bg-slate-50 px-5 sm:px-8 py-3 sm:py-4 border-b border-slate-100 flex justify-between items-center text-left">
          <div>
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{t('paymentAmount')}</span>
            <span className="block text-xl sm:text-2xl font-black text-slate-800">₹{amount.toLocaleString('en-IN')}</span>
          </div>
          <div className="text-right">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Security</span>
            <span className="inline-flex items-center space-x-1 bg-emerald-50 border border-emerald-100 rounded-full px-2.5 py-1 mt-0.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
              <span className="text-[9px] font-extrabold text-emerald-600 uppercase">Escrow Hold</span>
            </span>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-5 sm:p-8 text-left scrollbar-thin">
          
          {/* STEP 1: CHOOSE PAYMENT METHOD (LAUNCH RAZORPAY) */}
          {step === 'choose' && (
            <div className="space-y-6">
              
              <div className="bg-slate-50 border border-slate-100 rounded-2xl p-4 sm:p-5 flex items-start space-x-3.5">
                <Info className="h-5 w-5 text-agrogreen-700 shrink-0 mt-0.5" />
                <div className="space-y-1 text-xs text-slate-600 leading-relaxed">
                  <span className="font-extrabold text-slate-850 block">About Mandi+ Escrow Protection</span>
                  <p>
                    Funds are safely locked in an escrow account during transit. The farmer will be paid only after you verify and confirm receipt of the crops.
                  </p>
                </div>
              </div>

              {errorMsg && (
                <div className="bg-rose-50 border border-rose-100 text-rose-600 p-4 rounded-2xl text-xs font-bold flex items-center space-x-2">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}

              <div className="space-y-4">
                <span className="block text-[10px] text-slate-400 font-bold uppercase tracking-wider">Accepted Payment Methods</span>
                <div className="grid grid-cols-2 min-[380px]:grid-cols-4 gap-2.5 sm:gap-3">
                  <div className="flex flex-col items-center justify-center p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 text-slate-500">
                    <CreditCard className="h-5 w-5 text-slate-400 mb-1" />
                    <span className="text-[9px] font-bold">Cards</span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 text-slate-500">
                    <Smartphone className="h-5 w-5 text-slate-400 mb-1" />
                    <span className="text-[9px] font-bold">UPI / QR</span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 text-slate-500">
                    <Smartphone className="h-5 w-5 text-slate-400 mb-1 rotate-90" />
                    <span className="text-[9px] font-bold">Netbanking</span>
                  </div>
                  <div className="flex flex-col items-center justify-center p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 text-slate-500">
                    <ShieldCheck className="h-5 w-5 text-slate-400 mb-1" />
                    <span className="text-[9px] font-bold">Wallets</span>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 pt-4">
                <button
                  type="button" onClick={onClose}
                  className="border border-slate-200 hover:bg-slate-50 py-3.5 rounded-xl font-bold text-xs text-center text-slate-600 transition-colors"
                >
                  {t('cancel')}
                </button>
                <button
                  type="button"
                  onClick={handlePaymentSubmit}
                  className="bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-3.5 rounded-xl text-xs transition-colors flex items-center justify-center space-x-1.5 shadow-md shadow-slate-100 animate-pulse-slow"
                >
                  <ShieldCheck className="h-4.5 w-4.5 text-agrogreen-400" />
                  <span>{t('paySecurely')}</span>
                </button>
              </div>

            </div>
          )}

          {/* STEP 2: AUTHENTICATING / SCRIPT LOADING */}
          {step === 'authenticating' && (
            <div className="py-16 text-center space-y-6 animate-pulse-slow">
              <div className="relative w-16 h-16 mx-auto">
                <div className="w-16 h-16 border-4 border-slate-100 border-t-agrogreen-700 rounded-full animate-spin"></div>
              </div>
              <div className="space-y-1">
                <h4 className="font-extrabold text-slate-800 text-base">{t('authenticating')}</h4>
                <p className="text-xs text-slate-400">Loading Razorpay Secure Overlay...</p>
              </div>
            </div>
          )}

          {/* STEP 3: VERIFYING GATEWAY PROCESSING */}
          {step === 'verifying' && (
            <div className="py-16 text-center space-y-6">
              <div className="relative w-16 h-16 mx-auto">
                <div className="w-16 h-16 border-4 border-slate-100 border-t-emerald-600 rounded-full animate-spin"></div>
              </div>
              <div className="space-y-1">
                <h4 className="font-extrabold text-slate-800 text-base">{t('verifying')}</h4>
                <p className="text-xs text-slate-400">Confirming signature and allocating escrow reserves...</p>
              </div>
            </div>
          )}

          {/* STEP 4: SUCCESS TICKET SCREEN */}
          {step === 'success' && (
            <div className="py-6 text-center space-y-6">
              
              <div className="bg-emerald-50 text-emerald-600 h-16 w-16 rounded-full flex items-center justify-center mx-auto border border-emerald-100 shadow-sm animate-bounce">
                <CheckCircle2 className="h-10 w-10" />
              </div>

              <div className="space-y-2">
                <h4 className="text-xl font-black text-slate-800 tracking-tight">{t('txSuccess')}</h4>
                <p className="text-xs text-slate-500 max-w-sm mx-auto leading-relaxed">{t('txSuccessSubtitle')}</p>
              </div>

              <div className="bg-slate-50 border border-slate-100 rounded-3xl p-5 max-w-sm mx-auto space-y-3.5 text-xs text-slate-600 font-medium">
                <div className="flex justify-between items-center pb-2.5 border-b border-slate-150">
                  <span className="font-bold text-slate-400 uppercase tracking-wide text-[9px]">{t('txId')}</span>
                  <span className="font-mono font-black text-slate-850 truncate max-w-[180px]">{txId}</span>
                </div>
                
                <div className="flex justify-between items-center text-slate-500">
                  <span>Grand Total Paid:</span>
                  <span className="font-extrabold text-slate-850 text-sm">₹{amount.toLocaleString('en-IN')}</span>
                </div>

                <div className="flex justify-between items-center text-slate-500">
                  <span>Merchant Escrow Node:</span>
                  <span className="font-bold text-slate-700">Mandi+ Escrow Node</span>
                </div>
              </div>

              <div className="pt-4 max-w-sm mx-auto">
                <button
                  type="button"
                  onClick={() => {
                    onClose();
                    // Force redirect/refresh of orders list
                    window.location.reload();
                  }}
                  className="w-full bg-agrogreen-950 hover:bg-agrogreen-900 text-white font-bold py-4 rounded-xl text-xs transition-colors shadow-md shadow-slate-100"
                >
                  {t('viewOrders')}
                </button>
              </div>

            </div>
          )}

        </div>
      </div>
    </div>
  );
}
