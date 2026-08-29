package model.user;

import java.util.LinkedHashMap;
import java.util.Map;

/** لیست سوالات امنیتی که بازیکن هنگام ثبت‌نام یکی از آن‌ها را انتخاب می‌کند. */
public final class SecurityQuestions {

    private static final Map<Integer, String> QUESTIONS = new LinkedHashMap<>();

    static {
        QUESTIONS.put(1, "What was the name of your first pet?");
        QUESTIONS.put(2, "What city were you born in?");
        QUESTIONS.put(3, "Who was your favorite teacher in elementary school?");
        QUESTIONS.put(4, "What is your favorite name for a child?");
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
