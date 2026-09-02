# Advanced Armor Damage Limit

Advanced Armor Damage Limit is released under GNU Affero General Public License v3.0 only
(AGPL-3.0-only). It extends [ArmorDamageLimit](https://github.com/Rinko1231/ArmorDamageLimit)
with expression-based per-item durability limits.

## Build target

This branch is the standalone NeoForge 1.21.1 build.

## Expression-based durability cap

Forge and NeoForge use `config/AdvancedArmorDamageLimit.toml`. Fabric uses
`config/AdvancedArmorDamageLimit.properties`.

- `Max Armor Durability Loss Percentage` is the legacy fallback.
- `Armor Damage Expression` is empty by default. When empty, the legacy percentage is used.
- The expression result is the maximum durability damage for one armor item from one hit.
- Available variables are `max_durability` and `unbreaking`.
- Built-in functions include `min`, `max`, `abs`, `sqrt`, and `pow`.
- Comparison results are numeric: true is `1` and false is `0`.
- An expression result of `0` skips durability damage. A positive result below `1`
  still causes one point because item durability damage is integral.

The basic cap below chooses the smaller of 15 minus Unbreaking and five percent
of the item's maximum durability:

```text
max(0, min(15 - unbreaking, max_durability * 0.05))
```

The ternary operator supports durability tiers. This example returns 4 below 100
maximum durability, 10 from 100 through 499, and 5 percent from 500 onward:

```text
max_durability < 100 ? 4 : max_durability < 500 ? 10 : max_durability * 0.05
```

An Unbreaking tier can be written as:

```text
unbreaking < 2 ? 12 : unbreaking < 4 ? 8 : 4
```

The expressions must be written on one line in TOML or properties files. The
original fire-resistant armor behavior and item blacklist are preserved. Each
armor slot is evaluated independently.

## Building

Java 17 is required for Forge 1.19.2 and Forge 1.20.1. Java 21 is required for
NeoForge 1.21.1 and Fabric 1.21.1. NeoForge 1.26.1.2 requires Java 25.

```shell
./gradlew build
```

The expression library was copied from the local `Auto-Leveling-1.20` Maven cache
into `libs/`. Release JARs include the expression classes and the applicable
license and notice files.

## License

This project is distributed under AGPL-3.0-only. See `LICENSE` and `NOTICE.md`.
The original Armor Damage Limit contribution retains its MIT copyright and
permission notice. YiRanExpressionLib remains under GNU AGPL v3; its complete
license text is in `licenses/YiRanExpressionLib-LICENSE.txt`. The original
MIT text is in `licenses/ArmorDamageLimit-MIT-LICENSE.txt`.
