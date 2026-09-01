package com.rinko1231.armordamagelimit.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;


public class ArmorProtectionConfig
{
    public static ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec.DoubleValue maxArmorDurabilityLossPercent;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> itemProtectionBlacklist;

    static
    {
        BUILDER.push("Config");

        maxArmorDurabilityLossPercent = BUILDER
                .defineInRange("Max Armor Durability Loss Percentage", 0.2,0.01,1);

        itemProtectionBlacklist = BUILDER
                .comment("Armor items that will not be protected")
                .defineList("Item Protection Blacklist", List.of("modA:armorB"),
                        element -> element instanceof String);

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "AdvancedArmorDamageLimit.toml");
    }


}
