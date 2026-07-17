package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class JesterZombie extends Zombie {

    private boolean isSpinning = false;
    private int ticksSinceLastProjectile = 0;

    // مدت زمانی که دلقک بعد از آخرین تیرِ دریافتی همچنان به چرخش ادامه می‌دهد (مثلاً ۳۰ تیک)
    private static final int SPIN_SUSTAIN_TICKS = 30;

    private static final double WALK_SPEED = 0.015;
    private static final double SPIN_SPEED = 0.03;

    public JesterZombie() {
        super("jester", 350, WALK_SPEED, 200, 10);
    }

    @Override
    public double getSpeed() {
        // حین چرخش، سریع‌تر به جلو حرکت می‌کند
        return isSpinning ? SPIN_SPEED : WALK_SPEED;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // مدیریت زمان چرخش: اگر در حال چرخیدن است، تیک‌ها را می‌شماریم
        if (isSpinning) {
            ticksSinceLastProjectile++;
            // اگر برای مدتی هیچ پرتابه‌ای به سمتش نیامد، چرخش را متوقف می‌کند و عادی راه می‌رود
            if (ticksSinceLastProjectile >= SPIN_SUSTAIN_TICKS) {
                isSpinning = false;
                ticksSinceLastProjectile = 0;
            }
        }

        Tile currentTile = getCurrentTile(session);
        Plant target = (currentTile != null) ? currentTile.getPlant() : null;

        if (target != null && !target.isDead()) {
            // به محض رسیدن به گیاه برای خوردن، چرخش متوقف می‌شود
            isSpinning = false;
            ticksSinceLastProjectile = 0;

            setEating(true);
            target.takeDamage((int) getDamagePerTick());

            if (target.isDead() && currentTile != null) {
                currentTile.setPlant(null);
                setEating(false);
            }
        } else {
            // حرکت به جلو (سرعت بر اساس وضعیت چرخش در متد getSpeed اعمال می‌شود)
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }

    /**
     * 🌟 متد اصلی برخورد پرتابه‌ها به دلقک (توسط سیستم گلوله‌های بازی صدا زده می‌شود)
     *
     * @param session جلسه فعلی بازی
     * @param damage مقدار آسیب گلوله
     * @param isReflectable آیا گلوله مستقیم و قابل برگشت است؟ (نخودها = true، منجنیق‌ها = false)
     * @param isFrozenBullet آیا گلوله از نوع یخی است؟
     * @return true اگر تیر بازگردانده شد و دلقک آسیب ندید / false اگر تیر برگشت‌ناپذیر بود و دمیج خورد
     */
    public boolean handleProjectileHit(GameSession session, int damage, boolean isReflectable, boolean isFrozenBullet) {
        if (isDead()) return false;

        if (isReflectable) {
            // شروع چرخش (یا تمدید وضعیت چرخش فعال بدون محدودیت زمانی)
            isSpinning = true;
            ticksSinceLastProjectile = 0; // ریست کردن زمان‌سنج آخرین تیر دریافتی

            // برگرداندن تیر به سمت اولین گیاه موجود در سطر
            reflectProjectile(session, damage, isFrozenBullet);
            return true; // تیر با موفقیت برگشت داده شد و به دلقک آسیبی نرسید
        }

        // اگر تیر قابل برگشت نبود (مثل محصولات Lobber)، دلقک دمیج عادی می‌خورد
        super.takeDamage(damage);
        return false;
    }

    // 🌟 منطق بازگرداندن گلوله و اعمال اثرات یخ‌زدگی بر اساس مستندات جدید
    private void reflectProjectile(GameSession session, int damage, boolean isFrozenBullet) {
        Board board = session.getBoard();
        int currentCol = (int) Math.floor(getXPosition());

        // اسکن سطر از موقعیت فعلی زامبی به سمت چپ (خانه بازیکن)
        for (int col = currentCol; col >= 0; col--) {
            Tile tile = board.getTile(getRow(), col);
            if (tile != null) {
                Plant hitPlant = tile.getPlant();
                if (hitPlant != null && !hitPlant.isDead()) {

                    // ۱. تیر برگشتی به گیاه آسیب می‌زند
                    hitPlant.takeDamage(damage);

                    // ۲. اگر تیر یخی بود، افکت یخی زامبی شکارچی روی گیاه اعمال می‌شود
                    if (isFrozenBullet) {
                        hitPlant.receiveIceHit();
                    }

                    if (hitPlant.isDead()) {
                        tile.setPlant(null);
                    }
                    break; // تیر به اولین گیاه سطر برخورد کرد و ناپدید شد
                }
            }
        }
    }

    private Tile getCurrentTile(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        return session.getBoard().getTile(getRow(), Math.max(col, 0));
    }
}