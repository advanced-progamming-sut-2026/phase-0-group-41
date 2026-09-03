package model.zombie.zombies;

import model.game.GameSession;
import model.minigame.IZombieSession;
import model.zombie.Zombie;

public class IZombieSunProducer extends Zombie {

    private int tickCounter = 0;
    private int ticksAlive = 0;
    
    private int productionInterval = 100; 
    private int sunAmount = 25; // مقدار خورشید تولیدی

    public IZombieSunProducer() {
        super("sun_producer_zombie", 1300, 0.0, 0, 0);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        ticksAlive++;
        tickCounter++;

        // فرمول افزایش نرخ تولید: 
        // هر ۳۰ ثانیه (۳۰۰ تیک) که این زامبی زنده بماند، سرعت تولید خورشیدش ۲ ثانیه (۲۰ تیک) سریع‌تر می‌شود.
        // تا جایی که حداقل هر ۴ ثانیه (۴۰ تیک) یک‌بار تولید کند.
        if (ticksAlive % 300 == 0) {
            productionInterval = Math.max(40, productionInterval - 20);
            System.out.println("سرعت تولید خورشید زامبی در سطر " + getRow() + " افزایش یافت!");
        }

        if (tickCounter >= productionInterval) {
            // === رفع باگ بودجه‌ی مشترک ===
            // این زامبی باید فقط به استخر خورشید مخصوص طرف زامبی اضافه کند،
            // نه به SunManager مشترکی که طرف گیاه هم از آن خرج می‌کند؛ در غیر
            // این صورت هیچ تفکیک بودجه‌ای بین دو طرف وجود نخواهد داشت.
            if (session instanceof IZombieSession) {
                ((IZombieSession) session).getZombieSunManager().addSun(sunAmount);
            } else {
                session.getSunManager().addSun(sunAmount);
            }
            System.out.println("زامبی خورشیدزا در سطر " + getRow() + " مقدار " + sunAmount + " خورشید تولید کرد!");
            tickCounter = 0;
        }
    }
}