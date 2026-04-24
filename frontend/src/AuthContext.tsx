import { createContext, useContext, useState, ReactNode } from "react";
import { logout as apiLogout } from "./api";

type AuthState = {
  token: string;
  streamerId: string;
  role: string;
};

type AuthContextType = {
  auth: AuthState | null;
  signIn: (state: AuthState) => void;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = "auth";

function loadFromStorage(): AuthState | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthState) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<AuthState | null>(loadFromStorage);

  function signIn(state: AuthState) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    setAuth(state);
  }

  async function signOut() {
    if (auth) {
      try {
        await apiLogout(auth.token);
      } catch {
        // best-effort — clear locally regardless
      }
    }
    localStorage.removeItem(STORAGE_KEY);
    setAuth(null);
  }

  return (
    <AuthContext.Provider value={{ auth, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
