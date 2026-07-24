package model.game;

public class Grave {
    private int health = 700; // جان پیش‌فرض قبرها ۷۰۰ است
    private final boolean containsSun;
    private final boolean containsPlantFood;

    // سازنده
    public Grave(boolean containsSun, boolean containsPlantFood) {
        this.containsSun = containsSun;
        this.containsPlantFood = containsPlantFood;
    }

    public void takeDamage(int amount) {
        this.health -= amount;
    }

    public boolean isDestroyed() {
        return this.health <= 0;
    }

    public boolean hasSun() { return containsSun; }
    public boolean hasPlantFood() { return containsPlantFood; }
    public int getHealth() { return health; }
}