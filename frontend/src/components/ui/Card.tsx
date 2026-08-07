import { ButtonHTMLAttributes, forwardRef, HTMLAttributes } from "react";
import { cn } from "../../lib/cn";

type CardProps = HTMLAttributes<HTMLDivElement> & Pick<ButtonHTMLAttributes<HTMLButtonElement>, "onClick">;

const Card = forwardRef<HTMLDivElement | HTMLButtonElement, CardProps>(
  ({ className, onClick, ...props }, ref) => {
    if (onClick) {
      return (
        <button
          ref={ref as React.Ref<HTMLButtonElement>}
          type="button"
          onClick={onClick}
          className={cn(
            "block w-full text-left rounded-card border border-border bg-surface shadow-card",
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2",
            className
          )}
          {...(props as ButtonHTMLAttributes<HTMLButtonElement>)}
        />
      );
    }

    return (
      <div
        ref={ref as React.Ref<HTMLDivElement>}
        className={cn("rounded-card border border-border bg-surface shadow-card", className)}
        {...props}
      />
    );
  }
);
Card.displayName = "Card";

export { Card };
