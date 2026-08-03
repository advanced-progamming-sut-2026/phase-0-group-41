package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class GraveBuster extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 100;
    private boolean hasDestroyedGrave = false;
    private int level = 1;

    public GraveBuster() {
        super("gravebuster", PlantType.EXPLOSIVE, 0, 100, 0);
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

        if (!hasDestroyedGrave) {
            explode(session);
            hasDestroyedGrave = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        model.game.Tile tile = session.getBoard().getTile(getRow(), getCol());
        if (tile != null && tile.hasGrave()) {
            tile.damageGrave(9999); // نابودی کامل قبر
            System.out.println(getName() + " قبر را با موفقیت جوید و از بین برد.");
        }
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
        if (level >= 3) this.currentCooldown -= 20;
        if (level >= 4) {
            System.out.println("Explode on Finish فعال شد.");
        }
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}