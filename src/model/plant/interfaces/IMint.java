package model.plant.interfaces;

import model.game.GameSession;

public interface IMint {
    // اعمال اثر تقویتی روی تمام گیاهان هم‌خانواده در کل باغچه
    void applyMintEffect(GameSession session);
}