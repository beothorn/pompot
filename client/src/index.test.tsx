import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { App } from './index';

type MockResponse = Partial<Response> & { json?: () => Promise<unknown> };

const originalFetch = globalThis.fetch;

afterEach(() => {
  jest.clearAllMocks();
  globalThis.fetch = originalFetch;
});

describe('App', () => {
  it('renders an empty state when the server reports missing pom data', async () => {
    const response: MockResponse = {
      status: 404,
      ok: false,
      json: jest.fn(),
    };
    const fetchMock = jest.fn<Promise<Response>, Parameters<typeof fetch>>(() =>
      Promise.resolve(response as Response)
    );
    globalThis.fetch = fetchMock as unknown as typeof fetch;

    render(<App />);

    expect(screen.getByText(/Loading parsed pom information/)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/No pom information is available/)).toBeInTheDocument();
    });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/pom',
      expect.objectContaining({ signal: expect.any(AbortSignal) })
    );
  });

  it('shows the parsed poms when the server responds successfully', async () => {
    const parsedResponse = {
      scannedRoot: '/projects',
      entries: [
        {
          pomPath: '/projects/sample/pom.xml',
          relativePath: 'sample/pom.xml',
          groupId: 'org.example',
          artifactId: 'demo',
          model: {
            modelVersion: '4.0.0',
            parent: {
              groupId: 'org.example',
              artifactId: 'demo',
            },
            childProjectUrlInheritAppendPath: null,
          },
        },
      ],
    };
    const response: MockResponse = {
      status: 200,
      ok: true,
      json: jest.fn().mockResolvedValue(parsedResponse),
    };
    const fetchMock = jest.fn<Promise<Response>, Parameters<typeof fetch>>(() =>
      Promise.resolve(response as Response)
    );
    globalThis.fetch = fetchMock as unknown as typeof fetch;

    render(<App />);

    expect(await screen.findByText('Scanned root:')).toBeInTheDocument();
    const rootPath = await screen.findByText(parsedResponse.scannedRoot, {
      selector: 'code',
    });
    expect(rootPath).toBeInTheDocument();

    expect(await screen.findByRole('heading', { level: 2, name: 'org.example' })).toBeInTheDocument();
    const summaryPath = await screen.findByText(parsedResponse.entries[0].relativePath);
    expect(summaryPath).toBeInTheDocument();

    const summaryElement = summaryPath.closest('summary');
    expect(summaryElement).not.toBeNull();

    const icon = within(summaryElement as HTMLElement).getByTestId('collapsible-icon');
    expect(icon).toHaveTextContent('▸');

    const user = userEvent.setup();
    await user.click(summaryPath);

    await waitFor(() => {
      expect(icon).toHaveTextContent('▾');
    });

    const modelSummary = await screen.findByText('Model');
    expect(modelSummary).toBeInTheDocument();

    const modelVersionInput = await screen.findByLabelText('modelVersion');
    expect(modelVersionInput).toHaveValue('4.0.0');
    expect(modelVersionInput).toHaveAttribute('data-pompath', '/projects/sample/pom.xml.modelVersion');

    const parentSummary = await screen.findByText('parent · org.example:demo');
    await user.click(parentSummary);

    const parentGroupIdInput = await screen.findByLabelText('groupId');
    expect(parentGroupIdInput).toHaveValue('org.example');
    expect(parentGroupIdInput).toHaveAttribute('data-pompath', '/projects/sample/pom.xml.parent.groupId');

    expect(screen.queryByLabelText('childProjectUrlInheritAppendPath')).not.toBeInTheDocument();

    expect(response.json).toHaveBeenCalledTimes(1);
  });

  it('reports an error when the server request fails', async () => {
    const fetchMock = jest
      .fn<Promise<Response>, Parameters<typeof fetch>>()
      .mockRejectedValue(new Error('Connection refused'));
    globalThis.fetch = fetchMock as unknown as typeof fetch;

    render(<App />);

    const errorHeading = await screen.findByText(/Could not load the pom details/);
    expect(errorHeading).toBeInTheDocument();

    expect(screen.getByText('Connection refused')).toBeInTheDocument();
  });
});

