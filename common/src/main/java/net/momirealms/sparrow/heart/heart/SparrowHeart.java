package net.momirealms.sparrow.heart.heart;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SparrowHeart {

    public abstract void sendActionBar(Player player, String json);

    public abstract void sendTitle(Player player, String titleJson, String subTitleJson, int fadeInTicks, int stayTicks, int fadeOutTicks);

    public abstract void sendToast(Player player, ItemStack icon, String titleJson, String advancementType);

    public abstract void sendDemo(Player player);

    public abstract void sendCredits(Player player);

    public abstract void sendTotemAnimation(Player player, ItemStack totem);
}
