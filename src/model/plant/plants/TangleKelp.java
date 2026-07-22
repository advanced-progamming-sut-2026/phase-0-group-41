package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class TangleKelp extends Plant implements IExplosive {

    private int currentSunCost = 25;
    private int currentCooldown = 15;
    private int targetLimit = 1;
    private int level = 1;

    public TangleKelp() {
        super("tanglekelp", PlantType.EXPLOSIVE, 25, 15, 300, PlantTag.TRAP, PlantTag.WATER);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: کشیدن چند زامبی تصادفی در آب به زیر آب!");
            // منطق اتصال به نقشه بازی در آینده
            this.takeDamage(9999);
            return;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " " + targetLimit + " زامبی را به زیر آب کشید! (Insta-kill)");
        this.takeDamage(9999);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 5;
        if (level >= 3) this.targetLimit += 1;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}