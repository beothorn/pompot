package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.pomgraph.GraphNode;
import com.pompot.server.pomgraph.TextGraph;
import com.pompot.server.pomgraph.TextReference;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.StringJoiner;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Parses pom.xml files from a project root into JSON representations so the UI can render them.
 */
public class PomFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomFileParser.class);

    private final ModelReader modelReader;
    private final ObjectMapper objectMapper;

    /**
     * Creates a parser using the provided Maven model reader and JSON mapper.
     * @param modelReader reader capable of interpreting pom.xml files.
     * @param objectMapper mapper used to convert the parsed model into JSON.
     */
    public PomFileParser(ModelReader modelReader, ObjectMapper objectMapper) {
        this.modelReader = Objects.requireNonNull(modelReader, "modelReader");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper").copy();
    }

    /**
     * Parses the pom.xml located inside the provided project root.
     * @param projectRoot path to the project that contains the pom.xml file.
     * @return the parsed pom as JSON when successful, otherwise {@link Optional#empty()}.
     */
    public Optional<PomParseResult> parse(Path projectRoot) {
        if (projectRoot == null) {
            return Optional.empty();
        }

        Path pomLocation = projectRoot.resolve("pom.xml");
        if (!Files.isRegularFile(pomLocation)) {
            LOGGER.warn("pom.xml not found at {}", pomLocation.toAbsolutePath());
            return Optional.empty();
        }

        try {
            File pomFile = pomLocation.toFile();
            Map<String, ?> options = Map.of(ModelReader.IS_STRICT, Boolean.FALSE);
            Model model = modelReader.read(pomFile, options);
            removeRecursiveParentPointers(model);
            JsonNode asJson = objectMapper.valueToTree(model);
            String groupId = resolveGroupId(model);
            String artifactId = resolveArtifactId(model);
            TextGraph graph = buildGraph(projectRoot, model, groupId, artifactId);
            return Optional.of(new PomParseResult(groupId, artifactId, asJson, graph));
        } catch (IOException exception) {
            LOGGER.error("Failed to parse pom.xml at {}", pomLocation.toAbsolutePath(), exception);
            return Optional.empty();
        } catch (RuntimeException exception) {
            LOGGER.error(
                "Unexpected failure while converting pom.xml at {} into JSON", pomLocation.toAbsolutePath(), exception);
            return Optional.empty();
        }
    }

    /**
     * Determines the most appropriate group identifier for the provided Maven model.
     * @param model parsed Maven model.
     * @return resolved group identifier or an empty string when none is available.
     */
    private String resolveGroupId(Model model) {
        if (model == null) {
            return "";
        }

        String groupId = normalize(model.getGroupId());
        if (!groupId.isEmpty()) {
            return groupId;
        }

        Parent parent = model.getParent();
        if (parent != null) {
            return normalize(parent.getGroupId());
        }

        return "";
    }

    /**
     * Resolves the artifact identifier for the provided Maven model.
     * @param model parsed Maven model.
     * @return artifact identifier or an empty string when unavailable.
     */
    private String resolveArtifactId(Model model) {
        if (model == null) {
            return "";
        }

        return normalize(model.getArtifactId());
    }

    private TextGraph buildGraph(Path projectRoot, Model model, String groupId, String artifactId) {
        TextGraph graph = new TextGraph();
        GraphNode pomNode = graph.addNode("pom:" + projectRoot.toAbsolutePath().normalize());

        attachAttribute(graph, pomNode, "groupId", groupId);
        attachAttribute(graph, pomNode, "artifactId", artifactId);
        attachAttribute(graph, pomNode, "version", model == null ? null : model.getVersion());
        attachAttribute(graph, pomNode, "packaging", model == null ? null : model.getPackaging());

        if (model == null) {
            return graph;
        }

        Parent parent = model.getParent();
        if (parent != null) {
            GraphNode parentNode = graph.addNode(nodeId("parent:", parent.getGroupId(), parent.getArtifactId()));
            attachEdge(graph, pomNode, parentNode, "parent", parent.getVersion());
        }

        Properties properties = model.getProperties();
        if (properties != null) {
            for (String name : properties.stringPropertyNames()) {
                GraphNode propertyNode = graph.addNode("property:" + name);
                attachEdge(graph, pomNode, propertyNode, "property", properties.getProperty(name));
            }
        }

        attachDependencies(graph, pomNode, "dependency", model.getDependencies());

        DependencyManagement dependencyManagement = model.getDependencyManagement();
        if (dependencyManagement != null) {
            attachDependencies(graph, pomNode, "managedDependency", dependencyManagement.getDependencies());
        }

        List<String> modules = model.getModules();
        if (modules != null) {
            for (String module : modules) {
                GraphNode moduleNode = graph.addNode("module:" + module);
                attachEdge(graph, pomNode, moduleNode, "module", module);
            }
        }

        return graph;
    }

    private void attachDependencies(TextGraph graph, GraphNode pomNode, String relationship, List<Dependency> dependencies) {
        if (dependencies == null) {
            return;
        }

        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                continue;
            }

            GraphNode dependencyNode = graph.addNode(
                nodeId("dependency:", dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getClassifier()));
            attachEdge(graph, pomNode, dependencyNode, relationship, dependency.getVersion());
        }
    }

    private void attachAttribute(TextGraph graph, GraphNode source, String name, String value) {
        TextReference reference = createText(graph, value);
        if (reference == null) {
            return;
        }

        GraphNode attributeNode = graph.addNode("attribute:" + name);
        source.connect(name, attributeNode, reference);
    }

    private void attachEdge(TextGraph graph, GraphNode source, GraphNode target, String relationship, String value) {
        TextReference reference = createText(graph, value);
        if (reference == null) {
            return;
        }

        source.connect(relationship, target, reference);
    }

    private TextReference createText(TextGraph graph, String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return null;
        }
        return graph.createText(normalized);
    }

    private String nodeId(String prefix, String... parts) {
        StringJoiner joiner = new StringJoiner(":");
        for (String part : parts) {
            String normalized = normalize(part);
            if (!normalized.isEmpty()) {
                joiner.add(normalized);
            }
        }
        String joined = joiner.toString();
        if (joined.isEmpty()) {
            return prefix.substring(0, prefix.length() - 1);
        }
        return prefix + joined;
    }

    /**
     * Converts {@code null} values into empty strings while trimming whitespace.
     * @param value value to normalize.
     * @return trimmed value or empty string when {@code null}.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    /**
     * Removes parent pointers from XML DOM objects embedded in the Maven model to avoid cycles.
     * @param model the parsed Maven model that may contain {@link Xpp3Dom} instances with parent references.
     */
    private void removeRecursiveParentPointers(Model model) {
        if (model == null) {
            return;
        }

        sanitizeBuild(model.getBuild());
        sanitizeReporting(model.getReporting());
        sanitizeProfiles(model.getProfiles());
    }

    /**
     * Sanitizes the build section to guarantee plugin configurations have no parent cycles.
     * @param build build definition from the Maven model; may be {@code null} when the pom omits the block.
     */
    private void sanitizeBuild(Build build) {
        if (build == null) {
            return;
        }

        sanitizeBuildBase(build);
    }

    /**
     * Removes parent references from plugin configurations contained in the provided build base.
     * @param buildBase build configuration that stores plugin definitions.
     */
    private void sanitizeBuildBase(BuildBase buildBase) {
        if (buildBase == null) {
            return;
        }

        sanitizePlugins(buildBase.getPlugins());

        PluginManagement pluginManagement = buildBase.getPluginManagement();
        if (pluginManagement != null) {
            sanitizePlugins(pluginManagement.getPlugins());
        }
    }

    /**
     * Strips recursive parent pointers from plugin configurations and executions.
     * @param plugins plugins declared in the pom; the list may be {@code null}.
     */
    private void sanitizePlugins(List<Plugin> plugins) {
        if (plugins == null) {
            return;
        }

        for (Plugin plugin : plugins) {
            if (plugin == null) {
                continue;
            }

            sanitizeConfiguration(plugin.getConfiguration());

            for (PluginExecution execution : plugin.getExecutions()) {
                if (execution != null) {
                    sanitizeConfiguration(execution.getConfiguration());
                }
            }
        }
    }

    /**
     * Handles profiles declared in the pom so their build sections are safe to serialize.
     * @param profiles profiles configured by the pom author; may be {@code null}.
     */
    private void sanitizeProfiles(List<Profile> profiles) {
        if (profiles == null) {
            return;
        }

        for (Profile profile : profiles) {
            if (profile == null) {
                continue;
            }
            sanitizeBuildBase(profile.getBuild());
            sanitizeReporting(profile.getReporting());
        }
    }

    /**
     * Sanitizes reporting plugins to remove recursive parent pointers from their configurations.
     * @param reporting reporting configuration of the pom; may be {@code null}.
     */
    private void sanitizeReporting(Reporting reporting) {
        if (reporting == null) {
            return;
        }

        sanitizeReportPlugins(reporting.getPlugins());
    }

    /**
     * Removes cycles from the configuration of reporting plugins and their report sets.
     * @param reportPlugins reporting plugins defined in the pom; the list may be {@code null}.
     */
    private void sanitizeReportPlugins(List<ReportPlugin> reportPlugins) {
        if (reportPlugins == null) {
            return;
        }

        for (ReportPlugin reportPlugin : reportPlugins) {
            if (reportPlugin == null) {
                continue;
            }

            sanitizeConfiguration(reportPlugin.getConfiguration());

            for (ReportSet reportSet : reportPlugin.getReportSets()) {
                if (reportSet != null) {
                    sanitizeConfiguration(reportSet.getConfiguration());
                }
            }
        }
    }

    /**
     * Removes parent pointers from a Maven configuration object when it is expressed as {@link Xpp3Dom}.
     * @param configuration Maven configuration object extracted from the model.
     */
    private void sanitizeConfiguration(Object configuration) {
        if (configuration instanceof Xpp3Dom dom) {
            removeParentPointers(dom);
        }
    }

    /**
     * Recursively sets the parent of the provided node and its children to {@code null} so Jackson cannot loop forever.
     * @param dom configuration node that may contain parent relationships.
     */
    private void removeParentPointers(Xpp3Dom dom) {
        if (dom == null) {
            return;
        }

        dom.setParent(null);
        Xpp3Dom[] children = dom.getChildren();
        if (children == null) {
            return;
        }

        for (Xpp3Dom child : children) {
            removeParentPointers(child);
        }
    }
}
