# pomgraph package

Provides the graph abstraction used to represent parsed pom files. The graph is
centered around `TextReference` payloads so different parts of the model can
share the same textual value and update it in one place.

* `TextGraph` – owns nodes and text references.
* `GraphNode` – vertex with outgoing edges grouped by relationship name.
* `GraphEdge` – directed connection holding a `TextReference` payload.
* `Text` – immutable wrapper around the raw string.
* `TextReference` – mutable handle that allows sharing `Text` instances.

Use the API by creating a `TextGraph`, adding nodes, and connecting them with
edges that reference shared text values. Updating a `TextReference` instantly
propagates to every connected edge.
