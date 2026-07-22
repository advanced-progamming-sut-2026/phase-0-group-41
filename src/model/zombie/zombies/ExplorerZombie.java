package model.zombie.zombies;

import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class ExplorerZombie extends Zombie { // 🌟 اضافه شدن ارث‌بری

    private boolean isTorchLit = true;

    public ExplorerZombie() {
        super("explorer", 300, 0.015, 200, 10);
    }

    // متدی برای خاموش کردن مشعل (توسط گیاهان یخی)
    public void extinguishTorch() {
        isTorchLit = false;
    }

    // متدی برای روشن کردن مجدد مشعل (توسط گیاهان آتشی)
    public void igniteTorch() {
        isTorchLit = true;
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return; // 🌟 پرانتز اضافه شد

        if (isTorchLit) {
            burnAndWalk(session);
        } else {
            normalMoveOrAttack(session);
        }
    }

    private void burnAndWalk(GameSession session) {
        int col = (int) Math.floor(getXPosition());
        Tile tile = session.getBoard().getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        if (target != null && !target.isDead() && (getXPosition() - col) <= 1.0) {
            target.takeDamage(9999);
            if (target.isDead()) {
                tile.setPlant(null);
            }
        }

        setEating(false);
        setXPosition(getXPosition() - getSpeed());
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