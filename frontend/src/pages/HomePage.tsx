import { motion, useScroll } from "framer-motion";
import logo from "../../img/logos_Mesa de trabajo 1.png";
import joya2 from "../img/products/joya2.jpg";
import joya from "../../img/seleccion/pexels-kindelmedia-6994107.jpg";
import joya4 from "../../img/seleccion/pexels-mizunokozuki-13432260.jpg";
import joya5 from "../img/foto_1.jpg";
import joya3 from "../../img/seleccion/pexels-ivan-samkov-8962868.jpg";
import joya6 from "../../img/seleccion/pexels-cottonbro-6591429.jpg";
import joya10 from "../../img/seleccion/pexels-vlada-karpovich-4668356.jpg";
import personas from "../img/personas.jpg";
import momo from "../img/momo.jpg";
import celular from "../img/celu.jpg";
import instagramIcon from "../img/instagram-new.png";
import { useAuth } from "../context/AuthProvider";
import { Navigate, Link } from "react-router-dom";
import { Button } from "../components/ui/Button";
import { fadeIn, slideUp } from "../lib/motion";

const catalogItems = [
  { img: joya2, desc: "Intercambios" },
  { img: joya, desc: "Intercambia con cualquier persona" },
  { img: joya4, desc: "Intercambios" },
  { img: joya5, desc: "trueque digital" },
];

const galleryImages = [joya3, joya6, joya10];

const revealViewport = { once: true, amount: 0.3 } as const;

export default function HomePage() {
  const auth = useAuth();
  const { scrollYProgress } = useScroll();

  if (!auth.isAuthenticated) {
    return (
      <div>
        {/* Barra de progreso de scroll */}
        <motion.div
          className="fixed top-0 left-0 right-0 h-1 bg-primary origin-left z-50"
          style={{ scaleX: scrollYProgress }}
        />

        {/* Header */}
        <div
          className="h-screen bg-cover bg-center"
          style={{ backgroundImage: `url(${logo})` }}
        />

        {/* CTA */}
        <motion.div
          className="flex flex-wrap justify-center gap-4 py-8 bg-white"
          variants={slideUp}
          initial="hidden"
          animate="visible"
          transition={{ duration: 0.4 }}
        >
          <Button asChild size="lg">
            <Link to="/register">Regístrate</Link>
          </Button>
          <Button asChild variant="secondary" size="lg">
            <a href="#productos">Ver catálogo</a>
          </Button>
        </motion.div>

        {/* Navigation */}
        <nav className="sticky top-0 bg-primary/95 backdrop-blur text-white flex justify-center space-x-10 py-4 z-40">
          <a href="#nosotros" className="hover:text-gray-200 transition">NOSOTROS</a>
          <a href="#productos" className="hover:text-gray-200 transition">SERVICIOS</a>
          <a href="#contacto" className="hover:text-gray-200 transition">CONTACTOS</a>
        </nav>

        {/* Main Content */}
        <main id="nosotros" className="container mx-auto py-10 px-4">
          <motion.section
            className="text-center mb-16"
            variants={fadeIn}
            initial="hidden"
            whileInView="visible"
            viewport={revealViewport}
          >
            <h2 className="text-lg font-bold uppercase text-primary bg-muted inline-block px-4 py-2 mb-4 rounded-card">¿Quiénes somos?</h2>
            <h3 className="text-3xl font-semibold text-gray-900">Somos el puente hacia un mundo más sostenible y humano</h3>
            <p className="mt-4 text-lg text-gray-700 max-w-2xl mx-auto">
              En nuestra plataforma de trueque, cada intercambio es una historia, un gesto de consciencia y una oportunidad de darle nueva vida a lo que ya no usas.<br />
              Creemos en el valor de las cosas más allá del dinero, en la conexión entre personas y en un planeta con menos desperdicio.<br />
              ¡Únete a nosotros y sé parte del cambio!
            </p>
          </motion.section>

          <motion.section
            id="productos"
            className="bg-muted py-10 rounded-card"
            variants={fadeIn}
            initial="hidden"
            whileInView="visible"
            viewport={revealViewport}
          >
            <h2 className="text-lg font-bold uppercase text-primary bg-white inline-block px-4 py-2 mb-4 rounded-card ml-4">Nuestro catálogo</h2>
            <h3 className="text-3xl font-semibold text-gray-900 text-center">Aquí, cada objeto cuenta una historia y espera ser parte de la tuya.</h3>
            <p className="text-center mt-4 text-lg text-gray-700">
              Descubre un mundo donde todo tiene valor. Explora nuestra colección y encuentra ese artículo único que transformará tus momentos. <br />
              ¡Intercambia. Reutiliza. Revoluciona!
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mt-8 px-4">
              {catalogItems.map((catalogItem, idx) => (
                <motion.div
                  key={idx}
                  className="relative group rounded-card overflow-hidden"
                  whileHover={{ scale: 1.03 }}
                  transition={{ duration: 0.2 }}
                >
                  <img src={catalogItem.img} alt={catalogItem.desc} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-primary/80 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                    <p className="text-white text-lg text-center px-2">{catalogItem.desc}</p>
                  </div>
                </motion.div>
              ))}
            </div>
          </motion.section>
        </main>

        {/* Separador */}
        <motion.section
          className="bg-cover bg-center h-64"
          style={{ backgroundImage: `url(${joya5})` }}
          variants={fadeIn}
          initial="hidden"
          whileInView="visible"
          viewport={revealViewport}
        />

        {/* Acerca de */}
        <motion.section
          className="container mx-auto py-10 px-4"
          variants={fadeIn}
          initial="hidden"
          whileInView="visible"
          viewport={revealViewport}
        >
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
            <article>
              <figure className="text-center">
                <img src={personas} alt="Grupo de especialistas" className="rounded-card mx-auto" />
                <figcaption className="mt-4">
                  <p>
                    <strong className="block mb-2">Grupo de apasionados por el cambio</strong>
                    Nuestro equipo está dedicado a conectar personas, seleccionar los mejores artículos para el trueque y fomentar un mundo más sostenible y humano para todos.
                  </p>
                </figcaption>
              </figure>
            </article>
            <article>
              <figure className="text-center">
                <img src={momo} alt="Un relato de servicio" className="rounded-card mx-auto" />
                <figcaption className="mt-4">
                  <p>
                    <strong className="block mb-2">Un relato de servicio</strong>
                    Nuestra historia es un reflejo de nuestra pasión por transformar lo ordinario en extraordinario.
                    Cada intercambio que facilitamos está marcado por nuestro compromiso inquebrantable con la calidad, la conexión y el impacto positivo en el mundo.
                  </p>
                </figcaption>
              </figure>
            </article>
          </div>
        </motion.section>

        {/* Galería */}
        <motion.div
          className="grid grid-cols-3 gap-1"
          variants={fadeIn}
          initial="hidden"
          whileInView="visible"
          viewport={revealViewport}
        >
          {galleryImages.map((img, idx) => (
            <motion.div key={idx} className="col-span-1 overflow-hidden rounded-card" whileHover={{ scale: 1.03 }}>
              <img src={img} alt="" className="w-full h-full object-cover" />
            </motion.div>
          ))}
        </motion.div>

        {/* Contacto */}
        <motion.section
          id="contacto"
          className="bg-muted py-10 text-center"
          variants={fadeIn}
          initial="hidden"
          whileInView="visible"
          viewport={revealViewport}
        >
          <h2 className="text-lg font-bold uppercase text-primary bg-white inline-block px-4 py-2 mb-4 rounded-card">Contáctanos</h2>
          <p className="text-lg text-gray-700">
            <img src={celular} alt="Teléfono" className="inline-block w-12 h-6 mr-1" />
            Tel: 966462221
          </p>
        </motion.section>

        {/* Footer */}
        <footer className="bg-primary-hover text-gray-100 py-8 text-center">
          <a href="https://www.instagram.com/yeru_peru/">
            <img src={instagramIcon} alt="Instagram" className="w-8 h-8 inline-block mx-2" />
          </a>
        </footer>
      </div>
    );
  }

  return <Navigate to="/dashboard" />;
}
