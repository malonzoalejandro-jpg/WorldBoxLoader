# Genetics Overhaul — content pack

This pack defines exactly 500 genetic loci and 500 traits (1,000 entries total).

## Files
- `genes.json`: heritable loci, inheritance models, mutation rates, and links to trait expressions.
- `traits.json`: trait catalog and gameplay-effect declarations.

## TRUE IMMORTAL
The final trait entry (`trait_500`) is **TRUE IMMORTAL**. Its declared behavior prevents old-age death and all health loss (health floor 1). It is linked to the rare recessive `gene_500` Eternal Life Locus and is marked inheritable.

## Important compatibility status
This is a versioned data/content pack and genetics ruleset, not yet an executable WorldBox Android mod. The linked loader repository currently has no IL2CPP runtime bridge or game integration, so these definitions cannot yet appear in-game or alter native WorldBox health/aging. A compatible 0.51.4 Android IL2CPP bridge and game metadata are required before implementing and testing runtime hooks. The JSON is structured so the runtime adapter can consume it without changing the catalog.

## Suggested inheritance contract
For each offspring, select alleles from both parents; resolve dominant/recessive/codominant loci; apply mutation rates; then map expressed loci to trait IDs. For TRUE IMMORTAL, require the configured recessive locus from both parents unless a future in-game editor explicitly grants it. Once expressed, the runtime adapter must suppress aging death and intercept every health-decrease path, not merely set health to a large value.

## Validation
Both catalogs contain 500 unique IDs each. Names are unique within each catalog. JSON uses UTF-8 and schemaVersion 1.
