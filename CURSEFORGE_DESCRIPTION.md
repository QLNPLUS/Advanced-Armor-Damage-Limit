# Advanced Armor Damage Limit

## One-line description

Limit per-hit armor durability damage with a legacy percentage cap or custom expressions based on armor durability and Unbreaking level.

Mod ID: `advancedarmordamagelimit`.

## Full description

Advanced Armor Damage Limit prevents a single hit from removing an excessive amount of durability from each armor item. It is designed for modpacks where high-durability armor should remain meaningfully more durable instead of receiving proportionally larger durability losses.

### Features

- Legacy percentage mode for simple setups.
- Custom per-item durability damage expressions.
- Expressions can read the armor item's maximum durability through `max_durability`.
- Expressions can read the item's Unbreaking enchantment level through `unbreaking`.
- Per-item protection blacklist for modded armor.
- Invalid expressions are reported in the log and do not prevent the game from starting.
- The YiRan Expression Library is bundled in the released mod JAR; no separate library installation is required.

### Custom expressions

Set `Armor Damage Expression` in the Forge or NeoForge configuration, or `armor_damage_expression` in the Fabric properties file. The expression result is the maximum durability damage for one armor item from one hit. The actual durability damage is also limited by the incoming damage.

Available variables:

- `max_durability`: maximum durability of the armor item.
- `unbreaking`: Unbreaking enchantment level on the armor item.

Example:

```text
min(15 - unbreaking, max_durability * 0.05)
```

This example limits one hit to the smaller of `15 - unbreaking` and 5% of the armor item's maximum durability. Values below zero are treated as zero.

When the expression is empty, the legacy percentage setting is used. The default legacy cap is 20% of the armor item's maximum durability per hit.

### Expression syntax and examples

The expression library supports numeric arithmetic (`+`, `-`, `*`, `/`, `%`), parentheses, comparisons (`<`, `<=`, `>`, `>=`, `==`, `!=`), and the ternary conditional operator (`condition ? when_true : when_false`). Built-in functions are `min`, `max`, `abs`, `sqrt`, and `pow`.

All expressions are numeric. A comparison evaluates to `1` when true and `0` when false. Use `max(0, ...)` when a subtraction could produce a negative result.

Segment by maximum durability:

```text
max_durability < 100 ? 4 : max_durability < 500 ? 10 : max_durability * 0.05
```

This returns 4 for armor below 100 maximum durability, 10 for armor from 100 through 499, and 5% of maximum durability for armor at 500 or above.

Segment by Unbreaking level:

```text
unbreaking < 2 ? 12 : unbreaking < 4 ? 8 : 4
```

This returns 12 at Unbreaking levels 0-1, 8 at levels 2-3, and 4 at level 4 or higher.

Combine durability tiers with Unbreaking:

```text
max(0, min(max_durability < 500 ? 10 : max_durability * 0.05, 15 - unbreaking))
```

For a 500-durability item with Unbreaking II, this evaluates to `min(25, 13) = 13`. For an 80-durability item with no Unbreaking, it evaluates to `min(10, 15) = 10`.

Put the expression on one line in the configuration file:

```toml
Armor Damage Expression = "max_durability < 100 ? 4 : max_durability < 500 ? 10 : max_durability * 0.05"
```

```properties
armor_damage_expression=max(0, min(15 - unbreaking, max_durability * 0.05))
```

The expression result is the cap for one armor item from one hit. Incoming damage can reduce the final value further. A result of `0` skips durability damage; a positive result below `1` still causes one point because item durability damage is integral. If an expression is invalid, the error is logged and that hit falls back to uncapped incoming durability damage.

### Configuration

Forge and NeoForge use `config/AdvancedArmorDamageLimit.toml`.

Fabric uses `config/AdvancedArmorDamageLimit.properties`.

The item blacklist accepts registry IDs such as `modid:item_name`. Blacklisted armor keeps the normal Minecraft durability behavior.

### Supported versions

Separate builds are provided for:

- Forge 1.19.2
- Forge 1.20.1
- NeoForge 1.21.1
- NeoForge 1.26.1.2
- Fabric 1.21.1

Install the JAR matching both your Minecraft version and mod loader.

### License

Advanced Armor Damage Limit is released under the GNU Affero General Public License v3.0 only (AGPL-3.0-only). The project includes YiRanExpressionLib under GNU AGPL v3. See `LICENSE`, `NOTICE.md`, and `licenses/YiRanExpressionLib-LICENSE.txt` for the complete licensing information.
