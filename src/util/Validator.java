package util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");
    private static final Pattern SPECIAL_SYMBOL_PATTERN =
            Pattern.compile("[!#$%^&*()=+}{\\[\\]|/\\\\:;'\",><?]");
    private static final Pattern EMAIL_LOCAL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9._-]*[a-zA-Z0-9])?$");
    private static final Pattern EMAIL_DOMAIN_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$");

    private Validator() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /** رمز قوی: حداقل ۸ کاراکتر، شامل حرف کوچک، حرف بزرگ، عدد و نماد خاص. */
    public static String passwordWeaknessReason(String password) {
        if (password == null || password.length() < 8) {
            return "رمز عبور باید حداقل ۸ کاراکتر باشد.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "رمز عبور باید شامل حرف کوچک باشد.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "رمز عبور باید شامل حرف بزرگ باشد.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "رمز عبور باید شامل عدد باشد.";
        }
        if (!SPECIAL_SYMBOL_PATTERN.matcher(password).find()) {
            return "رمز عبور باید شامل یک نماد خاص باشد.";
        }
        return null;
    }

    public static boolean isValidNickname(String nickname) {
        return nickname != null && nickname.length() >= 3 && nickname.length() <= 30;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        int atCount = email.length() - email.replace("@", "").length();
        if (atCount != 1) {
            return false;
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        String local = parts[0];
        String domain = parts[1];
        if (local.contains("..") || domain.contains("..")) {
            return false;
        }
        return EMAIL_LOCAL_PATTERN.matcher(local).matches()
                && EMAIL_DOMAIN_PATTERN.matcher(domain).matches();
    }
}
