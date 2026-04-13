import { FormEvent, useState } from "react";
import { createDonation } from "../api";

export function DonatePage() {
  const [senderName, setSenderName] = useState("");
  const [amount, setAmount] = useState(5);
  const [messageText, setMessageText] = useState("");
  const [status, setStatus] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setStatus("Submitting...");
    try {
      const donation = await createDonation({
        senderName,
        amount,
        currency: "USD",
        messageText
      });
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
        <input value={senderName} onChange={(e) => setSenderName(e.target.value)} placeholder="Your name" required />
        <input type="number" min={1} value={amount} onChange={(e) => setAmount(Number(e.target.value))} required />
        <textarea value={messageText} onChange={(e) => setMessageText(e.target.value)} placeholder="Message for streamer" required />
        <button type="submit">Donate</button>
      </form>
      <p>{status}</p>
    </section>
  );
}
