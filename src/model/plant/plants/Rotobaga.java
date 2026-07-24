package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IShooter;

public class Rotobaga extends Plant implements IShooter {

    private int damagePerShot = 10;
    private int shotsPerCorner = 3;
    private int shootInterval = 15;
    private int tickCounter = 0;
    private int currentSunCost = 150;
    private int level = 1;

    public Rotobaga() {
        super("rotobaga", PlantType.SHOOTER, 150, 5, 300, PlantTag.DAY);
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
            shootDiagonal(session, true); // شلیک رگباری در 4 جهت
            decayFeedEffect();
            return;
        }

        tickCounter++;
        if (tickCounter >= shootInterval) {
            shootDiagonal(session, false);
            tickCounter = 0;
        }
    }

    @Override
    public void shoot(GameSession session) {
        // این گیاه مستقیم شلیک نمی‌کند، لذا متد shootDiagonal اختصاصی استفاده می‌شود
    }

    private void shootDiagonal(GameSession session, boolean isRapid) {
        int totalDamage = isRapid ? (damagePerShot * shotsPerCorner * 5) : (damagePerShot * shotsPerCorner);
        System.out.println(getName() + " در ۴ جهت اریب با دمیج کل " + totalDamage + " شلیک کرد.");
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.damagePerShot += 10;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}