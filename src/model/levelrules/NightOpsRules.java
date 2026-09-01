package model.levelrules;

import model.game.GameSession;

/**
 * شب عملیات (Night Ops): طبق سند، هیچ آفتابی از آسمان نمی‌بارد و بازیکن فقط
 * با آفتاب تولیدشده توسط گیاهان (مثل گل آفتابگردان) باید زنده بماند. برای
 * جبران نبود خورشید اولیه، مقداری خورشید کمکی در ابتدای مرحله داده می‌شود.
 */
public class NightOpsRules implements ILevelRules {

    private final int startingSunBonus;

    public NightOpsRules() {
        this(100);
    }

    /** @param startingSunBonus مقدار خورشید کمکی اولیه؛ در مراحل سخت‌تر می‌توان مقدار کمتری داد. */
    public NightOpsRules(int startingSunBonus) {
        this.startingSunBonus = startingSunBonus;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("عملیات شبانه آغاز شد! سقوط خورشید از آسمان غیرفعال است.");
        session.getSunManager().addSun(startingSunBonus);
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خاصی در حین تیک نمی‌افتد؛ فقط سقوط خورشید (در GameSession) غیرفعال است.
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت همان رسیدن زامبی به انتهای نقشه است
    }

    @Override
    public boolean allowsSkySun() {
        return false; // هسته‌ی اصلی این مُد: هیچ خورشیدی از آسمان نمی‌بارد
    }

    @Override
    public String getHudStatusText(GameSession session) {
        return "شب عملیات: خورشید فقط از گیاهان";
    }
}