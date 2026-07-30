import { Link } from "react-router-dom";

const mainLogo = "/img/logos_Mesa de trabajo 1 copia 3.png";

export default function AuthFooter() {
  return (
    <footer className="w-full bg-cream border-t border-cream-dark/60 py-6 px-4 sm:px-8">
      <div className="max-w-container mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-gray-600">
        <Link to="/" className="flex items-center gap-2 hover:opacity-90 transition-opacity">
          <img src={mainLogo} alt="Market Exchange" className="h-8 w-auto object-contain flex-shrink-0" />
          <span className="leading-tight">
            <span className="font-bold text-gray-900 block">market exchange</span>
            <span className="text-gray-500 text-[10px] block">Intercambia. Reutiliza. Revoluciona.</span>
          </span>
        </Link>
        <p>© {new Date().getFullYear()} Market Exchange. Todos los derechos reservados.</p>
      </div>
    </footer>
  );
}
