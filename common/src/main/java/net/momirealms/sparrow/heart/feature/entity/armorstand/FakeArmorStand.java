package net.momirealms.sparrow.heart.feature.entity.armorstand;

import net.momirealms.sparrow.heart.feature.entity.FakeNamedEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface FakeArmorStand extends FakeNamedEntity {

    void small(boolean small);

    void invisible(boolean invisible);

    void equipment(EquipmentSlot slot, ItemStack itemStack);

    void updateEquipment(Player player);
}
