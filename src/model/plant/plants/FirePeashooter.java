package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class FirePeashooter extends Plant implements IShooter {

    private int damage = 40;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 175;
    private int level = 1;

    public FirePeashooter() {
        super("firepeashooter", PlantType.SHOOTER, 175, 5, 300, PlantTag.FIRE, PlantTag.PEA);
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
            System.out.println("Plant Food: شلیک رگباری آتشین در کل لاین!");
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
        System.out.println(getName() + " تیر آتشین مستقیم با دمیج " + damage + " شلیک کرد.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}