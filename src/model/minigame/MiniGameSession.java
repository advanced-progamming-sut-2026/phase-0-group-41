package model.minigame;

import model.game.GameSession;
import model.user.User;

/**
 * کلاس پایه برای تمام مینی‌گیم‌ها. این کلاس ویژگی‌های پیش‌فرض مثل
 * عدم بارش خورشید (در اکثر مینی‌گیم‌ها) را مدیریت می‌کند.
 */
public abstract class MiniGameSession extends GameSession {

    public MiniGameSession(User user, int totalWaves) {
        super(user, totalWaves);
    }

    @Override
    public void advanceOneTick() {
        super.advanceOneTick();
        customMiniGameTick();
    }

    // متدی که هر مینی‌گیم می‌تواند منطق تیک اختصاصی خود را در آن بنویسد
    protected abstract void customMiniGameTick();
}