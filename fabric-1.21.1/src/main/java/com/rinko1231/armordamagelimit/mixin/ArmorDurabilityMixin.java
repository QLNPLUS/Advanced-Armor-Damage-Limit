package com.rinko1231.armordamagelimit.mixin;

import com.rinko1231.armordamagelimit.config.ArmorProtectionConfig;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypeTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class ArmorDurabilityMixin {
    @Shadow @Final public DefaultedList<ItemStack> armor;
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "damageArmor", at = @At("HEAD"), cancellable = true)
    private void modifyArmorDurability(DamageSource source, float amount, int[] slots, CallbackInfo callbackInfo) {
        if (amount > 0.0F) {
            float incomingDamage = amount / 4.0F;
            for (int slotIndex : slots) {
                ItemStack armorItem = armor.get(slotIndex);
                if ((!source.isIn(DamageTypeTags.IS_FIRE) || !armorItem.getItem().isFireproof())
                        && armorItem.getItem() instanceof ArmorItem) {
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
                    armorItem.damage(durabilityDamage, player, armorSlot(slotIndex));
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
