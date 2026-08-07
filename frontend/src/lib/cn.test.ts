import { describe, expect, it } from "vitest";
import { cn } from "./cn";

describe("cn", () => {
  it("joins plain class names", () => {
    expect(cn("foo", "bar")).toBe("foo bar");
  });

  it("ignores falsy values", () => {
    const showBar = false;
    expect(cn("foo", showBar && "bar", undefined, null, "", "baz")).toBe("foo baz");
  });

  it("supports conditional object syntax", () => {
    expect(cn("foo", { bar: true, baz: false })).toBe("foo bar");
  });

  it("resolves conflicting tailwind classes by keeping the last one", () => {
    expect(cn("p-2", "p-4")).toBe("p-4");
  });

  it("keeps non-conflicting classes from later arguments", () => {
    expect(cn("text-sm font-bold", "text-primary")).toBe("text-sm font-bold text-primary");
  });
});
