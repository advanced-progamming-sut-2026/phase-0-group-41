package model.plant;

import model.game.GameSession;

/**
 * پایه مشترک همه‌ی گیاهان. هر گیاه مشخص (Peashooter، Sunflower و ...) این کلاس را
 * extend کرده و رفتار مخصوص خودش را در onTick/onFeed پیاده می‌کند.
 */
public abstract class Plant {

    private final String name;
    private final PlantType type;
    private final int sunCost;
    private final int cooldownTicks;
    private final int maxHealth;

    private int health;
    private int row;
    private int col;
    private boolean fed = false;
    private int feedEffectTicksRemaining = 0;

    protected Plant(String name, PlantType type, int sunCost, int cooldownTicks, int maxHealth) {
        this.name = name;
        this.type = type;
        this.sunCost = sunCost;
        this.cooldownTicks = cooldownTicks;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void place(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /** هر تیک بازی یک بار روی این گیاه صدا زده می‌شود. */
    public abstract void onTick(GameSession session);

    /** وقتی «غذای گیاه» به این گیاه داده می‌شود صدا زده می‌شود. */
    public void feed(GameSession session) {
        fed = true;
        feedEffectTicksRemaining = 50; // اثر موقت برای مدت کوتاهی فعال است
    }

    protected boolean isFeedActive() {
        return feedEffectTicksRemaining > 0;
    }

    protected void decayFeedEffect() {
        if (feedEffectTicksRemaining > 0) {
            feedEffectTicksRemaining--;
        }
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public String getName() {
        return name;
    }

    public PlantType getType() {
        return type;
    }

    public int getSunCost() {
        return sunCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isFed() {
        return fed;
    }
}
