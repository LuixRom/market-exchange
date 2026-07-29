import { HTMLAttributes, forwardRef } from "react";
import { cn } from "../../lib/cn";

const Card = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "rounded-card border border-border bg-surface shadow-md",
        className
      )}
      {...props}
    />
  )
);
Card.displayName = "Card";

export { Card };
