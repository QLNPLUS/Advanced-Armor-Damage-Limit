package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ArmorDurabilityMixin {
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Inject(method = "damageArmor", at = @At("HEAD"), cancellable = true)
    private void modifyArmorDurability(DamageSource source, float amount, CallbackInfo callbackInfo) {
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                    EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                ItemStack armorItem = getEquippedStack(armorSlot);
                if (armorItem.getItem() instanceof ArmorItem && armorItem.takesDamageFrom(source)) {
                    PlayerEntity player = (PlayerEntity) (Object) this;
                    RegistryEntry<Enchantment> unbreaking = player.getWorld().getRegistryManager()
                            .get(RegistryKeys.ENCHANTMENT)
                            .getEntry(Enchantments.UNBREAKING)
                            .orElseThrow();
                    int unbreakingLevel = EnchantmentHelper.getLevel(unbreaking, armorItem);
                    float damage = ArmorProtectionConfig.limitDamage(armorItem, incomingDamage, unbreakingLevel);
                    if (damage <= 0.0F) {
                        continue;
                    }
                    int durabilityDamage = Math.max(1, (int) damage);
                    armorItem.damage(durabilityDamage, player, armorSlot);
                }
            }
        }
        callbackInfo.cancel();
    }
}
