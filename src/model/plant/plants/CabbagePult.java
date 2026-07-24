package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class CabbagePult extends Plant implements IShooter {
    private int damage = 40;
    private double actionInterval = 29.0;
    private double tickCounter = 0;
    private int currentCooldown = 5;
    private int level = 1;

    public CabbagePult() {
        super("cabbagepult", PlantType.LOBBER, 100, 5, 300);
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
            System.out.println("Plant Food: پرتاب کلم به چند زامبی تصادفی!");
            decayFeedEffect();
            return;
        }
        tickCounter += 1.0;
        if (tickCounter >= actionInterval) {
            shoot(session);
            tickCounter -= actionInterval;
        }
    }

    @Override
    public void shoot(GameSession session) {
        System.out.println(getName() + " کلم پرتاب کرد! (دمیج: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) this.actionInterval *= 0.85; // Atk Speed +15%
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
    }
    @Override
    public int getCooldownTicks() { return currentCooldown; }
}