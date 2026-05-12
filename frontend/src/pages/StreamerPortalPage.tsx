import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import {
  getStreamerOverlaySettings,
  listDonations,
  replayDonation,
  updateStreamerOverlaySettings,
} from "../api";
import { OverlayPositionPicker, parseOverlayPosition } from "../components/OverlayPositionPicker";
import type { Donation, OverlayPositionValue } from "../models";

export function StreamerPortalPage() {
  const { auth } = useAuth();
  const [donations, setDonations] = useState<Donation[]>([]);
  const [status, setStatus] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [overlayPosition, setOverlayPosition] = useState<OverlayPositionValue>("CENTER");
  const [savingLayout, setSavingLayout] = useState(false);
  const itemsPerPage = 10;

  async function load() {
    try {
      const result = await listDonations(auth!.token);
      setDonations(result);
      setStatus(`Loaded ${result.length} donation${result.length !== 1 ? 's' : ''}`);
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

  useEffect(() => {
    if (!auth?.token) return;
    void getStreamerOverlaySettings(auth.token)
      .then(c => setOverlayPosition(parseOverlayPosition(c.position)))
      .catch(() => {});
  }, [auth?.token]);

  async function handleOverlayPositionChange(next: OverlayPositionValue) {
    if (!auth?.token) return;
    setOverlayPosition(next);
    setSavingLayout(true);
    try {
      await updateStreamerOverlaySettings(auth.token, next);
      setStatus("Overlay position saved — refresh OBS browser source if it was already open.");
    } catch {
      setStatus("Failed to save overlay position");
    } finally {
      setSavingLayout(false);
    }
  }

  const overlayUrl = `/overlay?token=${auth!.overlayToken}`;
  const donateUrl = `/donate?token=${auth!.donationToken}`;

  const totalPages = Math.max(1, Math.ceil(donations.length / itemsPerPage));
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentDonations = donations.slice(startIndex, endIndex);
  const startNumber = startIndex + 1;

  return (
    <section>
      <h1>Streamer Portal</h1>

      <OverlayPositionPicker
        value={overlayPosition}
        onChange={handleOverlayPositionChange}
        disabled={savingLayout}
      />

      <div className="link-box">
        <h3>Your Donation Link</h3>
        <p>Share this link with your community to receive donations:</p>
        <code className="link-url">
          {window.location.origin}{donateUrl}
        </code>
        <a href={donateUrl} target="_blank" rel="noopener noreferrer" className="btn-preview">
          Open Donation Page
        </a>
      </div>

      <div className="link-box">
        <h3>Your Overlay URL</h3>
        <p>Add this URL as a Browser Source in OBS/Streamlabs:</p>
        <code className="link-url">
          {window.location.origin}{overlayUrl}
        </code>
        <a href={overlayUrl} target="_blank" rel="noopener noreferrer" className="btn-preview">
          Preview Overlay
        </a>
      </div>
      <button onClick={load}>Refresh Donations</button>
      
      <ul className="donation-list">
        {currentDonations.map((item, index) => (
          <li key={item.id}>
            <div className="donation-number">#{startNumber + index}</div>
            <div className="donation-info">
              <b>{item.senderName}</b> sent {item.amount} {item.currency} — {item.messageText} ({item.status})
            </div>
            <div className="donation-actions">
              <button onClick={() => replay(item.id)}>Replay</button>
            </div>
          </li>
        ))}
      </ul>

      {donations.length > 0 && (
        <div className="donation-footer">
          <p className="status-text">{status}</p>
          <div className="pagination-info">
            Page {currentPage} of {totalPages} • {donations.length} total result{donations.length !== 1 ? 's' : ''}
          </div>
          {totalPages > 1 && (
            <div className="pagination">
              <button 
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
              >
                Previous
              </button>
              <span>Page {currentPage} of {totalPages}</span>
              <button 
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}

      {donations.length === 0 && status && (
        <p className="status-text">{status}</p>
      )}
    </section>
  );
}
