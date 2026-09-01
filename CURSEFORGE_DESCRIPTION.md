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

### Configuration

Forge and NeoForge use `config/AdvancedArmorDamageLimit.toml`.

Fabric uses `config/AdvancedArmorDamageLimit.properties`.

The item blacklist accepts registry IDs such as `modid:item_name`. Blacklisted armor keeps the normal Minecraft durability behavior.

### Supported versions

Separate builds are provided for:

- Forge 1.19.2
- Forge 1.20.1
- NeoForge 1.21.1
- NeoForge 26.1.2
- Fabric 1.21.1

Install the JAR matching both your Minecraft version and mod loader.

### License

Advanced Armor Damage Limit is released under the MIT License.
