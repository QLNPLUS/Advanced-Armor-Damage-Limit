package com.rinko1231.armordamagelimit.config;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;
import org.slf4j.Logger;

public final class ArmorProtectionConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ALLOWED_VARIABLES = Set.of("max_durability", "unbreaking");
    private static final Object EXPRESSION_LOCK = new Object();

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue maxArmorDurabilityLossPercent;
    public static final ForgeConfigSpec.ConfigValue<String> armorDamageExpression;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> itemProtectionBlacklist;

    private static volatile String compiledSource;
    private static volatile Expression compiledExpression;
    private static volatile String lastInvalidSource;

    static {
        BUILDER.push("Config");
        maxArmorDurabilityLossPercent = BUILDER
                .comment(
                        "Legacy percentage cap used only when Armor Damage Expression is empty.",
                        "0.2 means 20 percent of each armor item's maximum durability per hit.")
                .defineInRange("Max Armor Durability Loss Percentage", 0.2, 0.01, 1.0);
        armorDamageExpression = BUILDER
                .comment(
                        "Maximum durability damage dealt to this armor item by one incoming hit.",
                        "This is a YiRan Expression Library expression evaluated for each armor item.",
                        "Variables: max_durability = maximum durability of the item; unbreaking = Unbreaking level.",
                        "The result is the per-item damage cap and may be a decimal value.",
                        "Use ?: for conditional tiers and min(...)/max(...) for bounds.",
                        "Examples (keep each expression on one line):",
                        "  max(0, min(15 - unbreaking, max_durability * 0.05))",
                        "  max_durability < 100 ? 4 : max_durability < 500 ? 10 : max_durability * 0.05",
                        "  unbreaking >= 3 ? max_durability * 0.02 : max_durability * 0.05",
                        "Only max_durability and unbreaking are allowed. Empty uses the legacy percentage setting.")
                .define("Armor Damage Expression", "", value -> value instanceof String);
        itemProtectionBlacklist = BUILDER
                .comment(
                        "Armor items that will not be protected.",
                        "Use a list of item IDs, for example [\"minecraft:leather_helmet\"].")
                .defineList("Item Protection Blacklist", List.of("modA:armorB"), value -> value instanceof String);
        SPEC = BUILDER.build();
    }

    private ArmorProtectionConfig() {
    }

    public static void setup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "AdvancedArmorDamageLimit.toml");
    }

    public static float limitDamage(ItemStack armorItem, float incomingDamage) {
        if (incomingDamage <= 0.0F || isBlacklisted(armorItem)) {
            return incomingDamage;
        }

        double maxDurability = armorItem.getMaxDamage();
        if (maxDurability <= 0.0D) {
            return incomingDamage;
        }

        String source = armorDamageExpression.get();
        double maximumDamage;
        try {
            if (source == null || source.isBlank()) {
                maximumDamage = maxDurability * maxArmorDurabilityLossPercent.get();
            } else {
                Expression expression = expressionFor(source);
                double unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, armorItem);
                maximumDamage = expression.evaluate(maxDurability, unbreaking);
            }
        } catch (RuntimeException exception) {
            reportInvalid(source, exception);
            return incomingDamage;
        }

        if (!Double.isFinite(maximumDamage)) {
            reportInvalid(source, new IllegalArgumentException("expression returned a non-finite value"));
            return incomingDamage;
        }
        maximumDamage = Math.max(0.0D, Math.min(maximumDamage, Float.MAX_VALUE));
        return Math.min(incomingDamage, (float) maximumDamage);
    }

    private static boolean isBlacklisted(ItemStack armorItem) {
        var itemId = ForgeRegistries.ITEMS.getKey(armorItem.getItem());
        return itemId != null && itemProtectionBlacklist.get().contains(itemId.toString());
    }

    private static Expression expressionFor(String source) {
        Expression current = compiledExpression;
        if (current != null && source.equals(compiledSource)) {
            return current;
        }
        synchronized (EXPRESSION_LOCK) {
            current = compiledExpression;
            if (current != null && source.equals(compiledSource)) {
                return current;
            }
            Expression expression = new ExpressionBuilder(source)
                    .variables("max_durability", "unbreaking")
                    .build();
            if (!ALLOWED_VARIABLES.containsAll(expression.getVariableNames())) {
                throw new IllegalArgumentException(
                        "Armor Damage Expression may only use max_durability and unbreaking.");
            }
            compiledSource = source;
            compiledExpression = expression;
            lastInvalidSource = null;
            return expression;
        }
    }

    private static void reportInvalid(String source, RuntimeException exception) {
        String safeSource = source == null ? "<null>" : source;
        if (!safeSource.equals(lastInvalidSource)) {
            lastInvalidSource = safeSource;
            LOGGER.error("Invalid Armor Damage Expression '{}'; using uncapped incoming damage: {}",
                    safeSource, exception.getMessage());
        }
    }
}
