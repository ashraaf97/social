import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api";
import { useAuth } from "../AuthContext";

type RoleTab = "STREAMER" | "ADMIN";

const ROLE_LABELS: Record<RoleTab, string> = {
  STREAMER: "Streamer",
  ADMIN: "Admin",
};

const ROLE_DESTINATIONS: Record<RoleTab, string> = {
  STREAMER: "/portal",
  ADMIN: "/admin",
};

export function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [selectedRole, setSelectedRole] = useState<RoleTab>("STREAMER");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await login(username, password);
      if (res.role !== selectedRole) {
        setError(
          selectedRole === "ADMIN"
            ? "This account does not have admin privileges."
            : "This is an admin account. Please select the Admin tab."
        );
        return;
      }
      signIn({ token: res.token, streamerId: res.streamerId, role: res.role });
      navigate(ROLE_DESTINATIONS[selectedRole]);
    } catch {
      setError("Invalid username or password");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="login-page">
      <div className="login-card">
        <div className="role-tabs">
          {(["STREAMER", "ADMIN"] as RoleTab[]).map((role) => (
            <button
              key={role}
              type="button"
              className={`role-tab${selectedRole === role ? " role-tab--active" : ""}`}
              onClick={() => { setSelectedRole(role); setError(""); }}
            >
              {ROLE_LABELS[role]}
            </button>
          ))}
        </div>

        <h1>Sign in as {ROLE_LABELS[selectedRole]}</h1>

        <form onSubmit={handleSubmit}>
          <label>
            Username
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? "Signing in…" : `Sign in as ${ROLE_LABELS[selectedRole]}`}
          </button>
        </form>
      </div>
    </section>
  );
}
