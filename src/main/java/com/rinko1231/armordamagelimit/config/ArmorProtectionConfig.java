package com.rinko1231.armordamagelimit.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArmorProtectionConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AdvancedArmorDamageLimit");
    private static final Set<String> ALLOWED_VARIABLES = Set.of("max_durability", "unbreaking");
    private static final Object EXPRESSION_LOCK = new Object();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("AdvancedArmorDamageLimit.properties");

    private static volatile double maxArmorDurabilityLossPercent = 0.2D;
    private static volatile String armorDamageExpression = "";
    private static volatile List<String> itemProtectionBlacklist = List.of();
    private static volatile String compiledSource;
    private static volatile Expression compiledExpression;
    private static volatile String lastInvalidSource;

    private ArmorProtectionConfig() {
    }

    public static void setup() {
        Properties properties = new Properties();
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            if (Files.notExists(CONFIG_FILE)) {
                writeDefaults(properties);
            } else {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    properties.load(reader);
                }
            }
            maxArmorDurabilityLossPercent = parsePercentage(properties.getProperty(
                    "max_armor_durability_loss_percent", "0.2"));
            armorDamageExpression = properties.getProperty("armor_damage_expression", "");
            itemProtectionBlacklist = parseBlacklist(properties.getProperty(
                    "item_protection_blacklist", ""));
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not load {}; using default armor damage settings.", CONFIG_FILE, exception);
        }
    }

    public static float limitDamage(ItemStack armorItem, float incomingDamage, int unbreakingLevel) {
        if (incomingDamage <= 0.0F || isBlacklisted(armorItem)) {
            return incomingDamage;
        }

        double maxDurability = armorItem.getMaxDamage();
        if (maxDurability <= 0.0D) {
            return incomingDamage;
        }

        String source = armorDamageExpression;
        double maximumDamage;
        try {
            if (source == null || source.isBlank()) {
                maximumDamage = maxDurability * maxArmorDurabilityLossPercent;
            } else {
                Expression expression = expressionFor(source);
                maximumDamage = expression.evaluate(maxDurability, unbreakingLevel);
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
        return itemProtectionBlacklist.contains(Registries.ITEM.getId(armorItem.getItem()).toString());
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

    private static double parsePercentage(String value) {
        double percentage = Double.parseDouble(value);
        if (!Double.isFinite(percentage) || percentage < 0.0D || percentage > 1.0D) {
            throw new IllegalArgumentException("percentage must be between 0 and 1");
        }
        return percentage;
    }

    private static List<String> parseBlacklist(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private static void writeDefaults(Properties properties) throws IOException {
        properties.setProperty("max_armor_durability_loss_percent", "0.2");
        properties.setProperty("armor_damage_expression", "");
        properties.setProperty("item_protection_blacklist", "");
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            properties.store(writer, "Advanced Armor Damage Limit configuration");
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
