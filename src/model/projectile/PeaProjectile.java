package model.projectile;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.plants.Torchwood;
import model.zombie.Zombie;

public class PeaProjectile extends Projectile {

    public PeaProjectile(int row, double startX, int damage) {
        super(row, startX, damage, 0.3); // سرعت حرکت ۰.۳ تایل بر تیک
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead) return;

        x += speed; // حرکت به جلو

        // ۱. نابودی در صورت خروج از صفحه بازی
        if (x > Board.COLS) {
            isDead = true;
            return;
        }

        // ۲. بررسی عبور از روی کنده آتشین (Torchwood)
        Tile tile = session.getBoard().getTile(row, (int) Math.floor(x));
        if (tile != null && tile.getPlant() instanceof Torchwood) {
            Torchwood torch = (Torchwood) tile.getPlant();
            torch.modifyProjectile(this);
        }

        // ۳. سیستم تشخیص برخورد (Collision Detection) با دقیق‌ترین زامبی
        Zombie target = null;
        double minX = Double.MAX_VALUE;

        for (Zombie z : session.getAliveZombies()) {
            // اگر زامبی در همین لاین است، زنده است و مختصاتش با پرتابه مماس شده:
            if (z.getRow() == row && !z.isDead() && z.getXPosition() >= this.x - 0.5 && z.getXPosition() <= this.x + 0.5) {
                // پیدا کردن زامبی‌ای که جلوتر از همه است (نزدیک‌ترین به پرتابه)
                if (z.getXPosition() < minX) {
                    minX = z.getXPosition();
                    target = z;
                }
            }
        }

        // ۴. اعمال اثرات برخورد
        if (target != null) {
            if(target instanceof model.zombie.zombies.JesterZombie) {
                model.zombie.zombies.JesterZombie jester = (model.zombie.zombies.JesterZombie) target;
                boolean reflected = jester.handleProjectileHit(session, damage, true, this.isIce);
            
                if(reflected) {
                    isDead = true; //تیر برگشت و این پرتابه نابود شد
                    System.out.println("پرتابه توسط Jester Zombie دفع و بازگردانده شد!");
                    return;
                }
            }

            target.takeDamage(damage, model.zombie.DamageType.NORMAL);

            if (this.isIce) {
                target.applyChill(50); // ۵۰ تیک (۵ ثانیه) کندی
            }
            // اگر تیر آتشین به زامبی یخی بخورد، یخش آب می‌شود
            if (this.isFire) {
                target.removeChill(); 
            }

            isDead = true; // نابودی پرتابه پس از برخورد با اولین زامبی
        }
    }
}