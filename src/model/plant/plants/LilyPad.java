package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class LilyPad extends Plant {

    private int currentSunCost = 25;
    private int currentCooldown = 5;
    private int level = 1;

    public LilyPad() {
        super("lilypad", PlantType.MODIFIER, 25, 5, 300, PlantTag.WATER, PlantTag.STACK);
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
            System.out.println("Plant Food: ایجاد چند کپی از خود روی خانه‌های خالی آب!");
            decayFeedEffect();
            return;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentSunCost -= 25;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentCooldown = Math.max(0, this.currentCooldown - 2);
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}