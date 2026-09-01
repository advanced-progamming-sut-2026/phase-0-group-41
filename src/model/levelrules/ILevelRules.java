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

    /**
     * آیا سامانه‌ی موج زامبی مرحله‌ی عادی فعال باشد؟ پیش‌فرض true است. فقط
     * «هرچه رسد بکار» (Plant What You Get) این مقدار را false برمی‌گرداند تا
     * بازیکن بتواند قبل از شروع موج‌ها، بدون محدودیت آماده‌سازی کند و با
     * دستور دلخواه خودش موج‌ها را فعال کند.
     */
    default boolean areWavesStarted() {
        return true;
    }

    /**
     * آیا در این مُد، خورشید به‌صورت خودکار از آسمان می‌بارد؟ پیش‌فرض true است؛
     * فقط «شب عملیات» (Night Ops) و «هرچه رسد بکار» (Plant What You Get) این
     * مقدار را false می‌کنند، چون طبق سند در این دو مُد سقوط خورشید غیرفعال است.
     */
    default boolean allowsSkySun() {
        return true;
    }

    /**
     * یک خط توضیحی کوتاه برای نمایش در نوار وضعیت بالای صفحه (HUD) حین بازی؛
     * مثلاً «۱۲ ثانیه باقی مانده» یا «حداکثر ۳ گیاه». مُدهایی که چیزی برای نمایش
     * ندارند (مثل مرحله‌ی عادی) رشته‌ی خالی برمی‌گردانند. session برای مُدهایی
     * که نیاز به خواندن وضعیت زنده‌ی بازی دارند (مثلاً تعداد گیاهان فعلی یا
     * پیشرفت هدف نبرد زمان‌دار) پاس داده می‌شود.
     */
    default String getHudStatusText(GameSession session) {
        return "";
    }
}