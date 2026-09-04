package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class EnchantMint extends Plant implements IExplosive {
    private int currentSunCost = 0;
    private int currentCooldown = 850;
    private int durationBonusTicks = 0; // برای لول 2 (افزایش زمان تأثیر)
    private boolean hasTriggered = false;
    private int level = 1;

    public EnchantMint() {
        // طبق شیت اکسل رسمی: Cost=0, HP=0, Recharge=85s → گیاه مصرفی آنی است.
        super("enchantmint", PlantType.MODIFIER, 0, 850, 0);
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
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان خانواده خود (Modifier) اعمال کرد!");
        // باگ قبلی: به‌اشتباه خانواده MELEE_ATTACKER (متعلق به EnforceMint) را
        // تغذیه می‌کرد؛ باید خانواده خودش یعنی MODIFIER تغذیه شود.
        this.takeDamage(9999); // پس از اعمال تأثیر فوراً از بین می‌رود
        session.triggerFamilyPlantFood(model.plant.PlantType.MODIFIER, durationBonusTicks);
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
        if (level >= 2) this.durationBonusTicks += 10; // Duration +1s (معادل 10 تیک)
        if (level >= 3) this.currentCooldown -= 50;
        if (level >= 4) {
            System.out.println("قابلیت ویژه Lvl 4: ریست کردن کول‌دان تمام گیاهان خانواده Enchant-mint در سطح نقشه!");
        }
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}