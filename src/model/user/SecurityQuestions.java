package model.user;

import java.util.LinkedHashMap;
import java.util.Map;

/** لیست سوالات امنیتی که بازیکن هنگام ثبت‌نام یکی از آن‌ها را انتخاب می‌کند. */
public final class SecurityQuestions {

    private static final Map<Integer, String> QUESTIONS = new LinkedHashMap<>();

    static {
        QUESTIONS.put(1, "نام اولین حیوان خانگی شما چیست؟");
        QUESTIONS.put(2, "نام شهر محل تولد شما چیست؟");
        QUESTIONS.put(3, "نام معلم مورد علاقه‌ی شما در دبستان چه بود؟");
        QUESTIONS.put(4, "اسم مورد علاقه شما برای فرزندتان چیست؟");
    }

    private SecurityQuestions() {
    }

    public static Map<Integer, String> all() {
        return QUESTIONS;
    }

    public static boolean exists(int id) {
        return QUESTIONS.containsKey(id);
    }

    public static String get(int id) {
        return QUESTIONS.get(id);
    }
}
