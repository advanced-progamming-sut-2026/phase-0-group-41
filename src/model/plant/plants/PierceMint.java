package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class PierceMint extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 85;
    private int durationBonusTicks = 0;
    private boolean hasTriggered = false;
    private int level = 1;

    public PierceMint() {
        super("piercemint", PlantType.STRIKE_THROUGH, 0, 85, 0);
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

        if (!hasTriggered) {
            explode(session);
            hasTriggered = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان خانواده خود (Strike-through) اعمال کرد!");
        // session.triggerFamilyPlantFood("STRIKE_THROUGH", durationBonusTicks);
        this.takeDamage(9999);
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10;
        if (level >= 3) this.currentCooldown -= 5;
        if (level >= 4) {
            System.out.println("قابلیت ویژه Lvl 4: ریست کردن کول‌دان تمام گیاهان خانواده Pierce-mint در سطح نقشه!");
        }
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}