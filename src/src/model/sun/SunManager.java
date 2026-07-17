package model.sun;

import model.game.Board;

import java.util.Random;

/** پیاده‌سازی مکانیزم سقوط خورشید از آسمان طبق فرمول x = max(6 + 0.05t, 12). */
public class SunManager {

    private final Random random = new Random();
    private int currentSun = 50;
    private double secondsSinceStart = 0;
    private double secondsUntilNextSun;
    private int nextSunId = 1;

    public SunManager() {
        recomputeNextInterval();
    }

    public int getCurrentSun() {
        return currentSun;
    }

    public void addSun(int amount) {
        currentSun += amount;
    }

    public boolean spendSun(int amount) {
        if (currentSun < amount) {
            return false;
        }
        currentSun -= amount;
        return true;
    }

    private void recomputeNextInterval() {
        // طبق فرمول داک: هرچه بازی جلوتر می‌رود، فاصله سقوط خورشید کمتر می‌شود (حداقل ۱۲ ثانیه? طبق فرمول
        // max(6+0.05t, 12) در واقع با افزایش t به سمت بالا می‌رود؛ اینجا دقیقا طبق فرمول داک عمل می‌کنیم)
        secondsUntilNextSun = Math.max(6 + 0.05 * secondsSinceStart, 12);
    }

    /**
     * باید به ازای هر تیک (0.1 ثانیه) صدا زده شود. اگر خورشیدی سقوط کند شیء آن برگردانده می‌شود، وگرنه null.
     */
    public FallingSun tick(Board board) {
        secondsSinceStart += 0.1;
        secondsUntilNextSun -= 0.1;
        if (secondsUntilNextSun <= 0) {
            recomputeNextInterval();
            int col = random.nextInt(Board.COLS);
            int row = random.nextInt(Board.ROWS);
            double roll = random.nextDouble();
            FallingSun.Kind kind;
            if (roll < 0.80) {
                kind = FallingSun.Kind.NORMAL;
            } else if (roll < 0.95) {
                kind = FallingSun.Kind.SPECIAL;
            } else {
                kind = FallingSun.Kind.RADIOACTIVE;
            }
            return new FallingSun(nextSunId++, row, col, kind);
        }
        return null;
    }
}
