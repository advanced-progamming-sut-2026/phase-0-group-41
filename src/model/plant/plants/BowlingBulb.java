package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class BowlingBulb extends Plant implements IShooter {

    private int shootInterval = 20; // 2 ثانیه
    private int tickCounter = 0;

    // زمان شارژ شدن هر پیاز بر حسب تیک
    private int blueRegenTicks = 50;  // 5 ثانیه
    private int orangeRegenTicks = 100; // 10 ثانیه
    private int timeSinceLastShot = 100; // فرض می‌کنیم در ابتدا پر است

    private int currentSunCost = 200;
    private int baseDamageBonus = 0;
    private int level = 1;

    public BowlingBulb() {
        super("bowlingbulb", PlantType.SHOOTER, 200, 5, 300, PlantTag.CHARGE);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        timeSinceLastShot++;

        if (isFeedActive()) {
            System.out.println("Plant Food: شلیک ۳ پیاز انفجاری بزرگ که کمانه می‌کنند!");
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            // شلیک فقط زمانی رخ می‌دهد که زامبی در لاین باشد (در GameSession چک می‌شود)
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        int damage;
        String type;
        if (timeSinceLastShot >= orangeRegenTicks) {
            damage = 180 + baseDamageBonus; type = "نارنجی (بزرگ)";
        } else if (timeSinceLastShot >= blueRegenTicks) {
            damage = 120 + baseDamageBonus; type = "آبی (متوسط)";
        } else {
            damage = 40 + baseDamageBonus; type = "فیروزه‌ای (کوچک)";
        }

        System.out.println(getName() + " پیاز " + type + " را شلیک کرد! (دمیج اولیه: " + damage + ")");
        timeSinceLastShot = 0; // ریست شدن زمان بازیابی
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.blueRegenTicks -= 10;   // Regen -1s
            this.orangeRegenTicks -= 10;
        }
        if (level >= 3) this.baseDamageBonus += 15;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}