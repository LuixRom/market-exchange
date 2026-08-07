import { describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { Card } from "./Card";

describe("Card", () => {
  it("renders a plain div when no onClick is provided", () => {
    render(<Card data-testid="card">contenido</Card>);
    const card = screen.getByTestId("card");
    expect(card.tagName).toBe("DIV");
    expect(card).toHaveTextContent("contenido");
  });

  it("renders a native button when onClick is provided", () => {
    const handleClick = vi.fn();
    render(
      <Card data-testid="card" onClick={handleClick}>
        contenido clickeable
      </Card>
    );

    const card = screen.getByTestId("card");
    expect(card.tagName).toBe("BUTTON");
    expect(card).toHaveAttribute("type", "button");

    fireEvent.click(card);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it("is keyboard-focusable natively when interactive (native <button>)", () => {
    const handleClick = vi.fn();
    render(
      <Card data-testid="card" onClick={handleClick}>
        contenido
      </Card>
    );

    const card = screen.getByTestId("card");
    card.focus();
    expect(card).toHaveFocus();
  });
});
