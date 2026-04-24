import type {
  AuthResponse,
  CreateDonationPayload,
  Donation,
  OverlayEvent,
  OverlayPollResult,
  StreamerProfile,
} from "./models";

export type {
  AuthResponse,
  CreateDonationPayload,
  Donation,
  OverlayEvent,
  OverlayPollResult,
  StreamerProfile,
};

const baseUrl = "";

function bearer(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export async function login(username: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${baseUrl}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) {
    throw new Error("Invalid credentials");
  }
  return response.json();
}

export async function logout(token: string): Promise<void> {
  await fetch(`${baseUrl}/auth/logout`, {
    method: "POST",
    headers: bearer(token),
  });
}

export async function createDonation(payload: CreateDonationPayload): Promise<Donation> {
  const response = await fetch(`${baseUrl}/api/v1/donations`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error("Failed to create donation");
  }
  return response.json();
}

export async function listDonations(token: string): Promise<Donation[]> {
  const response = await fetch(`${baseUrl}/api/v1/streamer/donations`, {
    headers: bearer(token),
  });
  if (!response.ok) {
    throw new Error("Failed to load donations");
  }
  return response.json();
}

export async function replayDonation(donationId: number, token: string): Promise<void> {
  const response = await fetch(`${baseUrl}/api/v1/streamer/donations/${donationId}/replay`, {
    method: "POST",
    headers: bearer(token),
  });
  if (!response.ok) {
    throw new Error("Failed to replay donation");
  }
}

export async function listStreamers(token: string): Promise<StreamerProfile[]> {
  const response = await fetch(`${baseUrl}/api/v1/admin/streamers`, {
    headers: bearer(token),
  });
  if (!response.ok) {
    throw new Error("Failed to load streamers");
  }
  return response.json();
}

export async function pollOverlay(streamerId: string, cursor: number): Promise<OverlayPollResult> {
  const response = await fetch(
    `${baseUrl}/api/v1/overlay/events?streamerId=${encodeURIComponent(streamerId)}&cursor=${cursor}`
  );
  if (!response.ok) {
    throw new Error("Failed to poll overlay events");
  }
  return response.json();
}

export async function fetchTtsAudio(donationId: number): Promise<string | null> {
  const response = await fetch(`${baseUrl}/api/v1/overlay/tts/${donationId}`);
  if (!response.ok) return null;
  const blob = await response.blob();
  return URL.createObjectURL(blob);
}
