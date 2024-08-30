package net.momirealms.sparrow.heart.feature.entity.display;

import net.momirealms.sparrow.heart.feature.entity.FakeEntity;
import org.bukkit.entity.Player;

public interface FakeTextDisplay extends FakeEntity {

    void rgba(int r, int g, int b, int a);

    void name(String json);
}
