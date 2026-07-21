package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class DodoRiderZombie extends Zombie {

    public DodoRiderZombie() {
        // نام، جان (۳۵۰)، سرعت (۰.۰۲)، هزینه موج (۱۷۵)، قدرت ضربه (۱۰)
        super("dodorider", 350, 0.02, 175, 10);
    }

    @Override
    public void onTick(GameSession session) {
        if (isDead()) return;

        Board board = session.getBoard();
        int col = (int) Math.floor(getXPosition());

        // جلوگیری از ارور خروج از آرایه
        Tile tile = board.getTile(getRow(), Math.max(col, 0));
        Plant target = (tile != null) ? tile.getPlant() : null;

        // بررسی اینکه به گیاهی رسیده باشه و گیاه زنده باشه
        if (target != null && !target.isDead() && Math.abs(getXPosition() - col) < 0.5) {
            handleObstacle(target, tile);
        } else {
            // حرکت عادی تو زمین خالی
            setXPosition(getXPosition() - getSpeed());
            setEating(false);
        }
    }

    private void handleObstacle(Plant target, Tile tile) {
        String plantName = target.getName().toLowerCase();

        // لیستی از گیاهانی که دودو می‌تواند از روی آن‌ها پرواز کند (طبق داک)
        // شامل: موانع با جان زیاد (wallnut)، مین‌ها (potatomine)، منحرف‌کننده‌ها (garlic)
        boolean canFlyOver = plantName.equals("wallnut") ||
                plantName.equals("potatomine") ||
                plantName.equals("garlic");

        if (canFlyOver) {
            // پرواز می‌کنه: توقف نمی‌کنه، دمیج نمی‌زنه و فقط از موقعیتش کم می‌شه تا رد بشه
            setEating(false);
            setXPosition(getXPosition() - getSpeed());
        } else {
            // اگر Tall-nut یا هر گیاه عادیِ دیگه‌ای (مثل نخودافکن) باشه:
            // توقف می‌کنه و گاز می‌گیره
            setEating(true);
            target.takeDamage(getDamagePerTick());

            // پاک کردن گیاه مرده از روی زمین برای باز شدن راه
            if (target.isDead()) {
                tile.setPlant(null);
                setEating(false);
            }
        }
    }
}