import { HTMLAttributes, KeyboardEvent, forwardRef } from "react";
import { cn } from "../../lib/cn";

const Card = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, onClick, onKeyDown, ...props }, ref) => {
    const isInteractive = !!onClick;

    const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
      onKeyDown?.(event);
      if (isInteractive && (event.key === "Enter" || event.key === " ")) {
        event.preventDefault();
        event.currentTarget.click();
      }
    };

    return (
      <div
        ref={ref}
        role={isInteractive ? "button" : undefined}
        tabIndex={isInteractive ? 0 : undefined}
        onClick={onClick}
        onKeyDown={isInteractive ? handleKeyDown : onKeyDown}
        className={cn(
          "rounded-card border border-border bg-surface shadow-card",
          isInteractive &&
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2",
          className
        )}
        {...props}
      />
    );
  }
);
Card.displayName = "Card";

export { Card };
