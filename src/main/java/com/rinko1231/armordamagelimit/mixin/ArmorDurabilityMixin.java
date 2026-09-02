package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, remap = false)
public abstract class ArmorDurabilityMixin {
    @Inject(method = "hurtArmor", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyArmorDurability(DamageSource source, float amount, CallbackInfo callbackInfo) {
        Player player = (Player) (Object) this;
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            for (EquipmentSlot armorSlot : EquipmentSlotGroup.ARMOR) {
                ItemStack armorItem = player.getItemBySlot(armorSlot);
                Equippable equippable = armorItem.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                DamageResistant damageResistant = armorItem.get(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT);
                if (equippable != null && equippable.slot() == armorSlot && equippable.damageOnHurt()
                        && (damageResistant == null || !damageResistant.isResistantTo(source))) {
                    int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(
                            player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                    .getOrThrow(Enchantments.UNBREAKING), armorItem);
                    float damage = ArmorProtectionConfig.limitDamage(armorItem, incomingDamage, unbreakingLevel);
                    if (damage <= 0.0F) {
                        continue;
                    }
                    int durabilityDamage = Math.max(1, (int) damage);
                    armorItem.hurtAndBreak(durabilityDamage, player, armorSlot);
                }
            }
        }
        callbackInfo.cancel();
    }
}
