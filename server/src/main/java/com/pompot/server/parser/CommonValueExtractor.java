package com.pompot.server.parser;

import com.pompot.server.pomgraph.GraphEdge;
import com.pompot.server.pomgraph.GraphNode;
import com.pompot.server.pomgraph.GraphValue;
import com.pompot.server.pomgraph.TextGraph;
import com.pompot.server.pomgraph.TextReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Aggregates repeated values across parsed pom graphs so the UI can highlight
 * them.
 */
@Component
public class CommonValueExtractor {

    /**
     * Extracts repeated values from the provided pom collection.
     *
     * @param entries parsed pom entries to inspect.
     * @return ordered list of repeated values sorted by category and identifier.
     */
    public List<CommonValue> extract(Collection<ParsedPom> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        Map<Key, Occurrence> occurrences = new LinkedHashMap<>();
        for (ParsedPom pom : entries) {
            if (pom == null) {
                continue;
            }
            TextGraph graph = pom.graph();
            for (GraphNode node : graph.nodes()) {
                collectPropertyValues(node.edges("property"), occurrences);
                collectDependencyValues("dependency", node.edges("dependency"), occurrences);
                collectDependencyValues("managed dependency", node.edges("managedDependency"), occurrences);
            }
        }

        return occurrences
            .values()
            .stream()
            .filter(entry -> entry.count > 1)
            .sorted((left, right) -> {
                int categoryComparison = left.category.compareToIgnoreCase(right.category);
                if (categoryComparison != 0) {
                    return categoryComparison;
                }
                int identifierComparison = left.identifier.compareToIgnoreCase(right.identifier);
                if (identifierComparison != 0) {
                    return identifierComparison;
                }
                return left.value.compareToIgnoreCase(right.value);
            })
            .map(entry -> new CommonValue(entry.category, entry.identifier, entry.value, entry.count))
            .collect(Collectors.toUnmodifiableList());
    }

    private void collectPropertyValues(Collection<GraphEdge> edges, Map<Key, Occurrence> storage) {
        if (edges == null || edges.isEmpty()) {
            return;
        }

        for (GraphEdge edge : edges) {
            Optional<TextReference> reference = edge.value().text();
            if (reference.isEmpty()) {
                continue;
            }

            String value = reference.get().value().value();
            if (value.isBlank()) {
                continue;
            }

            String identifier = derivePropertyIdentifier(edge.target());
            register(storage, new Key("property", identifier, value));
        }
    }

    private void collectDependencyValues(String category, Collection<GraphEdge> edges, Map<Key, Occurrence> storage) {
        if (edges == null || edges.isEmpty()) {
            return;
        }

        for (GraphEdge edge : edges) {
            GraphValue payload = edge.value();
            GraphValue versionValue = payload.children().get("version");
            if (versionValue == null) {
                continue;
            }
            Optional<TextReference> reference = versionValue.text();
            if (reference.isEmpty()) {
                continue;
            }

            String value = reference.get().value().value();
            if (value.isBlank()) {
                continue;
            }

            String identifier = deriveDependencyIdentifier(payload, edge.target());
            register(storage, new Key(category, identifier, value));
        }
    }

    private String deriveDependencyIdentifier(GraphValue payload, GraphNode target) {
        String groupId = readChildValue(payload, "groupId");
        String artifactId = readChildValue(payload, "artifactId");
        String type = readChildValue(payload, "type");
        String classifier = readChildValue(payload, "classifier");
        String scope = readChildValue(payload, "scope");

        StringBuilder builder = new StringBuilder();
        if (!groupId.isEmpty()) {
            builder.append(groupId);
        }
        if (!artifactId.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(':');
            }
            builder.append(artifactId);
        }
        if (builder.length() == 0) {
            builder.append(target.id());
        }
        if (!type.isEmpty()) {
            builder.append(':').append(type);
        }
        if (!classifier.isEmpty()) {
            builder.append(':').append(classifier);
        }
        if (!scope.isEmpty()) {
            builder.append(" [").append(scope).append(']');
        }
        return builder.toString();
    }

    private String readChildValue(GraphValue payload, String key) {
        GraphValue child = payload.children().get(key);
        if (child == null) {
            return "";
        }
        return child.text().map(reference -> reference.value().value()).orElse("");
    }

    private String derivePropertyIdentifier(GraphNode target) {
        String id = target == null ? "" : target.id();
        if (id.startsWith("property:")) {
            return id.substring("property:".length());
        }
        return id;
    }

    private void register(Map<Key, Occurrence> storage, Key key) {
        storage.compute(key, (ignored, current) -> {
            if (current == null) {
                return new Occurrence(key.category, key.identifier, key.value);
            }
            current.count += 1;
            return current;
        });
    }

    private record Key(String category, String identifier, String value) {
        private Key {
            Objects.requireNonNull(category, "category");
            Objects.requireNonNull(identifier, "identifier");
            Objects.requireNonNull(value, "value");
        }
    }

    private static final class Occurrence {

        private final String category;
        private final String identifier;
        private final String value;
        private int count;

        Occurrence(String category, String identifier, String value) {
            this.category = category;
            this.identifier = identifier;
            this.value = value;
            this.count = 1;
        }
    }
}
