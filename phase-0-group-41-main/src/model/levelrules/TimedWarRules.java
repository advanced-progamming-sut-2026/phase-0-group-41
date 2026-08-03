package model.levelrules;

import model.game.GameSession;

public class TimedWarRules implements ILevelRules {

    private int remainingTicks;

    /**
     * @param timeLimitSeconds زمان مرحله بر حسب ثانیه
     */
    public TimedWarRules(int timeLimitSeconds) {
        // فرض می‌کنیم هر ۱۰ تیک معادل ۱ ثانیه است
        this.remainingTicks = timeLimitSeconds * 10;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("مرحله نبرد زمان‌دار شروع شد! شما فقط " + (remainingTicks / 10) + " ثانیه برای پاکسازی زامبی‌ها فرصت دارید.");
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
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
        // اگر زمان تمام شود و بازی هنوز در حالت پیروزی نباشد، کاربر می‌بازد
        if (remainingTicks <= 0 && !session.isWon()) {
            System.out.println("زمان شما به پایان رسید! زامبی‌ها پیروز شدند.");
            return false; // فعال شدن شرط باخت
        }
        return true;
    }
}