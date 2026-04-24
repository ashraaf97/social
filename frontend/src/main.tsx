import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext";
import { DonatePage } from "./pages/DonatePage";
import { LoginPage } from "./pages/LoginPage";
import { OverlayPage } from "./pages/OverlayPage";
import { StreamerPortalPage } from "./pages/StreamerPortalPage";
import "./styles.css";

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { auth } = useAuth();
  return auth ? <>{children}</> : <Navigate to="/login" replace />;
}

function Nav() {
  const { auth, signOut } = useAuth();
  return (
    <nav>
      <Link to="/">Donate</Link>
      {auth ? (
        <>
          <Link to="/portal">Streamer Portal</Link>
          <button className="nav-logout" onClick={signOut}>Logout</button>
        </>
      ) : (
        <Link to="/login">Login</Link>
      )}
      <Link to="/overlay">Overlay Preview</Link>
    </nav>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Nav />
        <Routes>
          <Route path="/" element={<DonatePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/portal"
            element={
              <RequireAuth>
                <StreamerPortalPage />
              </RequireAuth>
            }
          />
          <Route path="/overlay" element={<OverlayPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
