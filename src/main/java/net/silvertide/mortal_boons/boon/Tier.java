package net.silvertide.mortal_boons.boon;

public enum Tier {
    IRON(1),
    GOLD(2),
    DIAMOND(3),
    NETHERITE(4);

    private final int level;

    Tier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static Tier fromLevel(int level) {
        return switch (level) {
            case 1 -> IRON;
            case 2 -> GOLD;
            case 3 -> DIAMOND;
            case 4 -> NETHERITE;
            default -> throw new IllegalArgumentException("No boon tier for level " + level);
        };
    }
}
