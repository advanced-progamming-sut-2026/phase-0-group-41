package model.projectile;

import model.game.Board;
import model.game.GameSession;
import model.zombie.DamageType;
import model.zombie.Zombie;
import model.zombie.zombies.ParasolZombie;

public class LobbedProjectile extends Projectile {
    
    private final int splashDamage;
    private final boolean hasSplash;
    private boolean isButter = false; // مخصوص Kernel-pult
    
    public LobbedProjectile(int row, double startX, int damage, int splashDamage, double speed) {
        super(row, startX, damage, speed);
        this.splashDamage = splashDamage;
        this.hasSplash = (splashDamage > 0);
    }
    
    public void setButter(boolean butter) { this.isButter = butter; }

    /** آیا این پرتابه آسیب مساحتی (Splash) دارد؟ برای تشخیص بصری هندوانه/Pepper-pult از پرتابه‌ی ساده‌ی معمولی. */
    public boolean hasSplash() { return hasSplash; }

    @Override
    public void onTick(GameSession session) {
        if (isDead) return;

        x += speed; // حرکت منحنی در فضای دوبعدی ما همان حرکت به جلو با منطق برخورد متفاوت است

        // ۱. نابودی در صورت خروج از صفحه
        if (x > Board.COLS) {
            isDead = true;
            return;
        }

        // ۲. پیدا کردن نزدیک‌ترین هدف (زامبی جلویی)
        Zombie target = null;
        double minX = Double.MAX_VALUE;

        for (Zombie z : session.getAliveZombies()) {
            if (z.getRow() == row && !z.isDead() && z.getXPosition() >= this.x - 0.5 && z.getXPosition() <= this.x + 0.5) {
                if (z.getXPosition() < minX) {
                    minX = z.getXPosition();
                    target = z;
                }
            }
        }

        // ۳. برخورد با هدف و اعمال اثرات
        if (target != null) {
            
            // الف: بررسی چتر (Parasol Zombie)
            if (target instanceof ParasolZombie) {
                ((ParasolZombie) target).receiveLobbedProjectile(damage);
            } else {
                // ب: برخورد عادی (ارسال نوع دمیج LOBBER برای زامبی‌های خاص مثل Snorkel)
                target.takeDamage(damage, DamageType.LOBBER);
                
                // ج: افکت‌های ویژه (یخ هندوانه یا کره ذرت)
                if (this.isIce) target.applyChill(50);
                if (this.isButter) target.applyFrozen(40); // توقف موقت با کره
            }

            // ۴. اعمال دمیج مساحتی (Splash) برای هندوانه
            if (hasSplash) {
                for (Zombie z : session.getAliveZombies()) {
                    if (!z.isDead() && z != target) {
                        // بررسی فاصله ۱x۱ (شعاع ۹ خانه اطراف)
                        if (Math.abs(z.getRow() - target.getRow()) <= 1 && Math.abs(z.getXPosition() - target.getXPosition()) <= 1.5) {
                            if (z instanceof ParasolZombie) {
                                 ((ParasolZombie) z).receiveLobbedProjectile(splashDamage);
                            } else {
                                 z.takeDamage(splashDamage, DamageType.LOBBER);
                                 if (this.isIce) z.applyChill(50);
                            }
                        }
                    }
                }
            }

            isDead = true; // نابودی پرتابه پس از اصابت
        }
    }
}