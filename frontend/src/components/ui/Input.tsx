import { InputHTMLAttributes, forwardRef } from "react";
import { cn } from "../../lib/cn";

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, ...props }, ref) => {
    return (
      <div className="w-full">
        <input
          ref={ref}
          className={cn(
            "w-full rounded-card border border-border bg-surface px-4 py-2.5 text-gray-800 shadow-sm transition-colors duration-base",
            "placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary",
            error && "border-danger focus:ring-danger focus:border-danger",
            className
          )}
          aria-invalid={!!error}
          {...props}
        />
        {error && <p className="mt-1 text-sm text-danger">{error}</p>}
      </div>
    );
  }
);
Input.displayName = "Input";

export { Input };
