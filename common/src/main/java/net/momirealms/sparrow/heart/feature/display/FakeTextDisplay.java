package net.momirealms.sparrow.heart.feature.display;

import org.bukkit.entity.Player;

public interface FakeTextDisplay {

    void rgba(int r, int g, int b, int a);

    void name(String json);

    void destroy(Player player);

    void spawn(Player player);

    int entityID();
}
