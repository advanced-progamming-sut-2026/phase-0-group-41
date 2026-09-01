package model.levelrules;

import model.game.GameSession;

/**
 * هرچه رسد بکار (Plant What You Get): طبق سند، بازی با مقدار مشخصی آفتاب
 * اولیه شروع می‌شود و دیگر هیچ آفتابی از آسمان نمی‌بارد و آفتابگردان قابل
 * انتخاب نیست. بازیکن قبل از شروع موج‌ها، بدون محدودیت cooldown می‌تواند
 * هرچقدر بخواهد گیاه بکارد/بردارد؛ سپس با دستور خودش موج‌ها را فعال می‌کند.
 */
public class PlantWhatYouGetRules implements ILevelRules {

    private final int startingSun;
    private boolean wavesStarted = false;

    public PlantWhatYouGetRules() {
        this(500); // مقدار پیش‌فرض طبق مثال سند
    }

    /** @param startingSun مقدار آفتاب اولیه‌ی ثابت (مثلاً ۵۰۰ یا ۸۰۰ طبق سند)؛ در مراحل سخت‌تر می‌توان کمتر داد. */
    public PlantWhatYouGetRules(int startingSun) {
        this.startingSun = startingSun;
    }

    @Override
    public void setupLevel(GameSession session) {
        session.getSunManager().setCurrentSun(startingSun);
        session.setCooldownsDisabled(true);
        System.out.println("چالش هرچه رسد بکار! " + startingSun + " واحد خورشید دارید. "
                + "دیگر خورشیدی از آسمان نمی‌بارد. هر زمان آماده بودید، موج‌ها را با دستور خودتان آغاز کنید.");
    }

    /** فراخوانی این متد (از طریق دستور کاربر) موج‌های زامبی را فعال می‌کند. */
    public void startWaves(GameSession session) {
        if (wavesStarted) {
            return;
        }
        wavesStarted = true;
        session.setCooldownsDisabled(false);
        System.out.println("موج‌های زامبی فعال شدند!");
    }

    public boolean isWavesStarted() {
        return wavesStarted;
    }

    @Override
    public boolean areWavesStarted() {
        return wavesStarted;
    }

    @Override
    public boolean allowsSkySun() {
        return false; // هیچ خورشیدی از آسمان نمی‌بارد؛ فقط همان مقدار اولیه در دسترس است
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        // اتفاق خودکار خاصی نیاز نیست؛ همه‌چیز با دستور کاربر (startWaves) کنترل می‌شود.
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت کلاسیک برقرار است
    }

    @Override
    public String getHudStatusText(GameSession session) {
        return wavesStarted
                ? "هرچه رسد بکار: موج‌ها فعال شدند"
                : "هرچه رسد بکار: قبل از شروع موج‌ها، آماده‌سازی کنید";
    }
}