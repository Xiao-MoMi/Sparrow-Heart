package net.momirealms.sparrow.heart.heart;

import org.bukkit.entity.Player;

public abstract class SparrowHeart {

    public abstract void sendActionBar(Player player, String json);

    public abstract void sendTitle(Player player, String titleJson, String subTitleJson, int fadeInTicks, int stayTicks, int fadeOutTicks);
}
