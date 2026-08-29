package model.zombie;

import model.game.GameSession;

public abstract class Zombie {

    private final String typeName;
    private int waveCost;
    private int maxHealth;
    private int spawnTick = 0;
    private final double baseSpeed; // خانه بر ثانیه
    private double currentSpeed; 
    private int damagePerTick;
    private int chilledTicks = 0;
    private int frozenTicks = 0;
    private int health;
    private int row;
    private double xPosition;
    private boolean eating = false;
    private boolean dead = false;
    private boolean isHypnotized = false; // متغیر برای بررسی وضعیت هیپنوتیزم
    // === متغیرها و متدهای مربوط به ارتقای شوالیه در کلاس Zombie یا NormalZombie ===

    protected int chillTicks = 0;
    protected int originalSpeed;

    // === متغیرهای اضافه شده برای مکانیزم "غذای گیاه" ===
    private boolean carriesPlantFood = false;

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setSpawnTick(int tick) {
        this.spawnTick = tick;
    }

    public int getSpawnTick() {
        return this.spawnTick;
    }
    
    public boolean isCarriesPlantFood() {
        return this.carriesPlantFood;
    }

    public void setCarriesPlantFood(boolean carries) {
        this.carriesPlantFood = carries;
    }

    public boolean isHypnotized() {
        return this.isHypnotized;
    }
    public void setHypnotized(boolean state) {
        this.isHypnotized = state;
    }
    public void setHealth(int health) {
        // اگر اسم متغیر جان در کلاس شما چیز دیگری است (مثل hp)، آن را جایگزین health کن
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    public void applyChilled(int seconds) {
        this.chilledTicks = seconds * 10; // هر ثانیه ۱۰ تیک است
        this.currentSpeed = this.baseSpeed / 2.0; // سرعت حرکت زامبی نصف می‌شود
    }
    public void applyFrozen(int seconds) {
        this.frozenTicks = seconds * 10;
    }
    public int getChilledTicks() { return chilledTicks; }
    public int getFrozenTicks() { return frozenTicks; }
    public int getBaseHealth() {
        return this.health;
    }

    public void applyChill(int durationTicks) {
        this.chilledTicks = durationTicks;
        this.currentSpeed = this.baseSpeed / 2.0; // سرعت حرکت زامبی نصف می‌شود
    }

    public void removeChill() {
        this.chilledTicks = 0;
        this.currentSpeed = this.baseSpeed; // سرعت به حالت عادی برمی‌گردد
    }

    protected Zombie(String typeName, int health, double baseSpeed, int waveCost, int damagePerTick) {
        this.typeName = typeName;
        this.health = health;
        this.baseSpeed = baseSpeed;
        this.waveCost = waveCost;
        this.damagePerTick = damagePerTick;
        this.maxHealth = health;
        this.currentSpeed = baseSpeed;
    }



    public void spawn(int row, double xPosition) {
        this.row = row;
        this.xPosition = xPosition;
    }

    public abstract void onTick(GameSession session);

    public void updateStatusEffects() {
        if (frozenTicks > 0) frozenTicks--;
        
        if (chilledTicks > 0) {
            chilledTicks--;
            if (chilledTicks <= 0) removeChill();
        }
    }
    
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
        if (frozenTicks > 0) return 0.0;
        return currentSpeed;
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
    // این متد را داخل کلاس Zombie کپی کنید
    public void applyDifficultyModifiers(int dl) {
        double increaseMultiplier = dl / 3.0;
        double decreaseMultiplier = 3.0 / dl;

        // جان زامبی‌ها افزایش می‌یابد
        this.health = (int) (this.health * increaseMultiplier);
        this.maxHealth = (int) (this.maxHealth * increaseMultiplier); // <--- این خط را اضافه کنید

        // دمیج زامبی‌ها افزایش می‌یابد
        this.damagePerTick = (int) (this.damagePerTick * increaseMultiplier);

        // هزینه موج زامبی‌ها کاهش می‌یابد
        this.waveCost = (int) (this.waveCost * decreaseMultiplier);
    }

    public void takeDamage(int amount, DamageType type) {
        takeDamage(amount); // در حالت پیش‌فرض همان دمیج عادی اعمال می‌شود
    }
}