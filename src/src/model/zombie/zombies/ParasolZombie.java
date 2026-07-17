package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class ParasolZombie extends Zombie {

    // پرچمی برای مشخص کردن اینکه آیا الان ضربه‌ای دفع شده یا نه
    private boolean isDeflecting = false;

    public ParasolZombie() {
        // نام (parasol)، جون (350)، سرعت (0.01)، هزینه موج (150)، قدرت ضربه (10)
        super("parasol", 350, 0.01, 150, 10);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        // --- بخش ۱: پیدا کردن هدف (تارگت) ---
        Board board = session.getBoard();
        int col = (int) Math.floor(getXPosition());
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // --- بخش ۲: منطق خوردن یا راه رفتن ---
        // چک می‌کنیم آیا گیاهی هست؟ آیا نمرده؟ آیا زامبی به اندازه کافی بهش نزدیک شده؟
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {

            setEating(true); // وضعیت زامبی رو می‌ذاریم رو حالت خوردن
            target.takeDamage(getDamagePerTick()); // از جون گیاه کم می‌کنیم

            // اگر بعد از این گاز گرفتن، گیاه مرد:
            if (target.isDead()) {
                tile.setPlant(null); // گیاه رو از روی زمین پاک می‌کنیم
                setEating(false); // زامبی دیگه چیزی برای خوردن نداره
            }

        } else {
            // اگر گیاهی نبود یا بهش نرسیده بود:
            setEating(false);
            setXPosition(getXPosition() - getSpeed()); // حرکت به سمت چپ
        }

        // --- بخش ۳: ریست کردن وضعیت چتر ---
        // هر بار که یک تیک (کسری از ثانیه) می‌گذره، پرچم دفع رو خاموش می‌کنیم
        if (isDeflecting) {
            isDeflecting = false;
        }
    }

    /**
     * گیاهان پرتابی (Lobber) مثل Cabbage-pult به جای takeDamage باید این متد را صدا بزنند.
     */
    public void receiveLobbedProjectile(int damage) {
        // هیچ دمیجی از health کم نمی‌شود چون چتر آن را دفع می‌کند!
        // فقط پرچم را روشن می‌کنیم تا سیستم بداند چتر عمل کرده است.
        this.isDeflecting = true;
    }

    /**
     * این متد برای کلاس View است تا بتواند بررسی کند آیا چتر الان فعال شده است یا خیر.
     */
    public boolean isCurrentlyDeflecting() {
        return isDeflecting;
    }
}