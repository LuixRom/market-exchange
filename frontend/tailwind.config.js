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
      },
    fontFamily: {
        belleza: ["Belleza", "sans-serif"],
        serif: ["Noto Serif", "serif"],
      },
    },
  },
  plugins: [],
}

