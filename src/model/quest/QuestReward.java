package model.quest;

import java.util.Objects;

/**این کلاس مربوط به پاداش های یک کوئست است که یا جم است یا کوین
 * براساس نوع هر کوئست هم جک و کوین را جدا محاسبه و کار مربوطه را انجام می دهد
 * در اینجا کوین و جم پروفایل نیز تغییر می کند
 */
public class QuestReward {

    public enum CurrencyType { COIN, GEM }

    private final RewardType type;

    // برای CURRENCY
    private final CurrencyType currencyType;
    private final int amount;

    // برای UNLOCKABLE
    private final String unlockableId; // نام گیاه یا مرحله

    // برای INVENTORY
    private final String itemId; // مثلا شناسه بسته بذر
    private final int itemCount;

    private QuestReward(RewardType type, CurrencyType currencyType, int amount,
                        String unlockableId, String itemId, int itemCount) {
        this.type = type;
        this.currencyType = currencyType;
        this.amount = amount;
        this.unlockableId = unlockableId;
        this.itemId = itemId;
        this.itemCount = itemCount;
    }

    public static QuestReward currency(CurrencyType currencyType, int amount) {
        return new QuestReward(RewardType.CURRENCY, currencyType, amount, null, null, 0);
    }

    public static QuestReward unlockable(String unlockableId) {
        return new QuestReward(RewardType.UNLOCKABLE, null, 0, unlockableId, null, 0);
    }

    public static QuestReward inventory(String itemId, int itemCount) {
        return new QuestReward(RewardType.INVENTORY, null, 0, null, itemId, itemCount);
    }

    public RewardType getType() {
        return type;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public int getAmount() {
        return amount;
    }

    public String getUnlockableId() {
        return unlockableId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    /**
     * اعمال پاداش روی پروفایل بازیکن.
     * PlayerProfile باید متدهای addCoins/addGems/unlock/addItemToInventory را داشته باشد.
     */
    public void apply(PlayerProfile profile) {
        Objects.requireNonNull(profile, "profile نمی‌تواند null باشد");
        switch (type) {
            case CURRENCY:
                if (currencyType == CurrencyType.COIN) {
                    profile.addCoins(amount);
                } else {
                    profile.addGems(amount);
                }
                break;
            case UNLOCKABLE:
                profile.unlock(unlockableId);
                break;
            case INVENTORY:
                profile.addItemToInventory(itemId, itemCount);
                break;
            default:
                throw new IllegalStateException("نوع پاداش نامعتبر است: " + type);
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case CURRENCY:
                return amount + " " + currencyType;
            case UNLOCKABLE:
                return "Unlock(" + unlockableId + ")";
            case INVENTORY:
                return itemCount + "x " + itemId;
            default:
                return "UnknownReward";
        }
    }
}