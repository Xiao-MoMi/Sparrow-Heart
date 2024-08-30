package net.momirealms.sparrow.heart.feature.entity;

import org.bukkit.entity.Player;

public interface FakeEntity {

    void destroy(Player player);

    void spawn(Player player);

    int entityID();
}
