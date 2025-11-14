# CommonValueExtractor

Aggregates repeated values across `ParsedPom` entries. It walks every graph,
collects edges for relationships of interest (`property`, `dependency` and
`managedDependency`) and tallies repeated values. The extractor returns
`CommonValue` records sorted by category, identifier and value.

```
class CommonValueExtractor {
  List<CommonValue> extract(Collection<ParsedPom> entries)
}
```
