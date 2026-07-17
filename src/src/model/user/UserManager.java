package model.user;

import util.HashUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 این کلاس وظیفه مدیریت کاربران، احراز هویت (لاگین/ثبت‌نام) و ذخیره اطلاعات را بر عهده دارد تا با بسته شدن برنامه، اطلاعات بازیکنان پاک نشود
 */
public class UserManager {

    private static final String SAVE_FILE = "pvz_users.dat";

    private final Map<String, User> usersByUsername = new HashMap<>();

    public UserManager() {
        load();
    }

    public boolean usernameExists(String username) {
        return usersByUsername.containsKey(username);
        //containsKey: این متدِ مپ، بررسی می‌کند که آیا نام کاربری ورودی، قبلاً به عنوان کلید در هش‌مپ ثبت شده است یا خیر. خروجی آن یک مقدار منطقی (true یا false) است.
    }

    public User register(String username, String rawPassword, String nickname, String email, String gender) {
        //توی خط پایین یک شی جدید از کلاس یوزر میسازه ولی نکته اینجاست که رمز عبور خام را مستقیم ذخیره نمیکنه و ابتدا ان را به متد هش می فرستد تا به صورت یک رشته رمزنگاری‌شده ذخیره شود
        User user = new User(username, HashUtil.sha256(rawPassword), nickname, email, gender);
        usersByUsername.put(username, user);
        save();//این کاربرِ جدید فوراً روی هارد دیسک ذخیره شود و با قطع برق یا بستن برنامه از بین نرود
        return user;
    }

    public User findByUsername(String username) {
        return usersByUsername.get(username);
    }

    public User findByEmail(String email) {
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

    public void changePassword(User user, String newRawPassword) {
        user.setPasswordHash(HashUtil.sha256(newRawPassword));
        save();
    }

    public void save() {
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
}
