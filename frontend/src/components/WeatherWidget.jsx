import React, { useState, useEffect } from 'react';
import { CloudSun, Droplets, CloudRain, MapPin, RefreshCw, Search, Check, X } from 'lucide-react';
import api from '../services/api';

const mapWmoCodeToCondition = (code) => {
  if (code === 0) return 'Clear Sky';
  if (code === 1) return 'Mainly Clear';
  if (code === 2) return 'Partly Cloudy';
  if (code === 3) return 'Overcast';
  if (code === 45 || code === 48) return 'Foggy';
  if (code === 51 || code === 53 || code === 55) return 'Drizzle';
  if (code === 56 || code === 57) return 'Freezing Drizzle';
  if (code === 61 || code === 63 || code === 65) return 'Rain';
  if (code === 66 || code === 67) return 'Freezing Rain';
  if (code === 71 || code === 73 || code === 75) return 'Snow';
  if (code === 77) return 'Snow Grains';
  if (code === 80 || code === 81 || code === 82) return 'Rain Showers';
  if (code === 85 || code === 86) return 'Snow Showers';
  if (code === 95) return 'Thunderstorm';
  if (code === 96 || code === 99) return 'Thunderstorm with Hail';
  return 'Partly Cloudy';
};

export default function WeatherWidget({ locationName = 'Pune, Maharashtra' }) {
  const [weatherData, setWeatherData] = useState({
    location: 'Loading Location...',
    temperatureCelsius: 0,
    conditionText: 'Detecting Weather...',
    humidityPercentage: 0,
    rainProbabilityPercentage: 0
  });
  const [loading, setLoading] = useState(false);
  const [coords, setCoords] = useState(null);

  const fetchWeatherByLocation = async (loc) => {
    setLoading(true);
    try {
      const apiKey = import.meta.env.VITE_OPENWEATHER_API_KEY || '';
      if (!apiKey) {
        throw new Error("OpenWeatherMap API key not configured, falling back to Open-Meteo");
      }
      const weatherUrl = `https://api.openweathermap.org/data/2.5/weather?q=${encodeURIComponent(loc)}&appid=${apiKey}&units=metric`;
      const response = await fetch(weatherUrl);
      if (!response.ok) {
        throw new Error("City not found or OpenWeatherMap failed");
      }
      const data = await response.json();
      
      if (data && data.main) {
        const temp = data.main.temp;
        const humidity = data.main.humidity;
        const mainCondition = data.weather[0].main;
        const description = data.weather[0].description;
        
        let rainProbability = 5;
        if (mainCondition === 'Rain') rainProbability = 85;
        else if (mainCondition === 'Drizzle') rainProbability = 70;
        else if (mainCondition === 'Thunderstorm') rainProbability = 90;
        else if (mainCondition === 'Clouds') rainProbability = 25;

        const condition = description.charAt(0).toUpperCase() + description.slice(1);
        
        setWeatherData({
          location: `${data.name}, ${data.sys.country}`,
          temperatureCelsius: temp,
          conditionText: condition,
          humidityPercentage: humidity,
          rainProbabilityPercentage: rainProbability
        });
      }
    } catch (error) {
      console.warn("Could not fetch real-time weather from OpenWeatherMap by location name, trying client-side Open-Meteo fallback.", error);
      try {
        const geoUrl = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(loc)}&count=1&language=en&format=json`;
        const geoRes = await fetch(geoUrl);
        if (!geoRes.ok) {
          throw new Error("Geocoding failed");
        }
        const geoData = await geoRes.json();
        if (geoData.results && geoData.results.length > 0) {
          const firstResult = geoData.results[0];
          const lat = firstResult.latitude;
          const lon = firstResult.longitude;
          const resolvedLocName = `${firstResult.name}, ${firstResult.admin1 || firstResult.country}`;
          
          const forecastUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,weather_code,precipitation_probability`;
          const forecastRes = await fetch(forecastUrl);
          if (!forecastRes.ok) {
            throw new Error("Forecast failed");
          }
          const forecastData = await forecastRes.json();
          if (forecastData && forecastData.current) {
            const current = forecastData.current;
            setWeatherData({
              location: resolvedLocName,
              temperatureCelsius: current.temperature_2m,
              conditionText: mapWmoCodeToCondition(current.weather_code),
              humidityPercentage: current.relative_humidity_2m || 50,
              rainProbabilityPercentage: current.precipitation_probability || 0
            });
            return;
          }
        }
        throw new Error("No geocoding results found");
      } catch (fallbackError) {
        console.warn("Client-side Open-Meteo fallback failed, trying backend fallback.", fallbackError);
        try {
          const fallbackRes = await api.get(`/public/weather?location=${encodeURIComponent(loc)}`);
          if (fallbackRes.data) {
            setWeatherData(fallbackRes.data);
          }
        } catch (e) {
          console.error("Backend weather fallback also failed", e);
          setWeatherData({
            location: loc,
            temperatureCelsius: 29.0,
            conditionText: 'Offline',
            humidityPercentage: 50,
            rainProbabilityPercentage: 0
          });
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchLocalWeatherByCoords = async (lat, lon) => {
    setLoading(true);
    try {
      const apiKey = import.meta.env.VITE_OPENWEATHER_API_KEY || '';
      if (!apiKey) {
        throw new Error("OpenWeatherMap API key not configured, falling back to Open-Meteo");
      }
      const weatherUrl = `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}&units=metric`;
      const response = await fetch(weatherUrl);
      if (!response.ok) {
        throw new Error("Coordinate lookup failed");
      }
      const data = await response.json();
      
      if (data && data.main) {
        const temp = data.main.temp;
        const humidity = data.main.humidity;
        const mainCondition = data.weather[0].main;
        const description = data.weather[0].description;
        
        let rainProbability = 5;
        if (mainCondition === 'Rain') rainProbability = 85;
        else if (mainCondition === 'Drizzle') rainProbability = 70;
        else if (mainCondition === 'Thunderstorm') rainProbability = 90;
        else if (mainCondition === 'Clouds') rainProbability = 25;

        const condition = description.charAt(0).toUpperCase() + description.slice(1);

        setWeatherData({
          location: `${data.name}, ${data.sys.country}`,
          temperatureCelsius: temp,
          conditionText: condition,
          humidityPercentage: humidity,
          rainProbabilityPercentage: rainProbability
        });

        setCoords({ lat, lon });
      }
    } catch (err) {
      console.warn("Local weather coordinate fetch failed using OpenWeatherMap, trying Open-Meteo fallback...", err);
      try {
        let resolvedLocName = `Lat: ${lat.toFixed(2)}, Lon: ${lon.toFixed(2)}`;
        try {
          const revRes = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`, {
            headers: {
              'User-Agent': 'MandiPlus-App/1.0'
            }
          });
          if (revRes.ok) {
            const revData = await revRes.json();
            if (revData && revData.address) {
              const addr = revData.address;
              const city = addr.city || addr.town || addr.village || addr.suburb || addr.state_district || addr.county;
              const region = addr.state || addr.country;
              if (city) {
                resolvedLocName = `${city}, ${region}`;
              }
            }
          }
        } catch (revErr) {
          console.warn("Nominatim reverse geocoding failed", revErr);
        }

        const forecastUrl = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,weather_code,precipitation_probability`;
        const forecastRes = await fetch(forecastUrl);
        if (!forecastRes.ok) {
          throw new Error("Open-Meteo forecast coordinate query failed");
        }
        const forecastData = await forecastRes.json();
        if (forecastData && forecastData.current) {
          const current = forecastData.current;
          setWeatherData({
            location: resolvedLocName,
            temperatureCelsius: current.temperature_2m,
            conditionText: mapWmoCodeToCondition(current.weather_code),
            humidityPercentage: current.relative_humidity_2m || 50,
            rainProbabilityPercentage: current.precipitation_probability || 0
          });
          setCoords({ lat, lon });
        }
      } catch (fallbackErr) {
        console.error("All geolocated weather fetches failed", fallbackErr);
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchWeatherByIpLookup = async () => {
    try {
      const ipRes = await fetch('https://ipapi.co/json/');
      if (ipRes.ok) {
        const ipData = await ipRes.json();
        if (ipData && ipData.city) {
          const resolvedCity = `${ipData.city}, ${ipData.region || ipData.country_name}`;
          console.log("Resolved local location via ipapi.co:", resolvedCity);
          await fetchWeatherByLocation(resolvedCity);
          return true;
        }
      }
    } catch (e) {
      console.warn("ipapi.co Geolocation lookup failed, trying fallback:", e);
    }

    try {
      const dbIpRes = await fetch('https://api.db-ip.com/v2/free/self');
      if (dbIpRes.ok) {
        const dbIpData = await dbIpRes.json();
        if (dbIpData && dbIpData.city) {
          const resolvedCity = `${dbIpData.city}, ${dbIpData.stateProv || dbIpData.countryName}`;
          console.log("Resolved local location via db-ip.com:", resolvedCity);
          await fetchWeatherByLocation(resolvedCity);
          return true;
        }
      }
    } catch (e) {
      console.warn("db-ip.com Geolocation lookup failed, trying fallback:", e);
    }

    try {
      const ipRes = await fetch('https://freeipapi.com/api/json');
      if (ipRes.ok) {
        const ipData = await ipRes.json();
        if (ipData && ipData.cityName) {
          const resolvedCity = `${ipData.cityName}, ${ipData.regionName || ipData.countryName}`;
          console.log("Resolved local location via freeipapi.com:", resolvedCity);
          await fetchWeatherByLocation(resolvedCity);
          return true;
        }
      }
    } catch (e) {
      console.warn("freeipapi.com Geolocation lookup failed:", e);
    }
    return false;
  };

  const [isSearching, setIsSearching] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const handleRefresh = async () => {
    const preferred = localStorage.getItem('preferred_weather_location');
    if (preferred) {
      fetchWeatherByLocation(preferred);
    } else if (coords) {
      fetchLocalWeatherByCoords(coords.lat, coords.lon);
    } else {
      const ipSuccess = await fetchWeatherByIpLookup();
      if (!ipSuccess) {
        fetchWeatherByLocation(locationName);
      }
    }
  };

  const handleSearchSubmit = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setIsSearching(false);
    localStorage.setItem('preferred_weather_location', searchQuery.trim());
    await fetchWeatherByLocation(searchQuery.trim());
  };

  useEffect(() => {
    const initWeather = async () => {
      const preferred = localStorage.getItem('preferred_weather_location');
      if (preferred) {
        await fetchWeatherByLocation(preferred);
        return;
      }

      const ipSuccess = await fetchWeatherByIpLookup();
      if (ipSuccess) return;

      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            fetchLocalWeatherByCoords(latitude, longitude);
          },
          async (error) => {
            console.warn("GPS Geolocation failed, falling back to profile location:", error);
            fetchWeatherByLocation(locationName);
          },
          { timeout: 4000, enableHighAccuracy: false }
        );
      } else {
        fetchWeatherByLocation(locationName);
      }
    };

    initWeather();
  }, [locationName]);

  return (
    <div className="bg-gradient-to-br from-slate-900 to-[#0c1322] rounded-3xl p-7 text-white shadow-xl border border-slate-800 relative overflow-hidden font-sans group">
      {/* Decorative Blur circles */}
      <div className="absolute -right-12 -top-12 w-32 h-32 bg-agrogreen-500/10 rounded-full blur-2xl group-hover:bg-agrogreen-500/20 transition-all"></div>
      
      <div className="flex justify-between items-start mb-6">
        <div>
          {isSearching ? (
            <form onSubmit={handleSearchSubmit} className="flex items-center space-x-2 mt-1 mb-1">
              <input 
                type="text" 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Enter city..."
                className="bg-slate-800/80 border border-slate-700 text-white text-xs rounded-lg px-2.5 py-1 w-36 focus:outline-none focus:border-agrogreen-500 font-sans"
                autoFocus
              />
              <button type="submit" className="text-agrogreen-400 hover:text-agrogreen-300 p-1">
                <Check className="h-3.5 w-3.5" />
              </button>
              <button type="button" onClick={() => setIsSearching(false)} className="text-slate-400 hover:text-slate-300 p-1">
                <X className="h-3.5 w-3.5" />
              </button>
            </form>
          ) : (
            <div 
              className="flex items-center space-x-1.5 group/loc cursor-pointer mb-1" 
              onClick={() => { setIsSearching(true); setSearchQuery(''); }}
              title="Click to search/change location"
            >
              <MapPin className="h-3 w-3 text-agrogreen-400" />
              <span className="text-xs text-agrogreen-400 font-semibold uppercase tracking-wider hover:text-white transition-colors">
                Near Location: {weatherData.location}
              </span>
              <Search className="h-3 w-3 text-slate-400 opacity-0 group-hover/loc:opacity-100 transition-opacity" />
            </div>
          )}
          <h4 className="text-xl font-bold tracking-tight text-slate-100">{weatherData.conditionText}</h4>
        </div>
        <button 
          onClick={handleRefresh} 
          disabled={loading}
          className="text-slate-400 hover:text-white transition-colors p-1.5 rounded-lg hover:bg-slate-800"
        >
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="flex items-center justify-between mb-6">
        <div>
          <span className="text-5xl font-extrabold tracking-tighter font-sans">{Math.round(weatherData.temperatureCelsius)}°C</span>
        </div>
        <div className="bg-slate-800/80 p-3 rounded-2xl border border-slate-700/50">
          <CloudSun className="h-10 w-10 text-amber-400" />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="bg-slate-800/40 rounded-2xl p-3 border border-slate-800 flex items-center space-x-2.5">
          <div className="text-agrogreen-400 bg-agrogreen-500/10 p-2 rounded-xl">
            <Droplets className="h-4 w-4" />
          </div>
          <div>
            <span className="block text-[10px] text-slate-400 font-semibold uppercase tracking-wide">Humidity</span>
            <span className="block text-sm font-bold text-slate-100">{weatherData.humidityPercentage}%</span>
          </div>
        </div>

        <div className="bg-slate-800/40 rounded-2xl p-3 border border-slate-800 flex items-center space-x-2.5">
          <div className="text-cyan-400 bg-cyan-500/10 p-2 rounded-xl">
            <CloudRain className="h-4 w-4" />
          </div>
          <div>
            <span className="block text-[10px] text-slate-400 font-semibold uppercase tracking-wide">Rain Prob.</span>
            <span className="block text-sm font-bold text-slate-100">{weatherData.rainProbabilityPercentage}%</span>
          </div>
        </div>
      </div>
    </div>
  );
}
