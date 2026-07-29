import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useState,
} from "react";
import * as RadixToast from "@radix-ui/react-toast";
import { AnimatePresence, motion } from "framer-motion";
import { cn } from "../../lib/cn";

type ToastVariant = "default" | "success" | "danger";

interface ToastItem {
  id: number;
  title: string;
  description?: string;
  variant: ToastVariant;
}

interface ToastContextValue {
  toast: (options: {
    title: string;
    description?: string;
    variant?: ToastVariant;
  }) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const variantStyles: Record<ToastVariant, string> = {
  default: "border-border bg-white text-gray-900",
  success: "border-success bg-white text-gray-900",
  danger: "border-danger bg-white text-gray-900",
};

let nextId = 1;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const toast = useCallback<ToastContextValue["toast"]>(
    ({ title, description, variant = "default" }) => {
      const id = nextId++;
      setToasts((prev) => [...prev, { id, title, description, variant }]);
    },
    []
  );

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toast }}>
      <RadixToast.Provider swipeDirection="right" duration={4000}>
        {children}
        <AnimatePresence>
          {toasts.map((t) => (
            <RadixToast.Root
              key={t.id}
              asChild
              forceMount
              onOpenChange={(open) => {
                if (!open) removeToast(t.id);
              }}
            >
              <motion.div
                initial={{ opacity: 0, y: -8, scale: 0.95 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.15 }}
                className={cn(
                  "rounded-card border p-4 shadow-lg",
                  variantStyles[t.variant]
                )}
              >
                <RadixToast.Title className="text-sm font-bold">
                  {t.title}
                </RadixToast.Title>
                {t.description && (
                  <RadixToast.Description className="mt-1 text-sm text-gray-600">
                    {t.description}
                  </RadixToast.Description>
                )}
              </motion.div>
            </RadixToast.Root>
          ))}
        </AnimatePresence>
        <RadixToast.Viewport className="fixed top-4 right-4 z-[100] flex w-full max-w-sm flex-col gap-2 outline-none" />
      </RadixToast.Provider>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error("useToast debe usarse dentro de <ToastProvider>");
  }
  return ctx;
}
