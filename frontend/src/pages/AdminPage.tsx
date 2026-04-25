import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import { listStreamers } from "../api";
import type { PageResponse, StreamerProfile } from "../models";

const PAGE_SIZE = 20;

export function AdminPage() {
  const { auth } = useAuth();
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PageResponse<StreamerProfile> | null>(null);
  const [status, setStatus] = useState("");

  const load = useCallback(
    async (p: number) => {
      try {
        const data = await listStreamers(auth!.token, p, PAGE_SIZE);
        setResult(data);
        setStatus(`${data.totalElements} streamer${data.totalElements === 1 ? "" : "s"} registered`);
      } catch {
        setStatus("Failed to load streamers");
      }
    },
    [auth]
  );

  useEffect(() => {
    void load(page);
  }, [load, page]);

  const streamers = result?.content ?? [];

  return (
    <section>
      <div className="page-header">
        <h1>Admin Portal</h1>
        <button onClick={() => void load(page)}>Refresh</button>
      </div>
      <p className="status-text">{status}</p>
      <table className="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Streamer ID</th>
            <th>Registered</th>
          </tr>
        </thead>
        <tbody>
          {streamers.map((s) => (
            <tr key={s.id}>
              <td>{s.id}</td>
              <td>{s.username}</td>
              <td>{s.email ?? "—"}</td>
              <td><code>{s.streamerId}</code></td>
              <td>{new Date(s.createdAt).toLocaleDateString()}</td>
            </tr>
          ))}
          {streamers.length === 0 && (
            <tr>
              <td colSpan={5} className="empty-row">No streamers found</td>
            </tr>
          )}
        </tbody>
      </table>
      {result && result.totalPages > 1 && (
        <div className="pagination">
          <button
            disabled={!result.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
          >
            Previous
          </button>
          <span>Page {result.page + 1} of {result.totalPages}</span>
          <button
            disabled={!result.hasNext}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </button>
        </div>
      )}
    </section>
  );
}
