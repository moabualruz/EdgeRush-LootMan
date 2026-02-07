/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    container: {
      center: true,
      padding: "2rem",
      screens: {
        "2xl": "1400px",
      },
    },
    extend: {
      colors: {
        border: "hsl(var(--border))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        foreground: "hsl(var(--foreground))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))",
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))",
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))",
        },
        popover: {
          DEFAULT: "hsl(var(--popover))",
          foreground: "hsl(var(--popover-foreground))",
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))",
        },
        // WoW & Loot Specifics
        quality: {
          poor: '#9d9d9d',
          common: '#ffffff',
          uncommon: '#1eff00',
          rare: '#0070dd',
          epic: '#a335ee',
          legendary: '#ff8000',
          artifact: '#e6cc80',
          heirloom: '#e6cc80',
        },
        class: {
          warrior: '#C79C6E',
          paladin: '#F58CBA',
          hunter: '#ABD473',
          rogue: '#FFF569',
          priest: '#FFFFFF',
          deathknight: '#C41F3B',
          shaman: '#0070DE',
          mage: '#69CCF0',
          warlock: '#9482C9',
          monk: '#00FF96',
          druid: '#FF7D0A',
          demonhunter: '#A330C9',
          evoker: '#33937F',
          // Aliases for uppercase usage if needed, or stick to lowercase
          DEATH_KNIGHT: '#C41F3B',
          DEMON_HUNTER: '#A330C9',
          DRUID: '#FF7D0A',
          EVOKER: '#33937F',
          HUNTER: '#ABD473',
          MAGE: '#69CCF0',
          MONK: '#00FF96',
          PALADIN: '#F58CBA',
          PRIEST: '#FFFFFF',
          ROGUE: '#FFF569',
          SHAMAN: '#0070DE',
          WARLOCK: '#9482C9',
          WARRIOR: '#C79C6E',
        }
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)",
      },
      keyframes: {
        "accordion-down": {
          from: { height: 0 },
          to: { height: "var(--radix-accordion-content-height)" },
        },
        "accordion-up": {
          from: { height: "var(--radix-accordion-content-height)" },
          to: { height: 0 },
        },
        "float": {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-5px)" },
        },
        "pulse-glow": {
          "0%, 100%": { opacity: 1, boxShadow: "0 0 10px var(--primary)" },
          "50%": { opacity: .8, boxShadow: "0 0 20px var(--primary)" },
        }
      },
      animation: {
        "accordion-down": "accordion-down 0.2s ease-out",
        "accordion-up": "accordion-up 0.2s ease-out",
        "float": "float 3s ease-in-out infinite",
        "pulse-glow": "pulse-glow 2s cubic-bezier(0.4, 0, 0.6, 1) infinite",
      },
    },
  },
  plugins: [],
}
