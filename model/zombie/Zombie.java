package model.zombie;

import model.game.GameSession;

public abstract class Zombie {

    private final String typeName;
    private final int waveCost;
    private final double baseSpeed; // خانه بر ثانیه
    private final int damagePerTick;

    private int health;
    private int row;
    private double xPosition; // اعشاری، از ستون ۸ (سمت راست) شروع می‌شود و به ۰ می‌رسد
    private boolean eating = false;
    private boolean dead = false;

    protected Zombie(String typeName, int health, double baseSpeed, int waveCost, int damagePerTick) {
        this.typeName = typeName;
        this.health = health;
        this.baseSpeed = baseSpeed;
        this.waveCost = waveCost;
        this.damagePerTick = damagePerTick;
    }

    public void spawn(int row, double xPosition) {
        this.row = row;
        this.xPosition = xPosition;
    }

    /** هر تیک صدا زده می‌شود؛ رفتار پیش‌فرض: اگر گیاهی جلو نیست حرکت کن، وگرنه بخور. */
    public abstract void onTick(GameSession session);

    public void takeDamage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            dead = true;
        }
    }

    public boolean isDead() {
        return dead;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getHealth() {
        return health;
    }

    public double getSpeed() {
        return baseSpeed;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public int getDamagePerTick() {
        return damagePerTick;
    }

    public int getRow() {
        return row;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public boolean isEating() {
        return eating;
    }

    public void setEating(boolean eating) {
        this.eating = eating;
    }
}
