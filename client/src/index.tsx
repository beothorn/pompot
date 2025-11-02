import React from 'react';
import { createRoot } from 'react-dom/client';

type ParsedPomResponse = {
  projectRoot: string;
  model: unknown;
};

type LoadState = 'loading' | 'ready' | 'empty' | 'error';

const isRecord = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
};

const isScalar = (value: unknown): boolean => {
  return (
    typeof value === 'string' ||
    typeof value === 'number' ||
    typeof value === 'boolean' ||
    value === null
  );
};

const formatScalar = (value: unknown): string => {
  if (value === null) {
    return 'null';
  }

  if (typeof value === 'string') {
    return `'${value}'`;
  }

  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }

  return String(value);
};

const formatValue = (value: unknown, indent = 0): string => {
  const spacing = ' '.repeat(indent);

  if (isScalar(value)) {
    return `${spacing}${formatScalar(value)}`;
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return `${spacing}[]`;
    }

    return value
      .map((item) => {
        if (isScalar(item)) {
          return `${spacing}- ${formatScalar(item)}`;
        }

        const formattedItem = formatValue(item, indent + 2);
        return `${spacing}-\n${formattedItem}`;
      })
      .join('\n');
  }

  if (isRecord(value)) {
    const entries = Object.entries(value);
    if (entries.length === 0) {
      return `${spacing}{}`;
    }

    return entries
      .map(([key, entryValue]) => {
        if (isScalar(entryValue)) {
          return `${spacing}${key}: ${formatScalar(entryValue)}`;
        }

        const formattedEntry = formatValue(entryValue, indent + 2);
        return `${spacing}${key}:\n${formattedEntry}`;
      })
      .join('\n');
  }

  return `${spacing}${String(value)}`;
};

export const App: React.FC = () => {
  const [status, setStatus] = React.useState<LoadState>('loading');
  const [parsedPom, setParsedPom] = React.useState<ParsedPomResponse | null>(null);
  const [errorMessage, setErrorMessage] = React.useState<string>('');

  React.useEffect(() => {
    const controller = new AbortController();

    setStatus('loading');
    setErrorMessage('');

    fetch('/api/pom', { signal: controller.signal })
      .then((response) => {
        if (response.status === 404) {
          setParsedPom(null);
          setStatus('empty');
          return null;
        }

        if (!response.ok) {
          throw new Error(`Request failed with status ${response.status}`);
        }

        return response.json() as Promise<ParsedPomResponse>;
      })
      .then((data) => {
        if (!data) {
          return;
        }

        setParsedPom(data);
        setStatus('ready');
      })
      .catch((error) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return;
        }

        const message = error instanceof Error ? error.message : 'Unexpected error while loading pom data.';
        setErrorMessage(message);
        setStatus('error');
      });

    return () => {
      controller.abort();
    };
  }, []);

  const formattedPom = React.useMemo(() => {
    if (!parsedPom) {
      return '';
    }

    return formatValue(parsedPom.model);
  }, [parsedPom]);

  return (
    <main
      style={{
        fontFamily: 'Inter, system-ui, sans-serif',
        margin: '2rem auto',
        maxWidth: '960px',
        lineHeight: 1.5,
        color: '#1f2933',
      }}
    >
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ marginBottom: '0.25rem' }}>Project pom overview</h1>
        <p style={{ margin: 0, color: '#52606d' }}>
          The application reads the pom defined by <code>--project=&lt;path&gt;</code> and shares it with the UI.
        </p>
      </header>

      {status === 'loading' && <p>Loading parsed pom information…</p>}

      {status === 'empty' && (
        <section>
          <p style={{ marginBottom: '0.5rem' }}>No pom information is available.</p>
          <p style={{ margin: 0 }}>
            Start the server with <code>--project=/path/to/project</code> so Pompot can parse the pom file.
          </p>
        </section>
      )}

      {status === 'error' && (
        <section>
          <p style={{ marginBottom: '0.5rem', color: '#b91d47' }}>Could not load the pom details.</p>
          <pre
            style={{
              background: '#fff5f5',
              borderRadius: '0.5rem',
              padding: '1rem',
              overflowX: 'auto',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word',
              margin: 0,
            }}
          >
            {errorMessage}
          </pre>
        </section>
      )}

      {status === 'ready' && parsedPom && (
        <section>
          <div style={{ marginBottom: '1rem' }}>
            <strong>Project path:</strong>{' '}
            <code>{parsedPom.projectRoot}</code>
          </div>
          <pre
            style={{
              background: '#f5f7fa',
              borderRadius: '0.75rem',
              padding: '1.25rem',
              overflowX: 'auto',
              fontFamily: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace',
              fontSize: '0.95rem',
              lineHeight: 1.6,
              whiteSpace: 'pre',
              margin: 0,
            }}
          >
            {formattedPom}
          </pre>
        </section>
      )}
    </main>
  );
};

const rootElement = document.getElementById('root');

if (rootElement) {
  const root = createRoot(rootElement);

  root.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
} else if (process.env.NODE_ENV !== 'test') {
  throw new Error('Root element not found');
}

