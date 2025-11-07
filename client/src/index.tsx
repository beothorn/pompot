import React from 'react';
import { createRoot } from 'react-dom/client';
import styled from 'styled-components';

type ParsedPomEntry = {
  pomPath: string;
  relativePath: string;
  groupId: string | null;
  artifactId: string | null;
  model: unknown;
};

type ParsedPomResponse = {
  scannedRoot: string;
  entries: ParsedPomEntry[];
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

const Page = styled.main`
  font-family: 'Inter, system-ui, sans-serif';
  margin: 2rem auto 3rem;
  max-width: 960px;
  line-height: 1.5;
  color: #1f2933;
  padding: 0 1.5rem;
`;

const PageHeader = styled.header`
  margin-bottom: 2.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const Title = styled.h1`
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
`;

const Subtitle = styled.p`
  margin: 0;
  color: #52606d;
  font-size: 1rem;
`;

const StatusSection = styled.section`
  background: #f5f7fa;
  border-radius: 0.75rem;
  padding: 1.25rem;
  color: #1f2933;
  box-shadow: inset 0 0 0 1px #d9e2ec;
`;

const GroupsContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 2rem;
`;

const GroupSection = styled.section`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const GroupTitle = styled.h2`
  margin: 0;
  font-size: 1.35rem;
  font-weight: 600;
  color: #243b53;
`;

const PomDetails = styled.details`
  border-radius: 0.75rem;
  border: 1px solid #d9e2ec;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.1);
  overflow: hidden;
  transition: box-shadow 0.2s ease;

  &[open] {
    box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
  }
`;

const PomSummary = styled.summary`
  cursor: pointer;
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 1.25rem;
  list-style: none;
  background: #f0f4f8;
  color: #1f2933;
  font-weight: 600;
  user-select: none;
  transition: background 0.2s ease;

  &:hover {
    background: #e1e8f0;
  }

  &::-webkit-details-marker {
    display: none;
  }
`;

const SummaryPath = styled.span`
  font-family: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace';
  font-size: 0.9rem;
  color: #334155;
`;

const SummaryMeta = styled.span`
  font-size: 0.9rem;
  color: #52606d;
`;

const SummaryIcon = styled.span`
  margin-left: auto;
  color: #3a4b6a;
  font-size: 0.9rem;
  transition: transform 0.2s ease;

  ${PomDetails}[open] & {
    transform: rotate(180deg);
  }
`;

const PomBody = styled.div`
  padding: 1rem 1.25rem 1.5rem;
  border-top: 1px solid #d9e2ec;
  background: #ffffff;
`;

const Metadata = styled.div`
  margin-bottom: 1rem;
  display: grid;
  gap: 0.35rem;
  color: #3a4b6a;
  font-size: 0.95rem;

  code {
    font-family: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace';
    background: #f0f4f8;
    border-radius: 0.5rem;
    padding: 0.1rem 0.4rem;
    color: #243b53;
    font-size: 0.9rem;
  }
`;

const MetadataLabel = styled.span`
  font-weight: 600;
  color: #1f2933;
`;

const CodeBlock = styled.pre`
  background: #10172a;
  color: #e5e9f0;
  border-radius: 0.75rem;
  padding: 1.25rem;
  overflow-x: auto;
  font-family: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace';
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0;
`;

const RootPath = styled.div`
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
  color: #3a4b6a;

  code {
    font-family: 'SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace';
    font-size: 0.9rem;
    background: #f0f4f8;
    border-radius: 0.5rem;
    padding: 0.15rem 0.45rem;
    color: #243b53;
  }
`;

const UNKNOWN_GROUP_LABEL = '(no group id)';
const UNKNOWN_ARTIFACT_LABEL = '(no artifact id)';

const normalizeCoordinatePart = (value: string | null | undefined, fallback: string): string => {
  if (typeof value !== 'string') {
    return fallback;
  }

  const trimmed = value.trim();
  return trimmed.length === 0 ? fallback : trimmed;
};

export const App: React.FC = () => {
  const [status, setStatus] = React.useState<LoadState>('loading');
  const [parsedPoms, setParsedPoms] = React.useState<ParsedPomResponse | null>(null);
  const [errorMessage, setErrorMessage] = React.useState<string>('');

  React.useEffect(() => {
    const controller = new AbortController();

    setStatus('loading');
    setErrorMessage('');

    fetch('/api/pom', { signal: controller.signal })
      .then((response) => {
        if (response.status === 404) {
          setParsedPoms(null);
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

        setParsedPoms(data);
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

  const groupedEntries = React.useMemo(() => {
    if (!parsedPoms) {
      return [] as Array<{ groupId: string; entries: ParsedPomEntry[] }>;
    }

    const groups = new Map<string, ParsedPomEntry[]>();
    parsedPoms.entries.forEach((entry) => {
      const key = normalizeCoordinatePart(entry.groupId, UNKNOWN_GROUP_LABEL);
      const existing = groups.get(key);
      if (existing) {
        existing.push(entry);
      } else {
        groups.set(key, [entry]);
      }
    });

    return Array.from(groups.entries())
      .map(([groupId, entries]) => ({
        groupId,
        entries: entries
          .slice()
          .sort((first, second) => first.relativePath.localeCompare(second.relativePath)),
      }))
      .sort((first, second) => first.groupId.localeCompare(second.groupId));
  }, [parsedPoms]);

  const formattedEntries = React.useMemo(() => {
    if (!parsedPoms) {
      return new Map<string, string>();
    }

    return parsedPoms.entries.reduce((accumulator, entry) => {
      accumulator.set(entry.pomPath, formatValue(entry.model));
      return accumulator;
    }, new Map<string, string>());
  }, [parsedPoms]);

  return (
    <Page>
      <PageHeader>
        <Title>Project pom overview</Title>
        <Subtitle>
          Pompot scans the current directory by default and can target another folder via <code>--parent=&lt;path&gt;</code>.
        </Subtitle>
      </PageHeader>

      {status === 'loading' && <StatusSection>Loading parsed pom information…</StatusSection>}

      {status === 'empty' && (
        <StatusSection>
          <p style={{ margin: 0, marginBottom: '0.5rem' }}>No pom information is available.</p>
          <p style={{ margin: 0 }}>
            Run Pompot from a directory containing pom files or provide <code>--parent=/path/to/projects</code>.
          </p>
        </StatusSection>
      )}

      {status === 'error' && (
        <StatusSection>
          <p style={{ margin: 0, marginBottom: '0.5rem', color: '#b91d47' }}>Could not load the pom details.</p>
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
        </StatusSection>
      )}

      {status === 'ready' && parsedPoms && (
        <>
          <RootPath>
            <MetadataLabel>Scanned root:</MetadataLabel> <code>{parsedPoms.scannedRoot}</code>
          </RootPath>

          <GroupsContainer>
            {groupedEntries.map((group) => (
              <GroupSection key={group.groupId}>
                <GroupTitle>{group.groupId}</GroupTitle>
                {group.entries.map((entry) => {
                  const formattedPom = formattedEntries.get(entry.pomPath) ?? '';
                  const displayGroup = normalizeCoordinatePart(entry.groupId, UNKNOWN_GROUP_LABEL);
                  const displayArtifact = normalizeCoordinatePart(entry.artifactId, UNKNOWN_ARTIFACT_LABEL);

                  return (
                    <PomDetails key={entry.pomPath}>
                      <PomSummary>
                        <SummaryPath>{entry.relativePath}</SummaryPath>
                        <SummaryMeta>
                          {displayGroup} · {displayArtifact}
                        </SummaryMeta>
                        <SummaryIcon aria-hidden="true">▾</SummaryIcon>
                      </PomSummary>
                      <PomBody>
                        <Metadata>
                          <span>
                            <MetadataLabel>File:</MetadataLabel> <code>{entry.pomPath}</code>
                          </span>
                          <span>
                            <MetadataLabel>Coordinates:</MetadataLabel>{' '}
                            <code>
                              {displayGroup}:{displayArtifact}
                            </code>
                          </span>
                        </Metadata>
                        <CodeBlock>{formattedPom}</CodeBlock>
                      </PomBody>
                    </PomDetails>
                  );
                })}
              </GroupSection>
            ))}
          </GroupsContainer>
        </>
      )}
    </Page>
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

