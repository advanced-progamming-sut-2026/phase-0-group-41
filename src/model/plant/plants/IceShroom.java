package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class IceShroom extends Plant implements IExplosive {

    private int freezeTimeTicks = 100; // فرض پایه
    private int damage = 0;
    private int currentSunCost = 75;
    private int currentCooldown = 50;
    private boolean hasExploded = false;
    private int level = 1;

    public IceShroom() {
        super("iceshroom", PlantType.EXPLOSIVE, 75, 50, 0, PlantTag.SHROOM, PlantTag.ICE);
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
        System.out.println(getName() + " منفجر شد و تمام زامبی‌های کل نقشه را فریز کرد! (مدت: " + (freezeTimeTicks/10) + " ثانیه)");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.freezeTimeTicks += 20; // Freeze Time +2s
        if (level >= 3) this.currentCooldown -= 5;
        if (level >= 4) this.damage += 50;
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}