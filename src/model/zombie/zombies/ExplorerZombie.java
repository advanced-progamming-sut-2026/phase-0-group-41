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

    public boolean isTorchLit() {
        return isTorchLit;
    }

    // === رفع باگ: مشعل هرگز خاموش/روشن نمی‌شد ===
    // extinguishTorch/igniteTorch از قبل نوشته شده بودند اما هیچ گیاه یخی یا
    // آتشینی آن‌ها را صدا نمی‌زد (SnowPea/WinterMelon/IcebergLettuce فقط
    // Zombie.applyChill عمومی را صدا می‌زدند، Jalapeno/PepperPult/FirePeashooter
    // فقط Zombie.removeChill عمومی را). با override این دو متد پایه، هر منبع
    // یخ/آتشِ موجود در بازی (بدون نیاز به تغییر در خودِ گیاهان) به‌صورت خودکار
    // مشعل کاوشگر را هم خاموش/روشن می‌کند؛ در نتیجه وقتی مشعل خاموش است، این
    // زامبی مثل یک زامبی عادی (کند و با گاز زدن) راه می‌رود، نه با سوزاندن آنی
    // گیاهان و عبور از آن‌ها.
    @Override
    public void applyChill(int durationTicks) {
        super.applyChill(durationTicks);
        extinguishTorch();
    }

    @Override
    public void removeChill() {
        super.removeChill();
        igniteTorch();
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