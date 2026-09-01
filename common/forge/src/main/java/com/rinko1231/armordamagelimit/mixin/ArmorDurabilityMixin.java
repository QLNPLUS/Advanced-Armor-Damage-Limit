package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class ArmorDurabilityMixin {
    @Shadow @Final private NonNullList<ItemStack> armor;
    @Shadow @Final private Player player;

    @Inject(method = "hurtArmor", at = @At("HEAD"), cancellable = true)
    private void modifyArmorDurability(DamageSource source, float amount, int[] slots, CallbackInfo callbackInfo) {
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            for (int slotIndex : slots) {
                ItemStack armorItem = armor.get(slotIndex);
                if ((!source.is(DamageTypeTags.IS_FIRE) || !armorItem.getItem().isFireResistant())
                        && armorItem.getItem() instanceof ArmorItem) {
                    float damage = ArmorProtectionConfig.limitDamage(armorItem, incomingDamage);
                    if (damage <= 0.0F) {
                        continue;
                    }
                    int durabilityDamage = Math.max(1, (int) damage);
                    armorItem.hurtAndBreak(durabilityDamage, player, brokenPlayer ->
                            brokenPlayer.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex)));
                }
            }
        }
        callbackInfo.cancel();
    }
}
