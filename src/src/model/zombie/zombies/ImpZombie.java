package model.zombie.zombies;

import model.game.Tile;
import model.zombie.Zombie;
import model.plant.Plant;
import model.game.GameSession;

public class ImpZombie extends Zombie {

    public ImpZombie() {
        super("imp", 100, 0.02, 50, 20);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        Plant target = findTarget(session);

        int col = (int) Math.floor(getXPosition());

        // بررسی اینکه گیاه وجود داره، نمرده و زامبی قشنگ بهش رسیده
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {

            target.takeDamage(getDamagePerTick());
            setEating(true);

            // 🌟 اگر گیاه مرد، از روی زمین پاکش کن تا راه باز بشه
            if (target.isDead()) {
                Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
                tile.setPlant(null);
                setEating(false);
            }

        } else {
            setXPosition(getXPosition() - getSpeed());
            setEating(false);
        }
    }

    private Plant findTarget(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        // 🌟 جلوگیری از ارور خروج از آرایه وقتی زامبی میرسه به آخر خط
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        return (tile != null) ? tile.getPlant() : null;
    }
}