import { useEffect, useState } from "react";
import { useAuth } from "../AuthContext";
import { StreamerProfile, listStreamers } from "../api";

export function AdminPage() {
  const { auth } = useAuth();
  const [streamers, setStreamers] = useState<StreamerProfile[]>([]);
  const [status, setStatus] = useState("");

  async function load() {
    try {
      const result = await listStreamers(auth!.token);
      setStreamers(result);
      setStatus(`${result.length} streamer${result.length !== 1 ? "s" : ""} registered`);
    } catch {
      setStatus("Failed to load streamers");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <section>
      <div className="page-header">
        <h1>Admin Portal</h1>
        <button onClick={load}>Refresh</button>
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
    </section>
  );
}
