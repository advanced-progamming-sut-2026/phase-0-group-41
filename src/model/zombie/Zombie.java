package model.zombie;

import model.game.GameSession;

public abstract class Zombie {

    private final String typeName;
    private final int maxHealth;
    private int waveCost;
    private double baseSpeed; // خانه بر ثانیه
    private int damagePerTick;
    private int chilledTicks = 0;
    private int frozenTicks = 0;
    private int health;
    private int maxHealth;
    private int row;
    private double xPosition;
    private boolean eating = false;
    private boolean dead = false;
    private boolean isHypnotized = false; // متغیر برای بررسی وضعیت هیپنوتیزم
    // === متغیرها و متدهای مربوط به ارتقای شوالیه در کلاس Zombie یا NormalZombie ===
    private boolean isKnight = false;

    // === متغیرهای اضافه شده برای مکانیزم "غذای گیاه" ===
    private boolean carriesPlantFood = false;

    public boolean isCarriesPlantFood() {
        return this.carriesPlantFood;
    }

    public void setCarriesPlantFood(boolean carries) {
        this.carriesPlantFood = carries;
    }

    public boolean isKnight() {
        return this.isKnight;
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
    public void convertToKnight() {
        if (this.isKnight) return;

        this.isKnight = true;
        // طبق مستندات: کلاه‌خود و شانه‌بند به آن‌ها داده می‌شود که یعنی جان زامبی به شدت بالا می‌رود
        // برای مثال ۲۰۰ واحد به جان فعلی زامبی ساده اضافه می‌کنیم یا جانش را ریست و تقویت می‌کنیم:
        int bonusHealth = 300;
        this.setHealth(this.getHealth() + bonusHealth);

        // در صورت تمایل می‌توانید نام یا ظاهر آن را هم تغییر دهید:
        // this.setName("knight_zombie");
    }
    public void applyChilled(int seconds) {
        this.chilledTicks = seconds * 10; // هر ثانیه ۱۰ تیک است
    }
    public void applyFrozen(int seconds) {
        this.frozenTicks = seconds * 10;
    }
    public int getChilledTicks() { return chilledTicks; }
    public int getFrozenTicks() { return frozenTicks; }
    public int getBaseHealth() {
        return this.health;
    }

    protected Zombie(String typeName, int health, double baseSpeed, int waveCost, int damagePerTick) {
        this.typeName = typeName;
        this.health = health;
        this.maxHealth = health; // <--- این خط حتماً باید اضافه شود
        this.baseSpeed = baseSpeed;
        this.waveCost = waveCost;
        this.damagePerTick = damagePerTick;
        this.maxHealth = health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    // <--- این متد حتماً باید اضافه شود تا CollectionView ارور ندهد
    public int getMaxHealth() {
        return maxHealth;
    }
    public void spawn(int row, double xPosition) {
        this.row = row;
        this.xPosition = xPosition;
    }

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
}