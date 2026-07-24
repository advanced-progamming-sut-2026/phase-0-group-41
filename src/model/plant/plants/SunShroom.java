package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.ISunProducer;

public class SunShroom extends Plant implements ISunProducer {

    private int productionInterval = 24;
    private int tickCounter = 0;
    private int ticksAlive = 0;

    private int growthStage = 1;
    private int timeToStage2 = 24;
    private int timeToStage3 = 72;
    private boolean doubleSunChance = false;
    private int level = 1;

    // متغیرهای مدیریت خورشید
    private boolean sunReady = false;
    private int readySunAmount = 0;

    public SunShroom() {
        super("sunshroom", PlantType.SUN_PRODUCER, 25, 5, 300, PlantTag.NIGHT, PlantTag.SHROOM, PlantTag.WRAMP_UP);
    }

    @Override
    public void onTick(GameSession session) {
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return; // نه رشد می‌کند و نه خورشید می‌دهد!
        }
        if (isTransformedToCat() || isOctopused()) {
            return;
        }
        // =======================

        if (sunReady) return;

        tickCounter++;
        ticksAlive++;

        // منطق رشد تدریجی
        if (growthStage == 1 && ticksAlive >= timeToStage2) {
            growthStage = 2;
            System.out.println(getName() + " بزرگتر شد! (مرحله ۲)");
        } else if (growthStage == 2 && ticksAlive >= timeToStage3) {
            growthStage = 3;
            System.out.println(getName() + " به حداکثر اندازه رسید! (مرحله ۳)");
        }

        if (tickCounter >= productionInterval) {
            produceSun();
            tickCounter = 0;
        }
    }

    @Override
    public void produceSun() {
        int baseSunAmount = (growthStage == 1) ? 25 : (growthStage == 2) ? 50 : 75;
        int finalSun = doubleSunChance && Math.random() > 0.5 ? baseSunAmount * 2 : baseSunAmount;

        this.readySunAmount = finalSun;
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
        this.growthStage = 3;
        this.readySunAmount = 225;
        this.sunReady = true;
        System.out.println("Plant Food: رشد آنی به مرحله ۳ و تولید فوری 225 خورشید!");
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) {
            this.timeToStage2 -= 5;
            this.timeToStage3 -= 5;
        }
        if (level >= 3) {
            this.setMaxHealth(this.getMaxHealth() + 150);
            this.setHealth(this.getMaxHealth());
        }
        if (level >= 4) this.doubleSunChance = true;
    }
}