package com.rinko1231.armordamagelimit;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.fabricmc.api.ModInitializer;

public final class ArmorDamageLimit implements ModInitializer {
    public static final String MOD_ID = "armordamagelimit";

    @Override
    public void onInitialize() {
        ArmorProtectionConfig.setup();
    }
}
