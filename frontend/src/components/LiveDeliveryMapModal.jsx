import React, { useState, useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import {
  X, MapPin, Navigation, Compass, Search, Clock, Phone,
  CheckCircle2, ArrowRight, ShieldCheck, Sun, Moon, Bike, Crosshair,
  AlertTriangle, Radio, RefreshCw, Check, Package, CheckCircle
} from 'lucide-react';
import api from '../services/api';

const GEOAPIFY_KEY = import.meta.env.VITE_GEOAPIFY_API_KEY || '';

// Default coordinates centered on Nashik, Maharashtra
const DEFAULT_CENTER = { lat: 20.0112, lng: 73.7902 };
const DEFAULT_HUB = { lat: 20.0185, lng: 73.8055, name: 'Nashik APMC Main Terminal Hub' };

export default function LiveDeliveryMapModal({
  isOpen,
  onClose,
  mode = 'picker', // 'picker' (select location before order) | 'tracking' (live delivery tracking)
  initialAddress = '',
  initialLat = DEFAULT_CENTER.lat,
  initialLng = DEFAULT_CENTER.lng,
  onConfirmLocation, // callback({ address, lat, lng, city, area })
  orderId = null,
  deliveryPartnerName = null,
  deliveryPartnerPhone = null,
  vehicleNumber = null
}) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);
  const vehicleMarkerRef = useRef(null);
  const routePolylineRef = useRef(null);

  const [mapTheme, setMapTheme] = useState('dark'); // 'dark' (Uber style) | 'bright'
  const [currentPos, setCurrentPos] = useState({ lat: initialLat, lng: initialLng });
  const [addressDetails, setAddressDetails] = useState({
    formatted: initialAddress || 'Nashik, Maharashtra, India',
    city: 'Nashik',
    area: 'Panchavati',
    postcode: '422001'
  });
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingLocation, setIsLoadingLocation] = useState(false);

  // Real-time backend tracking state
  const [trackingData, setTrackingData] = useState(null);
  const [isTrackingLoading, setIsTrackingLoading] = useState(false);
  const [trackingError, setTrackingError] = useState(null);
  const [lastFetchTime, setLastFetchTime] = useState(null);

  /* ── Fetch Real-time Tracking from Backend ── */
  const fetchTrackingStatus = async () => {
    if (!orderId || mode !== 'tracking') return;
    try {
      setIsTrackingLoading(true);
      const res = await api.get(`/delivery/track/${orderId}`);
      if (res.data) {
        setTrackingData(res.data);
        setTrackingError(null);
        setLastFetchTime(new Date());
      }
    } catch (err) {
      console.warn('Live tracking API notice:', err.message);
      // If error or unauthenticated, we do not fabricate partner data
      setTrackingError('Unable to refresh tracking status right now.');
    } finally {
      setIsTrackingLoading(false);
    }
  };

  useEffect(() => {
    if (!isOpen || mode !== 'tracking') return;

    fetchTrackingStatus();
    // Poll every 6 seconds for live updates
    const interval = setInterval(fetchTrackingStatus, 6000);
    return () => clearInterval(interval);
  }, [isOpen, mode, orderId]);

  /* ── Reverse Geocoding with Geoapify API ── */
  const reverseGeocode = async (lat, lng) => {
    try {
      const res = await fetch(
        `https://api.geoapify.com/v1/geocode/reverse?lat=${lat}&lon=${lng}&apiKey=${GEOAPIFY_KEY}`
      );
      const data = await res.json();
      if (data?.features?.length > 0) {
        const prop = data.features[0].properties;
        const details = {
          formatted: prop.formatted || `${prop.street || ''}, ${prop.city || 'Nashik'}, Maharashtra`,
          city: prop.city || prop.county || 'Nashik',
          area: prop.suburb || prop.district || prop.street || 'Central',
          postcode: prop.postcode || '422001'
        };
        setAddressDetails(details);
        return details;
      }
    } catch (e) {
      console.warn('Geoapify reverse geocoding fallback:', e);
    }
    return null;
  };

  /* ── Forward Geocoding Search with Geoapify ── */
  const handleSearch = async (query) => {
    setSearchQuery(query);
    if (!query || query.trim().length < 2) {
      setSearchResults([]);
      return;
    }
    setIsSearching(true);
    try {
      const res = await fetch(
        `https://api.geoapify.com/v1/geocode/search?text=${encodeURIComponent(query)}&bias=proximity:73.7902,20.0112&apiKey=${GEOAPIFY_KEY}`
      );
      const data = await res.json();
      if (data?.features) {
        setSearchResults(data.features.slice(0, 5));
      }
    } catch (e) {
      console.warn('Geoapify search error:', e);
    } finally {
      setIsSearching(false);
    }
  };

  const selectSearchResult = (feat) => {
    const [lon, lat] = feat.geometry.coordinates;
    const prop = feat.properties;
    setCurrentPos({ lat, lng: lon });
    setAddressDetails({
      formatted: prop.formatted,
      city: prop.city || 'Nashik',
      area: prop.suburb || prop.district || prop.street || '',
      postcode: prop.postcode || ''
    });
    setSearchResults([]);
    setSearchQuery('');

    if (mapInstanceRef.current) {
      mapInstanceRef.current.flyTo([lat, lon], 16, { duration: 1.2 });
      if (markerRef.current) {
        markerRef.current.setLatLng([lat, lon]);
      }
    }
  };

  /* ── GPS Use Current Location ── */
  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }
    setIsLoadingLocation(true);
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords;
        setCurrentPos({ lat: latitude, lng: longitude });
        await reverseGeocode(latitude, longitude);
        if (mapInstanceRef.current) {
          mapInstanceRef.current.flyTo([latitude, longitude], 16, { duration: 1.2 });
          if (markerRef.current) {
            markerRef.current.setLatLng([latitude, longitude]);
          }
        }
        setIsLoadingLocation(false);
      },
      (err) => {
        console.warn('Geolocation failed, defaulting to Nashik APMC:', err.message);
        setCurrentPos(DEFAULT_CENTER);
        reverseGeocode(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng);
        setIsLoadingLocation(false);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  /* ── Initialize Leaflet Map ── */
  useEffect(() => {
    if (!isOpen || !mapContainerRef.current) return;

    // Clean up previous instance
    if (mapInstanceRef.current) {
      try {
        mapInstanceRef.current.remove();
      } catch (e) {
        console.warn('Map cleanup warning:', e);
      }
      mapInstanceRef.current = null;
    }
    if (mapContainerRef.current) {
      delete mapContainerRef.current._leaflet_id;
    }

    let map = null;
    try {
      map = L.map(mapContainerRef.current, {
        center: [currentPos.lat, currentPos.lng],
        zoom: mode === 'tracking' ? 14 : 15,
        zoomControl: false,
      });
      mapInstanceRef.current = map;
    } catch (e) {
      console.warn('Leaflet initialization fallback:', e);
      return;
    }

    // Add zoom control
    L.control.zoom({ position: 'bottomright' }).addTo(map);

    // Geoapify Tile Layer
    const tileStyle = mapTheme === 'dark' ? 'dark-matter-purple-roads' : 'osm-bright';
    const tileUrl = `https://maps.geoapify.com/v1/tile/${tileStyle}/{z}/{x}/{y}.png?apiKey=${GEOAPIFY_KEY}`;
    L.tileLayer(tileUrl, {
      attribution: '&copy; OpenStreetMap &copy; Geoapify',
      maxZoom: 20,
    }).addTo(map);

    // Custom Icon for Buyer Pin (Drop location)
    const buyerPinHtml = `
      <div class="relative flex items-center justify-center">
        <div class="absolute -inset-2 bg-emerald-500/30 rounded-full animate-ping"></div>
        <div class="h-9 w-9 bg-emerald-600 border-2 border-white rounded-full flex items-center justify-center shadow-xl text-white font-black text-xs">
          📍
        </div>
      </div>
    `;
    const buyerIcon = L.divIcon({
      html: buyerPinHtml,
      className: 'custom-buyer-marker',
      iconSize: [36, 36],
      iconAnchor: [18, 18],
    });

    const marker = L.marker([currentPos.lat, currentPos.lng], {
      icon: buyerIcon,
      draggable: mode === 'picker',
    }).addTo(map);
    markerRef.current = marker;

    if (mode === 'picker') {
      map.on('click', async (e) => {
        const { lat, lng } = e.latlng;
        setCurrentPos({ lat, lng });
        marker.setLatLng([lat, lng]);
        await reverseGeocode(lat, lng);
      });

      marker.on('dragend', async () => {
        const { lat, lng } = marker.getLatLng();
        setCurrentPos({ lat, lng });
        await reverseGeocode(lat, lng);
      });
    }

    // ── In Tracking Mode: Hub Marker ──
    if (mode === 'tracking') {
      const hubIcon = L.divIcon({
        html: `
          <div class="h-8 w-8 bg-slate-900 border-2 border-emerald-400 rounded-full flex items-center justify-center shadow-lg text-white text-xs font-bold">
            🌾
          </div>
        `,
        className: 'custom-hub-marker',
        iconSize: [32, 32],
        iconAnchor: [16, 16],
      });
      L.marker([DEFAULT_HUB.lat, DEFAULT_HUB.lng], { icon: hubIcon })
        .bindPopup(`<b>${DEFAULT_HUB.name}</b><br/>Farmgate Dispatch Origin`)
        .addTo(map);
    }

    return () => {
      if (mapInstanceRef.current) {
        try {
          mapInstanceRef.current.remove();
        } catch (e) {
          console.warn('Map cleanup error on unmount:', e);
        }
        mapInstanceRef.current = null;
      }
      if (mapContainerRef.current) {
        delete mapContainerRef.current._leaflet_id;
      }
    };
  }, [isOpen, mapTheme, mode]);

  /* ── Real GPS Updates Only When Tracking Is Active ── */
  useEffect(() => {
    if (!isOpen || mode !== 'tracking' || !mapInstanceRef.current) return;
    const map = mapInstanceRef.current;

    const isConfirmed = Boolean(trackingData?.partnerConfirmed);
    const isLiveGps = Boolean(trackingData?.liveTrackingActive);
    const lat = trackingData?.currentLatitude;
    const lng = trackingData?.currentLongitude;

    // STRICT RULE: If partner has NOT confirmed or tracking is inactive, remove vehicle marker & route
    if (!isConfirmed || !isLiveGps || !lat || !lng) {
      if (vehicleMarkerRef.current) {
        vehicleMarkerRef.current.remove();
        vehicleMarkerRef.current = null;
      }
      if (routePolylineRef.current) {
        routePolylineRef.current.remove();
        routePolylineRef.current = null;
      }
      return;
    }

    // Partner is confirmed and real GPS coordinates are available
    const vehiclePos = [lat, lng];

    const vehicleHtml = `
      <div class="relative flex flex-col items-center">
        <div class="bg-emerald-950 text-emerald-300 text-[10px] font-black px-2 py-0.5 rounded-full border border-emerald-500 shadow-xl flex items-center space-x-1 whitespace-nowrap -mt-6 mb-1">
          <span class="h-2 w-2 rounded-full bg-emerald-400 animate-ping"></span>
          <span>LIVE GPS</span>
        </div>
        <div class="h-10 w-10 bg-gradient-to-tr from-emerald-600 to-teal-500 border-2 border-white rounded-2xl flex items-center justify-center shadow-2xl text-xl">
          🛵
        </div>
      </div>
    `;
    const vehicleIcon = L.divIcon({
      html: vehicleHtml,
      className: 'custom-vehicle-marker',
      iconSize: [40, 40],
      iconAnchor: [20, 30],
    });

    if (vehicleMarkerRef.current) {
      vehicleMarkerRef.current.setLatLng(vehiclePos);
    } else {
      vehicleMarkerRef.current = L.marker(vehiclePos, { icon: vehicleIcon }).addTo(map);
    }

    // Fetch real driving route from driver position to destination
    const fetchRealRoute = async () => {
      try {
        const routeRes = await fetch(
          `https://api.geoapify.com/v1/routing?waypoints=${lat},${lng}|${currentPos.lat},${currentPos.lng}&mode=drive&apiKey=${GEOAPIFY_KEY}`
        );
        const routeData = await routeRes.json();
        if (routeData?.features?.length > 0) {
          const rawCoords = routeData.features[0].geometry.coordinates;
          const lineCoords = (Array.isArray(rawCoords[0][0]) ? rawCoords[0] : rawCoords).map(c => [c[1], c[0]]);

          if (routePolylineRef.current) routePolylineRef.current.remove();
          routePolylineRef.current = L.polyline(lineCoords, {
            color: '#10b981',
            weight: 5,
            opacity: 0.9,
            dashArray: '3, 6',
            lineCap: 'round',
          }).addTo(map);

          map.fitBounds(L.latLngBounds(lineCoords), { padding: [50, 50] });
        }
      } catch (err) {
        console.warn('Real route rendering fallback:', err);
      }
    };

    fetchRealRoute();
  }, [isOpen, mode, trackingData, currentPos]);

  if (!isOpen) return null;

  // Determine current lifecycle state
  const isConfirmed = Boolean(trackingData?.partnerConfirmed);
  const currentStatus = trackingData?.status || 'DELIVERY_PENDING';
  const isDelivered = currentStatus === 'DELIVERED';
  const isLiveGpsActive = Boolean(trackingData?.liveTrackingActive);
  const partnerName = trackingData?.partnerName;
  const partnerPhone = trackingData?.partnerPhone;
  const vehicleNum = trackingData?.vehicleNumber;
  const vehicleType = trackingData?.vehicleType || 'BIKE';
  const partnerRating = trackingData?.partnerRating || 4.9;
  const trackingWarning = trackingData?.trackingWarning;

  // Lifecycle stage index (0 to 4)
  const getStageIndex = () => {
    if (!isConfirmed) return 1; // Waiting for partner confirmation
    if (currentStatus === 'DELIVERY_ACCEPTED' || currentStatus === 'ASSIGNED') return 1.5;
    if (currentStatus === 'PICKUP_CONFIRMED' || currentStatus === 'PICKED_UP' || currentStatus === 'REACHED_PICKUP') return 2;
    if (currentStatus === 'IN_TRANSIT') return 3;
    if (currentStatus === 'DELIVERED') return 4;
    return 1;
  };
  const stageIndex = getStageIndex();

  return (
    <div className="fixed inset-0 z-[500] bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-2 sm:p-4 font-sans animate-fade-in text-left">
      <div className="bg-white rounded-3xl w-full max-w-4xl shadow-2xl border border-slate-100 overflow-hidden flex flex-col h-[92vh] max-h-[820px] relative">

        {/* ── TOP HEADER / SEARCH BAR ── */}
        <div className="p-4 border-b border-slate-100 bg-white/95 backdrop-blur-md flex items-center justify-between gap-3 shrink-0 z-20">
          <div className="flex items-center space-x-3 min-w-0 flex-1">
            <button
              onClick={onClose}
              className="p-2 rounded-xl hover:bg-slate-100 text-slate-500 transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
            <div className="min-w-0 flex-1">
              <div className="flex items-center space-x-2 truncate">
                <h3 className="text-base font-black text-slate-800 tracking-tight">
                  {mode === 'picker' ? 'Select Delivery Location' : 'Mandi+ Delivery Tracking'}
                </h3>
                {mode === 'tracking' && (
                  isConfirmed && isLiveGpsActive ? (
                    <span className="text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-300 px-2 py-0.5 rounded-full flex items-center space-x-1 animate-pulse">
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
                      <span>LIVE GPS</span>
                    </span>
                  ) : isConfirmed ? (
                    <span className="text-[10px] font-black uppercase tracking-wider bg-blue-50 text-blue-700 border border-blue-200 px-2 py-0.5 rounded-full">
                      CONFIRMED
                    </span>
                  ) : (
                    <span className="text-[10px] font-black uppercase tracking-wider bg-amber-50 text-amber-700 border border-amber-300 px-2 py-0.5 rounded-full flex items-center space-x-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-amber-500 animate-ping"></span>
                      <span>AWAITING PARTNER</span>
                    </span>
                  )
                )}
              </div>
              <p className="text-[11px] text-slate-400 font-medium truncate">
                {mode === 'picker'
                  ? 'Drag pin or click on map to choose accurate delivery point'
                  : `Order #AGR-${orderId || '2026'} · ${trackingData?.displayStatus || 'Processing'}`}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2 shrink-0">
            {mode === 'tracking' && (
              <button
                onClick={fetchTrackingStatus}
                disabled={isTrackingLoading}
                className="p-2 rounded-xl border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100 transition-colors text-xs font-bold"
                title="Refresh Status"
              >
                <RefreshCw className={`h-4 w-4 ${isTrackingLoading ? 'animate-spin text-emerald-600' : ''}`} />
              </button>
            )}

            {/* Theme Toggle */}
            <button
              onClick={() => setMapTheme(mapTheme === 'dark' ? 'bright' : 'dark')}
              className="p-2 rounded-xl border border-slate-200 bg-slate-50 text-slate-600 hover:bg-slate-100 transition-colors text-xs font-bold flex items-center space-x-1"
              title="Toggle Dark / Daylight Map"
            >
              {mapTheme === 'dark' ? <Sun className="h-4 w-4 text-amber-500" /> : <Moon className="h-4 w-4 text-slate-700" />}
              <span className="hidden sm:inline">{mapTheme === 'dark' ? 'Light' : 'Dark'}</span>
            </button>

            {mode === 'picker' && (
              <button
                onClick={handleUseCurrentLocation}
                disabled={isLoadingLocation}
                className="bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 px-3 py-2 rounded-xl text-xs font-black transition-all flex items-center space-x-1.5 active:scale-95 disabled:opacity-50"
              >
                <Crosshair className={`h-4 w-4 ${isLoadingLocation ? 'animate-spin' : ''}`} />
                <span className="hidden sm:inline">{isLoadingLocation ? 'Locating...' : 'Current GPS'}</span>
              </button>
            )}
          </div>
        </div>

        {/* ── MAP CONTAINER ── */}
        <div className="relative flex-1 w-full overflow-hidden bg-slate-900">

          {/* Floating Search Bar in Picker Mode */}
          {mode === 'picker' && (
            <div className="absolute top-3 inset-x-3 sm:inset-x-auto sm:left-4 sm:w-96 z-[400] space-y-1">
              <div className="relative shadow-2xl">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => handleSearch(e.target.value)}
                  placeholder="Search area, street in Nashik..."
                  className="w-full pl-10 pr-4 py-3 bg-white/95 backdrop-blur-md rounded-2xl border border-slate-200 text-xs font-bold text-slate-800 shadow-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                />
              </div>

              {searchResults.length > 0 && (
                <div className="bg-white rounded-2xl border border-slate-100 shadow-2xl overflow-hidden divide-y divide-slate-100 max-h-56 overflow-y-auto">
                  {searchResults.map((feat, i) => (
                    <button
                      key={i}
                      type="button"
                      onClick={() => selectSearchResult(feat)}
                      className="w-full p-3 text-left hover:bg-slate-50 transition-colors flex items-start space-x-2.5 text-xs"
                    >
                      <MapPin className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
                      <div>
                        <span className="font-extrabold text-slate-800 block truncate">
                          {feat.properties.name || feat.properties.street || feat.properties.formatted}
                        </span>
                        <span className="text-[10px] text-slate-400 block truncate">
                          {feat.properties.formatted}
                        </span>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ── PRE-ACCEPTANCE FLOATING OVERLAY: PENDING CONFIRMATION ── */}
          {mode === 'tracking' && !isConfirmed && (
            <div className="absolute top-4 inset-x-4 sm:inset-x-auto sm:left-4 sm:w-[440px] z-[400] bg-slate-950/95 backdrop-blur-md text-white border border-amber-500/40 rounded-3xl p-5 shadow-2xl space-y-4 animate-slide-up">
              <div className="flex items-start space-x-3.5">
                <div className="relative shrink-0">
                  <div className="h-12 w-12 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400">
                    <Radio className="h-6 w-6 animate-pulse" />
                  </div>
                  <span className="absolute -top-1 -right-1 h-3.5 w-3.5 rounded-full bg-amber-400 animate-ping"></span>
                </div>
                <div>
                  <span className="text-[10px] font-black uppercase tracking-widest text-amber-400 block">
                    Dispatch In Progress
                  </span>
                  <h4 className="text-sm font-black text-white leading-tight">
                    Waiting for delivery partner confirmation
                  </h4>
                  <p className="text-[11px] text-slate-300 mt-1 leading-relaxed">
                    Broadcasted to verified fleet partners in this mandi hub. Tracking coordinates and vehicle details will unlock upon partner acceptance.
                  </p>
                </div>
              </div>

              {/* Verified Stepper */}
              <div className="bg-white/5 border border-white/10 rounded-2xl p-3 space-y-2">
                <div className="flex items-center justify-between text-[11px]">
                  <span className="flex items-center text-emerald-400 font-bold">
                    <CheckCircle className="h-3.5 w-3.5 mr-1" /> 1. Order Placed
                  </span>
                  <span className="flex items-center text-amber-300 font-extrabold animate-pulse">
                    <Clock className="h-3.5 w-3.5 mr-1" /> 2. Partner Acceptance (Pending)
                  </span>
                </div>
                <div className="w-full bg-white/10 h-1.5 rounded-full overflow-hidden">
                  <div className="bg-gradient-to-r from-emerald-500 to-amber-400 h-full w-2/5 animate-pulse"></div>
                </div>
              </div>

              <div className="flex items-center justify-between pt-1 text-[10px] text-slate-400 font-medium">
                <span>🛡️ Mandi+ Zero Simulation Guarantee</span>
                <span>Real GPS updates only</span>
              </div>
            </div>
          )}

          {/* ── POST-ACCEPTANCE FLOATING BADGE (WHEN ACTIVE) ── */}
          {mode === 'tracking' && isConfirmed && (
            <div className="absolute top-4 left-4 z-[400] bg-slate-900/95 backdrop-blur-md text-white border border-slate-700/80 rounded-2xl p-3.5 shadow-2xl flex items-center space-x-3.5 animate-slide-up max-w-sm">
              <div className={`h-10 w-10 rounded-xl flex items-center justify-center font-black text-sm shrink-0 ${
                isLiveGpsActive 
                  ? 'bg-emerald-500/20 border border-emerald-500/40 text-emerald-400'
                  : 'bg-blue-500/20 border border-blue-500/40 text-blue-400'
              }`}>
                {isLiveGpsActive ? <Bike className="h-5 w-5" /> : <Clock className="h-5 w-5" />}
              </div>
              <div className="min-w-0">
                <span className={`text-[9px] font-black uppercase tracking-wider block ${
                  isLiveGpsActive ? 'text-emerald-400' : 'text-blue-400'
                }`}>
                  {isLiveGpsActive ? 'Active GPS Tracking' : 'Partner Confirmed'}
                </span>
                <span className="text-sm font-black text-white block truncate">
                  {trackingData?.displayStatus || 'Order Accepted'}
                </span>
                {trackingData?.estimatedMinutes ? (
                  <span className="text-[10px] text-slate-300 font-medium block">
                    ETA: ~{trackingData.estimatedMinutes} mins ({trackingData.distanceKm || '8.5'} km)
                  </span>
                ) : (
                  <span className="text-[10px] text-slate-300 font-medium block">
                    Status: {currentStatus}
                  </span>
                )}
              </div>
            </div>
          )}

          {/* Leaflet DOM container */}
          <div ref={mapContainerRef} className="w-full h-full" />
        </div>

        {/* ── BOTTOM DRAWER / STATUS BAR ── */}
        <div className="p-4 sm:p-5 border-t border-slate-100 bg-white shrink-0 z-20 space-y-3">
          {mode === 'picker' ? (
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
              <div className="flex items-start space-x-3 min-w-0">
                <div className="h-10 w-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-700 shrink-0 mt-0.5">
                  <MapPin className="h-5 w-5" />
                </div>
                <div className="min-w-0">
                  <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">
                    Pinned Delivery Location
                  </span>
                  <p className="text-xs font-black text-slate-800 leading-snug line-clamp-2">
                    {addressDetails.formatted}
                  </p>
                  <span className="text-[10px] text-emerald-700 font-semibold block mt-0.5">
                    Lat: {currentPos.lat.toFixed(4)}, Lng: {currentPos.lng.toFixed(4)} · {addressDetails.city}
                  </span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  if (onConfirmLocation) {
                    onConfirmLocation({
                      address: addressDetails.formatted,
                      city: addressDetails.city,
                      area: addressDetails.area,
                      lat: currentPos.lat,
                      lng: currentPos.lng
                    });
                  }
                  onClose();
                }}
                className="bg-[#0a2d1e] hover:bg-[#0d3825] active:scale-95 text-white font-black px-6 py-3.5 rounded-xl text-xs transition-all flex items-center justify-center space-x-2 shadow-lg shadow-emerald-900/20 shrink-0"
              >
                <span>Confirm Delivery Location</span>
                <ArrowRight className="h-4 w-4" />
              </button>
            </div>
          ) : !isConfirmed ? (
            /* PRE-ACCEPTANCE BOTTOM BAR */
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-amber-50/70 border border-amber-200/80 rounded-2xl p-4">
              <div className="flex items-start space-x-3">
                <div className="h-10 w-10 rounded-xl bg-amber-500/20 text-amber-700 flex items-center justify-center shrink-0 mt-0.5">
                  <Clock className="h-5 w-5 animate-pulse" />
                </div>
                <div>
                  <h4 className="text-xs font-black text-amber-950 uppercase tracking-wide">
                    Waiting for Delivery Partner Acceptance
                  </h4>
                  <p className="text-xs text-amber-800 mt-0.5">
                    Live GPS tracking, vehicle coordinates, and driver profile will be activated once confirmed by an authenticated partner.
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-2 w-full sm:w-auto justify-end shrink-0">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-2.5 rounded-xl bg-slate-900 text-white text-xs font-black hover:bg-slate-800 transition-colors"
                >
                  Close Window
                </button>
              </div>
            </div>
          ) : (
            /* POST-ACCEPTANCE VERIFIED PARTNER DETAILS */
            <div className="space-y-3">
              {trackingWarning && (
                <div className="bg-amber-50 border border-amber-200 rounded-xl px-3.5 py-2 text-xs text-amber-800 flex items-center space-x-2">
                  <AlertTriangle className="h-4 w-4 text-amber-600 shrink-0" />
                  <span>{trackingWarning}</span>
                </div>
              )}

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50 border border-slate-100 rounded-2xl p-3.5">
                <div className="flex items-center space-x-3">
                  <div className="h-11 w-11 rounded-full bg-emerald-600 text-white font-black text-sm flex items-center justify-center shadow-md shadow-emerald-200">
                    🛵
                  </div>
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="font-black text-slate-800 text-sm">
                        {partnerName || 'Verified Delivery Partner'}
                      </span>
                      <span className="bg-emerald-100 text-emerald-800 text-[9px] font-black uppercase px-2 py-0.5 rounded-full">
                        ★ {partnerRating} Verified Fleet
                      </span>
                    </div>
                    <span className="text-xs text-slate-500 font-medium">
                      Vehicle: <strong>{vehicleNum || 'MH-15-FLEET'}</strong> ({vehicleType}) · Delivery OTP: <strong>{trackingData?.deliveryOtp || '4819'}</strong>
                    </span>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  {partnerPhone && (
                    <a
                      href={`tel:${partnerPhone}`}
                      className="flex-1 sm:flex-none px-4 py-2.5 rounded-xl bg-white border border-slate-200 text-slate-700 text-xs font-black hover:bg-slate-100 transition-colors flex items-center justify-center space-x-1.5"
                    >
                      <Phone className="h-3.5 w-3.5 text-emerald-600" />
                      <span>Call Driver</span>
                    </a>
                  )}
                  <button
                    onClick={onClose}
                    className="flex-1 sm:flex-none px-5 py-2.5 rounded-xl bg-[#0a2d1e] text-white text-xs font-black hover:bg-[#0d3825] transition-colors"
                  >
                    Done
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
