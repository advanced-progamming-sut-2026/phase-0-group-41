package network;

import model.user.User;

/**
 * پل ارتباطی بین کدهای موجود در کنترلرها (که تا فاز ۲ فقط با یک UserManager
 * محلی کار می‌کردند و روی شیء User به‌صورت مستقیم تغییر اعمال می‌کردند) و
 * سرور فاز ۳.
 *
 * ریشه‌ی اصلی باگ‌هایی که بعد از تغییر لاگین/رجیستر به سمت شبکه ایجاد شده بود
 * این بود: کنترلرها (MainController، ProfileController، PlayController،
 * ShopController، QuestController، GreenhouseController و ...) همگی هنوز
 * userManager.save() را روی یک UserManager کاملاً محلی و جدا از سرور صدا
 * می‌زدند. آن UserManager محلی هیچ ربطی به کاربرِ واقعی (که از سرور با
 * GET_USER گرفته می‌شد) نداشت، پس تمام تغییرات (سکه، الماس، پیشرفت مرحله،
 * کوئست‌ها و ...) در عمل گم می‌شدند و هیچ‌وقت به سرور نمی‌رسیدند؛ درست همان
 * چیزی که سند فاز ۳ می‌گوید نباید اتفاق بیفتد: «داده‌های مرتبط با کاربر در
 * بازی باید در سرور ذخیره شوند».
 *
 * به‌جای ریفکتور کردن ده‌ها کنترلر (که ریسک بالایی برای شکستن کد بقیه‌ی گروه
 * دارد)، این کلاس در نقاط حیاتی و کم‌تعداد (پایان بازی، خروج از حساب، خروج
 * از برنامه) صدا زده می‌شود تا آخرین نسخه‌ی شیء User را به سرور بفرستد.
 */
public final class UserSync {

    private UserSync() {
    }

    /**
     * آخرین وضعیت کاربر را به سرور می‌فرستد تا از هر دستگاه دیگری هم قابل
     * مشاهده باشد. اگر کاربر لاگین نکرده باشد یا اتصال برقرار نباشد، به‌صورت
     * بی‌سروصدا false برمی‌گرداند (بازی نباید به‌خاطر قطعی شبکه کرش کند).
     */
    public static boolean push(User user) {
        if (user == null || user.getUsername() == null) return false;

        NetworkMessage req = new NetworkMessage("SAVE_USER");
        req.data.put("username", user.getUsername());
        req.payload = user;

        NetworkMessage res = NetworkManager.sendRequest(req);
        return res != null && res.success;
    }
}
