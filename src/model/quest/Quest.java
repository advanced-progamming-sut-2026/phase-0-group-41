package model.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.io.Serializable;
import java.util.function.Function;

/**
 * یک کوئست کامل: شناسه، عنوان، توضیح، اولویت/دسته‌بندی، صفحه نمایش در travel-log،
 * شرط تکمیل، لیست پاداش‌ها و وضعیت فعلی (تکرارپذیر بودن برای کوئست‌های روزانه).
 */
public class Quest implements Serializable{
    private static final long serialVersionUID=1L;
    private final String id;
    private final String title;
    private final String description;
    private final QuestPriority priority;
    private final QuestPage page;
    private final boolean repeatable;
    private final QuestCondition condition;
    private final List<QuestReward> rewards;

    private boolean completed = false;
    private boolean rewardClaimed = false;

    public Quest(String id, String name, String description, QuestPriority priority,
                 QuestPage page, boolean repeatable,
                 QuestCondition condition, List<QuestReward> rewards){
        this.id = id;
        this.title = name;
        this.description = description;
        this.priority = priority;
        this.page = page;
        this.repeatable = repeatable;
        this.condition = condition;
        this.rewards = rewards;
    }

    /**
     * نام نمایشی کوئست. همیشه همان title سازنده است؛
     * قبلاً یک فیلد جدای همیشه-null بود که باعث می‌شد نام کوئست‌ها در UI خالی/نامفهوم دیده شود.
     */
    public String getName() {
        return this.title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestPriority getPriority() {
        return priority;
    }

    public QuestPage getPage() {
        return page;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public List<QuestReward> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    /**
     * Alias for isRewardClaimed(), kept for the UI/console layers that call
     * isClaimed(). There is only one claimed-state field (rewardClaimed);
     * this avoids having two independent flags that can drift out of sync.
     */
    public boolean isClaimed() {
        return rewardClaimed;
    }

    /**
     * توضیح خوانا از پاداش‌های این کوئست، برای نمایش در UI (مثلا "200 COIN, 1x seed_packet_random").
     */
    public String getRewardsSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rewards.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(rewards.get(i).toString());
        }
        return sb.toString();
    }

    /**
     * بررسی می‌کند آیا شرط این کوئست با وضعیت فعلی برآورده شده است یا نه.
     * در صورت برآورده شدن، وضعیت completed را true می‌کند.
     */
    public boolean checkCompletion(QuestContext context) {
        if (!completed && condition.isSatisfied(context)) {
            completed = true;
        }
        return completed;
    }

    /**
     * اعمال پاداش‌های این کوئست روی پروفایل بازیکن.
     * اگر کوئست تکمیل نشده باشد یا قبلا پاداشش گرفته شده باشد، خطا می‌دهد.
     */
    public void claimReward(PlayerProfile profile) {
        if (!completed) {
            throw new IllegalStateException("Quest is not completed yet: " + id);
        }
        if (rewardClaimed && !repeatable) {
            throw new IllegalStateException("Reward for this quest was already claimed: " + id);
        }
        for (QuestReward reward : rewards) {
            reward.apply(profile);
        }
        rewardClaimed = true;
    }

    /**
     * برای کوئست‌های تکرارپذیر (مثل روزانه)، بعد از دریافت پاداش،
     * وضعیت کوئست برای دور بعد ریست می‌شود.
     */
    public void resetForNextCycle() {
        if (!repeatable) {
            throw new IllegalStateException("This quest is not repeatable: " + id);
        }
        completed = false;
        rewardClaimed = false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s%s",
                id, title, priority,
                completed ? "Completed" : "In Progress",
                rewardClaimed ? " [Reward Claimed]" : "");
    }
}
