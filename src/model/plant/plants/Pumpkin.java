package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class Pumpkin extends Plant {

    private int armorHP = 0;
    private int currentCooldown = 20;
    private int level = 1;

    public Pumpkin() {
        super("pumpkin", PlantType.WALL_NUT, 150, 20, 4000, PlantTag.STACK);
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
            System.out.println("Plant Food: " + getName() + " زره فلزی قدرتمند دریافت کرد!");
            this.armorHP = 4000;
            decayFeedEffect();
            return;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.setMaxHealth(this.getMaxHealth() + 1000);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 3) this.currentCooldown -= 5;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 1500);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}