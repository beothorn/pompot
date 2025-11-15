# CommonValueExtractorTest

## extractReturnsRepeatedPropertiesAndDependencies

Creates two in-memory `ParsedPom` instances that share property and dependency
values. The extractor should report each repeated entry exactly once with the
correct occurrence count.

## parsesPomResourcesAndDetectsRepeatedValues

Parses two sample pom.xml files stored under
`src/test/resources/projects-with-common-values` and verifies three aspects:

1. The parser loads both pom files and populates the dependency graph with
   property, dependency and managed dependency edges.
2. Repeated values such as the shared dependency version and `java.version`
   property are highlighted by the extractor with the correct categories.
3. The resulting graph edges expose the expected node identifiers and version
   payloads for both managed and direct dependencies.
