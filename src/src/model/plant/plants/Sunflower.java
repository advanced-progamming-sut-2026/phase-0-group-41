package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantType;

public class Sunflower extends Plant {

    private static final int PRODUCE_INTERVAL_TICKS = 240; // هر ۲۴ ثانیه یک خورشید
    private int ticksSinceLastProduce = 0;
    private boolean sunReady = false;

    public Sunflower() {
        super("sunflower", PlantType.SUN_PRODUCER, 50, 75, 300);
    }

    @Override
    public void onTick(GameSession session) {
        if (sunReady) {
            return; // تا برداشت نشود، تولید بعدی شروع نمی‌شود
        }
        ticksSinceLastProduce++;
        int interval = isFed() && isFeedActive() ? PRODUCE_INTERVAL_TICKS / 4 : PRODUCE_INTERVAL_TICKS;
        if (ticksSinceLastProduce >= interval) {
            sunReady = true;
            ticksSinceLastProduce = 0;
            System.out.println("plant " + getName() + " produced a sun at (" + getCol() + ", " + getRow() + ")");
        }
        decayFeedEffect();
    }

    public boolean isSunReady() {
        return sunReady;
    }

    public void collectSun() {
        sunReady = false;
    }
}
