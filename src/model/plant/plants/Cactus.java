package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Cactus extends Plant implements IShooter {

    private int damage = 30;
    private int shootInterval = 15;
    private int pierceCount = 3; // تعداد نفوذ
    private int tickCounter = 0;
    private int currentSunCost = 175;
    private int level = 1;

    public Cactus() {
        super("cactus", PlantType.STRIKE_THROUGH, 175, 5, 300);
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
            System.out.println("Plant Food: شلیک خارهای برقی با دمیج بالا و نفوذ نامحدود!");
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
        System.out.println(getName() + " خار با دمیج " + damage + " و نفوذ در " + pierceCount + " زامبی شلیک کرد.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.pierceCount += 1;
        if (level >= 3) this.damage += 10;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}