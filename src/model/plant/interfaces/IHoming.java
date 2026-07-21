package model.plant.interfaces;

import model.game.GameSession;
import model.zombie.Zombie;

public interface IHoming {
    // جستجو و قفل کردن روی بهترین هدف در کل نقشه بازی
    Zombie findTarget(GameSession session);

    // شلیک پرتابه ردیاب به سمت هدفی که پیدا شده است
    void shootHoming(GameSession session, Zombie target);
}