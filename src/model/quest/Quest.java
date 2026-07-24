package model.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * یک کوئست کامل: شناسه، عنوان، توضیح، اولویت/دسته‌بندی، صفحه نمایش در travel-log،
 * شرط تکمیل، لیست پاداش‌ها و وضعیت فعلی (تکرارپذیر بودن برای کوئست‌های روزانه).
 */
public class Quest {

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

    public Quest(String id,
                 String title,
                 String description,
                 QuestPriority priority,
                 QuestPage page,
                 boolean repeatable,
                 QuestCondition condition,
                 List<QuestReward> rewards) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.priority = Objects.requireNonNull(priority);
        this.page = Objects.requireNonNull(page);
        this.repeatable = repeatable;
        this.condition = Objects.requireNonNull(condition);
        this.rewards = new ArrayList<>(rewards);
    }
    private boolean isClaimed = false;
    private RewardType rewardType;
    private int rewardAmount;
    private String rewardItemId;
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public void setClaimed(boolean claimed) {
        this.isClaimed = claimed;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    // برای تنظیم نوع پاداش (در کانستراکتور یا متدهای دیگر Quest استفاده کنید)
    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(int rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public String getRewardItemId() {
        return rewardItemId;
    }

    public void setRewardItemId(String rewardItemId) {
        this.rewardItemId = rewardItemId;
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
            throw new IllegalStateException("کوئست هنوز تکمیل نشده است: " + id);
        }
        if (rewardClaimed && !repeatable) {
            throw new IllegalStateException("پاداش این کوئست قبلا دریافت شده است: " + id);
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
            throw new IllegalStateException("این کوئست تکرارپذیر نیست: " + id);
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