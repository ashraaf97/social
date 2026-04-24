import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import { Donation, listDonations, replayDonation } from "../api";

export function StreamerPortalPage() {
  const { auth } = useAuth();
  const [donations, setDonations] = useState<Donation[]>([]);
  const [status, setStatus] = useState("");

  async function load() {
    try {
      const result = await listDonations(auth!.token);
      setDonations(result);
      setStatus(`Loaded ${result.length} donations`);
    } catch {
      setStatus("Failed to load donations");
    }
  }

  async function replay(id: number) {
    try {
      await replayDonation(id, auth!.token);
      setStatus(`Replayed donation #${id}`);
    } catch {
      setStatus("Replay failed");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <section>
      <h1>Streamer Portal</h1>
      <button onClick={load}>Refresh</button>
      <p>{status}</p>
      <ul>
        {donations.map((item) => (
          <li key={item.id}>
            <b>{item.senderName}</b> sent {item.amount} {item.currency} — {item.messageText} ({item.status})
            <button onClick={() => replay(item.id)}>Replay</button>
          </li>
        ))}
      </ul>
    </section>
  );
}
