package model.projectile;

import model.game.Board;
import model.game.GameSession;
import model.zombie.DamageType;
import model.zombie.Zombie;

import java.util.HashSet;
import java.util.Set;

public class StrikeThroughProjectile extends Projectile {
    
    private int pierceLimit; // تعداد زامبی‌هایی که می‌تواند سوراخ کند
    private final double maxRangeX; // حداکثر برد (برای FumeShroom)
    
    // لیستی برای ثبت زامبی‌هایی که قبلاً به آن‌ها ضربه زده‌ایم تا در تیک بعدی دوباره دمیج نخورند
    private final Set<Zombie> hitZombies = new HashSet<>();

    public StrikeThroughProjectile(int row, double startX, int damage, double speed, int pierceLimit, double maxRangeX) {
        super(row, startX, damage, speed);
        this.pierceLimit = pierceLimit;
        this.maxRangeX = maxRangeX;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead) return;

        x += speed;

        // نابودی در صورت خروج از صفحه بازی یا اتمام برد مجاز
        if (x > Board.COLS || x > maxRangeX) {
            isDead = true;
            return;
        }

        // برخورد با تمام زامبی‌های در حال عبور
        for (Zombie z : session.getAliveZombies()) {
            if (z.getRow() == row && !z.isDead() && z.getXPosition() >= this.x - 0.5 && z.getXPosition() <= this.x + 0.5) {
                
                // اگر قبلاً به این زامبی برخورد نکرده‌ایم
                if (!hitZombies.contains(z)) {
                    z.takeDamage(damage, DamageType.NORMAL);
                    hitZombies.add(z);
                    
                    pierceLimit--; // کاهش تعداد نفوذ باقی‌مانده
                    
                    // اگر قدرت نفوذ تمام شد، پرتابه نابود می‌شود
                    if (pierceLimit <= 0) {
                        isDead = true;
                        return;
                    }
                }
            }
        }
    }
}