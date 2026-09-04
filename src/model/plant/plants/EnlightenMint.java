package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class EnlightenMint extends Plant implements IExplosive {

    private int currentCooldown = 850;
    private int durationBonusTicks = 0; // برای لول 2
    private boolean hasTriggered = false;
    private int level = 1;

    public EnlightenMint() {
        // طبق شیت اکسل رسمی: Cost=0, HP=0, Recharge=85s → گیاه مصرفی آنی است.
        super("enlightenmint", PlantType.SUN_PRODUCER, 0, 850, 0);
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
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان خانواده خود (Sun Producer) اعمال کرد!");
        this.takeDamage(9999);
        session.triggerFamilyPlantFood(model.plant.PlantType.SUN_PRODUCER, durationBonusTicks);
    }

    @Override
    public void feed(GameSession session) {
        if (!hasTriggered) {
            explode(session); // در اینجا به عنوان انتشار پالس منت عمل می‌کند
            hasTriggered = true;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10; // Duration +1s (10 تیک)
        if (level >= 3) this.currentCooldown -= 50;
        if (level >= 4) {
            System.out.println("ریست کردن کول‌دان تمام گیاهان خانواده در سطح نقشه (Reset family cooldowns)");
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}