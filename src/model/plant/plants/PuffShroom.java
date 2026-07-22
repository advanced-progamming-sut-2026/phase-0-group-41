package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class PuffShroom extends Plant implements IShooter {
    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int lifespanTicks = 600; // 60 ثانیه
    private int ageTicks = 0;
    private int rangeBonus = 0;
    private int currentCooldown = 5;
    private int level = 1;

    public PuffShroom() {
        super("puffshroom", PlantType.SHOOTER, 0, 5, 300, PlantTag.SHROOM);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        ageTicks++;
        if (ageTicks >= lifespanTicks) {
            System.out.println(getName() + " عمرش تمام شد و ناپدید گردید.");
            this.takeDamage(9999);
            return;
        }

        if (isFeedActive()) {
            System.out.println("Plant Food: شلیک رگباری و ریست شدن طول عمر تمام Puff-shroom ها!");
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

    public void resetLifespan() {
        this.ageTicks = 0;
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.lifespanTicks += 100; // Lifespan +10s
        if (level >= 3) this.damage += 10;
        if (level >= 4) this.rangeBonus += 1; // Range +1 Tile
    }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}