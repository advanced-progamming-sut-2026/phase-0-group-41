package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.ISunProducer;

public class GoldBloom extends Plant implements ISunProducer {

    private int sunAmount = 375;
    private int currentSunCost = 0;
    private int currentCooldown = 0; // طبق جدول صفر است (اما برای آپگرید آماده شده)
    private int level = 1;

    private boolean sunReady = false;
    private int readySunAmount = 0;
    private boolean hasProduced = false; // فلگ برای اینکه فقط یک بار تولید کند

    public GoldBloom() {
        // نام، نوع، هزینه (0)، زمان شارژ (0)، جان (0 - چون بلافاصله از بین می‌رود)
        super("goldbloom", PlantType.SUN_PRODUCER, 0, 0, 0, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        // اگر توسط زامبی‌ها بلاک شده باشد کاری نمی‌کند
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        // فقط یک بار خورشید را تولید می‌کند و بعد منتظر جمع‌آوری می‌ماند
        if (!hasProduced) {
            produceSun();
            hasProduced = true;
        }

        // نکته: برای گیاهان فوری، معمولاً به محض برداشت خورشید، گیاه نابود می‌شود
        // این منطق در متد collectSun پیاده‌سازی شده است
    }

    @Override
    public void produceSun() {
        this.readySunAmount = this.sunAmount;
        this.sunReady = true;
        System.out.println("Gold Bloom شکوفا شد! " + readySunAmount + " خورشید آماده برداشت است.");
    }

    @Override
    public boolean isSunReady() {
        return this.sunReady;
    }

    @Override
    public void collectSun() {
        if (this.sunReady) {
            System.out.println(readySunAmount + " خورشید از Gold Bloom جمع‌آوری شد!");
            this.sunReady = false;
            this.readySunAmount = 0;
            this.takeDamage(9999); // پس از دادن خورشید، درجا نابود می‌شود
        }
    }

    @Override
    public void feed(GameSession session) {
        // طبق جدول: پلنت فود ندارد (مصرفی آنی)
        System.out.println("Gold Bloom پلنت فود دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown = Math.max(0, this.currentCooldown - 5);
        if (level >= 3) this.sunAmount += 50; // خورشید: 425
        if (level >= 4) this.currentSunCost = Math.max(0, this.currentSunCost - 25);
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }

    @Override
    public int getCooldownTicks() {
        return currentCooldown;
    }
}