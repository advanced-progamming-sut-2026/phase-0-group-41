package model.greenhouse;

/**
 * TODO: پیاده‌سازی کامل گلخانه طبق داک (۲۰ گلدان در ۴×۵، قفل بودن ردیف‌های ۲ تا ۴،
 * کاشت تصادفی گل/گیاه، رشد بر اساس ساعت سیستم، برداشت و تسریع رشد با الماس).
 * این کلاس صرفا نقطه‌ی شروع (اسکلت) است.
 */
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class Greenhouse implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int ROWS = 4;
    public static final int COLS = 5;

    private final Pot[][] pots = new Pot[ROWS][COLS];

    public Greenhouse() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                pots[r][c] = new Pot();
            }
        }
        for (int c = 0; c < COLS; c++) {
            pots[0][c].setUnlocked(true);
        }
    }

    public boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public boolean isUnlocked(int row, int col) {
        return isValidCoordinate(row, col) && pots[row][col].isUnlocked();
    }

    public boolean isLocked(int row, int col) {
        return isValidCoordinate(row, col) && !pots[row][col].isUnlocked();
    }

    public boolean isEmpty(int row, int col) {
        return isValidCoordinate(row, col) && pots[row][col].isEmpty();
    }

    public boolean hasPlant(int row, int col) {
        return isValidCoordinate(row, col) && !pots[row][col].isEmpty();
    }

    public boolean canPlant(int row, int col) {
        return isValidCoordinate(row, col) && isUnlocked(row, col) && pots[row][col].isEmpty();
    }

    public void unlock(int row, int col) {
        if (isValidCoordinate(row, col)) {
            pots[row][col].setUnlocked(true);
        }
    }

    public void plantAt(int row, int col, String plantName, long growDurationMillis) {
        if (!isValidCoordinate(row, col)) {
            return;
        }
        Pot pot = pots[row][col];
        pot.setPlantName(plantName);
        pot.setPlantedAtMillis(System.currentTimeMillis());
        pot.setGrowDurationMillis(growDurationMillis);
    }

    public boolean isReady(int row, int col) {
        return hasPlant(row, col) && getRemainingMillis(row, col) <= 0;
    }

    public long getRemainingMillis(int row, int col) {
        if (!hasPlant(row, col)) {
            return 0;
        }
        Pot pot = pots[row][col];
        long remaining = pot.getPlantedAtMillis() + pot.getGrowDurationMillis() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public String getPlantName(int row, int col) {
        return hasPlant(row, col) ? pots[row][col].getPlantName() : null;
    }

    public void clearPot(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return;
        }
        pots[row][col].clear();
    }

    public long acceleratePot(int row, int col) {
        long remaining = getRemainingMillis(row, col);
        if (!hasPlant(row, col)) {
            return 0;
        }
        pots[row][col].setPlantedAtMillis(System.currentTimeMillis() - pots[row][col].getGrowDurationMillis());
        return remaining;
    }

    public String getDisplayStatus(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return "INVALID";
        }
        Pot pot = pots[row][col];
        if (!pot.isUnlocked()) {
            return "LOCKED";
        }
        if (pot.isEmpty()) {
            return "EMPTY";
        }
        if (isReady(row, col)) {
            return "READY:" + pot.getPlantName();
        }
        return pot.getPlantName() + "(" + formatDuration(getRemainingMillis(row, col)) + ")";
    }

    public static String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        return hours + "h " + minutes + "m";
    }


}
