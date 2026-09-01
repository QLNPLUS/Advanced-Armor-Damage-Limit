package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ArmorDurabilityMixin {
    @Inject(method = "hurtArmor", at = @At("HEAD"), cancellable = true)
    private void modifyArmorDurability(DamageSource source, float amount, CallbackInfo callbackInfo) {
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            Player player = (Player) (Object) this;
            EquipmentSlot[] armorSlots = {
                    EquipmentSlot.FEET,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.HEAD
            };
            for (EquipmentSlot slot : armorSlots) {
                ItemStack armorItem = player.getItemBySlot(slot);
                if ((!source.is(DamageTypeTags.IS_FIRE) || armorItem.canBeHurtBy(source))
                        && armorItem.getItem() instanceof ArmorItem) {
                    float damage = ArmorProtectionConfig.limitDamage(armorItem, incomingDamage);
                    if (damage <= 0.0F) {
                        continue;
                    }
                    int durabilityDamage = Math.max(1, (int) damage);
                    armorItem.hurtAndBreak(durabilityDamage, player, slot);
                }
            }
        }
        callbackInfo.cancel();
    }
}
