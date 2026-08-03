package model.levelrules;

import model.game.GameSession; // ایمپورت ضروری

public interface ILevelRules {
    /**
     * آماده‌سازی اولیه نقشه، کاشت گیاهان پیش‌فرض یا تنظیمات خاص مرحله
     */
    void setupLevel(GameSession session);

    /**
     * اجرای قوانین خاص در هر تیک (مثلا کاهش زمان یا دادن گیاه)
     */
    void applySpecialTickRules(GameSession session);

    /**
     * بررسی شروط باخت. اگر false برگرداند، کاربر بازی را می‌بازد.
     */
    boolean checkCustomLossConditions(GameSession session);
}