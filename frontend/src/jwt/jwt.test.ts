import { afterEach, describe, expect, it, vi } from "vitest";
import extractRoleFromToken from "./jwt";

function buildToken(payload: unknown): string {
  const base64url = (input: string) =>
    btoa(input).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");

  const header = base64url(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = base64url(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe("extractRoleFromToken", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("returns the role encoded in a valid token", () => {
    const token = buildToken({ role: "ADMIN" });
    expect(extractRoleFromToken(token)).toBe("ADMIN");
  });

  it("returns null when the token has no role claim", () => {
    const token = buildToken({ sub: "user@example.com" });
    expect(extractRoleFromToken(token)).toBeNull();
  });

  it("returns null and logs an error for a malformed token", () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => undefined);
    expect(extractRoleFromToken("not-a-real-token")).toBeNull();
    expect(consoleSpy).toHaveBeenCalled();
  });
});
