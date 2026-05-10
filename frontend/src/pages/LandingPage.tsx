import { Link } from "react-router-dom";

export function LandingPage() {
  return (
    <div className="landing-page">
      <section className="landing-hero">
        <h1 className="landing-title">
          Monetization tools <span className="landing-title-accent">for creators</span>
        </h1>
        <p className="landing-subtitle">
          Receive support from your community with live streaming overlays,
          AI-powered text-to-speech, and a beautiful donation experience.
        </p>
        <div className="landing-cta">
          <Link to="/login" className="btn-primary-large">Sign In</Link>
          <a href="#features" className="btn-secondary-large">Learn More</a>
        </div>
      </section>

      <section id="features" className="landing-features">
        <h2 className="landing-section-title">Everything you need to engage your audience</h2>

        <div className="feature-grid">
          <div className="feature-card">
            <div className="feature-icon">$</div>
            <h3>Receive Support</h3>
            <p>Share your unique donation link with your community and start receiving tips in minutes.</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">@</div>
            <h3>Live Streaming Overlays</h3>
            <p>Beautiful, customizable overlays that make donations more exciting during your streams.</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">~</div>
            <h3>AI Text-to-Speech</h3>
            <p>Donor messages are read aloud with realistic AI voices to engage your audience.</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">#</div>
            <h3>Secure Tokens</h3>
            <p>Each streamer gets unique secure tokens for both donation and overlay links.</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">+</div>
            <h3>Replay Donations</h3>
            <p>Missed a moment? Replay donations on your overlay anytime from the streamer portal.</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">*</div>
            <h3>Modern Dashboard</h3>
            <p>Track all your donations, paginated and numbered for easy management.</p>
          </div>
        </div>
      </section>

      <section className="landing-how">
        <h2 className="landing-section-title">How it works</h2>
        <div className="how-steps">
          <div className="how-step">
            <div className="how-step-number">1</div>
            <h3>Sign In</h3>
            <p>Log in to your streamer account to access your unique tokens.</p>
          </div>
          <div className="how-step">
            <div className="how-step-number">2</div>
            <h3>Share Your Link</h3>
            <p>Share your secure donation link with your community.</p>
          </div>
          <div className="how-step">
            <div className="how-step-number">3</div>
            <h3>Add to Stream</h3>
            <p>Add your overlay URL as a Browser Source in OBS or Streamlabs.</p>
          </div>
          <div className="how-step">
            <div className="how-step-number">4</div>
            <h3>Engage Live</h3>
            <p>Donations appear live on your stream with TTS audio.</p>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <p>&copy; {new Date().getFullYear()} Streamer Donation Platform. Built for creators.</p>
      </footer>
    </div>
  );
}
