package net.momirealms.sparrow.heart.util;

public class ColorUtils {

    public static int rgbaToDecimal(String rgba) {
        String[] split = rgba.split(",");
        int r = Integer.parseInt(split[0]);
        int g = Integer.parseInt(split[1]);
        int b = Integer.parseInt(split[2]);
        int a = Integer.parseInt(split[3]);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rgbaToDecimal(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
