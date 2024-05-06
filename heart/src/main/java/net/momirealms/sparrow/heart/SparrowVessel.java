package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.heart.SparrowHeart;
import net.momirealms.sparrow.heart.heart.exception.UnsupportedVersionException;
import net.momirealms.sparrow.heart.impl.*;
import org.bukkit.Bukkit;

public class SparrowVessel {

    private final SparrowHeart heart;

    public SparrowVessel() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        if (isMojMap()) {
            switch (bukkitVersion) {
                case "1.20.5", "1.20.6" -> this.heart = new Mojmap_R1();
                default -> throw new UnsupportedVersionException();
            }
        } else {
            switch (bukkitVersion) {
                case "1.20.5", "1.20.6" -> this.heart = new Reobf_1_20_R4();
                case "1.20.3", "1.20.4" -> this.heart = new Reobf_1_20_R3();
                case "1.20.2" -> this.heart = new Reobf_1_20_R2();
                case "1.20", "1.20.1" -> this.heart = new Reobf_1_20_R1();
                case "1.19.4" -> this.heart = new Reobf_1_19_R3();
                case "1.19.3" -> this.heart = new Reobf_1_19_R2();
                case "1.19.2", "1.19.1" -> this.heart = new Reobf_1_19_R1();
                case "1.18.2" -> this.heart = new Reobf_1_18_R2();
                case "1.18.1", "1.18" -> this.heart = new Reobf_1_18_R1();
                default -> throw new UnsupportedVersionException();
            }
        }
    }

    public SparrowHeart getHeart() {
        return heart;
    }

    private static boolean isMojMap() {
        try {
            Class.forName("net.minecraft.world.level.biome.Biome");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }
}
