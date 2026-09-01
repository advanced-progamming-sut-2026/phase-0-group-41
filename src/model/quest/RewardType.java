package model.quest;

/**
 * انواع پاداش طبق داک:
 * CURRENCY    -> افزایش Coins یا Gems در پروفایل کاربر
 * UNLOCKABLE  -> تغییر وضعیت یک گیاه یا مرحله از Locked به Available
 * INVENTORY   -> اضافه کردن آیتم مصرفی (مثل بسته بذر) به انبار کاربر
 */
public enum RewardType {
    CURRENCY,
    UNLOCKABLE,
    INVENTORY
}