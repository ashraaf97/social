import { useEffect, useRef, useState } from "react";
import { pollOverlay } from "../api";

type OverlayEvent = {
  id: number;
  senderName: string;
  amount: number;
  currency: string;
  messageText: string;
};

export function OverlayPage() {
  const cursorRef = useRef(0);
  const [current, setCurrent] = useState<OverlayEvent | null>(null);

  useEffect(() => {
    const pollTimer = window.setInterval(async () => {
      try {
        const payload = await pollOverlay("streamer-demo", cursorRef.current);
        cursorRef.current = payload.nextCursor;
        if (payload.events.length > 0) {
          setCurrent(payload.events[0]);
        }
      } catch {
        return;
      }
    }, 1500);
    return () => window.clearInterval(pollTimer);
  }, []);

  return (
    <section>
      <h1>Overlay Preview</h1>
      {current ? (
        <div className="overlayCard">
          <div>{current.senderName}</div>
          <div>{current.amount} {current.currency}</div>
          <div>{current.messageText}</div>
        </div>
      ) : (
        <p>No events yet</p>
      )}
    </section>
  );
}
