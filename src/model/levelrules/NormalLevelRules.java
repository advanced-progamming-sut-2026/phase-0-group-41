package model.levelrules;

import model.game.GameSession;

public class NormalLevelRules implements ILevelRules {

    @Override
    public void applySpecialTickRules(GameSession session) {
        // در مرحله عادی اتفاق خاصی در تیک‌ها نمی‌افتد
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت خاصی ندارد (همان رسیدن به انتهای نقشه کافیست)
    }

    @Override
    public void setupLevel(GameSession session) {
        // در مرحله عادی، هیچ گیاه یا تنظیمات خاصی اضافه نمی‌شود
    }
}