package model.user;

import util.HashUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 این کلاس وظیفه مدیریت کاربران، احراز هویت (لاگین/ثبت‌نام) و ذخیره اطلاعات را بر عهده دارد تا با بسته شدن برنامه، اطلاعات بازیکنان پاک نشود
 */
public class UserManager {

    private static final String SAVE_FILE = "pvz_users.dat";

    private final Map<String, User> usersByUsername = new HashMap<>();

    // نکته فاز ۳: این کلاس هم روی سرور استفاده می‌شود (منبع اصلی و واقعی داده‌ها)
    // و هم به صورت میراثی روی کلاینت (برای منوی کنسولی تک‌نفره‌ی فازهای قبل) وجود دارد.
    // چون چند Thread از ClientHandler به صورت هم‌زمان به یک نمونه مشترک از این کلاس
    // روی سرور دسترسی دارند، تمام متدهایی که مپ داخلی را می‌خوانند یا تغییر می‌دهند
    // باید synchronized باشند تا از خرابی داده در اثر Race Condition جلوگیری شود.

    public UserManager() {
        load();
    }

    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(usersByUsername.values());
    }

    public synchronized boolean usernameExists(String username) {
        return usersByUsername.containsKey(username);
        //containsKey: این متدِ مپ، بررسی می‌کند که آیا نام کاربری ورودی، قبلاً به عنوان کلید در هش‌مپ ثبت شده است یا خیر. خروجی آن یک مقدار منطقی (true یا false) است.
    }

    public synchronized User register(String username, String rawPassword, String nickname, String email, String gender) {
        // بررسی یکتا بودن نام کاربری و ساخت کاربر باید اتمیک باشند تا دو کلاینت
        // هم‌زمان نتوانند با یک نام کاربری یکسان ثبت‌نام کنند (طبق سند فاز ۳).
        if (usersByUsername.containsKey(username)) {
            return null;
        }
        //توی خط پایین یک شی جدید از کلاس یوزر میسازه ولی نکته اینجاست که رمز عبور خام را مستقیم ذخیره نمیکنه و ابتدا ان را به متد هش می فرستد تا به صورت یک رشته رمزنگاری‌شده ذخیره شود
        User user = new User(username, HashUtil.sha256(rawPassword), nickname, email, gender);
        usersByUsername.put(username, user);
        save();//این کاربرِ جدید فوراً روی هارد دیسک ذخیره شود و با قطع برق یا بستن برنامه از بین نرود
        return user;
    }

    public synchronized User findByUsername(String username) {
        return usersByUsername.get(username);
    }

    public synchronized User findByEmail(String email) {
        for (User user : usersByUsername.values()) {//چون مپ ما بر اساس نام کاربری چیده شده و نه ایمیل
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public boolean checkPassword(User user, String rawPassword) {
        return user.getPasswordHash().equals(HashUtil.sha256(rawPassword));
    }

    public synchronized void changePassword(User user, String newRawPassword) {
        user.setPasswordHash(HashUtil.sha256(newRawPassword));
        save();
    }

    /**
     * جایگزینِ کاملِ اطلاعات یک کاربر روی سرور با نسخه‌ی به‌روزشده‌ای که از کلاینت رسیده است.
     * این متد پلی است بین حالت آفلاینِ کاربر (که تغییراتش را روی یک شیء User محلی اعمال می‌کند)
     * و ذخیره‌سازی نهایی روی سرور. بدون این متد، فرمان SAVE_USER معنایی نداشت.
     */
    public synchronized void updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getUsername() == null) return;
        usersByUsername.put(updatedUser.getUsername(), updatedUser);
        save();
    }

    public synchronized void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(usersByUsername);
        } catch (IOException e) {
            System.err.println("خطا در ذخیره‌سازی اطلاعات کاربران: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, User> loaded = (Map<String, User>) in.readObject();
            usersByUsername.putAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("خطا در بازیابی اطلاعات کاربران: " + e.getMessage());
        }
    }
    public synchronized void updateUsernameKey(String oldUsername, String newUsername) {
        if (usersByUsername.containsKey(oldUsername)) {
            User user = usersByUsername.remove(oldUsername); // حذف کاربر با کلید قدیمی
            usersByUsername.put(newUsername, user);          // اضافه کردن همان کاربر با کلید جدید
            save();                                          // ذخیره تغییرات در فایل
        }
    }
}
