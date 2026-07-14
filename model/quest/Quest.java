package model.quest;

/**
 * TODO: پیاده‌سازی کامل کوئست‌ها طبق داک (چهار دسته: critical/داستانی، High/Epic،
 * روزانه/تکرارپذیر و ...) با انواع پاداش Currency/Unlockable/Inventory.
 */
public class Quest {

    public enum Priority { CRITICAL, HIGH, DAILY }

    private final String id;
    private final String description;
    private final Priority priority;
    private boolean completed = false;

    public Quest(String id, String description, Priority priority) {
        this.id = id;
        this.description = description;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        completed = true;
    }
}
