import { ReactNode } from "react";
import { motion, useScroll, MotionConfig } from "framer-motion";
import {
  FaLeaf,
  FaUsers,
  FaShieldAlt,
  FaHandHoldingHeart,
  FaArrowRight,
  FaExchangeAlt,
  FaPhone,
  FaEnvelope,
  FaMapMarkerAlt,
  FaUser,
} from "react-icons/fa";
import { useAuth } from "../context/AuthProvider";
import { Navigate, Link } from "react-router-dom";
import { Button } from "../components/ui/Button";
import { Card } from "../components/ui/Card";
import { fadeIn, slideUp, staggerChildren } from "../lib/motion";

// Imágenes servidas desde public/img — no se importan como módulo, Vite las
// copia tal cual y se referencian por su ruta absoluta.
const heroIllustration = "/img/logos_Mesa de trabajo 1 copia 3.png"; // versión solo-ícono, fondo transparente
const quienesSomosPhoto = "/img/image1.png";
const planta = "/img/planta.jpg";
const libro = "/img/libro.jpg";
const silla = "/img/silla.jpg";
const camara = "/img/camara.jpg";

const revealViewport = { once: true, amount: 0.3 } as const;

const catalogItems = [
  { img: planta, name: "Planta decorativa", category: "Hogar" },
  { img: libro, name: "Colección de libros", category: "Educación" },
  { img: silla, name: "Silla de madera", category: "Muebles" },
  { img: camara, name: "Cámara fotográfica", category: "Tecnología" },
];

const features = [
  {
    icon: FaUsers,
    title: "Comunidad consciente",
    description: "Conectamos personas que comparten valores y quieren generar un impacto positivo.",
  },
  {
    icon: FaLeaf,
    title: "Sostenibilidad real",
    description: "Cada intercambio ayuda a reducir el desperdicio y cuidar nuestro planeta.",
  },
  {
    icon: FaShieldAlt,
    title: "Intercambios seguros",
    description: "Creamos confianza con perfiles verificados y un entorno seguro para todos.",
  },
  {
    icon: FaHandHoldingHeart,
    title: "Más que objetos, conexiones",
    description: "No solo intercambias cosas, también historias, experiencias y nuevas amistades.",
  },
];

// Placeholder del stack de avatares: no tenemos fotos reales de usuarios todavía —
// círculos de color con ícono en vez de fotos inventadas. Reemplazar por fotos reales
// cuando estén disponibles.
const avatarColors = ["bg-primary", "bg-terracotta", "bg-primary-hover", "bg-terracotta-hover"];

const footerLinks = [
  { label: "Inicio", href: "/" },
  { label: "Catálogo", href: "#productos" },
  { label: "¿Cómo funciona?", href: "#porque" },
  { label: "Sobre nosotros", href: "#nosotros" },
  { label: "Contacto", href: "#contacto" },
];

function SectionBadge({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <span className="inline-flex items-center gap-2 bg-cream-dark text-primary text-caption font-bold uppercase px-4 py-2 rounded-pill">
      {children}
    </span>
  );
}

export default function HomePage() {
  const auth = useAuth();
  const { scrollYProgress } = useScroll();

  if (!auth.isAuthenticated) {
    return (
      <MotionConfig reducedMotion="user">
        <div className="overflow-x-hidden">
          {/* Barra de progreso de scroll */}
          <motion.div
            aria-hidden="true"
            className="fixed top-0 left-0 right-0 h-1 bg-primary origin-left z-50"
            style={{ scaleX: scrollYProgress }}
          />

          {/* Hero */}
          <section className="relative bg-cream">
            <div className="max-w-container mx-auto px-4 sm:px-6 py-10 md:py-16 grid md:grid-cols-2 gap-6 items-center">
              <motion.div variants={staggerChildren} initial="hidden" animate="visible">
                <motion.div variants={slideUp}>
                  <SectionBadge>
                    <FaLeaf aria-hidden="true" /> Bienvenido a Market Exchange
                  </SectionBadge>
                </motion.div>
                <motion.h1
                  variants={slideUp}
                  className="mt-6 text-4xl sm:text-5xl md:text-display lg:text-6xl xl:text-7xl text-gray-900"
                >
                  Intercambia.
                  <br />
                  Reutiliza.
                  <br />
                  <span className="text-primary">Revoluciona.</span>
                </motion.h1>
                <motion.p variants={slideUp} className="mt-6 text-xl text-gray-700 max-w-md">
                  Conectamos personas que quieren dar una nueva vida a lo que ya no usan y
                  encontrar lo que necesitan, cuidando nuestro planeta.
                </motion.p>
                <motion.div variants={slideUp} className="mt-8 flex flex-wrap gap-4">
                  <Button asChild size="lg">
                    <a href="#productos">
                      Explorar catálogo <FaArrowRight aria-hidden="true" />
                    </a>
                  </Button>
                  <Button asChild variant="secondary" size="lg">
                    <a href="#porque">¿Cómo funciona?</a>
                  </Button>
                </motion.div>
                <motion.div variants={slideUp} className="mt-10 flex items-center gap-4">
                  <div className="flex -space-x-3" aria-hidden="true">
                    {avatarColors.map((color, idx) => (
                      <span
                        key={idx}
                        className={`w-10 h-10 rounded-full ${color} ring-2 ring-cream flex items-center justify-center text-white`}
                      >
                        <FaUser size={14} />
                      </span>
                    ))}
                    <span className="w-10 h-10 rounded-full bg-espresso ring-2 ring-cream flex items-center justify-center text-white text-xs font-bold">
                      +120
                    </span>
                  </div>
                  <p className="text-sm text-gray-600">
                    Más de 120 personas
                    <br />
                    ya forman parte del cambio
                  </p>
                </motion.div>
              </motion.div>

              <motion.div
                className="relative flex justify-center md:justify-start"
                variants={fadeIn}
                initial="hidden"
                animate="visible"
                transition={{ delay: 0.2, duration: 0.5 }}
              >
                <div
                  aria-hidden="true"
                  className="absolute -top-8 -right-8 w-72 h-72 bg-cream-dark rounded-full blur-3xl opacity-70"
                />
                <div
                  aria-hidden="true"
                  className="absolute bottom-0 -left-8 w-56 h-56 bg-primary/10 rounded-full blur-3xl"
                />
                <div className="relative w-full max-w-xl lg:max-w-3xl aspect-[4/3]">
                  <img
                    src={heroIllustration}
                    alt="Ilustración de una caja con símbolo de reciclaje, representando el trueque de Market Exchange"
                    className="absolute inset-0 h-full w-full object-contain scale-[2.1] md:scale-[2.35]"
                  />
                </div>
              </motion.div>
            </div>
          </section>

          <main id="main-content">
            {/* ¿Quiénes somos? */}
            <motion.section
              id="nosotros"
              className="bg-white py-16 md:py-24"
              variants={staggerChildren}
              initial="hidden"
              whileInView="visible"
              viewport={revealViewport}
            >
              <div className="max-w-container mx-auto px-4 sm:px-6 grid md:grid-cols-2 gap-12 items-center">
                <motion.div variants={slideUp} className="relative">
                  <div
                    aria-hidden="true"
                    className="hidden sm:grid absolute -top-4 -left-4 grid-cols-4 gap-1.5 opacity-40"
                  >
                    {Array.from({ length: 16 }).map((_, i) => (
                      <span key={i} className="w-1.5 h-1.5 rounded-full bg-primary" />
                    ))}
                  </div>
                  <img
                    src={quienesSomosPhoto}
                    alt="Persona sosteniendo una caja con libros, un peluche y frascos para intercambiar"
                    loading="lazy"
                    decoding="async"
                    className="relative rounded-card shadow-elevated w-full max-w-full h-auto"
                  />
                </motion.div>
                <motion.div variants={slideUp}>
                  <SectionBadge>¿Quiénes somos?</SectionBadge>
                  <h2 className="mt-6 text-h2 text-gray-900">
                    Somos el puente hacia un mundo más sostenible y humano
                  </h2>
                  <p className="mt-4 text-body text-gray-700">
                    En nuestra plataforma de trueque, cada intercambio es una historia, un gesto
                    de consciencia y una oportunidad de darle nueva vida a lo que ya no usas.
                  </p>
                  <p className="mt-4 text-body text-gray-700">
                    Creemos en el valor de las cosas más allá del dinero, en la conexión entre
                    personas y en un planeta con menos desperdicio.
                  </p>
                  <p className="mt-4 text-body-lg font-semibold text-primary">
                    ¡Únete a nosotros y sé parte del cambio!
                  </p>
                </motion.div>
              </div>
            </motion.section>

            {/* Nuestro catálogo */}
            <motion.section
              id="productos"
              className="bg-cream-dark py-16"
              variants={staggerChildren}
              initial="hidden"
              whileInView="visible"
              viewport={revealViewport}
            >
              <div className="max-w-container mx-auto px-4 sm:px-6 text-center">
                <motion.div variants={slideUp}>
                  <SectionBadge>Nuestro catálogo</SectionBadge>
                </motion.div>
                <motion.h2 variants={slideUp} className="mt-6 text-h2 text-gray-900">
                  Aquí, cada objeto cuenta una historia y espera ser parte de la tuya.
                </motion.h2>
                <motion.p variants={slideUp} className="mt-4 text-body-lg text-gray-700 max-w-2xl mx-auto">
                  Descubre un mundo donde todo tiene valor. Explora nuestra colección y
                  encuentra ese artículo único que transformará tus momentos.
                </motion.p>

                <motion.div
                  variants={staggerChildren}
                  className="mt-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 text-left"
                >
                  {catalogItems.map((item) => (
                    <motion.div key={item.name} variants={slideUp}>
                      <Card className="overflow-hidden h-full">
                        <div className="aspect-square overflow-hidden">
                          <img
                            src={item.img}
                            alt={item.name}
                            loading="lazy"
                            decoding="async"
                            className="w-full h-full object-cover"
                          />
                        </div>
                        <div className="p-4">
                          <h3 className="font-bold text-gray-900">{item.name}</h3>
                          <p className="text-caption text-terracotta font-semibold">{item.category}</p>
                          <p className="mt-2 flex items-center gap-2 text-sm text-primary font-semibold">
                            <FaExchangeAlt aria-hidden="true" /> Intercambio
                          </p>
                        </div>
                      </Card>
                    </motion.div>
                  ))}
                </motion.div>

                <motion.div variants={slideUp} className="mt-10">
                  <Button asChild size="lg">
                    <Link to="/register">
                      Ver todo el catálogo <FaArrowRight aria-hidden="true" />
                    </Link>
                  </Button>
                </motion.div>
              </div>
            </motion.section>

            {/* ¿Por qué elegirnos? */}
            <motion.section
              id="porque"
              className="bg-white py-16 md:py-24"
              variants={staggerChildren}
              initial="hidden"
              whileInView="visible"
              viewport={revealViewport}
            >
              <div className="max-w-container mx-auto px-4 sm:px-6 text-center">
                <motion.div variants={slideUp}>
                  <SectionBadge>¿Por qué elegirnos?</SectionBadge>
                </motion.div>
                <motion.div
                  variants={staggerChildren}
                  className="mt-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4"
                >
                  {features.map((feature, idx) => (
                    <motion.div
                      key={feature.title}
                      variants={slideUp}
                      className={`px-6 py-6 ${idx > 0 ? "lg:border-l lg:border-border" : ""}`}
                    >
                      <span
                        aria-hidden="true"
                        className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-primary/10 text-primary mb-4"
                      >
                        <feature.icon size={22} />
                      </span>
                      <h3 className="font-bold text-gray-900">{feature.title}</h3>
                      <p className="mt-2 text-sm text-gray-600">{feature.description}</p>
                    </motion.div>
                  ))}
                </motion.div>
              </div>
            </motion.section>

            {/* Banner CTA */}
            <motion.section
              className="max-w-container mx-auto px-4 sm:px-6 py-8"
              variants={fadeIn}
              initial="hidden"
              whileInView="visible"
              viewport={revealViewport}
            >
              <div className="relative rounded-card bg-primary shadow-elevated px-8 py-10 sm:px-12 sm:py-12 flex flex-col sm:flex-row items-center justify-between gap-6">
                {/* Hojas semitransparentes a la izquierda (marca de agua) */}
                <div
                  className="absolute inset-0 overflow-hidden rounded-card pointer-events-none"
                  aria-hidden="true"
                >
                  <FaLeaf
                    className="absolute -left-6 -bottom-6 text-white/20 -rotate-45"
                    size={140}
                  />
                  <FaLeaf
                    className="absolute left-14 -bottom-4 text-white/15 rotate-12"
                    size={90}
                  />
                </div>

                <div className="relative z-10 text-center sm:text-left">
                  <h2 className="text-h2 text-white">¿Listo para hacer parte del cambio?</h2>
                  <p className="mt-2 text-body-lg text-white/80">
                    Únete a nuestra comunidad y comienza a intercambiar hoy mismo.
                  </p>
                </div>
                <Button asChild variant="secondary" size="lg" className="relative z-10 flex-shrink-0">
                  <Link to="/register">
                    Regístrate gratis <FaArrowRight aria-hidden="true" />
                  </Link>
                </Button>

                {/* Hojas verdes destacadas en la esquina inferior derecha */}
                <div
                  aria-hidden="true"
                  className="absolute -bottom-5 -right-5 z-20 pointer-events-none"
                >
                  <svg
                    className="w-28 h-28 sm:w-32 sm:h-32 md:w-36 md:h-36 drop-shadow-xl"
                    viewBox="0 0 120 120"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    {/* Sombra base */}
                    <ellipse cx="75" cy="100" rx="32" ry="8" fill="black" fillOpacity="0.2" />

                    {/* Hoja trasera (verde oliva) */}
                    <g transform="translate(5, 5) rotate(-20 60 60)">
                      <path
                        d="M 95 95 C 90 45 50 20 15 45 C 25 80 60 100 95 95 Z"
                        fill="#648a33"
                      />
                      <path
                        d="M 95 95 C 70 70 48 52 15 45"
                        stroke="#4d6c25"
                        strokeWidth="2.5"
                        strokeLinecap="round"
                      />
                    </g>

                    {/* Hoja frontal (verde vivo) */}
                    <g transform="translate(15, 15) rotate(10 60 60)">
                      <path
                        d="M 90 90 C 85 40 45 15 10 40 C 20 75 55 95 90 90 Z"
                        fill="#84b038"
                      />
                      <path
                        d="M 90 90 C 65 65 42 48 10 40"
                        stroke="#678f26"
                        strokeWidth="2.5"
                        strokeLinecap="round"
                      />
                    </g>
                  </svg>
                </div>
              </div>
            </motion.section>
          </main>

          {/* Footer */}
          <footer className="bg-cream-dark pt-16 pb-8">
            <div className="max-w-container mx-auto px-4 sm:px-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10">
              <div>
                <div className="flex items-center gap-2">
                  <img
                    src={heroIllustration}
                    alt="Market Exchange"
                    className="h-12 w-auto object-contain flex-shrink-0"
                  />
                  <div>
                    <span className="block font-bold text-gray-900">market exchange</span>
                    <span className="block text-caption text-gray-500">
                      Intercambia. Reutiliza. Revoluciona.
                    </span>
                  </div>
                </div>
                <p className="mt-4 text-sm text-gray-600">
                  Plataforma de trueque comprometida con un mundo más sostenible, humano y
                  consciente.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-gray-900 mb-4">Enlaces</h3>
                <ul className="space-y-2">
                  {footerLinks.map((link) => (
                    <li key={link.label}>
                      <a
                        href={link.href}
                        className="text-sm text-gray-600 hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary rounded-card"
                      >
                        {link.label}
                      </a>
                    </li>
                  ))}
                </ul>
              </div>

              <div id="contacto">
                <h3 className="font-bold text-gray-900 mb-4">Contáctanos</h3>
                <ul className="space-y-3 text-sm text-gray-600">
                  <li className="flex items-center gap-2">
                    <FaPhone aria-hidden="true" className="text-primary" />
                    999 999 999
                  </li>
                  <li className="flex items-center gap-2">
                    <FaEnvelope aria-hidden="true" className="text-primary" />
                    contacto@marketexchange.com
                  </li>
                  <li className="flex items-center gap-2">
                    <FaMapMarkerAlt aria-hidden="true" className="text-primary" />
                    Lima, Perú
                  </li>
                </ul>
              </div>
            </div>

            <p className="mt-12 text-center text-caption text-gray-500">
              © {new Date().getFullYear()} Market Exchange. Todos los derechos reservados.
            </p>
          </footer>
        </div>
      </MotionConfig>
    );
  }

  return <Navigate to="/dashboard" />;
}
