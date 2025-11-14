# pomgraph package

Provides the graph abstraction used to represent parsed pom files. The graph is
centered around `GraphValue` payloads so different parts of the model can share
the same textual value or expose composite structures when additional context is
required.

* `TextGraph` – owns nodes and text references.
* `GraphNode` – vertex with outgoing edges grouped by relationship name.
* `GraphEdge` – directed connection holding a `GraphValue` payload.
* `GraphValue` – payload abstraction that can wrap text references or nested
  structures.
* `Text` – immutable wrapper around the raw string.
* `TextReference` – mutable handle that allows sharing `Text` instances.

Use the API by creating a `TextGraph`, adding nodes, and connecting them with
edges that reference shared text values. Updating a `TextReference` instantly
propagates to every connected edge.
