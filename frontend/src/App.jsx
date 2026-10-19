import React, { Component } from 'react';
import { HashRouter as Router } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import AppRoutes from './routes/AppRoutes';
import WhatsAppChatWidget from './components/WhatsAppChatWidget';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Mandi+ ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReset = () => {
    try {
      localStorage.removeItem('mandiplus_cart');
    } catch (_) {}
    this.setState({ hasError: false, error: null });
    window.location.hash = '#/';
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6 font-sans text-center">
          <div className="bg-white rounded-3xl p-8 max-w-lg w-full border border-slate-200 shadow-xl space-y-4">
            <div className="h-16 w-16 rounded-2xl bg-rose-50 text-rose-600 border border-rose-100 flex items-center justify-center mx-auto text-3xl font-black">
              ⚠️
            </div>
            <h2 className="text-xl font-black text-slate-800">Something went wrong</h2>
            <p className="text-xs text-slate-500 font-medium">
              An unexpected application error occurred. You can safely reload the view or reset local cache to continue.
            </p>
            {this.state.error && (
              <div className="text-left bg-slate-900 text-rose-300 p-3.5 rounded-2xl text-[11px] font-mono overflow-auto max-h-36 border border-slate-800">
                <span className="font-bold text-rose-400 block mb-0.5">{this.state.error.name || 'Error'}: {this.state.error.message || String(this.state.error)}</span>
                {this.state.error.stack && (
                  <span className="text-slate-400 text-[10px] whitespace-pre-wrap break-all block opacity-80">{this.state.error.stack.split('\n').slice(0, 4).join('\n')}</span>
                )}
              </div>
            )}
            <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-3 pt-2">
              <button
                onClick={() => {
                  this.setState({ hasError: false, error: null });
                  window.location.hash = '#/buyer';
                  window.location.reload();
                }}
                className="flex-1 bg-[#0a2d1e] hover:bg-[#0d3825] text-white font-black py-3 rounded-xl text-xs transition-all shadow-md"
              >
                Go to Marketplace
              </button>
              <button
                onClick={() => window.location.reload()}
                className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-3 rounded-xl text-xs transition-all"
              >
                Reload Page
              </button>
              <button
                onClick={this.handleReset}
                className="flex-1 bg-rose-50 hover:bg-rose-100 text-rose-700 font-bold py-3 rounded-xl text-xs transition-all border border-rose-200/60"
                title="Clears cart cache and returns to Home"
              >
                Reset Cache
              </button>
            </div>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

export default function App() {
  return (
    <ErrorBoundary>
      <Router>
        <LanguageProvider>
          <AuthProvider>
            <AppRoutes />
            <WhatsAppChatWidget />
          </AuthProvider>
        </LanguageProvider>
      </Router>
    </ErrorBoundary>
  );
}

