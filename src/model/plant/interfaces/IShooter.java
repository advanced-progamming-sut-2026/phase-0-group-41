package model.plant.interfaces;

import model.game.GameSession;

public interface IShooter {
    // این متد قراره فقط یک پرتابه (Projectile) رو بسازه و تو زمین رها کنه
    void shoot(GameSession session);
}