package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class Squash extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentCooldown = 20;
    private int crushLimit = 1;
    private int crushesDone = 0;
    private int level = 1;

    public Squash() {
        super("squash", PlantType.EXPLOSIVE, 50, 20, 300, PlantTag.TRAP);
    }

    @Override
    public void onTick(GameSession session) {
        if (isTransformedToCat() || isOctopused() || isFrozenSolid()) return;

        if (isFeedActive()) {
            System.out.println("Plant Food: له کردن ۲ زامبی تصادفی در سطح زمین!");
            // این بخش در آینده توسط متدهای GameSession تکمیل می‌شود
            // List<Zombie> targets = session.getRandomZombies(2);
            // for(Zombie z : targets) { z.takeDamage(damage); }
            decayFeedEffect();
            return;
        }
    }

    @Override
    public void explode(GameSession session) {
        if (crushesDone < crushLimit) {
            System.out.println(getName() + " روی زامبی پرید و له کرد! (دمیج: " + damage + ")");
            crushesDone++;
            if (crushesDone >= crushLimit) {
                this.takeDamage(9999);
            }
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 3;
        if (level >= 3) this.damage += 600;
        if (level >= 4) this.crushLimit = 2;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}