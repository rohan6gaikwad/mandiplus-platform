import React from 'react';
import { Link } from 'react-router-dom';
import { Globe, Share2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-agrogreen-950 text-white border-t border-agrogreen-900 pt-16 pb-12 font-sans">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 pb-12 border-b border-agrogreen-900">
          
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-6">
            <Link to="/" className="inline-block group">
              <div className="bg-white rounded-2xl p-2.5 shadow-md inline-flex items-center justify-center hover:shadow-lg transition-all duration-300 group-hover:scale-[1.02]">
                <img 
                  src="/images/logo.png" 
                  alt="Mandi+ Market Solutions" 
                  className="h-12 w-auto object-contain" 
                />
              </div>
            </Link>
            <p className="text-agrogreen-200/70 text-sm max-w-sm leading-relaxed">
              Empowering Indian Agriculture through direct marketplace connections, smart temperature-controlled logistics, and real-time mandi data insights.
            </p>
            <div className="flex space-x-4">
              <a href="#" className="bg-agrogreen-900/60 hover:bg-agrogreen-800 p-2.5 rounded-lg text-agrogreen-200 transition-colors">
                <Globe className="h-5 w-5" />
              </a>
              <a href="#" className="bg-agrogreen-900/60 hover:bg-agrogreen-800 p-2.5 rounded-lg text-agrogreen-200 transition-colors">
                <Share2 className="h-5 w-5" />
              </a>
            </div>
          </div>

          {/* Links Column 1 */}
          <div>
            <h4 className="text-agrogreen-400 font-bold uppercase tracking-wider text-xs mb-6">Marketplace & Resources</h4>
            <ul className="space-y-3.5 text-sm text-agrogreen-200/80">
              <li>
                <Link to="/services" className="hover:text-white transition-colors">Buy Crops</Link>
              </li>
              <li>
                <Link to="/services" className="hover:text-white transition-colors">Sell Produce</Link>
              </li>
              <li>
                <Link to="/services" className="hover:text-white transition-colors">Mandi rates API</Link>
              </li>
              <li>
                <Link to="/services" className="hover:text-white transition-colors">Cold Storage Units</Link>
              </li>
            </ul>
          </div>

          {/* Links Column 2 */}
          <div>
            <h4 className="text-agrogreen-400 font-bold uppercase tracking-wider text-xs mb-6">Legal & Support</h4>
            <ul className="space-y-3.5 text-sm text-agrogreen-200/80">
              <li>
                <Link to="/contact" className="hover:text-white transition-colors">Support & Help</Link>
              </li>
              <li>
                <Link to="/about" className="hover:text-white transition-colors">Partner Program</Link>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">Terms of Service</a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom copyright */}
        <div className="flex flex-col sm:flex-row justify-between items-center pt-8 text-xs text-agrogreen-300/60">
          <p>© 2026 Mandi+ Technologies. Empowering Indian Agriculture.</p>
          <p className="mt-2 sm:mt-0 font-medium">Mandi+ Office, Nashik, Maharashtra</p>
        </div>
      </div>
    </footer>
  );
}
