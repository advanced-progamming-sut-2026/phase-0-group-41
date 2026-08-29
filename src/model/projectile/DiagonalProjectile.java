package model.projectile;

import model.game.Board;
import model.game.GameSession;
import model.zombie.DamageType;
import model.zombie.Zombie;

public class DiagonalProjectile extends Projectile {
    
    private final double speedY; // سرعت در محور Y (بالا و پایین)
    private double currentY;     // موقعیت اعشاری برای محاسبه دقیق مسیر

    public DiagonalProjectile(int startRow, double startX, int damage, double speedX, double speedY) {
        super(startRow, startX, damage, speedX);
        this.speedY = speedY;
        this.currentY = startRow;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead) return;

        this.x += speed; // حرکت در محور X
        this.currentY += speedY; // حرکت در محور Y
        
        // آپدیت کردن ردیف تقریبی پرتابه برای تشخیص برخورد
        this.row = (int) Math.round(currentY);

        // نابودی در صورت خروج از مرزهای نقشه (بالا، پایین، چپ، راست)
        if (x < 0 || x > Board.COLS || currentY < 0 || currentY >= Board.ROWS) {
            isDead = true;
            return;
        }

        // بررسی برخورد با زامبی‌ها در ردیف فعلی
        for (Zombie z : session.getAliveZombies()) {
            if (z.getRow() == this.row && !z.isDead() && Math.abs(z.getXPosition() - this.x) < 0.5) {
                z.takeDamage(damage, DamageType.NORMAL);
                isDead = true;
                return;
            }
        }
    }
}