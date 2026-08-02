package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class MegaGatlingPea extends Plant implements IShooter {

    private int damage = 20;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 400;
    private double autoPlantFoodChance = 0.0; // برای لول 3
    private int level = 1;

    public MegaGatlingPea() {
        super("megagatlingpea", PlantType.SHOOTER, 400, 50, 300, PlantTag.PEA);
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
            System.out.println("Plant Food: شلیک رگباری عظیم همراه با ۴ تیر غول‌پیکر با سرعت بالا!");
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            // چک کردن شانس فعال شدن خودکار پلنت فود (Lvl 3)
            if (autoPlantFoodChance > 0 && Math.random() < autoPlantFoodChance) {
                this.feed(session);
            } else {
                shoot(session);
            }
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        double startX = getCol() + 0.5;
        
        // فاصله هر تیر دقیقا به اندازه سرعت پرتابه (0.3) تنظیم شده تا پشت سر هم حرکت کنند
        model.projectile.PeaProjectile pea1 = new model.projectile.PeaProjectile(getRow(), startX, damage);
        model.projectile.PeaProjectile pea2 = new model.projectile.PeaProjectile(getRow(), startX - 0.3, damage);
        model.projectile.PeaProjectile pea3 = new model.projectile.PeaProjectile(getRow(), startX - 0.6, damage);
        model.projectile.PeaProjectile pea4 = new model.projectile.PeaProjectile(getRow(), startX - 0.9, damage);

        session.spawnProjectile(pea1);
        session.spawnProjectile(pea2);
        session.spawnProjectile(pea3);
        session.spawnProjectile(pea4);

        System.out.println(getName() + " ۴ تیر متوالی شلیک کرد. (دمیج هر تیر: " + damage + ")");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damage += 10;
        if (level >= 3) this.autoPlantFoodChance = 0.05; // Plant Food Chance +5%
        if (level >= 4) this.currentSunCost -= 50;
    }

    @Override
    public int getSunCost() { return currentSunCost; }
}