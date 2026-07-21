package model.plant;

import model.game.GameSession;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * پایه مشترک همه‌ی گیاهان. هر گیاه مشخص (Peashooter، Sunflower و ...) این کلاس را
 * extend کرده و رفتار مخصوص خودش را در onTick/onFeed پیاده می‌کند.
 */
public abstract class Plant {

    private final String name;
    private final PlantType type;
    private final int sunCost;
    private final int cooldownTicks;

    // کلمه final برداشته شد تا در ارتقاء (لول 3) بتوانیم جان ماکزیمم را افزایش دهیم
    private int maxHealth;
    private int health;

    private int row;
    private int col;
    private boolean fed = false;
    private int feedEffectTicksRemaining = 0;

    private boolean octopused = false;
    private int octopusHealth = 0;

    // === متدهای موقت برای پشتیبانی از زامبی جادوگر ===
    private boolean isCat = false;

    // لیست نگهداری تگ‌های گیاه
    private final Set<PlantTag> tags = new HashSet<>();

    public boolean isTransformedToCat() {
        return this.isCat;
    }

    public void setTransformedToCat(boolean state) {
        this.isCat = state;
        // بعداً اینجا منطقی می‌نویسیم که اگر true شد، شلیک گیاه قطع شود
    }

    public boolean isOctopused() {
        return this.octopused;
    }

    // متدی که زامبی اختاپوس آن را صدا می‌زند
    public void bindByOctopus(int hp) {
        this.octopused = true;
        this.octopusHealth = hp;
    }

    // متدی برای آسیب دیدن خود اختاپوس توسط تیر بقیه گیاهان
    public void damageOctopus(int damage) {
        if (!octopused) return;

        this.octopusHealth -= damage;
        if (this.octopusHealth <= 0) {
            this.octopused = false;
            this.octopusHealth = 0; // اختاپوس نابود شد و گیاه آزاد می‌شود!
        }
    }

    // سازنده اصلی به همراه دریافت تگ‌ها (Varargs)
    protected Plant(String name, PlantType type, int sunCost, int cooldownTicks, int maxHealth, PlantTag... initialTags) {
        this.name = name;
        this.type = type;
        this.sunCost = sunCost;
        this.cooldownTicks = cooldownTicks;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        // اضافه کردن تگ‌های ورودی به لیست
        if (initialTags != null) {
            this.tags.addAll(Arrays.asList(initialTags));
        }
    }

    // متد بررسی وجود یک تگ خاص در گیاه
    public boolean hasTag(PlantTag tag) {
        return tags.contains(tag);
    }

    public void place(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // داخل کلاس Plant
    private int iceHitsReceived = 0;
    private boolean isFrozenSolid = false;
    private int iceBlockHealth = 0; // جون قالب یخی که دور گیاه رو گرفته

    public void receiveIceHit() {
        if (isFrozenSolid) return; // اگر از قبل کامل یخ زده، دیگه تاثیری نداره

        iceHitsReceived++;
        if (iceHitsReceived >= 3) {
            isFrozenSolid = true;
            iceBlockHealth = 500; // ایجاد سپر یخی با ۵۰۰ تا جون
            // اینجا می‌توانید انیمیشن یا وضعیت گیاه را به حالت یخی تغییر دهید
        }
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

    // === متدهای اضافه شده برای پشتیبانی از سیستم ارتقاء گیاهان ===

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isFrozenSolid() {
        return this.isFrozenSolid;
    }


}

