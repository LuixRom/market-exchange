import { describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { Button } from "./Button";

describe("Button", () => {
  it("renders its children", () => {
    render(<Button>Guardar</Button>);
    expect(screen.getByRole("button", { name: "Guardar" })).toBeInTheDocument();
  });

  it("calls onClick when clicked", () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Enviar</Button>);

    fireEvent.click(screen.getByRole("button", { name: "Enviar" }));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it("does not call onClick when disabled", () => {
    const handleClick = vi.fn();
    render(
      <Button onClick={handleClick} disabled>
        Enviar
      </Button>
    );

    fireEvent.click(screen.getByRole("button", { name: "Enviar" }));
    expect(handleClick).not.toHaveBeenCalled();
  });

  it("applies variant and size classes", () => {
    render(
      <Button variant="danger" size="lg">
        Eliminar
      </Button>
    );

    const button = screen.getByRole("button", { name: "Eliminar" });
    expect(button.className).toContain("bg-danger");
    expect(button.className).toContain("h-12");
  });

  it("renders the child element instead of a button when asChild is used", () => {
    render(
      <Button asChild>
        <a href="/dashboard">Ir al dashboard</a>
      </Button>
    );

    const link = screen.getByRole("link", { name: "Ir al dashboard" });
    expect(link).toHaveAttribute("href", "/dashboard");
    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });
});
