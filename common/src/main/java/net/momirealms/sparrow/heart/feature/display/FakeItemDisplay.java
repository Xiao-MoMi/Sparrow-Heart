package net.momirealms.sparrow.heart.feature.display;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FakeItemDisplay {

    void destroy(Player player);

    void spawn(Player player);

    void item(ItemStack itemStack);

    int entityID();
}
