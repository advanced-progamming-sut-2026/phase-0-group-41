package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class NewspaperZombie extends Zombie {

    private int paperHealth = 150; // جان روزنامه
    private boolean isAngry = false;
    private static final double NORMAL_SPEED = 0.01;
    private static final double ANGRY_SPEED = 0.03;

    public NewspaperZombie() {

        super("newspaper", 200, NORMAL_SPEED, 150, 10);
    }

    @Override
    public double getSpeed() {
        return isAngry ? ANGRY_SPEED : NORMAL_SPEED;
    }

    @Override
    public int getDamagePerTick() {
        // وقتی عصبانی است، قدرت خوردنش هم چند برابر می‌شود
        return isAngry ? super.getDamagePerTick() * 3 : super.getDamagePerTick();
    }

    @Override
    public void takeDamage(int amount) {
        if (paperHealth > 0) {
            paperHealth -= amount;
            if (paperHealth <= 0) {
                // روزنامه پاره شد، زامبی عصبانی می‌شود!
                isAngry = true;
                // اگر دمیج اضافه‌ای ماند، به جان اصلی وارد شود
                super.takeDamage(-paperHealth);
                paperHealth = 0;
            }
        } else {
            super.takeDamage(amount);
        }
    }

    @Override
    public int getHealth() {
        return super.getHealth() + paperHealth;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        Plant target = findTarget(session);
        if (target != null) {
            target.takeDamage(getDamagePerTick());
            setEating(true);
        } else {
            setXPosition(getXPosition() - getSpeed());
            setEating(false);
        }
    }

    private Plant findTarget(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), col);
        return (tile != null) ? tile.getPlant() : null;
    }
}