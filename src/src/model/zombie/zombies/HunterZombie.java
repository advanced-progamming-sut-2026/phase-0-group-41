package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class HunterZombie extends Zombie {

    private static final int THROW_COOLDOWN_TICKS = 30; // مثلا هر ۳ ثانیه یکبار یخ پرتاب می‌کند
    private int cooldownTimer = 0;

    public HunterZombie() {
        // نام، جان (معمولی رو به بالا)، سرعت استاندارد، هزینه موج
        super("hunter", 350, 0.01, 250, 10);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // ۱. پیدا کردن نزدیک‌ترین گیاه در سطر فعلی
        Plant nearestPlant = findNearestPlantInRow(session);

        if (nearestPlant != null) {
            // اگر گیاهی در سطر بود، زامبی می‌ایستد (یا حرکت را کند می‌کند) و یخ می‌اندازد
            setEating(false); // انیمیشن گاز گرفتن نداریم، چون از دور می‌زند

            if (cooldownTimer <= 0) {
                throwIceAt(nearestPlant);
                cooldownTimer = THROW_COOLDOWN_TICKS; // ریست کردن تایمر
            } else {
                cooldownTimer--; // صبر برای پرتاب بعدی
            }
        } else {
            // اگر هیچ گیاهی در سطر نبود، با سرعت عادی به حرکت ادامه می‌دهد
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
            cooldownTimer = 0; // آماده پرتاب به محض دیدن گیاه جدید
        }
    }

    // 🌟 متد تارگت‌گیری دقیق: اسکن کردن سطر از جایگاه زامبی تا آخر سمت چپ
    private Plant findNearestPlantInRow(GameSession session) {
        int myCol = (int) Math.floor(getXPosition());
        Board board = session.getBoard();

        // حلقه از ستون فعلی زامبی به سمت چپ (ستون ۰) حرکت می‌کند
        for (int c = myCol; c >= 0; c--) {
            Tile tile = board.getTile(getRow(), c);
            if (tile != null) {
                Plant potentialTarget = tile.getPlant();
                // اولین گیاه زنده‌ای که پیدا کرد را برمی‌گرداند (چون از سمت راست اسکن می‌کنیم، این نزدیک‌ترین است)
                if (potentialTarget != null && !potentialTarget.isDead()) {
                    return potentialTarget;
                }
            }
        }
        return null; // اگر حلقه‌ تمام شد و چیزی نبود، یعنی سطر خالیه
    }

    // 🌟 متد اعمال ضربه یخی
    private void throwIceAt(Plant target) {
        // در یک معماری بی‌نقص، زامبی به گیاه می‌گوید "یک ضربه یخی دریافت کن"
        // شمارش تعداد ضربات یخی وظیفه خود گیاه است.

        // نکته: اگر سیستم پرتابه (Projectile) برای زامبی‌ها داری، اینجا باید
        // گلوله یخی رو Spawn کنی. اگر نه، ضربه رو مستقیم (Hitscan) وارد می‌کنیم:
        target.receiveIceHit();
    }
}