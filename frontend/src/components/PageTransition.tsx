import { ReactNode } from "react";
import { motion } from "framer-motion";
import { useLocation } from "react-router-dom";
import { fadeIn } from "../lib/motion";

export function PageTransition({ children }: Readonly<{ children: ReactNode }>) {
  const location = useLocation();
  return (
    <motion.div
      key={location.pathname}
      variants={fadeIn}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.2 }}
    >
      {children}
    </motion.div>
  );
}
