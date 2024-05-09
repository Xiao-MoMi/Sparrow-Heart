package net.momirealms.sparrow.heart.util;

public class SelfIncreaseInt {

    private static int id = 0;

    public static int getAndIncrease() {
        int i = id;
        id++;
        return i;
    }
}
