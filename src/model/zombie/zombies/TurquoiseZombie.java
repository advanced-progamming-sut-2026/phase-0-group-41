package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class TurquoiseZombie extends Zombie {

    private int stolenSuns = 0;

    // 🌟 ترفند دسترسی به Session در کل کلاس
    private GameSession currentSession;

    // متغیرهای فاز تمرکز (Channeling)
    private boolean isChanneling = false;
    private int channelTicks = 0;

    public TurquoiseZombie() {
        super("turquoise", 400, 0.01, 300, 10);
    }

    @Override
    public void onTick(GameSession session) {
        // 🌟 ذخیره سشن در متغیر کلاس برای استفاده در takeDamage
        this.currentSession = session;

        if (isDead()) return;

        if (isChanneling) {
            channelTicks++;

            if (channelTicks % 10 == 0) {
                stealSun(session);
            }

            if (channelTicks >= 50) {
                shootLaser(session);
                isChanneling = false;
                channelTicks = 0;
            }
        }
        else {
            if (isPlantInRadius(session)) {
                isChanneling = true;
                channelTicks = 0;
                setEating(false);
            } else {
                normalMoveOrAttack(session);
            }
        }
    }

    private boolean isPlantInRadius(GameSession session) {
        int currentCol = (int) Math.floor(getXPosition());
        Board board = session.getBoard();

        for (int i = 1; i <= 4; i++) {
            int targetCol = currentCol - i;
            if (targetCol >= 0) {
                Tile tile = board.getTile(getRow(), targetCol);
                if (tile != null && tile.getPlant() != null && !tile.getPlant().isDead()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void stealSun(GameSession session) {
        // 🌟 اصلاح نام متد به getCurrentSun بر اساس کلاس SunManager شما
        int currentPlayerSun = session.getSunManager().getCurrentSun();

        if (currentPlayerSun > 0) {
            int sunToSteal = Math.min(currentPlayerSun, 25);
            session.getSunManager().spendSun(sunToSteal);
            stolenSuns += sunToSteal;
        }
    }

    private void shootLaser(GameSession session) {
        int currentCol = (int) Math.floor(getXPosition());
        Board board = session.getBoard();

        for (int i = 1; i <= 4; i++) {
            int targetCol = currentCol - i;
            if (targetCol >= 0) {
                Tile tile = board.getTile(getRow(), targetCol);
                if (tile != null && tile.getPlant() != null) {
                    Plant target = tile.getPlant();
                    target.takeDamage(9999);

                    if (target.isDead()) {
                        tile.setPlant(null);
                    }
                }
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        boolean wasDead = isDead();
        super.takeDamage(amount);

        // منطق مرگ و پس دادن خورشید
        if (!wasDead && isDead() && stolenSuns > 0) {
            int sunsToReturn = stolenSuns / 2;

            // 🌟 استفاده از currentSession که قبلاً در onTick ذخیره شده بود
            if (currentSession != null) {
                currentSession.getSunManager().addSun(sunsToReturn);
            }
        }
    }

    private void normalMoveOrAttack(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            setEating(true);
            target.takeDamage(getDamagePerTick());
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        } else {
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        }
    }
}