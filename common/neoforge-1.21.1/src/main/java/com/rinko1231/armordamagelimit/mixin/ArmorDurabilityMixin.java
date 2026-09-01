package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ArmorDurabilityMixin {
    @Inject(method = "hurtArmor", at = @At("HEAD"), cancellable = true)
    private void modifyArmorDurability(DamageSource source, float amount, CallbackInfo callbackInfo) {
        Player player = (Player) (Object) this;
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
                ItemStack armorItem = player.getInventory().armor.get(slotIndex);
                if ((!source.is(DamageTypeTags.IS_FIRE) || !armorItem.getComponents().has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT))
                        && armorItem.getItem() instanceof ArmorItem) {
                    int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(
                            player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                    .getOrThrow(Enchantments.UNBREAKING), armorItem);
                    float damage = ArmorProtectionConfig.limitDamage(armorItem, incomingDamage, unbreakingLevel);
                    if (damage <= 0.0F) {
                        continue;
                    }
                    int durabilityDamage = Math.max(1, (int) damage);
                    armorItem.hurtAndBreak(durabilityDamage, player, armorSlot(slotIndex));
                }
            }
        }
        callbackInfo.cancel();
    }

    private static EquipmentSlot armorSlot(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> throw new IllegalArgumentException("Invalid armor slot index: " + index);
        };
    }
}
