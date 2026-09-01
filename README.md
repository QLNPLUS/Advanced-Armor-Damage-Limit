# Advanced Armor Damage Limit

This repository is an MIT-licensed extension of [ArmorDamageLimit](https://github.com/Rinko1231/ArmorDamageLimit).
It provides separate build targets for Forge 1.19.2, Forge 1.20.1, NeoForge 1.21.1,
NeoForge 26.1.2, and Fabric 1.21.1.

## Expression-based durability cap

Forge and NeoForge use `config/AdvancedArmorDamageLimit.toml`. Fabric uses
`config/AdvancedArmorDamageLimit.properties`.

- `Max Armor Durability Loss Percentage` is the legacy fallback.
- `Armor Damage Expression` is empty by default. When empty, the legacy percentage is used.
- When present, its result is the maximum durability damage for one armor item on one hit.
- Available variables are `max_durability` and `unbreaking`.
- Built-in functions include `min` and `max`.
- An expression result of `0` skips durability damage. A positive result below `1` still causes one point because item durability damage is integral.

For example, this caps a hit at the smaller of `15 - unbreaking` and five percent of the item's maximum durability:

```toml
Armor Damage Expression = "max(0, min(15 - unbreaking, max_durability * 0.05))"
```

The original fire-resistant armor behavior and item blacklist are preserved. Each armor slot is evaluated independently.

## Building

Java 17 is required for Forge 1.19.2 and Forge 1.20.1. Java 21 is required for
NeoForge 1.21.1 and Fabric 1.21.1. NeoForge 26.1.2 requires Java 25.

```shell
./gradlew :forge-1.19.2:build
./gradlew :forge-1.20.1:build
./gradlew :neoforge-1.21.1:build
./gradlew :neoforge-26.1.2:build
./gradlew :fabric-1.21.1:build
```

The expression library was copied from the local `Auto-Leveling-1.20` Maven cache
into `libs/`. Forge 1.20.1 and NeoForge 1.21.1 use it as a separate required
dependency. The other targets embed only its expression classes because the
available library metadata targets different loader/version combinations.

## Branches

The additional compatibility branches are `neoforge-26.1.2`, `forge-1.19.2`, and
`fabric-1.21.1`. The branch uses NeoForge `26.1.2.71` with Minecraft `26.1.2`.
