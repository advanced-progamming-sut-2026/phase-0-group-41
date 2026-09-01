package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class Jalapeno extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentSunCost = 125;
    private int currentCooldown = 350;
    private boolean hasExploded = false;
    private int level = 1;

    public Jalapeno() {
        super("jalapeno", PlantType.EXPLOSIVE, 125, 350, 0, PlantTag.FIRE);
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

        if (!hasExploded) {
            explode(session);
            hasExploded = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        // ۱. سوزاندن تمام زامبی‌های لاین خودش
        for (model.zombie.Zombie z : session.getAliveZombies()) {
            if (!z.isDead() && z.getRow() == getRow()) {
                z.takeDamage(damage, model.zombie.DamageType.FIRE);
                z.removeChill(); // ذوب کردن یخ زامبی
            }
        }
        // ۲. آب کردن قالب‌های یخی احتمالی در همان لاین (غارهای یخی)
        for (int c = 0; c < model.game.Board.COLS; c++) {
            model.game.Tile t = session.getBoard().getTile(getRow(), c);
            if (t != null) t.removeIceBlock();
        }
        
        System.out.println(getName() + " کل لاین را به آتش کشید!");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 50;
        if (level >= 3) this.damage += 600;
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}