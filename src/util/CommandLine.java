package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * یک خط دستور را به «کلمات اصلی» (بدون فلگ) و «فلگ‌ها» تفکیک می‌کند.
 * مثال: register -u ali -p 123 456 -n Ali  =>
 *   tokens = [register]
 *   flags  = { u: [ali], p: [123, 456], n: [Ali] }
 */
public final class CommandLine {

    private final List<String> tokens = new ArrayList<>();
    private final Map<String, List<String>> flags = new HashMap<>();

    public CommandLine(String rawLine) {
        parse(rawLine);
    }

    private void parse(String rawLine) {
        String[] parts = rawLine.trim().split("\\s+");
        String currentFlag = null;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (part.startsWith("-") && part.length() > 1 && !isNumeric(part)) {
                currentFlag = part.substring(1);
                flags.put(currentFlag, new ArrayList<>());
            } else if (currentFlag != null) {
                flags.get(currentFlag).add(part);
            } else {
                tokens.add(part);
            }
        }
    }

    private boolean isNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    public List<String> getTokens() {
        return tokens;
    }

    public String tokenAt(int index) {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    public String joinedTokens() {
        return String.join(" ", tokens);
    }

    public boolean has(String flag) {
        return flags.containsKey(flag);
    }

    public String get(String flag) {
        List<String> values = flags.get(flag);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    public List<String> getMulti(String flag) {
        return flags.getOrDefault(flag, new ArrayList<>());
    }
}
