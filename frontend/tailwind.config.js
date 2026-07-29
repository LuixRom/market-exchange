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
      },
      fontFamily: {
        belleza: ["Belleza", "sans-serif"],
        serif: ["Noto Serif", "serif"],
      },
      borderRadius: {
        card: "1rem",
      },
    },
  },
  plugins: [],
}

