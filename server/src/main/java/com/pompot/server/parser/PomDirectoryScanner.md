# PomDirectoryScanner

Scans a directory tree looking for `pom.xml` files and converts them into
`ParsedPom` entries using `PomFileParser`. The scanner keeps the parsed list
sorted by groupId, artifactId and relative path so consumers can display the
result deterministically. When parsing fails it logs the issue and returns an
empty list, allowing callers to react accordingly.
