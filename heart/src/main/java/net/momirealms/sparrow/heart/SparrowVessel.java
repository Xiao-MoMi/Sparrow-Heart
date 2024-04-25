package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.heart.SparrowHeart;
import net.momirealms.sparrow.heart.heart.exception.UnsupportedVersionException;
import net.momirealms.sparrow.heart.heart.impl.MC_1_20_4;
import org.bukkit.Bukkit;

public class SparrowVessel {

    private final SparrowHeart heart;

    public SparrowVessel() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        switch (bukkitVersion) {
            case "1.20.3", "1.20.4" -> this.heart = new MC_1_20_4();
            default -> throw new UnsupportedVersionException();
        }
    }

    public SparrowHeart getHeart() {
        return heart;
    }
}
