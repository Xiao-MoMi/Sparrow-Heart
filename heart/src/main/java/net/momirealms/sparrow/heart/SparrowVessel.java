package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.heart.SparrowHeart;
import net.momirealms.sparrow.heart.heart.exception.UnsupportedVersionException;

import net.momirealms.sparrow.heart.impl.*;
import org.bukkit.Bukkit;

public class SparrowVessel {

    private final SparrowHeart heart;

    public SparrowVessel() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        switch (bukkitVersion) {
            case "1.20.3", "1.20.4" -> this.heart = new MC_1_20_4();
            case "1.20.2" -> this.heart = new MC_1_20_2();
            case "1.20", "1.20.1" -> this.heart = new MC_1_20_1();
            case "1.19.4" -> this.heart = new MC_1_19_4();
            case "1.19.3" -> this.heart = new MC_1_19_3();
            case "1.19.2", "1.19.1" -> this.heart = new MC_1_19_1();
            case "1.18.2" -> this.heart = new MC_1_18_2();
            case "1.18.1", "1.18" -> this.heart = new MC_1_18_1();
            default -> throw new UnsupportedVersionException();
        }
    }

    public SparrowHeart getHeart() {
        return heart;
    }
}
