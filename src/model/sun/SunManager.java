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

    // === متغیر جدید برای ذخیره درجه سختی ===
    private final int difficultyLevel;

    // سازنده کلاس حالا درجه سختی را دریافت می‌کند
    public SunManager(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
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
        // ۱. محاسبه فاصله زمانی پایه طبق فرمول اولیه
        double baseInterval = Math.max(6 + 0.05 * secondsSinceStart, 12);

        // ۲. اعمال ضریب سختی:
        // چون نرخ ظاهر شدن باید کاهش یابد، پس زمانِ انتظار باید "افزایش" یابد.
        // طبق داکیومنت، ضریب افزایش برابر است با (dl / 3)
        double difficultyMultiplier = this.difficultyLevel / 3.0;

        this.secondsUntilNextSun = baseInterval * difficultyMultiplier;
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
