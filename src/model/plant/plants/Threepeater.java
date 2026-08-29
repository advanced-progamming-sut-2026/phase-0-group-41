package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Threepeater extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 300;
    private int level = 1;

    public Threepeater() {
        super("threepeater", PlantType.SHOOTER, 300, 50, 300, PlantTag.DAY);
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
            // شلیک رگباری بادبزنی در تمام لاین‌ها
            shootAllLanes(session);
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
        int r = getRow();
        session.spawnProjectile(new model.projectile.PeaProjectile(r, getCol() + 0.5, damage));
        if (r - 1 >= 0) session.spawnProjectile(new model.projectile.PeaProjectile(r - 1, getCol() + 0.5, damage));
        if (r + 1 < model.game.Board.ROWS) session.spawnProjectile(new model.projectile.PeaProjectile(r + 1, getCol() + 0.5, damage));
        System.out.println(getName() + " در لاین‌های " + (r - 1) + "، " + r + " و " + (r + 1) + " نخود پرت کرد.");
    }

    private void shootAllLanes(GameSession session) {
        System.out.println(getName() + " (Plant Food) در تمام لاین‌ها شلیک می‌کند!");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentSunCost -= 25; // هزینه 275
        if (level >= 3) this.damage += 10;
        if (level >= 4) {
            this.setMaxHealth(this.getMaxHealth() + 200);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}