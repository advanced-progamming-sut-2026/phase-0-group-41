package model.greenhouse;

/**
 * TODO: پیاده‌سازی کامل گلخانه طبق داک (۲۰ گلدان در ۴×۵، قفل بودن ردیف‌های ۲ تا ۴،
 * کاشت تصادفی گل/گیاه، رشد بر اساس ساعت سیستم، برداشت و تسریع رشد با الماس).
 * این کلاس صرفا نقطه‌ی شروع (اسکلت) است.
 */
public class Greenhouse {

    public static final int ROWS = 4;
    public static final int COLS = 5;

    private final boolean[][] potUnlocked = new boolean[ROWS][COLS];

    public Greenhouse() {
        for (int c = 0; c < COLS; c++) {
            potUnlocked[0][c] = true; // ردیف اول از ابتدا باز است
        }
    }

    public boolean isUnlocked(int row, int col) {
        return potUnlocked[row][col];
    }

    public void unlock(int row, int col) {
        potUnlocked[row][col] = true;
    }
}
