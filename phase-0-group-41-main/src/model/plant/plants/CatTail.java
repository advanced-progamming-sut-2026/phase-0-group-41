package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;
import model.zombie.Zombie;

public class CatTail extends Plant implements IShooter {

    private int damage = 15;
    private int shootInterval = 15; // 1.5 ثانیه (15 تیک)
    private int tickCounter = 0;
    private int currentSunCost = 175;
    private int currentCooldown = 200;
    private int level = 1;

    public CatTail() {
        super("cattail", PlantType.HOMING, 175, 200, 300, PlantTag.WATER);
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
            System.out.println("Plant Food: شلیک رگباری تیر هدف‌دار به سمت زامبی‌ها!");
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
        Zombie bestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (Zombie zombie : session.getAliveZombies()) {
            if (!zombie.isDead() && zombie.getXPosition() < closestDistance) {
                closestDistance = zombie.getXPosition();
                bestTarget = zombie;
            }
        }

        if(bestTarget != null) {
            model.projectile.HomingProjectile homingProjectile = new model.projectile.HomingProjectile(getRow(), getCol() + 0.5, damage, 0.4, bestTarget);
            session.spawnProjectile(homingProjectile);
            System.out.println(getName() + " تیر هدف‌دار (Homing) به سمت نزدیک‌ترین زامبی شلیک کرد! (دمیج: " + damage + ")");
        }
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

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}