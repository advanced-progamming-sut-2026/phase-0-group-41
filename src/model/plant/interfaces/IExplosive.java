package model.plant.interfaces;

import model.game.GameSession;

public interface IExplosive {
    // اجرای منطق انفجار و اعمال دمیج مساحتی
    void explode(GameSession session);
}