package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Starfruit extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 150;
    private int level = 1;

    public Starfruit() {
        super("starfruit", PlantType.SHOOTER, 150, 5, 300);
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
            System.out.println("Plant Food: شلیک رگباری ستاره در تمام جهات!");
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
        System.out.println(getName() + " در ۵ جهت ستاره شلیک کرد. (دمیج هرکدام: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.shootInterval = (int)(this.shootInterval * 0.9); // سرعت حمله +10%
        if (level >= 3) this.damage += 10;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}