# Advanced Armor Damage Limit

This project is released under GNU Affero General Public License v3.0 only
(AGPL-3.0-only). It extends [ArmorDamageLimit](https://github.com/Rinko1231/ArmorDamageLimit)
with expression-based per-item durability limits.

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

+### Syntax and examples

The expression library supports numeric arithmetic, comparison operators, parentheses,
the ternary conditional operator, and the built-in functions `min`, `max`, `abs`,
`sqrt`, and `pow`.

```text
max_durability < 100 ? 4 : max_durability < 500 ? 10 : max_durability * 0.05
```

This gives armor with less than 100 maximum durability a cap of 4, armor from
100 through 499 a cap of 10, and armor with at least 500 a cap of 5 percent.
The same expression must be written on one line in TOML or properties files.

```text
unbreaking < 2 ? 12 : unbreaking < 4 ? 8 : 4
```

This creates three Unbreaking tiers: levels 0-1 use 12, levels 2-3 use 8,
and level 4 or higher uses 4.

```text
max(0, min(max_durability < 500 ? 10 : max_durability * 0.05, 15 - unbreaking))
```

This combines a durability-based tier with an Unbreaking-based cap. Use
`max(0, ...)` when a subtraction could otherwise produce a negative result.
Comparison results are numeric: true is 1 and false is 0.

## Building

This standalone project targets Forge 1.19.2 and requires Java 17.

```shell
./gradlew build
```

The release JAR embeds YiRanExpressionLib 1.0.1 as a nested Jar-in-Jar dependency
resolved from the deterministic local artifact in `libs`. Its classes are not merged into the mod's root package,
so another mod can bundle the same library without a split-package conflict.
The applicable license and notice files remain included.

## License

This project is distributed under AGPL-3.0-only. See `LICENSE` and `NOTICE.md`.
The original Armor Damage Limit contribution retains its MIT copyright and
permission notice. YiRanExpressionLib remains under GNU AGPL v3; its complete
license text is in `licenses/YiRanExpressionLib-LICENSE.txt`. The original
MIT text is in `licenses/ArmorDamageLimit-MIT-LICENSE.txt`.
