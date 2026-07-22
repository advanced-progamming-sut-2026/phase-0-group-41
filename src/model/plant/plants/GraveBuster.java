package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class GraveBuster extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 10;
    private boolean hasDestroyedGrave = false;
    private int level = 1;

    public GraveBuster() {
        super("gravebuster", PlantType.EXPLOSIVE, 0, 10, 0);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (!hasDestroyedGrave) {
            explode(session);
            hasDestroyedGrave = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " قبر موجود روی تایل را به طور کامل از بین برد (Insta-kill قبر).");
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            // کاهش زمان جویدن قبر
            System.out.println("Eat Time -1s");
        }
        if (level >= 3) this.currentCooldown -= 2;
        if (level >= 4) {
            System.out.println("Explode on Finish فعال شد.");
        }
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}