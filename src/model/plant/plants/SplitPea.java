package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class SplitPea extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 125;
    private int level = 1;

    public SplitPea() {
        super("splitpea", PlantType.SHOOTER, 125, 5, 300, PlantTag.PEA);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: شلیک رگباری همزمان از جلو و عقب!");
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
        System.out.println(getName() + " ۱ تیر به جلو و ۲ تیر به عقب شلیک کرد. (دمیج هر تیر: " + damage + ")");
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
    public int getSunCost() {
        return currentSunCost;
    }
}