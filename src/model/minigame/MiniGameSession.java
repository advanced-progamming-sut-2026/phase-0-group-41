package model.minigame;

import model.game.GameSession;
import model.user.User;

/**
 * کلاس پایه برای تمام مینی‌گیم‌ها. این کلاس ویژگی‌های پیش‌فرض مثل
 * عدم بارش خورشید (در اکثر مینی‌گیم‌ها) را مدیریت می‌کند.
 */
public abstract class MiniGameSession extends GameSession {

    // سطح سختی مینی‌گیم: ۱ تا ۳ (هر مرحله از قبلی سخت‌تر است، طبق سند فاز یک)
    private final int level;

    public MiniGameSession(User user, int totalWaves) {
        this(user, totalWaves, 1);
    }

    public MiniGameSession(User user, int totalWaves, int level) {
        super(user, totalWaves);
        this.level = Math.max(1, Math.min(3, level));
    }

    public int getLevel() {
        return level;
    }

    @Override
    protected boolean isWaveSystemEnabled() {
        // مینی‌گیم‌ها شرط برد/باخت و منبع اسپاون زامبی خودشان را دارند؛ سامانه‌ی
        // موج/چمن‌زن مرحله‌ی عادی (که با totalWaves ساختگی راه‌اندازی شده) نباید
        // زامبی تصادفی اضافه کند یا برد/باخت جعلی تولید کند.
        return false;
    }

    @Override
    public void advanceOneTick() {
        if (isGameOver()) {
            return;
        }
        super.advanceOneTick(); // فقط تیک گیاه/زامبی/پرتابه/خورشید (بدون موج و بدون چک باخت عادی)
        if (!isGameOver()) {
            customMiniGameTick();
        }
    }

    // متدی که هر مینی‌گیم می‌تواند منطق تیک اختصاصی خود را در آن بنویسد
    protected abstract void customMiniGameTick();
}