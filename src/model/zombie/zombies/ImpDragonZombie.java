package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class ImpDragonZombie extends Zombie {

    private static final double IMP_SPEED = 0.025; // ایمپ‌ها معمولاً سرعت حرکت بالایی دارند

    public ImpDragonZombie() {
        // نام، جان (معمولاً کم مثل بقیه ایمپ‌ها)، سرعت، دمیج گاز گرفتن، هزینه
        super("imp_dragon", 200, IMP_SPEED, 100, 5);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // منطق حرکت و خوردن استاندارد ایمپ‌ها
        Tile currentTile = getCurrentTile(session);
        Plant target = (currentTile != null) ? currentTile.getPlant() : null;

        if (target != null && !target.isDead()) {
            setEating(true);
            target.takeDamage((int) getDamagePerTick());

            if (target.isDead() && currentTile != null) {
                currentTile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    /**
     * 🌟 متد اختصاصی دریافت دمیج برای بررسی نوع حمله آتشین
     *
     * @param damage مقدار آسیب
     * @param isFireDamage آیا این آسیب از نوع آتشین است؟ (مثل تیرهای Peashooter که از Torchwood رد شده‌اند)
     */
    public void takeDamage(int damage, boolean isFireDamage) {
        if (isDead()) return;

        // طبق داکیومنت: تیرهای آتشین رویش تاثیری ندارند
        if (isFireDamage) {
            return; // دمیج کاملاً خنثی می‌شود و هیچ اتفاقی نمی‌افتد
        }

        // در غیر این صورت، آسیب عادی به کلاس پدر فرستاده می‌شود
        super.takeDamage(damage);
    }

    private Tile getCurrentTile(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        return session.getBoard().getTile(getRow(), Math.max(col, 0));
    }
}