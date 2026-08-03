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
        java.util.List<Zombie> activeZombies = session.getAliveZombies(); 
        java.util.List<Zombie> eligibleZombies = new java.util.ArrayList<>();

        if (activeZombies == null || activeZombies.isEmpty()) {
            return false;
        }

        // جستجوی زامبی‌های معمولی در همان سطر پادشاه
        for (int i = 0; i < activeZombies.size(); i++) {
            Zombie z = activeZombies.get(i);
            if (z != null && !z.isDead() && z.getRow() == this.getRow()) {
                if ("normal".equals(z.getTypeName())) {
                    eligibleZombies.add(z);
                }
            }
        }

        if (!eligibleZombies.isEmpty()) {
            Zombie targetZombie = eligibleZombies.get(0);

            // تبدیل قطعی زامبی معمولی به شوالیه با استفاده از ArmorDecorator
            ArmorDecorator knightZombie = new ArmorDecorator(targetZombie, "knight", 3000, targetZombie.getWaveCost());
            knightZombie.spawn(targetZombie.getRow(), targetZombie.getXPosition());
            knightZombie.setSpawnTick(targetZombie.getSpawnTick());

            // جایگزینی فیزیکی زامبی در لیست موتور بازی
            int index = activeZombies.indexOf(targetZombie);
            if (index != -1) {
                activeZombies.set(index, knightZombie);
                System.out.println("پادشاه یک زامبی معمولی را در مختصات (" + (int)targetZombie.getXPosition() + ", " + targetZombie.getRow() + ") به شوالیه تبدیل کرد!");
                return true;
            }
        }
        return false;
    }
}