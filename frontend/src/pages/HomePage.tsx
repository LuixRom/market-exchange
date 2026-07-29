import React, { useEffect } from "react";
import logo from "/img/logos_Mesa de trabajo 1.png"
import joya2 from "../img/products/joya2.jpg";
import joya from "/img/seleccion/pexels-kindelmedia-6994107.jpg";
import joya4 from "/img/seleccion/pexels-mizunokozuki-13432260.jpg";
import joya5 from "../img/foto_1.jpg";
import joya3 from "/img/seleccion/pexels-ivan-samkov-8962868.jpg";
import joya6 from "/img/seleccion/pexels-cottonbro-6591429.jpg";
import joya10 from "/img/seleccion/pexels-vlada-karpovich-4668356.jpg";
import personas from "../img/personas.jpg";
import momo from "../img/momo.jpg";
import celular from "../img/celu.jpg";
import instagramIcon from "../img/instagram-new.png";
import { useAuth } from "../context/AuthProvider";
import { Navigate } from "react-router-dom";

export default function HomePage() {
  const auth = useAuth();
  useEffect(() => {
    const altura = document.body.scrollHeight - window.innerHeight;
    const fondo = document.getElementById("fondo");

    const handleScroll = () => {
      const anchoFondo = (window.pageYOffset / altura) * 700;
      if (fondo && anchoFondo <= 100) {
        fondo.style.width = `${anchoFondo}%`;
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  if(!auth.isAuthenticated){
  return (
    <div>
      {/* Header */}
      <div className="contenido-header relative h-screen bg-cover bg-center" style={{ backgroundImage: `url(${logo})` }}>
        <div id="fondo" className="fondo absolute"></div>
      </div>

      {/* Navigation */}
      <nav className="menu sticky top-0 bg-purple-700 text-gray-200 flex justify-center space-x-10 py-4 z-50">
        <a href="#nosotros" className="hover:text-gray-100 transition">NOSOTROS</a>
        <a href="#productos" className="hover:text-gray-100 transition">SERVICIOS</a>
        <a href="#contacto" className="hover:text-gray-100 transition">CONTACTOS</a>
      </nav>

      {/* Main Content */}
      <main id="nosotros" className="container mx-auto py-10 px-4">
        {/* Nosotros */}
        <section className="text-center mb-16">
          <h2 className="subtitulo text-lg font-bold uppercase text-purple-800 bg-gray-200 inline-block px-4 py-2 mb-4">¿Quiénes somos?</h2>
          <h3 className="titulo text-3xl font-semibold text-gray-900">Somos el puente hacia un mundo más sostenible y humano</h3>
          <p className="mt-4 text-lg text-gray-700">
          En nuestra plataforma de trueque, cada intercambio es una historia, un gesto de consciencia y una oportunidad de darle nueva vida a lo que ya no usas.<br />
          Creemos en el valor de las cosas más allá del dinero, en la conexión entre personas y en un planeta con menos desperdicio.<br />
          ¡Únete a nosotros y sé parte del cambio!
          </p>
        </section>

        {/* Productos */}
        <section id="productos" className="productos bg-gray-100 py-10">
          <h2 className="subtitulo text-lg font-bold uppercase text-purple-800 bg-gray-200 inline-block px-4 py-2 mb-4">Nuestro catálogo</h2>
          <h3 className="titulo text-3xl font-semibold text-gray-900 text-center">Aquí, cada objeto cuenta una historia y espera ser parte de la tuya.</h3>
          <p className="text-center mt-4 text-lg text-gray-700">
          Descubre un mundo donde todo tiene valor. Explora nuestra colección y encuentra ese artículo único que transformará tus momentos. <br />
          ¡Intercambia. Reutiliza. Revoluciona!
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
            {[{ img: joya2, desc: "Intercambios" }, { img: joya, desc: "Intercambia con cualquier persona" }, { img: joya4, desc: "Intercambios" }, { img: joya5, desc: "trueque digital" }].map((item, idx) => (
              <div key={idx} className="relative group">
                <img src={item.img} alt={item.desc} className="w-full h-full object-cover" />
                <div className="absolute inset-0 bg-purple-800 opacity-0 group-hover:opacity-80 flex items-center justify-center transition-opacity">
                  <p className="text-white text-lg">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>

      {/* Separador */}
      <section className="separador bg-cover bg-center h-64 text-center flex items-center justify-center text-gray-900" style={{ backgroundImage: `url(${joya5})` }}>
      </section>

      {/* Acerca de */}
      <section className="container mx-auto py-10 px-4">
        <div className="row acerc-de justify-content-center grid grid-cols-1 sm:grid-cols-2 gap-8">
          <article>
            <figure className="text-center">
              <img src={personas} alt="Grupo de especialistas" />
              <figcaption>
                <p>
                  <strong className="mb-5">Grupo de apasionados por el cambio</strong><br />
                  Nuestro equipo está dedicado a conectar personas, seleccionar los mejores artículos para el trueque y fomentar un mundo más sostenible y humano para todos.
                </p>
              </figcaption>
            </figure>
          </article>
          <article>
            <figure className="text-center">
              <img src={momo} alt="Un relato de servicio" />
              <figcaption>
                <p>
                  <strong className="mb-5">Un relato de servicio</strong><br />
                  Nuestra historia es un reflejo de nuestra pasión por transformar lo ordinario en extraordinario.
                  Cada intercambio que facilitamos está marcado por nuestro compromiso inquebrantable con la calidad, la conexión y el impacto positivo en el mundo.
                </p>
              </figcaption>
            </figure>
          </article>
        </div>
      </section>

      {/* Galería */}
      <div className="galeria grid grid-cols-3 gap-0">
        {[joya3, joya6, joya10].map((img, idx) => (
          <div key={idx} className="col-span-1">
            <img src={img} alt="" className="w-full h-full object-cover" />
          </div>
        ))}
      </div>

      {/* Contacto */}
      <section id="contacto" className="contacto bg-gray-100 py-10 text-center">
        <h2 className="subtitulo text-lg font-bold uppercase text-purple-800 bg-gray-200 inline-block px-4 py-2 mb-4">Contáctanos</h2>
        <p className="text-lg text-gray-700">
          <img src={celular} alt="Teléfono" className="inline-block w-12 h-6 mr-1" />
          Tel: 966462221
        </p>
      </section>

      {/* Footer */}
      <footer className="redes bg-purple-900 text-gray-100 py-8 text-center">
        <a href="https://www.instagram.com/yeru_peru/">
          <img src={instagramIcon} alt="Instagram" className="w-8 h-8 inline-block mx-2" />
        </a>
      </footer>
    </div>
  );
}
else{
  return <Navigate to="/dashboard" />
}
}