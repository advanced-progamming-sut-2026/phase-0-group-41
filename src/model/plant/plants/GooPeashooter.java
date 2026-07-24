package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class GooPeashooter extends Plant implements IShooter {

    private int damage = 20;
    private int poisonDamagePerTick = 5; // دمیج مستمر سمی
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 125;
    private int level = 1;

    public GooPeashooter() {
        super("goopeashooter", PlantType.SHOOTER, 125, 5, 300, PlantTag.POISON);
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
            System.out.println("Plant Food: شلیک رگباری سمی که زامبی‌ها را درجا مسموم می‌کند!");
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
        System.out.println(getName() + " تیر سمی شلیک کرد! (دمیج: " + damage + " | دمیج مستمر سمی: " + poisonDamagePerTick + " - نادیده گرفتن زره)");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.poisonDamagePerTick += 5; // Dmg/Tick +5
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}