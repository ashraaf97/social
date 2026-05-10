import { FormEvent, useEffect, useState } from "react";
import { createDonation, fetchDonationStreamer, markDonationPaid } from "../api";
import type { DonationStreamer } from "../models";

export function DonatePage() {
  const token = new URLSearchParams(window.location.search).get("token") ?? "";

  const [streamer, setStreamer] = useState<DonationStreamer | null>(null);
  const [loadingStreamer, setLoadingStreamer] = useState(true);
  const [streamerError, setStreamerError] = useState("");

  const [senderName, setSenderName] = useState("");
  const [amount, setAmount] = useState(5);
  const [currency, setCurrency] = useState("MYR");
  const [messageText, setMessageText] = useState("");
  const [status, setStatus] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!token) {
      setStreamerError("Missing donation token. Please use a valid donation link.");
      setLoadingStreamer(false);
      return;
    }
    void fetchDonationStreamer(token)
      .then(setStreamer)
      .catch(() => setStreamerError("Invalid or expired donation link."))
      .finally(() => setLoadingStreamer(false));
  }, [token]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!token) return;

    setSubmitting(true);
    setStatus("Submitting donation...");
    try {
      const donation = await createDonation({
        donationToken: token,
        senderName,
        amount,
        currency,
        messageText,
      });
      setStatus(`Processing payment...`);
      await markDonationPaid(donation.id);
      setStatus(`Donation sent! Your message will appear on stream shortly.`);
      setSenderName("");
      setMessageText("");
      setAmount(5);
    } catch {
      setStatus("Failed to submit donation.");
    } finally {
      setSubmitting(false);
    }
  }

  if (loadingStreamer) {
    return (
      <section className="donate-page">
        <p className="status-text">Loading...</p>
      </section>
    );
  }

  if (streamerError || !streamer) {
    return (
      <section className="donate-page">
        <div className="donate-error">
          <h1>Invalid Link</h1>
          <p>{streamerError || "Streamer not found."}</p>
        </div>
      </section>
    );
  }

  return (
    <section className="donate-page">
      <div className="donate-card">
        <div className="donate-streamer">
          <div className="donate-avatar">{streamer.username.charAt(0).toUpperCase()}</div>
          <h1>Support {streamer.username}</h1>
          <p>Send a message and a tip to show your support.</p>
        </div>

        <form onSubmit={submit}>
          <label>
            Your Name
            <input
              value={senderName}
              onChange={(e) => setSenderName(e.target.value)}
              placeholder="Your name"
              required
              maxLength={120}
            />
          </label>

          <label>
            Amount
            <div className="amount-row">
              <input
                type="number"
                min={0.01}
                step={0.01}
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
                required
              />
              <input
                className="currency-input"
                value={currency}
                onChange={(e) => setCurrency(e.target.value.toUpperCase())}
                placeholder="MYR"
                maxLength={16}
                required
              />
            </div>
          </label>

          <label>
            Message
            <textarea
              value={messageText}
              onChange={(e) => setMessageText(e.target.value)}
              placeholder={`Leave a message for ${streamer.username}...`}
              required
              maxLength={1000}
            />
          </label>

          <button type="submit" disabled={submitting}>
            {submitting ? "Sending..." : `Send ${amount} ${currency}`}
          </button>
        </form>
        {status && <p className="status-text">{status}</p>}
      </div>
    </section>
  );
}
