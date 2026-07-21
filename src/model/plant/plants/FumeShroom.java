package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class FumeShroom extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 125;
    private int rangeBonus = 0;
    private int level = 1;

    public FumeShroom() {
        super("fumeshroom", PlantType.STRIKE_THROUGH, 125, 5, 300, PlantTag.SHROOM);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: پرتاب توده دود عظیم که زامبی‌ها را به عقب می‌راند!");
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
        System.out.println(getName() + " دود متوسط (برد + " + rangeBonus + ") پرتاب کرد. (از زامبی‌ها رد می‌شود)");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.rangeBonus += 1;
        if (level >= 3) this.damage += 10;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}