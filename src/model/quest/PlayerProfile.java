package model.quest;

/**
 * رابط مینیمال با پروفایل بازیکن که سیستم کوئست برای اعمال پاداش‌ها به آن نیاز دارد.
 * پیاده‌سازی واقعی این کلاس باید در کلاس اصلی مدل کاربر (User/Profile) شما باشد.
 */
public interface PlayerProfile {
    void addCoins(int amount);
    void addGems(int amount);
    void unlock(String unlockableId);
    void addItemToInventory(String itemId, int count);
}