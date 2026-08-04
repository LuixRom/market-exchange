import { motion } from "framer-motion";
import AllItems from "../components/AllItems";
import { fadeIn } from "../lib/motion";

export default function AdminItemModerationPage() {
  return (
    <motion.div
      className="w-full max-w-container mx-auto px-4 sm:px-6 py-8"
      variants={fadeIn}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
    >
      <div className="mb-6">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
          Moderacion de publicaciones
        </h1>
        <p className="mt-1 text-sm text-gray-600">
          Revisa los items pendientes y decide si se aprueban o se rechazan antes de mostrarlos en el catalogo.
        </p>
      </div>

      <AllItems />
    </motion.div>
  );
}
