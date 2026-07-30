/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}", // Asegúrate de incluir todas las extensiones que usas
  ],
  theme: {
    extend: {
      colors: {
        "purple-dark": "#170312",
        "purple-medium": "#33032d",
        "purple-light": "#531253",
        "gray-light": "#eaeaea",
        "white": "#f4fffd",
        primary: {
          DEFAULT: "#6d28d9",
          hover: "#5b21b6",
          foreground: "#ffffff",
        },
        surface: "#ffffff",
        muted: "#f4f4f5",
        border: "#e4e4e7",
        danger: "#dc2626",
        success: "#16a34a",
        // Paleta cálida neutra — usada en HomePage para transiciones de sección más
        // naturales que bloques de blanco puro / morado sólido alternados.
        cream: {
          DEFAULT: "#faf6ec",
          dark: "#f0e8d4",
        },
        terracotta: {
          DEFAULT: "#c1694f",
          hover: "#a8543c",
        },
        espresso: {
          DEFAULT: "#2b2417",
          hover: "#1c170f",
        },
      },
      fontFamily: {
        belleza: ["Belleza", "sans-serif"],
        serif: ["Noto Serif", "serif"],
      },
      // Escala tipográfica nombrada — evita tamaños ad-hoc repetidos por archivo.
      fontSize: {
        display: ["3.5rem", { lineHeight: "1.1", fontWeight: "700" }],
        h1: ["2.5rem", { lineHeight: "1.15", fontWeight: "700" }],
        h2: ["2rem", { lineHeight: "1.2", fontWeight: "700" }],
        h3: ["1.5rem", { lineHeight: "1.3", fontWeight: "600" }],
        "body-lg": ["1.125rem", { lineHeight: "1.6" }],
        body: ["1rem", { lineHeight: "1.6" }],
        caption: ["0.875rem", { lineHeight: "1.5" }],
      },
      borderRadius: {
        card: "1rem",
        pill: "9999px",
      },
      boxShadow: {
        card: "0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)",
        elevated: "0 10px 30px -10px rgba(0,0,0,0.15)",
        popover: "0 4px 12px rgba(0,0,0,0.10)",
      },
      transitionDuration: {
        fast: "150ms",
        base: "200ms",
        slow: "300ms",
      },
      maxWidth: {
        container: "72rem",
      },
    },
  },
  plugins: [],
}

