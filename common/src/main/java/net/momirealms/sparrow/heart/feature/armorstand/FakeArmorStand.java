package net.momirealms.sparrow.heart.feature.armorstand;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface FakeArmorStand {

    void small(boolean small);

    void invisible(boolean invisible);

    void name(String json);

    void equipment(EquipmentSlot slot, ItemStack itemStack);

    void destroy(Player player);

    void spawn(Player player);

    int entityID();
}
