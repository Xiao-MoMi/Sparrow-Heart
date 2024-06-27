package net.momirealms.sparrow.heart.util;

public class BossBarUtils {

    public static int encodeProperties(boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
        int i = 0;
        if (darkenSky) {
            i |= 1;
        }
        if (dragonMusic) {
            i |= 2;
        }
        if (thickenFog) {
            i |= 4;
        }
        return i;
    }
}
