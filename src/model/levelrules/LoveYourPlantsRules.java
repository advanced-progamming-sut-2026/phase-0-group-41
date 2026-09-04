package model.levelrules;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * از دست نده (Love Your Plants): طبق سند، اگر تعداد مشخصی از گیاهان (مثلاً ۵
 * عدد) از بین بروند یا توسط زامبی‌ها خورده شوند، بازیکن می‌بازد. توجه: این
 * شرط روی «تعداد گیاهانی که تا الان از بین رفته‌اند» است، نه سقفِ همزمانِ
 * گیاهان روی زمین؛ بنابراین برداشتن گیاه با بیلچه (که خود بازیکن انتخاب کرده)
 * جزو این شمارش نیست، فقط گیاهانی که توسط زامبی خورده/نابود شده‌اند حساب
 * می‌شوند.
 */
public class LoveYourPlantsRules implements ILevelRules {

    private final int maxAllowedLosses;
    private int plantsLost = 0;

    // ردیابیِ گیاهانی که در فریم قبل زنده روی زمین بودند، برای تشخیص اینکه
    // یک گیاهِ ناپدیدشده «خورده شده» (isDead()==true) بوده یا با بیلچه توسط
    // خود بازیکن برداشته شده (isDead()==false)؛ فقط حالت اول شمارش می‌شود.
    private final Map<Plant, Boolean> previouslyAlivePlants = new IdentityHashMap<>();

    public LoveYourPlantsRules(int maxAllowedLosses) {
        this.maxAllowedLosses = maxAllowedLosses;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("چالش از دست نده: اگر " + maxAllowedLosses + " گیاه یا بیشتر توسط زامبی‌ها از بین بروند، می‌بازید!");
        previouslyAlivePlants.clear();
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // شرط اصلی در checkCustomLossConditions که هر تیک بعد از حذف گیاهان
        // مرده از تخته فراخوانی می‌شود بررسی می‌گردد؛ اینجا کاری لازم نیست.
    }

    /**
     * مقایسه‌ی گیاهانِ زنده‌ی فریم فعلی با فریم قبل؛ هر گیاهی که در فریم قبل
     * زنده بود ولی الان دیگر روی همان خانه نیست و isDead() آن true بود (یعنی
     * توسط زامبی از بین رفته، نه با بیلچه برداشته شده)، به‌عنوان «از دست
     * رفته» شمارش می‌شود.
     */
    private void updateLossCount(GameSession session) {
        Board board = session.getBoard();
        Set<Plant> currentlyAlive = Collections.newSetFromMap(new IdentityHashMap<>());

        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                Tile tile = board.getTile(r, c);
                Plant p = tile.getPlant();
                if (p != null && !p.isDead()) {
                    currentlyAlive.add(p);
                }
            }
        }

        for (Plant p : previouslyAlivePlants.keySet()) {
            if (!currentlyAlive.contains(p) && p.isDead()) {
                plantsLost++;
                System.out.println("یک گیاه توسط زامبی‌ها از بین رفت! مجموع گیاهان از دست‌رفته: "
                        + plantsLost + "/" + maxAllowedLosses);
            }
        }

        previouslyAlivePlants.clear();
        for (Plant p : currentlyAlive) {
            previouslyAlivePlants.put(p, Boolean.TRUE);
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        updateLossCount(session);

        if (plantsLost >= maxAllowedLosses) {
            System.out.println("خطا! " + plantsLost + " گیاه از دست دادید که به حد مجاز (" + maxAllowedLosses + ") رسید!");
            return false; // بازیکن باخت!
        }

        return true; // شرایط عادی است
    }

    public int getPlantsLost() {
        return plantsLost;
    }

    public int getMaxAllowedLosses() {
        return maxAllowedLosses;
    }

    @Override
    public String getHudStatusText(GameSession session) {
        int remaining = Math.max(0, maxAllowedLosses - plantsLost);
        return "از دست نده: " + remaining + " گیاه دیگر مجاز به از دست دادن هستید (" + plantsLost + "/" + maxAllowedLosses + ")";
    }
}
