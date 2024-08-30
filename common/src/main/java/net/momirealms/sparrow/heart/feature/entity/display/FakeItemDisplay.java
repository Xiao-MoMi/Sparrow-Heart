package net.momirealms.sparrow.heart.feature.entity.display;

import net.momirealms.sparrow.heart.feature.entity.FakeEntity;
import org.bukkit.inventory.ItemStack;

public interface FakeItemDisplay extends FakeEntity {

    void item(ItemStack itemStack);
}
