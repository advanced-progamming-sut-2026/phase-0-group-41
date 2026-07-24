package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class EnlightenMint extends Plant implements IExplosive {

    private int currentSunCost = 0;
    private int currentCooldown = 85;
    private int durationBonusTicks = 0; // برای لول 2
    private boolean hasTriggered = false;
    private int level = 1;

    public EnlightenMint() {
        super("enlightenmint", PlantType.SUN_PRODUCER, 0, 85, 0);
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
            explode(session); // در اینجا به عنوان انتشار پالس منت عمل می‌کند
            hasTriggered = true;
        }
    }

    @Override
    public void explode(GameSession session) {
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان خانواده خود اعمال کرد!");
        // session.triggerFamilyPlantFood("SUN_PRODUCER", durationBonusTicks);
        this.takeDamage(9999); // پس از انتشار پالس از بین می‌رود
    }

    @Override
    public void feed(GameSession session) {
        System.out.println(getName() + " مصرفی آنی است و Plant Food دریافت نمی‌کند.");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10; // Duration +1s (10 تیک)
        if (level >= 3) this.currentCooldown -= 5;
        if (level >= 4) {
            System.out.println("ریست کردن کول‌دان تمام گیاهان خانواده در سطح نقشه (Reset family cooldowns)");
        }
    }

    @Override
    public int getSunCost() { return currentSunCost; }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}