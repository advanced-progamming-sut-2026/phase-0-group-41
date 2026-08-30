package model.levelrules;

import model.game.GameSession;
import java.util.Random;

public class ConveyorBeltRules implements ILevelRules {

    private int tickCounter = 0;
    private final int BELT_SPEED_TICKS = 130; // ۱۳ ثانیه (با فرض هر ۱۰ تیک = ۱ ثانیه)
    private final String[] plantPool = {"peashooter", "cabbagepult", "wallnut", "potatomine"};
    private final Random random = new Random();

    @Override
    public void setupLevel(GameSession session) {
        System.out.println("مرحله تسمه نقاله آغاز شد! جمع‌آوری خورشید در این مرحله غیرفعال است.");
        // در صورت نیاز می‌توانید در اینجا لیست کارت‌های عادی کاربر را قفل کنید
    }

    @Override
    public void applySpecialTickRules(GameSession session) {
        tickCounter++;
        
        // هر ۱۳ ثانیه یک گیاه جدید تولید می‌شود
        if (tickCounter >= BELT_SPEED_TICKS) {
            String randomPlant = plantPool[random.nextInt(plantPool.length)];
            
            // TODO: در مرحله اتصال به UI، این گیاه باید به صفِ تسمه نقاله در گرافیک اضافه شود
            System.out.println("یک [" + randomPlant + "] روی تسمه نقاله قرار گرفت!");
            
            tickCounter = 0; // ریست کردن تایمر
        }
    }

    @Override
    public boolean checkCustomLossConditions(GameSession session) {
        return true; // شرط باخت خاصی ندارد (رسیدن زامبی به انتها کافیست)
    }
}