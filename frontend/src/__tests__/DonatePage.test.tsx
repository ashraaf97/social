import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent, { type UserEvent } from "@testing-library/user-event";
import { DonatePage } from "../pages/DonatePage";
import * as api from "../api";

type FormOverrides = Partial<{ name: string; amount: string; currency: string; message: string }>;

async function fillForm({
  name = "Alice",
  amount = "20",
  currency = "eur",
  message = "thanks!",
}: FormOverrides = {}): Promise<UserEvent> {
  const user = userEvent.setup();
  await user.clear(screen.getByPlaceholderText("Your name"));
  await user.type(screen.getByPlaceholderText("Your name"), name);
  const amountInput = screen.getByRole("spinbutton");
  await user.clear(amountInput);
  await user.type(amountInput, amount);
  const currencyInput = screen.getByPlaceholderText("USD");
  await user.clear(currencyInput);
  await user.type(currencyInput, currency);
  await user.clear(screen.getByPlaceholderText("Message for streamer"));
  await user.type(screen.getByPlaceholderText("Message for streamer"), message);
  return user;
}

describe("<DonatePage />", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders the form with default amount and uppercase currency", () => {
    render(<DonatePage />);

    expect(screen.getByRole("heading", { name: /send donation/i })).toBeInTheDocument();
    expect(screen.getByRole("spinbutton")).toHaveValue(5);
    expect(screen.getByPlaceholderText("USD")).toHaveValue("USD");
    expect(screen.getByRole("button", { name: /donate/i })).toBeInTheDocument();
  });

  it("uppercases the currency input as the user types", async () => {
    const user = userEvent.setup();
    render(<DonatePage />);

    const currencyInput = screen.getByPlaceholderText("USD") as HTMLInputElement;
    await user.clear(currencyInput);
    await user.type(currencyInput, "eur");

    expect(currencyInput.value).toBe("EUR");
  });

  it("submits the donation, shows the success status, and clears the message field", async () => {
    const createDonationSpy = vi.spyOn(api, "createDonation").mockResolvedValue({
      id: 7,
      senderName: "Alice",
      amount: 20,
      currency: "EUR",
      messageText: "thanks!",
      status: "PENDING_PAYMENT",
      createdAt: "2026-04-27T00:00:00Z",
    });

    render(<DonatePage />);
    const user = await fillForm();
    await user.click(screen.getByRole("button", { name: /donate/i }));

    await waitFor(() => {
      expect(createDonationSpy).toHaveBeenCalledWith({
        senderName: "Alice",
        amount: 20,
        currency: "EUR",
        messageText: "thanks!",
      } satisfies api.CreateDonationPayload);
    });

    expect(await screen.findByText(/created donation #7/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Message for streamer")).toHaveValue("");
    expect(screen.getByPlaceholderText("Your name")).toHaveValue("Alice");
  });

  it("displays a failure status when createDonation rejects and keeps the message intact", async () => {
    vi.spyOn(api, "createDonation").mockRejectedValue(new Error("Failed to create donation"));

    render(<DonatePage />);
    const user = await fillForm({ message: "should stay" });
    await user.click(screen.getByRole("button", { name: /donate/i }));

    expect(await screen.findByText(/failed to submit donation/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Message for streamer")).toHaveValue("should stay");
  });

  it("blocks submission when required fields are empty (HTML5 validation)", async () => {
    const createDonationSpy = vi.spyOn(api, "createDonation");

    render(<DonatePage />);
    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: /donate/i }));

    expect(createDonationSpy).not.toHaveBeenCalled();
  });
});
