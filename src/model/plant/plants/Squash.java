package model.plant.plants;

import model.game.GameSession;
import model.plant.Plant;
import model.plant.PlantTag;
import model.plant.PlantType;
import model.plant.interfaces.IExplosive;

public class Squash extends Plant implements IExplosive {

    private int damage = 1800;
    private int currentCooldown = 200;
    private int crushLimit = 1;
    private int crushesDone = 0;
    private int level = 1;

    public Squash() {
        super("squash", PlantType.EXPLOSIVE, 50, 200, 300, PlantTag.TRAP);
    }

    @Override
    public void onTick(GameSession session) {
        // === تغییرات اینجاست ===
        if (isFrozenSolid()) {
            handleIceMelting(session);
            return;
        }
        if (isTransformedToCat() || isOctopused()) return;
        // =======================

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
            model.zombie.Zombie target = null;
            // پیدا کردن زامبی که دقیقا در نیم خانه‌ایِ کدو تنبل است
            for (model.zombie.Zombie z : session.getAliveZombies()) {
                if (!z.isDead() && z.getRow() == getRow() && Math.abs(z.getXPosition() - getCol()) <= 0.8) {
                    target = z;
                    break;
                }
            }

            if (target != null) {
                target.takeDamage(damage, model.zombie.DamageType.NORMAL);
                System.out.println(getName() + " روی " + target.getTypeName() + " پرید و او را له کرد!");
                crushesDone++;
                if (crushesDone >= crushLimit) {
                    this.takeDamage(9999);
                }
            }
        }
    }

    public void applyUpgradeLevel(int newLevel) {
        this.level = newLevel;
        if (level >= 2) this.currentCooldown -= 30;
        if (level >= 3) this.damage += 600;
        if (level >= 4) this.crushLimit = 2;
    }

    @Override
    public int getCooldownTicks() { return currentCooldown; }
}