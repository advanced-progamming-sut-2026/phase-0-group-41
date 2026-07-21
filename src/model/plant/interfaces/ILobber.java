package model.plant.interfaces;

import model.game.GameSession;

public interface ILobber {
    // پرتاب تیر به صورت منحنی با عبور از موانع
    void lob(GameSession session);
}