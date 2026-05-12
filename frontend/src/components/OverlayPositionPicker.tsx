import type { OverlayPositionValue } from "../models";
import { OVERLAY_POSITION_VALUES } from "../models";

const GRID_ORDER: OverlayPositionValue[] = [
  "TOP_LEFT",
  "TOP",
  "TOP_RIGHT",
  "LEFT",
  "CENTER",
  "RIGHT",
  "BOTTOM_LEFT",
  "BOTTOM",
  "BOTTOM_RIGHT",
];

const LABELS: Record<OverlayPositionValue, string> = {
  TOP_LEFT: "Top left",
  TOP: "Top center",
  TOP_RIGHT: "Top right",
  LEFT: "Middle left",
  CENTER: "Center",
  RIGHT: "Middle right",
  BOTTOM_LEFT: "Bottom left",
  BOTTOM: "Bottom center",
  BOTTOM_RIGHT: "Bottom right",
};

function toKebab(p: OverlayPositionValue): string {
  return p.toLowerCase().replace(/_/g, "-");
}

type Props = {
  value: OverlayPositionValue;
  onChange: (next: OverlayPositionValue) => void;
  disabled?: boolean;
};

export function OverlayPositionPicker({ value, onChange, disabled }: Props) {
  return (
    <div className="overlay-position-picker">
      <h3>Message box position</h3>
      <p className="position-picker-desc">
        Choose where donation alerts appear on your stream preview. The sample card moves as you
        select a zone.
      </p>
      <div className="position-picker-frame" aria-hidden={disabled}>
        <div className="position-picker-grid">
          {GRID_ORDER.map(pos => (
            <button
              key={pos}
              type="button"
              className={`position-picker-cell${value === pos ? " position-picker-cell--selected" : ""}`}
              onClick={() => !disabled && onChange(pos)}
              disabled={disabled}
              aria-pressed={value === pos}
              aria-label={LABELS[pos]}
            />
          ))}
        </div>
        <div className={`position-picker-sample-wrap position-picker-sample-wrap--${toKebab(value)}`}>
          <div className="overlay-card overlay-card--position-preview">
            <div className="overlay-sender">Preview</div>
            <div className="overlay-amount">10 MYR</div>
            <div className="overlay-message">Your alerts will show here</div>
          </div>
        </div>
      </div>
      <p className="position-picker-current">
        Selected: <strong>{LABELS[value]}</strong>
      </p>
    </div>
  );
}

export function parseOverlayPosition(raw: string | undefined | null): OverlayPositionValue {
  const u = (raw ?? "CENTER").toUpperCase();
  return (OVERLAY_POSITION_VALUES as readonly string[]).includes(u)
    ? (u as OverlayPositionValue)
    : "CENTER";
}
