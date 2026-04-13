export type Donation = {
  id: number;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
  status: string;
  createdAt: string;
};

const baseUrl = "";

export async function createDonation(payload: {
  streamerId?: string;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
  voiceProfile?: string;
}) {
  const response = await fetch(`${baseUrl}/api/v1/donations`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    throw new Error("Failed to create donation");
  }
  return response.json();
}

export async function listDonations(streamerKey: string): Promise<Donation[]> {
  const response = await fetch(`${baseUrl}/api/v1/streamer/donations`, {
    headers: { "X-Streamer-Key": streamerKey }
  });
  if (!response.ok) {
    throw new Error("Failed to load donations");
  }
  return response.json();
}

export async function replayDonation(donationId: number, streamerKey: string): Promise<void> {
  const response = await fetch(`${baseUrl}/api/v1/streamer/donations/${donationId}/replay`, {
    method: "POST",
    headers: { "X-Streamer-Key": streamerKey }
  });
  if (!response.ok) {
    throw new Error("Failed to replay donation");
  }
}

export async function pollOverlay(streamerId: string, cursor: number) {
  const response = await fetch(`${baseUrl}/api/v1/overlay/events?streamerId=${encodeURIComponent(streamerId)}&cursor=${cursor}`);
  if (!response.ok) {
    throw new Error("Failed to poll overlay events");
  }
  return response.json();
}
