package model.zombie.zombies;

import model.game.Board;
import model.game.GameSession;
import model.game.Season;
import model.game.Tile;
import model.plant.Plant;
import model.zombie.Zombie;

public class DodoRiderZombie extends Zombie {

    public DodoRiderZombie() {
        // نام، جان (۳۵۰)، سرعت (۰.۰۲)، هزینه موج (۱۷۵)، قدرت ضربه (۱۰)، فصل غار یخی
        super("dodorider", 350, 0.02, 175, 10, Season.ICE_CAVES);
    }

    // === طبق داکیومنت: این زامبی در برابر تیرهای یخی و سرما مصون است ===
    @Override
    public void applyChilled(int seconds) {
        // کاملاً خالی می‌ماند تا یخ نزند
    }

    @Override
    public void applyFrozen(int seconds) {
        // کاملاً خالی می‌ماند تا منجمد نشود
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
        boolean isJumpablePlant = plantName.equals("wallnut") ||
                plantName.equals("potatomine") ||
                plantName.equals("garlic");

        // اگر گیاه قابل پریدن بود و البته بلند هم نبود (مثل گردوی بلند نبود)، پرواز می‌کند
        if (isJumpablePlant && !target.isTall()) {
            setEating(false);
            // پرواز می‌کنه: توقف نمی‌کنه، دمیج نمی‌زنه و فقط از موقعیتش کم می‌شه تا رد بشه
            setXPosition(getXPosition() - getSpeed());
        } else {
            // اگر Tall-nut باشه (چون isTall آن true است) یا هر گیاه عادیِ دیگه‌ای باشه:
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