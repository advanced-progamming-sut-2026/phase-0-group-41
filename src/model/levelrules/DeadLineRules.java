package model.levelrules;

import model.game.GameSession;
import model.zombie.Zombie;

public class DeadLineRules implements ILevelRules {

    private final int deadLineColumn; // ستونی که خط مرگ محسوب می‌شود

    public DeadLineRules(int deadLineColumn) {
        this.deadLineColumn = deadLineColumn;
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خاصی در حین تیک نمی‌افتد، فقط شرط باخت چک می‌شود
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        // بررسی تمام زامبی‌های زنده در صفحه
        for (Zombie z : session.getAliveZombies()) {
            // اگر زامبی از ستون تعیین‌شده عبور کرده باشد (کوجک‌تر یا مساوی خط مرگ)
            if (z.getXPosition() <= deadLineColumn) {
                System.out.println("یک زامبی از خط مرگ (ستون " + deadLineColumn + ") عبور کرد!");
                return false; // بازیکن باخت!
            }
        }
        return true; // هنوز کسی رد نشده
    }

    @Override
    public void setupLevel(GameSession session) {
        // در این مُد، هیچ گیاه یا تنظیمات خاصی اضافه نمی‌شود
    }
}