package model.plant.interfaces;

import model.game.GameSession;

public interface IMeleeAttacker {
    // حمله از نزدیک به زامبی در بلاک مجاور
    void attackMelee(GameSession session);
}