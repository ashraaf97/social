import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createDonation, type CreateDonationPayload, type Donation } from "../api";

describe("createDonation", () => {
  const payload: CreateDonationPayload = {
    streamerId: "streamer-1",
    senderName: "Alice",
    amount: 12.5,
    currency: "USD",
    messageText: "great stream",
  };

  const mockDonation: Donation = {
    id: 42,
    senderName: "Alice",
    amount: 12.5,
    currency: "USD",
    messageText: "great stream",
    status: "PENDING_PAYMENT",
    createdAt: "2026-04-27T00:00:00Z",
  };

  beforeEach(() => {
    vi.spyOn(globalThis, "fetch").mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("POSTs to /api/v1/donations with the payload as JSON and returns the created donation", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify(mockDonation), {
        status: 201,
        headers: { "Content-Type": "application/json" },
      })
    );

    const result = await createDonation(payload);

    expect(result).toEqual(mockDonation);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/v1/donations");
    expect(init?.method).toBe("POST");
    expect(init?.headers).toEqual({ "Content-Type": "application/json" });
    expect(JSON.parse(init?.body as string)).toEqual(payload);
  });

  it("throws when the server responds with a non-2xx status", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("validation_error", { status: 400 })
    );

    await expect(createDonation(payload)).rejects.toThrow("Failed to create donation");
  });

  it("propagates fetch transport errors", async () => {
    vi.spyOn(globalThis, "fetch").mockRejectedValue(new Error("network down"));

    await expect(createDonation(payload)).rejects.toThrow("network down");
  });
});
