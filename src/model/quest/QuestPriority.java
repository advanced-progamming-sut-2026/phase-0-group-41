package model.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * دسته‌بندی کلی کوئست‌ها طبق داک:
 * CRITICAL -> کوئست‌های داستانی / باز کردن گیاهان جدید (همیشه در صدر لیست)
 * HIGH     -> چالش‌های Epic با پاداش الماس
 * DAILY    -> کوئست‌های روزانه و تکرارپذیر
 * NORMAL   -> سایر کوئست‌های غیر روزانه با اولویت متوسط/کم
 */
public enum QuestPriority {
    CRITICAL,
    HIGH,
    DAILY,
    NORMAL
}