package model.levelrules;

import model.game.GameSession;

/**
 * نبرد زمان‌دار (Timed War): طبق سند دو نوع هدف دارد — «N زامبی را در T ثانیه
 * بکش» یا «به مقدار N خورشید در T ثانیه تولید کن». اگر تا پایان زمان به هدف
 * نرسد، بازیکن می‌بازد.
 */
public class TimedWarRules implements ILevelRules {

    public enum Objective { KILL_ZOMBIES, PRODUCE_SUN }

    private final int timeLimitTicks;
    private final Objective objective;
    private final int targetAmount;

    private int remainingTicks;
    private int baselineZombiesKilled = 0;
    private int baselineSunCollected = 0;
    private boolean objectiveMet = false;

    /** سازنده‌ی ساده (سازگاری با کدهای قبلی): هدف پیش‌فرض کشتن ۱۰ زامبی در timeLimitSeconds ثانیه است. */
    public TimedWarRules(int timeLimitSeconds) {
        this(timeLimitSeconds, Objective.KILL_ZOMBIES, 10);
    }

    /**
     * @param timeLimitSeconds زمان مرحله بر حسب ثانیه
     * @param objective        نوع هدف (کشتن زامبی یا تولید خورشید)
     * @param targetAmount     مقدار هدف (تعداد زامبی یا مقدار خورشید)
     */
    public TimedWarRules(int timeLimitSeconds, Objective objective, int targetAmount) {
        // فرض می‌کنیم هر ۱۰ تیک معادل ۱ ثانیه است
        this.timeLimitTicks = timeLimitSeconds * 10;
        this.remainingTicks = this.timeLimitTicks;
        this.objective = objective;
        this.targetAmount = targetAmount;
    }

    @Override
    public void setupLevel(GameSession session) {
        baselineZombiesKilled = session.getTotalZombiesKilled();
        baselineSunCollected = session.getSunManager().getTotalSunCollected();
        String goalText = (objective == Objective.KILL_ZOMBIES)
                ? ("کشتن " + targetAmount + " زامبی")
                : ("تولید " + targetAmount + " واحد خورشید");
        System.out.println("مرحله نبرد زمان‌دار شروع شد! هدف: " + goalText + " در " + (remainingTicks / 10) + " ثانیه.");
    }

    private int currentProgress(GameSession session) {
        if (objective == Objective.KILL_ZOMBIES) {
            return session.getTotalZombiesKilled() - baselineZombiesKilled;
        }
        return session.getSunManager().getTotalSunCollected() - baselineSunCollected;
    }

    /** پیشرفت فعلی نسبت به هدف (برای HUD)؛ عددی بین ۰ و targetAmount. */
    public int getProgress(GameSession session) {
        return Math.min(targetAmount, currentProgress(session));
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public Objective getObjective() {
        return objective;
    }

    /** ثانیه‌های باقی‌مانده تا پایان مهلت؛ برای نمایش شمارش معکوس در HUD. */
    public int getRemainingSeconds() {
        return Math.max(0, remainingTicks / 10);
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        if (objectiveMet) {
            return;
        }
        if (currentProgress(session) >= targetAmount) {
            objectiveMet = true;
            System.out.println("هدف مرحله‌ی نبرد زمان‌دار با موفقیت انجام شد!");
            return;
        }
        if (remainingTicks > 0) {
            remainingTicks--;

            // چاپ هشدار در ۱۰ ثانیه پایانی
            if (remainingTicks > 0 && remainingTicks % 10 == 0 && remainingTicks <= 100) {
                System.out.println("هشدار: فقط " + (remainingTicks / 10) + " ثانیه تا پایان زمان باقی مانده!");
            }
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        // اگر زمان تمام شود و هنوز به هدف نرسیده باشیم، کاربر می‌بازد
        if (!objectiveMet && remainingTicks <= 0) {
            System.out.println("زمان شما به پایان رسید! زامبی‌ها پیروز شدند.");
            return false; // فعال شدن شرط باخت
        }
        return true;
    }

    @Override
    public String getHudStatusText(GameSession session) {
        String goalText = (objective == Objective.KILL_ZOMBIES) ? "زامبی کشته‌شده" : "خورشید تولیدشده";
        return "نبرد زمان‌دار: " + getRemainingSeconds() + " ثانیه | " + goalText + ": "
                + getProgress(session) + "/" + targetAmount;
    }
}