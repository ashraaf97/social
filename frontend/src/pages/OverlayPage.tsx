import { useCallback, useEffect, useRef, useState } from "react";
import { fetchOverlayConfig, fetchTtsAudio, pollOverlay } from "../api";
import { parseOverlayPosition } from "../components/OverlayPositionPicker";
import type { OverlayEvent, OverlayPositionValue } from "../models";

const POLL_MS = 1500;
const CONFIG_REFRESH_MS = 10_000;
const TTS_MAX_WAIT_MS = 12_000;
const TTS_RETRY_MS = 1_000;
const CARD_FALLBACK_MS = 7_000;
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

function positionToContainerClass(position: OverlayPositionValue): string {
  return `overlay-container--${position.toLowerCase().replace(/_/g, "-")}`;
}

export function OverlayPage() {
  const token = new URLSearchParams(window.location.search).get("token") ?? "";

  const cursorRef = useRef(0);
  const queueRef = useRef<OverlayEvent[]>([]);
  const processingRef = useRef(false);
  const initializedRef = useRef(false);

  const [current, setCurrent] = useState<OverlayEvent | null>(null);
  const [visible, setVisible] = useState(false);
  const [layoutPosition, setLayoutPosition] = useState<OverlayPositionValue>("CENTER");

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
    document.body.classList.add("overlay-mode");
    return () => {
      document.body.classList.remove("overlay-mode");
    };
  }, []);

  useEffect(() => {
    async function refreshLayout() {
      if (!token) return;
      try {
        const cfg = await fetchOverlayConfig(token);
        setLayoutPosition(parseOverlayPosition(cfg.position));
      } catch {
        // keep previous layout
      }
    }
    void refreshLayout();
    const timer = window.setInterval(refreshLayout, CONFIG_REFRESH_MS);
    return () => window.clearInterval(timer);
  }, [token]);

  useEffect(() => {
    async function initializeCursor() {
      try {
        const payload = await pollOverlay(token, 0);
        cursorRef.current = payload.nextCursor;
        if (payload.events.length > 0) {
          queueRef.current.push(...payload.events);
        }
        initializedRef.current = true;
        void processQueue();
      } catch {
        initializedRef.current = true;
      }
    }
    void initializeCursor();
  }, [token, processQueue]);

  useEffect(() => {
    const timer = window.setInterval(async () => {
      if (!initializedRef.current) return;

      try {
        const payload = await pollOverlay(token, cursorRef.current);
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
  }, [token, processQueue]);

  return (
    <div className={`overlay-container ${positionToContainerClass(layoutPosition)}`}>
      <div className={`overlay-card${visible ? " overlay-card--visible" : ""}`}>
        {current && (
          <>
            <div className="overlay-sender">{current.senderName}</div>
            <div className="overlay-amount">
              {current.amount} {current.currency}
            </div>
            <div className="overlay-message">{current.messageText}</div>
          </>
        )}
      </div>
    </div>
  );
}
