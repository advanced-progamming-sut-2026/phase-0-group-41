package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class SunBean extends Plant {

    private int sunDropPerHit = 5;
    private int armorHP = 0;
    private int currentSunCost = 50;
    private int currentCooldown = 20;
    private int level = 1;

    public SunBean() {
        super("sunbean", PlantType.WALL_NUT, 50, 20, 1000, PlantTag.SUN);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: " + getName() + " زره فلزی قدرتمند دریافت کرد!");
            this.armorHP = 3000;
            decayFeedEffect();
            return;
        }
    }

    // وقتی زامبی به آن ضربه می‌زند این متد فراخوانی می‌شود
    public void onHit(GameSession session) {
        System.out.println(getName() + " ضربه خورد و " + sunDropPerHit + " خورشید تولید کرد!");
        // session.addSun(sunDropPerHit);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.sunDropPerHit += 5; // Sun Drop +5
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}