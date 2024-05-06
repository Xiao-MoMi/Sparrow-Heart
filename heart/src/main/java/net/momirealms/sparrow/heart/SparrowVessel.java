package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.heart.SparrowHeart;
import net.momirealms.sparrow.heart.heart.exception.UnsupportedVersionException;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;

public class SparrowVessel {

    private SparrowHeart heart;

    public SparrowVessel() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String className;
        if (isMojMap()) {
            switch (bukkitVersion) {
                case "1.20.5", "1.20.6" -> className = "Mojmap_R1";
                default -> throw new UnsupportedVersionException();
            }
        } else {
            switch (bukkitVersion) {
                case "1.20.5", "1.20.6" -> className = "Reobf_1_20_R4";
                case "1.20.3", "1.20.4" -> className = "Reobf_1_20_R3";
                case "1.20.2" -> className = "Reobf_1_20_R2";
                case "1.20", "1.20.1" -> className = "Reobf_1_20_R1";
                case "1.19.4" -> className = "Reobf_1_19_R3";
                case "1.19.3" -> className = "Reobf_1_19_R2";
                case "1.19.2", "1.19.1" -> className = "Reobf_1_19_R1";
                case "1.18.2" -> className = "Reobf_1_18_R2";
                case "1.18.1", "1.18" -> className = "Reobf_1_18_R1";
                default -> throw new UnsupportedVersionException();
            }
        }
        try {
            Class<?> clazz = Class.forName("net.momirealms.sparrow.heart.impl." + className);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            heart = (SparrowHeart) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public SparrowHeart getHeart() {
        requireNonNull(heart, "heart");
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
