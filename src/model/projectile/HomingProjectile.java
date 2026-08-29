package model.projectile;

import model.game.Board;
import model.game.GameSession;
import model.zombie.DamageType;
import model.zombie.Zombie;

public class HomingProjectile extends Projectile {

    private Zombie targetZombie;
    private double currentY; // برای حرکت نرم بین ردیف‌ها

    public HomingProjectile(int startRow, double startX, int damage, double speed, Zombie initialTarget) {
        super(startRow, startX, damage, speed);
        this.targetZombie = initialTarget;
        this.currentY = startRow; // شروع از ردیف گیاه
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead) return;

        // اگر هدف مرد، نزدیک‌ترین زامبی جدید را پیدا کن
        if (targetZombie == null || targetZombie.isDead()) {
            targetZombie = findNewTarget(session);
            // اگر هیچ زامبی‌ای در زمین نمانده بود، مستقیم از صفحه خارج شو
            if (targetZombie == null) {
                x += speed;
                if (x > Board.COLS) isDead = true;
                return;
            }
        }

        // حرکت به سمت هدف (محاسبه بردار جهت)
        double dx = targetZombie.getXPosition() - this.x;
        double dy = targetZombie.getRow() - this.currentY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // اگر به هدف رسید
        if (distance <= speed) {
            targetZombie.takeDamage(damage, DamageType.NORMAL);
            isDead = true;
        } else {
            // حرکت در مسیر بردار
            this.x += (dx / distance) * speed;
            this.currentY += (dy / distance) * speed;
            
            // آپدیت کردن ردیف تقریبی برای رندر شدن (در صورت نیاز به گرافیک)
            this.row = (int) Math.round(currentY);
        }
    }

    private Zombie findNewTarget(GameSession session) {
        Zombie bestTarget = null;
        double minDistance = Double.MAX_VALUE;

        for (Zombie z : session.getAliveZombies()) {
            if (!z.isDead()) {
                double dist = Math.abs(z.getXPosition() - this.x) + Math.abs(z.getRow() - this.currentY);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestTarget = z;
                }
            }
        }
        return bestTarget;
    }
}