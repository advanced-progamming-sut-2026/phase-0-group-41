package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class OctopusZombie extends Zombie {

    private static final int THROW_COOLDOWN_TICKS = 40; // مثلاً هر ۴ ثانیه یک‌بار اختاپوس پرتاب می‌کند
    private int cooldownTimer = 0;

    public OctopusZombie() {
        // نام، جان (بالا)، سرعت استاندارد، دمیج گاز گرفتن، هزینه موج
        super("octopus", 450, 0.01, 100, 15);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // ۱. پیدا کردن فعال‌ترین یا نزدیک‌ترین گیاهی که هنوز اختاپوس روی سرش نیست
        Plant targetPlant = findTargetPlantInRow(session);

        if (targetPlant != null) {
            // وقتی هدف پیدا شد، زامبی متوقف می‌شود تا پرتاب کند
            setEating(false);

            if (cooldownTimer <= 0) {
                throwOctopusAt(targetPlant);
                cooldownTimer = THROW_COOLDOWN_TICKS; // ریست کردن تایمرCooldown
            } else {
                cooldownTimer--; // شمارش معکوس تیک‌ها
            }
        } else {
            // ۲. اگر هیچ گیاهی در سطر نبود یا همه از قبل اختاپوس داشتند، به حرکت ادامه می‌دهد
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
            cooldownTimer = 0; // آماده‌باش برای پرتاب آنی به محض دیدن گیاه جدید
        }
    }

    // 🌟 اسکن دقیق سطر برای پیدا کردن گیاهی که آزاد است و اختاپوس ندارد
    private Plant findTargetPlantInRow(GameSession session) {
        int myCol = (int) Math.floor(getXPosition());
        Board board = session.getBoard();

        // حرکت کاشی به کاشی از موقعیت زامبی به سمت چپ نقشه
        for (int c = myCol; c >= 0; c--) {
            Tile tile = board.getTile(getRow(), c);
            if (tile != null) {
                Plant potentialTarget = tile.getPlant();
                // شرط کلیدی: گیاه وجود داشته باشد، زنده باشد و از قبل اختاپوس روی سرش نباشد!
                if (potentialTarget != null && !potentialTarget.isDead() && !potentialTarget.isOctopused()) {
                    return potentialTarget;
                }
            }
        }
        return null;
    }

    // 🌟 متد پرتاب اختاپوس روی گیاه هدف
    private void throwOctopusAt(Plant target) {
        // به گیاه دستور می‌دهیم که افکت وضعیت اختاپوس را فعال کند
        target.bindByOctopus(300); // عدد ۳۰۰ مقدار جان (HP) خود اختاپوس است که باید با تیر گیاهان کم شود
    }
}