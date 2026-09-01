package com.rinko1231.armordamagelimit;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(ArmorDamageLimit.MOD_ID)
public class ArmorDamageLimit {
    public static final String MOD_ID = "advancedarmordamagelimit";

    public ArmorDamageLimit() {
        MinecraftForge.EVENT_BUS.register(this);
        ArmorProtectionConfig.setup();
    }

}
