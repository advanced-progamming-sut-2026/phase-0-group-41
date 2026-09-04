package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class AppeaseMint extends Plant implements IExplosive {
    private int currentCooldown = 850;
    private int durationBonusTicks = 0;
    private boolean hasTriggered = false;
    private int level = 1;

    public AppeaseMint() {
        // طبق شیت اکسل رسمی: Cost=0, HP=0, Recharge=85s → گیاه مصرفی آنی است.
        super("appeasemint", PlantType.SHOOTER, 0, 850, 0);
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
        System.out.println(getName() + " فعال شد و Plant Food موقت به تمام گیاهان Shooter اعمال کرد!");
        // باگ قبلی: چون AppeaseMint خودش هم از نوع SHOOTER است، حلقه‌ی
        // triggerFamilyPlantFood قبل از نابودی این گیاه به خودش هم feed() می‌زد؛
        // این گیاه هنوز روی زمین است وقتی حلقه اجرا می‌شود، پس ابتدا آن را از
        // بازی خارج می‌کنیم و سپس افکت خانوادگی را روی بقیه اعمال می‌کنیم تا
        // به‌درستی به همه‌ی گیاهان هم‌خانواده‌ی *دیگر* برسد.
        this.takeDamage(9999);
        session.triggerFamilyPlantFood(model.plant.PlantType.SHOOTER, durationBonusTicks);
    }

    @Override
    public void feed(GameSession session) {
        // Plant Food مستقیم روی خودِ Mint هم باید همان اثر آنی را ایجاد کند.
        if (!hasTriggered) {
            explode(session);
            hasTriggered = true;
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.durationBonusTicks += 10;
        if (level >= 3) this.currentCooldown -= 50;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}