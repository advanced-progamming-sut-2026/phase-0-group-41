package model.zombie.zombies;

import model.game.GameSession;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class KingZombie extends Zombie {

    private static final int BUFF_COOLDOWN_TICKS = 40; // هر ۴ ثانیه یک بار جادو/ارتقا اعمال می‌شود
    private int ticksSinceLastBuff = 0;

    public KingZombie() {
        // نام، جان (بالا)، سرعت حرکت (صفر چون طبق داک حرکت نمی‌کند)، دمیج خوردن، هزینه
        super("king", 450, 0.0, 0, 20);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // زامبی پادشاه اصلاً حرکت نمی‌کند و setXPosition را تغییر نمی‌دهد.

        ticksSinceLastBuff++;
        if (ticksSinceLastBuff >= BUFF_COOLDOWN_TICKS) {
            boolean buffSuccessful = tryUpgradeNearbyZombie(session);
            if (buffSuccessful) {
                ticksSinceLastBuff = 0; // فقط در صورت پیدا کردن هدف و ارتقای موفق، تایمر ریست می‌شود
            }
        }
    }

    /**
     * 🌟 پیدا کردن زامبی‌های ساده در سطر پادشاه یا کل بورد و تبدیل آن‌ها به شوالیه
     */
    private boolean tryUpgradeNearbyZombie(GameSession session) {
        List<Zombie> activeZombies = session.getAliveZombies(); // فرض بر اینکه لیست زامبی‌های فعال را داریم
        List<Zombie> eligibleZombies = new ArrayList<>();

        if (activeZombies == null || activeZombies.isEmpty()) {
            return false;
        }

        for (Zombie z : activeZombies) {
            // شرایط واجد شرایط بودن: زنده باشد، در همان سطر پادشاه باشد، زامبی ساده باشد و از قبل شوالیه نشده باشد
            if (z != null && !z.isDead() && z.getRow() == this.getRow()) {
                // بررسی اینکه آیا زامبی از نوع ساده ("normal") است و قابلیت ارتقا دارد
                if ("normal".equals(z.getTypeName()) && !z.isKnight()) {
                    eligibleZombies.add(z);
                }
            }
        }

        // اگر زامبی ساده‌ای در آن سطر پیدا شد، نزدیک‌ترین زامبی به جبهه جلو یا راست‌ترین را انتخاب می‌کنیم
        if (!eligibleZombies.isEmpty()) {
            // برای سادگی یا جذابیت بازی، اولین زامبی ساده واجد شرایط را به شوالیه تبدیل می‌کنیم
            Zombie targetZombie = eligibleZombies.get(0);

            // اعمال ارتقا به شوالیه (کلاه‌خود و شانه‌بند)
            targetZombie.convertToKnight();
            return true;
        }

        return false;
    }
}