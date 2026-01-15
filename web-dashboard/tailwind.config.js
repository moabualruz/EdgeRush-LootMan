/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // WoW-inspired color scheme
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        },
        // Class colors
        'class-warrior': '#C79C6E',
        'class-paladin': '#F58CBA',
        'class-hunter': '#ABD473',
        'class-rogue': '#FFF569',
        'class-priest': '#FFFFFF',
        'class-deathknight': '#C41F3B',
        'class-shaman': '#0070DE',
        'class-mage': '#69CCF0',
        'class-warlock': '#9482C9',
        'class-monk': '#00FF96',
        'class-druid': '#FF7D0A',
        'class-demonhunter': '#A330C9',
        'class-evoker': '#33937F',
        // Score colors
        'score-high': '#22c55e',
        'score-medium': '#eab308',
        'score-low': '#ef4444',
      },
    },
  },
  plugins: [],
}
