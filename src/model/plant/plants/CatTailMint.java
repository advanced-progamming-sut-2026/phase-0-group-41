package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class CatTailMint extends Plant implements IExplosive {

    private int currentCooldown = 850;
    private int durationBonusTicks = 0;
    private boolean hasTriggered = false;
    private int level = 1;

    public CatTailMint() {
        // طبق شیت اکسل رسمی: Cost=0, HP=0, Recharge=85s → گیاه مصرفی آنی است.
        super("cattailmint", PlantType.HOMING, 0, 850, 0);
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

        // طبق داک: مصرفی آنی. بلافاصله بعد از کاشت اثر خانوادگی را اعمال می‌کند.
        if (!hasTriggered) {
            explode(session);
            hasTriggered = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان خانواده خود (Homing) اعمال کرد!");
        this.takeDamage(9999);
        session.triggerFamilyPlantFood(model.plant.PlantType.HOMING, durationBonusTicks);
    }

    @Override
    public void feed(GameSession session) {
        if (!hasTriggered) {
            explode(session);
            hasTriggered = true;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10;
        if (level >= 3) this.currentCooldown -= 50;
        if (level >= 4) {
            System.out.println("قابلیت ویژه Lvl 4: ریست کردن کول‌دان تمام گیاهان خانواده catTail-mint در سطح نقشه!");
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}