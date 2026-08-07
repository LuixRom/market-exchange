import { motion } from "framer-motion";
import { cn } from "../../lib/cn";

interface SpinnerProps {
  className?: string;
  label?: string;
}

export function Spinner({ className, label = "Cargando..." }: Readonly<SpinnerProps>) {
  return (
    <output className="flex flex-col items-center justify-center gap-3 py-10 text-gray-500">
      <motion.span
        aria-hidden="true"
        className={cn(
          "block h-8 w-8 rounded-full border-2 border-border border-t-primary",
          className
        )}
        animate={{ rotate: 360 }}
        transition={{ repeat: Infinity, duration: 0.8, ease: "linear" }}
      />
      {label && <span className="text-sm">{label}</span>}
    </output>
  );
}
