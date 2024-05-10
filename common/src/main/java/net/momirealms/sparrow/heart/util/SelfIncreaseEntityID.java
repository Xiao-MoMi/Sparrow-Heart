package net.momirealms.sparrow.heart.util;

public class SelfIncreaseEntityID {

    private static int id = 1921781232;

    public static int getAndIncrease() {
        int i = id;
        id++;
        return i;
    }
}
