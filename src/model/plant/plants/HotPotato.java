package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class HotPotato extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 5;
    private boolean hasExploded = false;
    private int level = 1;

    public HotPotato() {
        super("hotpotato", PlantType.EXPLOSIVE, 0, 5, 0, PlantTag.FIRE);
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
        System.out.println(getName() + " یخ تایل موردنظر را به طور آنی ذوب کرد!");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 2;
        if (level >= 3) {
            System.out.println("Melt Area 3x3 فعال شد.");
        }
        if (level >= 4) {
            System.out.println("Explode on Finish فعال شد.");
        }
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}