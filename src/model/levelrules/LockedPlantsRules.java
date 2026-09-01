package model.levelrules;

import model.game.GameSession;
import java.util.Collections;
import java.util.List;

/**
 * گیاهان زندانی (Locked Plants): طبق سند، تعدادی از اسلات‌های انتخاب گیاه یا
 * برخی گیاهان خاص در این مرحله در دسترس نیستند و بازیکن مجبور است فقط با
 * گیاهان مشخص‌شده بازی را شروع کند.
 */
public class LockedPlantsRules implements ILevelRules {

    private final List<String> lockedPlantsList;

    /**
     * @param lockedPlantsList لیستی از نام گیاهانی که بازیکن مجبور است فقط از آن‌ها استفاده کند
     */
    public LockedPlantsRules(List<String> lockedPlantsList) {
        this.lockedPlantsList = lockedPlantsList;
    }

    /** لیست گیاهان مجاز؛ لایه‌ی گرافیکی/کنسول با همین لیست نوار گیاه انتخابی
     *  کاربر را فیلتر می‌کند تا فقط همین‌ها قابل کاشت باشند. */
    public List<String> getAllowedPlants() {
        return Collections.unmodifiableList(lockedPlantsList);
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("مرحله گیاهان قفل‌شده! شما فقط مجاز به استفاده از این گیاهان هستید: " + lockedPlantsList);
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خاصی در حین تیک نمی‌افتد
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت همان رسیدن زامبی به انتهاست
    }

    @Override
    public String getHudStatusText(GameSession session) {
        return "گیاهان مجاز: " + String.join("، ", lockedPlantsList);
    }
}