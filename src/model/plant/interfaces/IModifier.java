package model.plant.interfaces;

import model.game.GameSession;

public interface IModifier {
    // اعمال تغییرات روی محیط یا وضعیت زامبی‌ها
    void applyModification(GameSession session);
}