package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class SeaShroom extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;

    // مکانیزم طول عمر محدود
    private int lifespanTicks = 600; // 60 ثانیه
    private int ageTicks = 0;

    private int rangeBonus = 0; // برد کوتاه پایه
    private int currentCooldown = 15;
    private int level = 1;

    public SeaShroom() {
        // هزینه صفر، زمان شارژ 15 ثانیه
        super("seashroom", PlantType.SHOOTER, 0, 15, 300, PlantTag.SHROOM, PlantTag.WATER);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        // سیستم پیری: قارچ بعد از مدتی از بین می‌رود
        ageTicks++;
        if (ageTicks >= lifespanTicks) {
            System.out.println(getName() + " عمرش به پایان رسید و ناپدید شد.");
            this.takeDamage(9999);
            return;
        }

        if (isFeedActive()) {
            System.out.println("Plant Food: شلیک رگباری و ریست شدن طول عمر تمام Sea-shroom ها!");
            // نکته معماری: اینجا باید به GameSession بگوییم طول عمر بقیه را هم صفر کند
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shoot(session);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        System.out.println(getName() + " با برد محدود (+ " + rangeBonus + " تایل) شلیک کرد. (دمیج: " + damage + ")");
    }

    // متدی برای صدا زدن توسط سایر قارچ‌ها در زمان Plant Food
    public void resetLifespan() {
        this.ageTicks = 0;
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.rangeBonus += 1; // Range +1 Tile
        if (level >= 3) this.damage += 5;
        if (level >= 4) this.lifespanTicks += 100; // Lifespan +10s
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}