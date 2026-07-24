package model.quest;

import java.util.*;

public class MeowPoint {

    // ==================== مدل داده رویدادهای بازی ====================

    public enum EventType {
        ZOMBIE_KILLED_MULTI_HIT,   // کشتن چند زامبی با یک تیر
        ZOMBIE_KILLED_FAST,        // کشتن سریع زامبی (کمتر از حد آستانه زمانی)
        ZOMBIE_KILLED_SIMULTANEOUS,// کشتن همزمان چند زامبی
        WAVE_CLEARED_NO_DAMAGE,    // پاک کردن یک موج بدون از دست دادن گیاه
        SUN_COLLECTED_STREAK       // جمع‌آوری پیوسته خورشید بدون وقفه
    }

    public static class GameEvent {
        EventType type;
        int zombiesInvolved;   // تعداد زامبی‌های درگیر در این رویداد
        double timeSinceSpawnMs; // زمان سپری‌شده از ظهور زامبی تا مرگش (میلی‌ثانیه)

        public GameEvent(EventType type, int zombiesInvolved, double timeSinceSpawnMs) {
            this.type = type;
            this.zombiesInvolved = zombiesInvolved;
            this.timeSinceSpawnMs = timeSinceSpawnMs;
        }
    }

    // ==================== ثابت‌های امتیازدهی ====================

    private static final int BASE_KILL_POINT = 10;
    private static final int MULTI_HIT_BONUS_PER_EXTRA_ZOMBIE = 15;
    private static final double FAST_KILL_THRESHOLD_MS = 2000.0;
    private static final int FAST_KILL_BONUS = 20;
    private static final int SIMULTANEOUS_KILL_BONUS_PER_ZOMBIE = 12;
    private static final int WAVE_CLEAN_BONUS = 100;
    private static final int SUN_STREAK_BONUS_PER_UNIT = 5;

    // ==================== محاسبه امتیاز کل ====================

    public int calculateMyuPoints(List<GameEvent> events) {
        int total = 0;
        for (GameEvent event : events) {
            total += calculateSingleEvent(event);
        }
        return total;
    }

    private int calculateSingleEvent(GameEvent event) {
        switch (event.type) {
            case ZOMBIE_KILLED_MULTI_HIT:
                return calculateMultiHitScore(event);
            case ZOMBIE_KILLED_FAST:
                return calculateFastKillScore(event);
            case ZOMBIE_KILLED_SIMULTANEOUS:
                return calculateSimultaneousKillScore(event);
            case WAVE_CLEARED_NO_DAMAGE:
                return WAVE_CLEAN_BONUS;
            case SUN_COLLECTED_STREAK:
                return event.zombiesInvolved * SUN_STREAK_BONUS_PER_UNIT;
            default:
                return 0;
        }
    }

    // الگو ۱: کشتن چند زامبی با یک تیر
    private int calculateMultiHitScore(GameEvent event) {
        int extraZombies = Math.max(0, event.zombiesInvolved - 1);
        return BASE_KILL_POINT + extraZombies * MULTI_HIT_BONUS_PER_EXTRA_ZOMBIE;
    }

    // الگو ۲: کشتن سریع زامبی
    private int calculateFastKillScore(GameEvent event) {
        int score = BASE_KILL_POINT;
        if (event.timeSinceSpawnMs <= FAST_KILL_THRESHOLD_MS) {
            score += FAST_KILL_BONUS;
        }
        return score;
    }

    // الگو ۳: کشتن همزمان زامبی‌ها
    private int calculateSimultaneousKillScore(GameEvent event) {
        return event.zombiesInvolved * (BASE_KILL_POINT + SIMULTANEOUS_KILL_BONUS_PER_ZOMBIE);
    }

    // ==================== مثال استفاده ====================

    public static void main(String[] args) {
        MeowPoint calculator = new MeowPoint();

        List<GameEvent> events = new ArrayList<>();
        events.add(new GameEvent(EventType.ZOMBIE_KILLED_MULTI_HIT, 3, 0));
        events.add(new GameEvent(EventType.ZOMBIE_KILLED_FAST, 1, 1500));
        events.add(new GameEvent(EventType.ZOMBIE_KILLED_SIMULTANEOUS, 4, 0));
        events.add(new GameEvent(EventType.WAVE_CLEARED_NO_DAMAGE, 0, 0));
        events.add(new GameEvent(EventType.SUN_COLLECTED_STREAK, 10, 0));

        int totalScore = calculator.calculateMyuPoints(events);
        System.out.println("Total MeowPoints: " + totalScore);
    }
}
