export type Donation = {
  id: number;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
  status: string;
  createdAt: string;
};

export type AuthResponse = {
  token: string;
  role: string;
  streamerId: string;
};

export type StreamerProfile = {
  id: number;
  username: string;
  email: string | null;
  streamerId: string;
  createdAt: string;
};

export type OverlayEvent = {
  id: number;
  donationId: number;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
  createdAt: string;
};

export type OverlayPollResult = {
  nextCursor: number;
  events: OverlayEvent[];
};

export type CreateDonationPayload = {
  streamerId?: string;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
  voiceProfile?: string;
};
