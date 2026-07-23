package model.minigame;

import model.game.Board;
import model.game.Tile;
import model.plant.Plant;
import model.plant.PlantFactory;
import model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeghouledSession extends MiniGameSession {

    public enum SwapResult {
        SUCCESS, INVALID_LOCATION, NOT_ADJACENT, EMPTY_TILE, NO_MATCH
    }

    public enum UpgradeResult {
        SUCCESS, NOT_ENOUGH_SUN, INVALID_UPGRADE
    }

    // ۵ گیاه پایه‌ای که در زمین می‌افتند
    private final String[] activePlantPool = {"peashooter", "wallnut", "puffshroom", "cabbagepult", "magnetshroom"};
    
    private int matchCount = 0;
    private static final int TARGET_MATCHES = 75; // شرط برد
    private boolean isGameWon = false;

    private final List<String> recentEvents = new ArrayList<>();
    private final Random random = new Random();

    public BeghouledSession(User user) {
        super(user, 1);
        getSunManager().addSun(0); // خورشید اولیه صفر
        fillBoard();
        // پاکسازی مچ‌های اولیه‌ای که تصادفی ایجاد شده‌اند بدون دادن امتیاز
        resolveMatches(false); 
    }

    private void fillBoard() {
        Board board = getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.getTile(r, c).isEmpty()) {
                    String randomPlant = activePlantPool[random.nextInt(activePlantPool.length)];
                    board.getTile(r, c).setPlant(PlantFactory.create(randomPlant));
                }
            }
        }
    }

    public SwapResult swapPlants(int r1, int c1, int r2, int c2) {
        if (r1 < 0 || r1 >= Board.ROWS || c1 < 0 || c1 >= Board.COLS ||
            r2 < 0 || r2 >= Board.ROWS || c2 < 0 || c2 >= Board.COLS) {
            return SwapResult.INVALID_LOCATION;
        }

        // بررسی مجاورت (فقط بالا، پایین، چپ، راست)
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) {
            return SwapResult.NOT_ADJACENT;
        }

        Tile t1 = getBoard().getTile(r1, c1);
        Tile t2 = getBoard().getTile(r2, c2);

        // اگر زامبی گیاهی را خورده باشد، جابجایی در آن نقطه (crater) ممکن نیست
        if (t1.isEmpty() || t2.isEmpty()) {
            return SwapResult.EMPTY_TILE;
        }

        // انجام جابجایی موقت
        Plant temp = t1.getPlant();
        t1.setPlant(t2.getPlant());
        t2.setPlant(temp);

        // بررسی اینکه آیا مچی ایجاد شد یا خیر
        if (!resolveMatches(true)) {
            // اگر مچ نشد، جابجایی برمی‌گردد
            t2.setPlant(t1.getPlant());
            t1.setPlant(temp);
            return SwapResult.NO_MATCH;
        }

        return SwapResult.SUCCESS;
    }

    // متد بازگشتی برای پیدا کردن و حذف مچ‌ها و اعمال جاذبه
    private boolean resolveMatches(boolean giveReward) {
        boolean matchFound = false;
        boolean[][] matched = new boolean[Board.ROWS][Board.COLS];
        Board board = getBoard();

        // بررسی ردیف‌ها (افقی)
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS - 2; c++) {
                Plant p = board.getTile(r, c).getPlant();
                if (p == null) continue;
                String name = p.getName();
                int length = 1;
                
                for (int k = c + 1; k < Board.COLS; k++) {
                    Plant pk = board.getTile(r, k).getPlant();
                    if (pk != null && pk.getName().equals(name)) length++;
                    else break;
                }
                
                if (length >= 3) {
                    matchFound = true;
                    if (giveReward) awardMatch(length);
                    for (int k = 0; k < length; k++) matched[r][c + k] = true;
                    c += length - 1;
                }
            }
        }

        // بررسی ستون‌ها (عمودی)
        for (int c = 0; c < Board.COLS; c++) {
            for (int r = 0; r < Board.ROWS - 2; r++) {
                Plant p = board.getTile(r, c).getPlant();
                if (p == null) continue;
                String name = p.getName();
                int length = 1;

                for (int k = r + 1; k < Board.ROWS; k++) {
                    Plant pk = board.getTile(k, c).getPlant();
                    if (pk != null && pk.getName().equals(name)) length++;
                    else break;
                }

                if (length >= 3) {
                    matchFound = true;
                    if (giveReward) awardMatch(length);
                    for (int k = 0; k < length; k++) matched[r + k][c] = true;
                    r += length - 1;
                }
            }
        }

        if (!matchFound) return false;

        // حذف گیاهان مچ شده
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (matched[r][c]) board.getTile(r, c).setPlant(null);
            }
        }

        // اعمال جاذبه (سقوط گیاهان به پایین)
        applyGravity();
        
        // پر کردن خانه‌های خالی بالای صفحه
        fillBoard();
        
        // فراخوانی مجدد برای مچ‌های زنجیره‌ای (Cascading)
        resolveMatches(giveReward);

        return true;
    }

    private void applyGravity() {
        Board board = getBoard();
        for (int c = 0; c < Board.COLS; c++) {
            for (int r = Board.ROWS - 1; r >= 0; r--) {
                if (board.getTile(r, c).isEmpty()) {
                    // گشتن به دنبال اولین گیاه در بالای این خانه برای سقوط
                    for (int k = r - 1; k >= 0; k--) {
                        if (!board.getTile(k, c).isEmpty()) {
                            board.getTile(r, c).setPlant(board.getTile(k, c).getPlant());
                            board.getTile(k, c).setPlant(null);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void awardMatch(int length) {
        matchCount++;
        int sunsToGive = (length - 2) * 50; // ۳تایی = ۵۰، ۴تایی = ۱۰۰، ۵تایی = ۱۵۰
        getSunManager().addSun(sunsToGive);
        
        recentEvents.add("ترکیب " + length + "تایی! دریافت " + sunsToGive + " خورشید. (مجموع مچ‌ها: " + matchCount + "/" + TARGET_MATCHES + ")");

        if (matchCount >= TARGET_MATCHES && !isGameWon) {
            isGameWon = true;
            getAliveZombies().clear(); // نابودی تمام زامبی‌ها طبق داک
            recentEvents.add("تبریک! شما به هدف " + TARGET_MATCHES + " مچ رسیدید و برنده شدید!");
        }
    }

    public UpgradeResult upgradePlant(String oldPlantName) {
        String oldName = oldPlantName.toLowerCase();
        String newName = null;
        int cost = 0;

        switch (oldName) {
            case "peashooter": newName = "repeater"; cost = 500; break;
            case "repeater": newName = "megagatlingpea"; cost = 1500; break;
            case "wallnut": newName = "tallnut"; cost = 500; break;
            case "puffshroom": newName = "fumeshroom"; cost = 250; break;
            case "cabbagepult": newName = "melonpult"; cost = 1000; break;
            case "melonpult": newName = "wintermelon"; cost = 750; break;
            default: return UpgradeResult.INVALID_UPGRADE;
        }

        // بررسی وجود این گیاه در استخر فعلی
        int poolIndex = -1;
        for (int i = 0; i < activePlantPool.length; i++) {
            if (activePlantPool[i].equals(oldName)) {
                poolIndex = i;
                break;
            }
        }

        if (poolIndex == -1) return UpgradeResult.INVALID_UPGRADE; // گیاه در زمین نیست

        if (!getSunManager().spendSun(cost)) {
            return UpgradeResult.NOT_ENOUGH_SUN;
        }

        // ۱. جایگزینی در استخر سقوط (تا گیاهان جدید ارتقایافته باشند)
        activePlantPool[poolIndex] = newName;

        // ۲. جایگزینی تمام گیاهان موجود در زمین
        Board board = getBoard();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Plant p = board.getTile(r, c).getPlant();
                if (p != null && p.getName().equalsIgnoreCase(oldName)) {
                    Plant newPlant = PlantFactory.create(newName);
                    newPlant.place(r, c);
                    board.getTile(r, c).setPlant(newPlant);
                }
            }
        }

        // چون نوع گیاهان عوض شد، ممکن است مچ جدیدی رخ داده باشد
        resolveMatches(true);
        return UpgradeResult.SUCCESS;
    }

    @Override
    protected void customMiniGameTick() {
        if (isGameWon) return;
        getFallingSuns().clear(); // بدون بارش خورشید از آسمان
    }

    public List<String> pollRecentEvents() {
        List<String> copy = new ArrayList<>(recentEvents);
        recentEvents.clear();
        return copy;
    }
}