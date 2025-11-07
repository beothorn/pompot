import { render, screen, waitFor } from '@testing-library/react';
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
    const summary = await screen.findByText(parsedResponse.entries[0].relativePath);
    expect(summary).toBeInTheDocument();

    const user = userEvent.setup();
    await user.click(summary);

    expect(await screen.findByText("modelVersion: '4.0.0'", { exact: false })).toBeInTheDocument();
    expect(await screen.findByText(/parent:/)).toBeInTheDocument();
    expect(await screen.findByText("groupId: 'org.example'", { exact: false })).toBeInTheDocument();

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

