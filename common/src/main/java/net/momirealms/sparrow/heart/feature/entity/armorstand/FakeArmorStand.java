package net.momirealms.sparrow.heart.feature.entity.armorstand;

import net.momirealms.sparrow.heart.feature.entity.FakeEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface FakeArmorStand extends FakeEntity {

    void small(boolean small);

    void invisible(boolean invisible);

    void name(String json);

    void equipment(EquipmentSlot slot, ItemStack itemStack);
}
