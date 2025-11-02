import React from 'react';
import { createRoot } from 'react-dom/client';

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('Root element not found');
}

const root = createRoot(rootElement);

root.render(
  <React.StrictMode>
    <main style={{ fontFamily: 'sans-serif', margin: '2rem' }}>
      <h1>Hello, Pompot!</h1>
      <p>Welcome to the workspace manager prototype.</p>
    </main>
  </React.StrictMode>
);
