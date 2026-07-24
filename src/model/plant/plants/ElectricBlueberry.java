package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class ElectricBlueberry extends Plant implements IShooter {

    private int damage = 5000;
    private int shootInterval = 120; // 12 ثانیه
    private int tickCounter = 0;
    private int currentSunCost = 150;
    private int currentCooldown = 15;
    private boolean highTargetPriority = false;
    private int level = 1;

    public ElectricBlueberry() {
        super("electricblueberry", PlantType.HOMING, 150, 15, 300, PlantTag.CHARGE);
    }

    @Override
    public void onTick(GameSession session) {
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;
        // =======================

        if (isFeedActive()) {
            System.out.println("Plant Food: نابودی کامل ۳ زامبی تصادفی با رعد و برق!");
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
        String priority = highTargetPriority ? " (با اولویت اهداف قوی)" : "";
        System.out.println(getName() + " شلیک رعدوبرق به زامبی تصادفی" + priority + " با دمیج: " + damage);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown = Math.max(0, this.currentCooldown - 2);
        if (level >= 3) this.highTargetPriority = true; // Target Priority Up
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}