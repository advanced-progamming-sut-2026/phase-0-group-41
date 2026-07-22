package model.quest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * مدیریت کل کوئست‌های بازی: نگهداری لیست، مرتب‌سازی بر اساس اولویت،
 * بررسی تکمیل‌شدن و دسته‌بندی بر اساس صفحه travel-log.
 *
 * اولویت نمایش طبق داک:
 * CRITICAL بالاترین اولویت (همیشه صدر لیست)، سپس HIGH (چالش‌های Epic)،
 * و در نهایت DAILY/NORMAL با اولویت پایین‌تر.
 */
public class QuestManager {

    private  List<Quest> quests = new ArrayList<>();

    public QuestManager(List<Quest> quests) {
        this.quests = quests;
    }

    // مرتب‌سازی اولویت‌ها طبق ترتیب گفته‌شده در داک
    private static final Map<QuestPriority, Integer> PRIORITY_ORDER = new EnumMap<>(QuestPriority.class);
    static {
        PRIORITY_ORDER.put(QuestPriority.CRITICAL, 0);
        PRIORITY_ORDER.put(QuestPriority.HIGH, 1);
        PRIORITY_ORDER.put(QuestPriority.NORMAL, 2);
        PRIORITY_ORDER.put(QuestPriority.DAILY, 3);
    }

    public void addQuest(Quest quest) {
        quests.add(Objects.requireNonNull(quest));
    }

    public Quest findById(String id) {
        return quests.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * لیست کوئست‌های یک صفحه خاص از travel-log، مرتب‌شده بر اساس اولویت.
     * travel log page <page_name>
     */
    public List<Quest> getQuestsForPage(QuestPage page) {
        return quests.stream()
                .filter(q -> q.getPage() == page)
                .sorted(Comparator.comparing(q -> PRIORITY_ORDER.get(q.getPriority())))
                .collect(Collectors.toList());
    }

    /**
     * بررسی همه‌ی کوئست‌های در حال انجام و به‌روزرسانی وضعیت تکمیل آن‌ها
     * بر اساس وضعیت فعلی بازیکن.
     */
    public List<Quest> refreshCompletionStatus(QuestContext context) {
        List<Quest> newlyCompleted = new ArrayList<>();
        for (Quest quest : quests) {
            boolean wasCompleted = quest.isCompleted();
            if (!wasCompleted && quest.checkCompletion(context)) {
                newlyCompleted.add(quest);
            }
        }
        return newlyCompleted;
    }

    /**
     * دریافت پاداش یک کوئست تکمیل‌شده و در صورت تکرارپذیر بودن، ریست کردن آن
     * برای دور بعدی (مثلا روز بعد).
     */
    public void claimReward(String questId, PlayerProfile profile) {
        Quest quest = findById(questId);
        if (quest == null) {
            throw new IllegalArgumentException("کوئستی با این شناسه یافت نشد: " + questId);
        }
        quest.claimReward(profile);
        if (quest.isRepeatable()) {
            quest.resetForNextCycle();
        }
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests);
    }

    /**
     * ریست کردن تمام کوئست‌های روزانه در صورت ورود کاربر در روز جدید (ساعت 00:00 به بعد)
     */
    public void resetDailyQuests() {
        quests.stream()
                .filter(Quest::isRepeatable)
                .forEach(Quest::resetForNextCycle);
    }
    /**
     * ثبت کشته شدن زامبی و بررسی مجدد وضعیت کوئست‌ها
     */
    public void recordZombieKill(QuestContext context) {
        if (context != null) {
            context.setZombiesKilled(context.getZombiesKilled() + 1);
            refreshCompletionStatus(context);
        }
    }
}