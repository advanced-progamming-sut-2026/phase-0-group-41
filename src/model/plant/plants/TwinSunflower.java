package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.ISunProducer;

public class TwinSunflower extends Plant implements ISunProducer {

    private int sunAmount = 100;
    private int productionInterval = 24;
    private int tickCounter = 0;
    private int currentSunCost = 125;
    private int level = 1;

    // متغیرهای مدیریت خورشید (طبق اینترفیس)
    private boolean sunReady = false;
    private int readySunAmount = 0;

    public TwinSunflower() {
        super("twinsunflower", PlantType.SUN_PRODUCER, 125, 15, 300, PlantTag.DAY);
    }

    @Override
    public void onTick(GameSession session) {
        // === تأثیر زامبی‌ها ===
        // اگر گربه شده، اختاپوس رویش است یا یخ زده، کاملاً متوقف می‌شود
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) {
            return;
        }

        // اگر از قبل خورشید داده و هنوز جمع نکردیم، منتظر می‌ماند
        if (sunReady) return;

        tickCounter++;
        if (tickCounter >= productionInterval) {
            produceSun();
            tickCounter = 0;
        }
    }

    @Override
    public void produceSun() {
        this.readySunAmount = this.sunAmount;
        this.sunReady = true;
        System.out.println(getName() + " آماده برداشت است (" + readySunAmount + " خورشید)");
    }

    @Override
    public boolean isSunReady() {
        return this.sunReady;
    }

    @Override
    public void collectSun() {
        if (this.sunReady) {
            System.out.println(readySunAmount + " خورشید از " + getName() + " جمع‌آوری شد!");
            this.sunReady = false;
            this.readySunAmount = 0;
        }
    }

    @Override
    public void feed(GameSession session) {
        super.feed(session);
        this.readySunAmount = 250;
        this.sunReady = true;
        System.out.println("Plant Food: تولید فوری 250 خورشید!");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.productionInterval -= 2;
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.currentSunCost -= 25;
    }

    @Override
    public int getSunCost() {
        return currentSunCost;
    }
}