package com.rinko1231.armordamagelimit.config;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;
import org.slf4j.Logger;

public final class ArmorProtectionConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ALLOWED_VARIABLES = Set.of("max_durability", "unbreaking");
    private static final Object EXPRESSION_LOCK = new Object();

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.DoubleValue maxArmorDurabilityLossPercent;
    public static final ModConfigSpec.ConfigValue<String> armorDamageExpression;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> itemProtectionBlacklist;

    private static volatile String compiledSource;
    private static volatile Expression compiledExpression;
    private static volatile String lastInvalidSource;

    static {
        BUILDER.push("Config");
        maxArmorDurabilityLossPercent = BUILDER
                .comment("Legacy fallback used when Armor Damage Expression is empty.")
                .defineInRange("Max Armor Durability Loss Percentage", 0.2, 0.01, 1.0);
        armorDamageExpression = BUILDER
                .comment("Maximum durability damage per armor item. Variables: max_durability, unbreaking. Empty uses the legacy percentage.")
                .define("Armor Damage Expression", "", value -> value instanceof String);
        itemProtectionBlacklist = BUILDER
                .comment("Armor items that will not be protected.")
                .defineList("Item Protection Blacklist", List.of("modA:armorB"), value -> value instanceof String);
        SPEC = BUILDER.build();
    }

    private ArmorProtectionConfig() {
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
                ItemEnchantments enchantments = armorItem.getOrDefault(
                        DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                double unbreaking = enchantments.keySet().stream()
                        .filter(holder -> holder.is(Enchantments.UNBREAKING))
                        .findFirst()
                        .map(enchantments::getLevel)
                        .orElse(0);
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
        var itemId = BuiltInRegistries.ITEM.getKey(armorItem.getItem());
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
