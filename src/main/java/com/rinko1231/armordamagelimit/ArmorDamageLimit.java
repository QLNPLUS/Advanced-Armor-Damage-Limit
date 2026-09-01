package com.rinko1231.armordamagelimit;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ArmorDamageLimit.MOD_ID)
public final class ArmorDamageLimit {
    public static final String MOD_ID = "advancedarmordamagelimit";

    public ArmorDamageLimit(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,
                ArmorProtectionConfig.SPEC, "AdvancedArmorDamageLimit.toml");
    }
}
