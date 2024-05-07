package net.momirealms.sparrow.heart.argument;

public enum HandSlot {

    MAIN(0),
    OFF(3);

    private final int id;

    HandSlot(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
