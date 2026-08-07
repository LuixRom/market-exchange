import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { Spinner } from "./Spinner";

describe("Spinner", () => {
  it("renders the default label", () => {
    render(<Spinner />);
    expect(screen.getByText("Cargando...")).toBeInTheDocument();
  });

  it("renders a custom label", () => {
    render(<Spinner label="Guardando cambios..." />);
    expect(screen.getByText("Guardando cambios...")).toBeInTheDocument();
  });

  it("omits the label text node when label is empty", () => {
    render(<Spinner label="" />);
    expect(screen.queryByText("Cargando...")).not.toBeInTheDocument();
  });

  it("uses a native <output> element for the status role", () => {
    const { container } = render(<Spinner />);
    expect(container.querySelector("output")).not.toBeNull();
  });
});
