package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.ISunProducer;

public class Sunflower extends Plant implements ISunProducer {

    private int sunAmount = 50;
    private int productionInterval = 24;
    private int tickCounter = 0;
    private boolean doubleSunChance = false;
    private int level = 1;

    // متغیرهای جدید برای مدیریت وضعیت خورشید طبق اینترفیس شما
    private boolean sunReady = false;
    private int readySunAmount = 0; // مقداری که آماده برداشت است

    public Sunflower() {
        super("sunflower", PlantType.SUN_PRODUCER, 50, 5, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) {
            return;
        }

        // اگر از قبل خورشیدی تولید کرده و هنوز بازیکن آن را برنداشته، تایمر را متوقف می‌کنیم
        if (sunReady) {
            return;
        }

        tickCounter++;
        if (tickCounter >= productionInterval) {
            produceSun(); // متد اینترفیس صدا زده می‌شود
            tickCounter = 0;
        }
    }

    // ==========================================
    // پیاده‌سازی هر ۳ متد اجباریِ اینترفیس ISunProducer
    // ==========================================

    @Override
    public void produceSun() {
        int finalSun = doubleSunChance && Math.random() > 0.5 ? sunAmount * 2 : sunAmount;
        this.readySunAmount = finalSun;
        this.sunReady = true; // وضعیت به "آماده برداشت" تغییر می‌کند
        System.out.println(getName() + " خورشید را تولید کرد و منتظر کلیک شماست!");
    }

    @Override
    public boolean isSunReady() {
        return this.sunReady; // موتور بازی با این متد می‌فهمد که آیا خورشید روی این گیاه هست یا نه
    }

    @Override
    public void collectSun() {
        if (this.sunReady) {
            System.out.println(this.readySunAmount + " خورشید با موفقیت از " + getName() + " جمع‌آوری شد!");
            this.sunReady = false;      // ریست شدن وضعیت
            this.readySunAmount = 0;    // ریست شدن مقدار
            // اینجا در سیستم اصلی بازی، مقدار readySunAmount به بانک پول کاربر اضافه می‌شود
        }
    }

    // ==========================================

    @Override
    public void feed(GameSession session) {
        super.feed(session);
        // به جای تولید عادی، در صورت دریافت غذا بلافاصله آماده برداشت می‌شود
        this.readySunAmount = 150;
        this.sunReady = true;
        System.out.println("Plant Food: تولید فوری 150 خورشید!");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.productionInterval -= 2;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.doubleSunChance = true;
    }
}