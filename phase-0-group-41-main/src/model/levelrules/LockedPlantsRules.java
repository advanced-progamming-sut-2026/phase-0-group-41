package model.levelrules;

import model.game.GameSession;
import java.util.List;

public class LockedPlantsRules implements ILevelRules {

    private final List<String> lockedPlantsList;

    /**
     * @param lockedPlantsList لیستی از نام گیاهانی که بازیکن مجبور است فقط از آن‌ها استفاده کند
     */
    public LockedPlantsRules(List<String> lockedPlantsList) {
        this.lockedPlantsList = lockedPlantsList;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("مرحله گیاهان قفل‌شده! شما فقط مجاز به استفاده از این گیاهان هستید: " + lockedPlantsList);
        
        // TODO: اگر در GameSession لیستی برای "کارت‌های انتخاب شده در دست بازیکن" دارید،
        // باید در اینجا آن لیست را پاک کرده و با lockedPlantsList جایگزین کنید.
        // مثال فرضی: session.setActiveDeck(lockedPlantsList);
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خاصی در حین تیک نمی‌افتد
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت همان رسیدن زامبی به انتهاست
    }
}