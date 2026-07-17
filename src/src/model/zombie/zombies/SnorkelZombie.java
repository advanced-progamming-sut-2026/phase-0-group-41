package model.zombie.zombies;

import model.game.GameSession;
import model.game.TerrainType;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class SnorkelZombie extends Zombie {

    private boolean submerged = false;

    public SnorkelZombie() {
        // نام، جان (معمولی)، سرعت، دمیج در هر تیک، هزینه موج
        super("snorkel", 200, 0.015, 125, 10);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        Plant target = findTarget(session);
        Tile currentTile = getCurrentTile(session);

        if (target != null && !target.isDead()) {
            // ۱. برای خوردن گیاهان، به سطح آب می‌آید و کاملاً آسیب‌پذیر می‌شود
            submerged = false;
            setEating(true);
            target.takeDamage(getDamagePerTick());

            if (target.isDead()) {
                if (currentTile != null) currentTile.setPlant(null);
                setEating(false);
            }
        } else {
            // ۲. حرکت به جلو؛ اگر روی کاشی آب بود، زیر آب می‌رود
            setEating(false);
            setXPosition(getXPosition() - getSpeed());

            // بررسی نوع زمین برای رفتن به زیر آب
            submerged = (currentTile != null && currentTile.getTerrainType() == TerrainType.WATER);
        }
    }

    /**
     * 🌟 بخش حیاتیِ جا افتاده (مطابق با عکس):
     * اورراید کردن متد گرفتن دمیج برای پیاده‌سازی منطق Lobber
     */
    /**
     * اورراید کردن متد گرفتن دمیج برای پیاده‌سازی منطق Lobber
     */
    public void takeDamage(int damage, boolean isLobberAttack) {
        // اگر زامبی زیر آب باشد و حمله از نوع lobber نباشد، دمیج بی‌اثر است
        if (submerged && !isLobberAttack) {
            return; // گلوله‌های عادی از روی سر زامبی رد می‌شوند
        }

        // در غیر این صورت، دمیج به صورت int به کلاس پدر فرستاده می‌شود
        super.takeDamage(damage);
    }

    public boolean isSubmerged() {
        return submerged;
    }

    private Tile getCurrentTile(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        return session.getBoard().getTile(getRow(), Math.max(col, 0));
    }

    private Plant findTarget(GameSession session) {
        Tile tile = getCurrentTile(session);
        return (tile != null) ? tile.getPlant() : null;
    }
}