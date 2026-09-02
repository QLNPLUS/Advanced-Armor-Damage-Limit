# Changelog

## 1.2.0

- Refreshed the Fabric 1.21.1 release package.
- Existing expression settings and armor durability behavior remain unchanged.

## 1.1.0

- Packaged YiRanExpressionLib as a nested Fabric dependency to avoid duplicate library packages.
- Added support for bounded durability expressions using `min(...)` and `max(...)`.

## 1.0.1

- Added detailed Fabric properties comments with one-line expression examples.
- Documented durability-based tiers, Unbreaking-based tiers, and min/max bounds.
- Standardized the release artifact name as mod ID, loader, Minecraft version, and mod version.
- Kept the empty expression default as the legacy percentage-based behavior.
