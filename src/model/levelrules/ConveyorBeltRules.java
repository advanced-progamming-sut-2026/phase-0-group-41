package model.levelrules;

import model.game.GameSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * نوار کناری (Conveyor Belt): طبق سند، به‌جای انتخاب گیاه در ابتدای مرحله،
 * گیاهان به‌صورت تصادفی روی یک نوار ظاهر می‌شوند. بازیکن فقط گیاهانی که در
 * حال حاضر روی نوار هستند را می‌تواند بردارد و بکارد؛ بعد از کاشت، آن گیاه
 * از روی نوار برداشته می‌شود و دیگر نمایش داده نمی‌شود.
 */
public class ConveyorBeltRules implements ILevelRules {

    private static final int MAX_BELT_SIZE = 5;

    private int tickCounter = 0;
    private final int beltSpeedTicks; // فاصله‌ی تولید گیاه جدید روی نوار
    private final String[] plantPool = {"peashooter", "cabbagepult", "wallnut", "potatomine", "sunflower"};
    private final Random random = new Random();

    // صف گیاهانِ روی نوار؛ اولین عضو یعنی قدیمی‌ترین گیاه (سمت چپ نوار)
    private final List<String> belt = new LinkedList<>();

    public ConveyorBeltRules() {
        this(130); // پیش‌فرض: هر ۱۳ ثانیه (سازگاری با کدهای قبلی)
    }

    /** @param beltSpeedTicks فاصله‌ی زمانی تولید گیاه جدید بر حسب تیک؛ در مراحل سخت‌تر می‌توان کمتر داد. */
    public ConveyorBeltRules(int beltSpeedTicks) {
        this.beltSpeedTicks = beltSpeedTicks;
    }

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("مرحله تسمه نقاله آغاز شد! جمع‌آوری خورشید در این مرحله غیرفعال است.");
        // طبق سند، اولین گیاه همان لحظه‌ی ورود بازیکن به مرحله تولید می‌شود
        addRandomPlantToBelt();
    }

    /** لیست گیاهان فعلی روی نوار (قدیمی‌ترین اول)؛ لایه‌ی گرافیکی/کنسول همین
     *  لیست را به‌جای کارت‌های ثابت انتخاب گیاه نمایش می‌دهد. */
    public List<String> getBeltPlants() {
        return belt;
    }

    /**
     * برداشتن یک گیاه از روی نوار برای کاشت (اولین باری که آن نام روی نوار
     * پیدا شود). اگر آن گیاه روی نوار نباشد، false برمی‌گرداند و کاشت نباید
     * انجام شود.
     */
    public boolean takeFromBelt(String plantName) {
        return belt.remove(plantName);
    }

    private void addRandomPlantToBelt() {
        if (belt.size() >= MAX_BELT_SIZE) {
            return; // نوار پر است
        }
        String randomPlant = plantPool[random.nextInt(plantPool.length)];
        belt.add(randomPlant);
        System.out.println("یک [" + randomPlant + "] روی تسمه نقاله قرار گرفت!");
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        tickCounter++;
        if (tickCounter >= beltSpeedTicks) {
            addRandomPlantToBelt();
            tickCounter = 0; // ریست کردن تایمر
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت خاصی ندارد (رسیدن زامبی به انتها کافیست)
    }

    @Override
    public String getHudStatusText(GameSession session) {
        return "نوار نقاله: " + belt.size() + " گیاه در دسترس";
    }
}