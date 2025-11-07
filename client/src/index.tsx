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
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
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

const NodeChildren = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const FieldGroup = styled.label`
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
`;

const FieldName = styled.span`
  font-weight: 600;
  color: #243b53;
  font-size: 0.9rem;
`;

const FieldInput = styled.input`
  border: 1px solid #cbd2d9;
  border-radius: 0.5rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.95rem;
  color: #1f2933;
  background: #f8fafc;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:focus {
    outline: none;
    border-color: #486581;
    box-shadow: 0 0 0 3px rgba(72, 101, 129, 0.2);
  }
`;

const CheckboxField = styled.label`
  display: flex;
  align-items: center;
  gap: 0.6rem;
`;

const CheckboxInput = styled.input`
  width: 1.1rem;
  height: 1.1rem;
  accent-color: #3a4b6a;
`;

const CheckboxLabel = styled.span`
  font-size: 0.95rem;
  color: #1f2933;
`;

const EmptyState = styled.div`
  font-size: 0.9rem;
  color: #52606d;
  font-style: italic;
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

type PomPrimitive = string | number | boolean | null;
type PomPathSegment = string;

const isPrimitive = (value: unknown): value is PomPrimitive => {
  return (
    typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean' || value === null
  );
};

const buildPomPath = (base: string, segments: PomPathSegment[]): string => {
  return segments.reduce((path, segment) => {
    if (segment.startsWith('[')) {
      return `${path}${segment}`;
    }

    return `${path}.${segment}`;
  }, base);
};

const summarizeString = (value: unknown): string | null => {
  if (typeof value !== 'string') {
    return null;
  }

  const trimmed = value.trim();
  return trimmed.length === 0 ? null : trimmed;
};

const createObjectSummary = (label: string | undefined, record: Record<string, unknown>): string => {
  const group = summarizeString(record.groupId);
  const artifact = summarizeString(record.artifactId);
  const name = summarizeString(record.name);
  const id = summarizeString(record.id);
  const version = summarizeString(record.version);
  const scope = summarizeString(record.scope);

  const coordinateParts: string[] = [];
  if (group && artifact) {
    coordinateParts.push(`${group}:${artifact}`);
  } else if (group) {
    coordinateParts.push(group);
  } else if (artifact) {
    coordinateParts.push(artifact);
  }

  if (!coordinateParts.length) {
    const fallback = summarizeString(record.key) ?? name ?? id;
    if (fallback) {
      coordinateParts.push(fallback);
    }
  }

  if (version && coordinateParts.length) {
    coordinateParts[coordinateParts.length - 1] = `${coordinateParts[coordinateParts.length - 1]} ${version}`;
  }

  if (scope && coordinateParts.length) {
    coordinateParts[coordinateParts.length - 1] = `${coordinateParts[coordinateParts.length - 1]} (${scope})`;
  }

  if (!label && coordinateParts.length) {
    return coordinateParts.join(' · ');
  }

  if (label && coordinateParts.length) {
    return `${label} · ${coordinateParts.join(' · ')}`;
  }

  return label ?? 'Object';
};

const createArraySummary = (label: string | undefined, length: number): string => {
  const base = label ?? 'List';
  const suffix = length === 1 ? '1 item' : `${length} items`;
  return `${base} · ${suffix}`;
};

const singularize = (label: string): string => {
  if (label.endsWith('ies')) {
    return `${label.slice(0, -3)}y`;
  }

  if (label.endsWith('ses')) {
    return label.slice(0, -2);
  }

  if (label.endsWith('s') && label.length > 1) {
    return label.slice(0, -1);
  }

  return label;
};

const createArrayItemLabel = (parentLabel: string | undefined, index: number): string => {
  if (!parentLabel) {
    return `Item ${index + 1}`;
  }

  return `${singularize(parentLabel)} ${index + 1}`;
};

type RenderOptions = {
  entryPath: string;
  segments: PomPathSegment[];
  label?: string;
  defaultOpen?: boolean;
};

const renderPomNode = (value: unknown, options: RenderOptions): React.ReactNode => {
  const { entryPath, segments, label, defaultOpen } = options;
  const pomPath = buildPomPath(entryPath, segments);
  const displayLabel = label ?? (segments.length ? segments[segments.length - 1] : 'value');

  if (isPrimitive(value)) {
    if (typeof value === 'boolean') {
      return (
        <CheckboxField key={pomPath}>
          <CheckboxInput
            type="checkbox"
            checked={value}
            readOnly
            aria-readonly="true"
            data-pompath={pomPath}
          />
          <CheckboxLabel>{displayLabel}</CheckboxLabel>
        </CheckboxField>
      );
    }

    const inputType = typeof value === 'number' ? 'number' : 'text';
    const stringValue = value === null ? '' : String(value);
    const placeholder = value === null ? 'null' : undefined;

    return (
      <FieldGroup key={pomPath}>
        <FieldName>{displayLabel}</FieldName>
        <FieldInput
          type={inputType}
          value={stringValue}
          placeholder={placeholder}
          readOnly
          data-pompath={pomPath}
        />
      </FieldGroup>
    );
  }

  if (Array.isArray(value)) {
    const summary = createArraySummary(displayLabel, value.length);
    return (
      <PomDetails key={pomPath} open={defaultOpen}>
        <PomSummary>
          <SummaryPath>{summary}</SummaryPath>
          <SummaryIcon aria-hidden="true">▾</SummaryIcon>
        </PomSummary>
        <PomBody>
          {value.length === 0 ? (
            <EmptyState>Empty list</EmptyState>
          ) : (
            <NodeChildren>
              {value.map((item, index) =>
                renderPomNode(item, {
                  entryPath,
                  segments: [...segments, `[${index}]`],
                  label: createArrayItemLabel(displayLabel, index),
                })
              )}
            </NodeChildren>
          )}
        </PomBody>
      </PomDetails>
    );
  }

  if (isRecord(value)) {
    const entries = Object.entries(value).sort((first, second) => first[0].localeCompare(second[0]));
    const summary = createObjectSummary(displayLabel, value);

    return (
      <PomDetails key={pomPath} open={defaultOpen}>
        <PomSummary>
          <SummaryPath>{summary}</SummaryPath>
          <SummaryIcon aria-hidden="true">▾</SummaryIcon>
        </PomSummary>
        <PomBody>
          {entries.length === 0 ? (
            <EmptyState>No fields</EmptyState>
          ) : (
            <NodeChildren>
              {entries.map(([childKey, childValue]) =>
                renderPomNode(childValue, {
                  entryPath,
                  segments: [...segments, childKey],
                  label: childKey,
                })
              )}
            </NodeChildren>
          )}
        </PomBody>
      </PomDetails>
    );
  }

  return (
    <FieldGroup key={pomPath}>
      <FieldName>{displayLabel}</FieldName>
      <FieldInput type="text" value={String(value)} readOnly data-pompath={pomPath} />
    </FieldGroup>
  );
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
                  const displayGroup = normalizeCoordinatePart(entry.groupId, UNKNOWN_GROUP_LABEL);
                  const displayArtifact = normalizeCoordinatePart(entry.artifactId, UNKNOWN_ARTIFACT_LABEL);
                  const modelNode = renderPomNode(entry.model, {
                    entryPath: entry.pomPath,
                    segments: [],
                    label: 'Model',
                    defaultOpen: true,
                  });

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
                        <NodeChildren>{modelNode}</NodeChildren>
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

