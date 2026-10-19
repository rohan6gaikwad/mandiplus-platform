/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        agrogreen: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
          950: '#052e16',
        },
        earth: {
          50: '#fdf8f6',
          100: '#f2e8e5',
          200: '#e5d1cb',
          300: '#d1b2a9',
          400: '#b58a7e',
          500: '#9b6c5f',
          600: '#835448',
          700: '#6c433a',
          800: '#5c3830',
          900: '#4e302a',
        }
      },
      fontFamily: {
        sans: ['Outfit', 'Inter', 'sans-serif'],
      },
      boxShadow: {
        'premium': '0 8px 30px rgb(0,0,0,0.04)',
        'glass': '0 8px 32px 0 rgba(31, 38, 135, 0.04)',
      }
    },
  },
  plugins: [],
}
