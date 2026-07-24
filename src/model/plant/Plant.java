package model.plant;

import model.game.GameSession;
import model.game.Board;
import model.game.Tile;

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

    private int maxHealth;
    private int health;

    private int row;
    private int col;
    private boolean fed = false;
    private int feedEffectTicksRemaining = 0;

    private boolean octopused = false;
    private int octopusHealth = 0;
    private boolean greenhouseBoosted = false;
    private boolean isCat = false;

    // لیست نگهداری تگ‌های گیاه
    private final Set<PlantTag> tags = new HashSet<>();

    // === متغیرهای مربوط به سیستم یخ‌زدگی (غارهای یخی) ===
    private int freezeLevel = 0; // 0, 1, 2, 3
    private boolean isFrozenSolid = false;
    private int iceBlockHealth = 0; // جون قالب یخی (۶۰۰)

    // سازنده اصلی
    protected Plant(String name, PlantType type, int sunCost, int cooldownTicks, int maxHealth, PlantTag... initialTags) {
        this.name = name;
        this.type = type;
        this.sunCost = sunCost;
        this.cooldownTicks = cooldownTicks;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        if (initialTags != null) {
            this.tags.addAll(Arrays.asList(initialTags));
        }
    }

    // ==========================================
    // === منطق یخ‌زدگی (منطبق با داکیومنت) ===
    // ==========================================

    public void applyFreezeWind() {
        if (isFrozenSolid) return;
        if (hasTag(PlantTag.FIRE)) return; // گیاهان آتشین یخ نمی‌زنند

        freezeLevel++;
        if (freezeLevel >= 3) {
            isFrozenSolid = true;
            iceBlockHealth = 600; // سپر یخی با ۶۰۰ سلامتی تشکیل می‌شود
        }
    }

    // برای زامبی شکارچی که همان اثر باد یخی را دارد
    public void receiveIceHit() {
        applyFreezeWind();
    }

    public void damageIceBlock(int damage, boolean isFireDamage) {
        if (!isFrozenSolid) return;

        if (isFireDamage) {
            iceBlockHealth = 0; // تیر آتشین یخ را بلافاصله از بین می‌برد
        } else {
            iceBlockHealth -= damage;
        }

        if (iceBlockHealth <= 0) {
            freezeLevel = 0;
            isFrozenSolid = false;
            iceBlockHealth = 0; // گیاه آزاد شد
        }
    }

    /**
     * این متد باید در ابتدای onTick کلاس‌های فرزند (مثل Peashooter) صدا زده شود.
     * چک می‌کند که آیا گیاه آتشی در ۸ خانه اطراف هست تا یخ را آب کند یا نه.
     */
    protected void handleIceMelting(GameSession session) {
        if (!isFrozenSolid) return;

        boolean fireNear = false;
        Board board = session.getBoard();

        // آرایه‌های کمکی برای چک کردن ۸ خانه اطراف
        int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int r = this.row + dRow[i];
            int c = this.col + dCol[i];

            // بررسی محدوده‌ی نقشه (جلوگیری از ارور OutOfBounds)
            if (r >= 0 && r < Board.ROWS && c >= 0 && c < Board.COLS) {
                Tile t = board.getTile(r, c);
                if (t != null && t.getPlant() != null && t.getPlant().hasTag(PlantTag.FIRE)) {
                    fireNear = true;
                    break;
                }
            }
        }

        if (fireNear) {
            // ۶۰ آسیب در ثانیه (اگر بازی ۱۰ تیک بر ثانیه است، می‌شود ۶ آسیب در هر تیک)
            damageIceBlock(6, false);
        }
    }

    // ==========================================
    // === بقیه متدهای کلاس ===
    // ==========================================

    public boolean isTransformedToCat() { return this.isCat; }
    public void setTransformedToCat(boolean state) { this.isCat = state; }

    public boolean isOctopused() { return this.octopused; }
    public void bindByOctopus(int hp) {
        this.octopused = true;
        this.octopusHealth = hp;
    }
    public void damageOctopus(int damage) {
        if (!octopused) return;
        this.octopusHealth -= damage;
        if (this.octopusHealth <= 0) {
            this.octopused = false;
            this.octopusHealth = 0;
        }
    }

    public boolean isGreenhouseBoosted() { return greenhouseBoosted; }
    public void setGreenhouseBoosted(boolean greenhouseBoosted) { this.greenhouseBoosted = greenhouseBoosted; }

    public boolean hasTag(PlantTag tag) { return tags.contains(tag); }

    public void place(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public abstract void onTick(GameSession session);

    public void feed(GameSession session) {
        fed = true;
        feedEffectTicksRemaining = 50;
    }
    protected boolean isFeedActive() { return feedEffectTicksRemaining > 0; }
    protected void decayFeedEffect() {
        if (feedEffectTicksRemaining > 0) feedEffectTicksRemaining--;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDead() { return health <= 0; }

    public String getName() { return name; }
    public PlantType getType() { return type; }
    public int getSunCost() { return sunCost; }
    public int getCooldownTicks() { return cooldownTicks; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isFed() { return fed; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public void setHealth(int health) { this.health = health; }

    public boolean isFrozenSolid() { return this.isFrozenSolid; }

    public boolean isTall() { return false; }
}