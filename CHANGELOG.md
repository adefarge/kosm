# Changelog

## [1.1.0] - 2019-06-06
### Deprecated
- `WaysBuilderTrait` extensions moved to `io.adefarge.kosm.dsl.extensions`
- `NodeBuilder`, `WayBuilder`, and `RelationBuilder` id field.
  Use the id field in the dsl functions instead

### Added
- `nodesFromGrid`, a dsl function to specify node coordinates from an ascii string
- Extensions function for `OsmGraph` to retrieve osm elements from their id (with `orNull` variants)
- Allow merging of multiple definitions of an element in the DSL
