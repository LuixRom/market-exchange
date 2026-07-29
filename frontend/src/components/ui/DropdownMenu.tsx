import { ReactNode } from "react";
import * as RadixDropdown from "@radix-ui/react-dropdown-menu";
import { motion } from "framer-motion";
import { cn } from "../../lib/cn";

interface DropdownMenuProps {
  trigger: ReactNode;
  children: ReactNode;
  align?: "start" | "center" | "end";
}

export function DropdownMenu({
  trigger,
  children,
  align = "end",
}: DropdownMenuProps) {
  return (
    <RadixDropdown.Root>
      <RadixDropdown.Trigger asChild>{trigger}</RadixDropdown.Trigger>
      <RadixDropdown.Portal>
        <RadixDropdown.Content align={align} sideOffset={8} asChild>
          <motion.div
            className="z-50 min-w-[12rem] rounded-card border border-border bg-white p-1 shadow-lg"
            initial={{ opacity: 0, y: -4 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.12 }}
          >
            {children}
          </motion.div>
        </RadixDropdown.Content>
      </RadixDropdown.Portal>
    </RadixDropdown.Root>
  );
}

interface DropdownMenuItemProps {
  children: ReactNode;
  onSelect?: () => void;
  className?: string;
  asChild?: boolean;
}

export function DropdownMenuItem({
  children,
  onSelect,
  className,
  asChild,
}: DropdownMenuItemProps) {
  return (
    <RadixDropdown.Item
      asChild={asChild}
      onSelect={onSelect}
      className={cn(
        "block cursor-pointer rounded-md px-3 py-2 text-sm text-gray-700 outline-none transition-colors",
        "hover:bg-muted focus:bg-muted",
        className
      )}
    >
      {children}
    </RadixDropdown.Item>
  );
}
