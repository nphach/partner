import { useEffect, useState } from "react";

type HealthResponse = {
  status: string;
};

export default function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch("/api/health")
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        return response.json() as Promise<HealthResponse>;
      })
      .then(setHealth)
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "Request failed");
      });
  }, []);

  return (
    <main className="mx-auto flex min-h-screen max-w-2xl flex-col justify-center gap-4 p-8">
      <h1 className="text-3xl font-semibold tracking-tight">Partner</h1>
      <section className="rounded-lg border border-neutral-200 bg-neutral-50 p-4">
        <h2 className="text-sm font-medium text-neutral-500">Server health</h2>
        {health && (
          <p className="mt-2 font-mono text-sm">status: {health.status}</p>
        )}
        {error && (
          <p className="mt-2 text-sm text-red-600" role="alert">
            Could not reach API: {error}
          </p>
        )}
        {!health && !error && (
          <p className="mt-2 text-sm text-neutral-500">Checking…</p>
        )}
      </section>
    </main>
  );
}
