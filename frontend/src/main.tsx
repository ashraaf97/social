import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import { DonatePage } from "./pages/DonatePage";
import { StreamerPortalPage } from "./pages/StreamerPortalPage";
import { OverlayPage } from "./pages/OverlayPage";
import "./styles.css";

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Donate</Link>
        <Link to="/portal">Streamer Portal</Link>
        <Link to="/overlay">Overlay Preview</Link>
      </nav>
      <Routes>
        <Route path="/" element={<DonatePage />} />
        <Route path="/portal" element={<StreamerPortalPage />} />
        <Route path="/overlay" element={<OverlayPage />} />
      </Routes>
    </BrowserRouter>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
