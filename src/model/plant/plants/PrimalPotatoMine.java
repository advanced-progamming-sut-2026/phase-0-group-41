package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;

public class PrimalPotatoMine extends Plant {

    private int damage = 2400;
    private int armTimeTicks = 50; // 5 ثانیه زمان مسلح شدن
    private int armCounter = 0;
    private boolean isArmed = false;

    private int currentCooldown = 5;
    private int level = 1;

    public PrimalPotatoMine() {
        super("primalpotatomine", PlantType.EXPLOSIVE, 50, 5, 300, PlantTag.TRAP, PlantTag.CHARGE);
    }

    @Override
    public void onTick(GameSession session) {
        // حتی اگر مین زیر زمین باشد، جادوگر می‌تواند آن را گربه کند!
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: مسلح شدن آنی و پرتاب ۲ مین به نقاط تصادفی دیگر!");
            this.isArmed = true;
            decayFeedEffect();
            return;
        }

        // سیستم مسلح شدن
        if (!isArmed) {
            armCounter++;
            if (armCounter >= armTimeTicks) {
                isArmed = true;
                System.out.println(getName() + " مسلح شد و از خاک بیرون آمد!");
            }
        }
    }

    // این متد توسط GameSession وقتی زامبی پایش را روی این تایل می‌گذارد صدا زده می‌شود
    public void triggerExplosion(GameSession session) {
        if (isArmed) {
            System.out.println("بوم! " + getName() + " با دمیج " + damage + " و انفجار مساحتی ۳x۳ منفجر شد.");
            this.takeDamage(9999); // نابودی پس از انفجار
        } else {
            System.out.println(getName() + " هنوز مسلح نشده بود و متاسفانه خورده شد!");
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.armTimeTicks = Math.max(0, this.armTimeTicks - 10); // Arm Time -1s (10 تیک)
        if (level >= 3) this.currentCooldown = Math.max(0, this.currentCooldown - 3);
        if (level >= 4) this.damage += 400;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}