import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext";
import { AdminPage } from "./pages/AdminPage";
import { DonatePage } from "./pages/DonatePage";
import { LandingPage } from "./pages/LandingPage";
import { LoginPage } from "./pages/LoginPage";
import { OverlayPage } from "./pages/OverlayPage";
import { StreamerPortalPage } from "./pages/StreamerPortalPage";
import "./styles.css";

function RequireAuth({ role, children }: { role?: string; children: React.ReactNode }) {
  const { auth } = useAuth();
  if (!auth) return <Navigate to="/login" replace />;
  if (role && auth.role !== role) return <Navigate to="/" replace />;
  return <>{children}</>;
}

function Nav() {
  const { auth, signOut } = useAuth();
  return (
    <nav>
      <Link to="/" className="nav-brand">StreamDonate</Link>
      {auth ? (
        <>
          {auth.role === "ADMIN" && <Link to="/admin">Admin Portal</Link>}
          {auth.role === "STREAMER" && <Link to="/portal">Streamer Portal</Link>}
          <span className="nav-username">{auth.role === "ADMIN" ? "Admin" : auth.streamerId}</span>
          <button className="nav-logout" onClick={signOut}>Logout</button>
        </>
      ) : (
        <Link to="/login">Login</Link>
      )}
    </nav>
  );
}

function AppContent() {
  const location = useLocation();
  const isOverlay = location.pathname === "/overlay";

  return (
    <>
      {!isOverlay && <Nav />}
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/donate" element={<DonatePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/portal"
          element={
            <RequireAuth role="STREAMER">
              <StreamerPortalPage />
            </RequireAuth>
          }
        />
        <Route
          path="/admin"
          element={
            <RequireAuth role="ADMIN">
              <AdminPage />
            </RequireAuth>
          }
        />
        <Route path="/overlay" element={<OverlayPage />} />
      </Routes>
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
