# Changelog

## Unreleased
### Changed
- Breaking: `WaysBuilderTrait` extensions moved to `io.adefarge.kosm.dsl.extensions`
- Breaking: `NodeBuilder`, `WayBuilder`, and `RelationBuilder` don't have an id field anymore.
  Ids should be specified as a parameter to the function used to call the builder
  (ex `node(<id>) { ... }`)

### Added
- `nodesFromGrid`, a dsl to specify node coordinates from an ascii string
- Extensions function for `OsmGraph` to retrieve osm elements from their id (with `orNull` variants)
- Allow merging of multiple definitions of an element in the DSL
