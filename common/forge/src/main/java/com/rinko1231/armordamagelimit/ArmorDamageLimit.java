package com.rinko1231.armordamagelimit;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(ArmorDamageLimit.MOD_ID)
public final class ArmorDamageLimit {
    public static final String MOD_ID = "armordamagelimit";

    public ArmorDamageLimit() {
        ArmorProtectionConfig.setup();
    }
}
