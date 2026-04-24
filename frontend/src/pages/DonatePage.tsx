import { FormEvent, useState } from "react";
import { createDonation } from "../api";

export function DonatePage() {
  const [senderName, setSenderName] = useState("");
  const [amount, setAmount] = useState(5);
  const [currency, setCurrency] = useState("USD");
  const [messageText, setMessageText] = useState("");
  const [status, setStatus] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setStatus("Submitting...");
    try {
      const donation = await createDonation({ senderName, amount, currency, messageText });
      setStatus(`Created donation #${donation.id}. Mark as paid via API to trigger overlay.`);
      setMessageText("");
    } catch {
      setStatus("Failed to submit donation.");
    }
  }

  return (
    <section>
      <h1>Send Donation</h1>
      <form onSubmit={submit}>
        <input
          value={senderName}
          onChange={(e) => setSenderName(e.target.value)}
          placeholder="Your name"
          required
        />
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
            placeholder="USD"
            maxLength={16}
            required
          />
        </div>
        <textarea
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          placeholder="Message for streamer"
          required
        />
        <button type="submit">Donate</button>
      </form>
      <p>{status}</p>
    </section>
  );
}
