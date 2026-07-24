package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Caulipower extends Plant implements IShooter {

    private int shootInterval = 120; // 12 ثانیه
    private int tickCounter = 0;
    private int currentSunCost = 250;
    private int currentCooldown = 15; // Cooldown اولیه
    private int level = 1;

    public Caulipower() {
        super("caulipower", PlantType.HOMING, 250, 15, 300, PlantTag.MAGIC, PlantTag.CHARGE);
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
            System.out.println("Plant Food: هیپنوتیزم کردن چند زامبی تصادفی در باغچه!");
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
        System.out.println(getName() + " شلیک تیر جادویی! (Insta-kill / هیپنوتیزم یک زامبی)");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown = Math.max(0, this.currentCooldown - 2);
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 50;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}