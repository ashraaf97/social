import { useCallback, useEffect, useRef, useState } from "react";
import { fetchTtsAudio, pollOverlay } from "../api";
import type { OverlayEvent } from "../models";

const POLL_MS = 1500;
const TTS_MAX_WAIT_MS = 12_000;
const TTS_RETRY_MS = 1_000;
const CARD_FALLBACK_MS = 5_000;
const CARD_EXIT_MS = 400;

async function waitForTtsAudio(donationId: number): Promise<string | null> {
  const deadline = Date.now() + TTS_MAX_WAIT_MS;
  while (Date.now() < deadline) {
    const url = await fetchTtsAudio(donationId);
    if (url) return url;
    await new Promise(r => setTimeout(r, TTS_RETRY_MS));
  }
  return null;
}

function playAudio(url: string): Promise<void> {
  return new Promise(resolve => {
    const audio = new Audio(url);
    audio.onended = () => resolve();
    audio.onerror = () => resolve();
    audio.play().catch(() => resolve());
  });
}

function delay(ms: number) {
  return new Promise<void>(r => setTimeout(r, ms));
}

export function OverlayPage() {
  const streamerId = new URLSearchParams(window.location.search).get("streamerId") ?? "streamer-demo";

  const cursorRef = useRef(0);
  const queueRef = useRef<OverlayEvent[]>([]);
  const processingRef = useRef(false);

  const [current, setCurrent] = useState<OverlayEvent | null>(null);
  const [visible, setVisible] = useState(false);

  const processQueue = useCallback(async () => {
    if (processingRef.current || queueRef.current.length === 0) return;
    processingRef.current = true;

    while (queueRef.current.length > 0) {
      const event = queueRef.current.shift()!;

      setCurrent(event);
      setVisible(true);

      const audioUrl = await waitForTtsAudio(event.donationId);

      if (audioUrl) {
        await playAudio(audioUrl);
        URL.revokeObjectURL(audioUrl);
      } else {
        await delay(CARD_FALLBACK_MS);
      }

      setVisible(false);
      await delay(CARD_EXIT_MS);
    }

    setCurrent(null);
    processingRef.current = false;
  }, []);

  useEffect(() => {
    const timer = window.setInterval(async () => {
      try {
        const payload = await pollOverlay(streamerId, cursorRef.current);
        cursorRef.current = payload.nextCursor;
        if (payload.events.length > 0) {
          queueRef.current.push(...payload.events);
          void processQueue();
        }
      } catch {
        // network hiccup — keep polling
      }
    }, POLL_MS);
    return () => window.clearInterval(timer);
  }, [streamerId, processQueue]);

  return (
    <section>
      <h1>Overlay Preview</h1>
      <div className={`overlayCard overlay-card${visible ? " overlay-card--visible" : ""}`}>
        {current && (
          <>
            <div className="overlay-sender">{current.senderName}</div>
            <div className="overlay-amount">{current.amount} {current.currency}</div>
            <div className="overlay-message">{current.messageText}</div>
          </>
        )}
      </div>
    </section>
  );
}
